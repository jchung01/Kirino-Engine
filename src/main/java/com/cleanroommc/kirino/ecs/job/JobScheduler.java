package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArrayRange;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JobScheduler {
    private final JobRegistry jobRegistry;

    public JobScheduler(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    public CompletableFuture<?> executeParallelJob(EntityManager entityManager, Class<? extends IParallelJob> clazz, @Nullable Map<String, Object> externalData, Executor executor) {
        Map<JobDataQuery, IJobDataInjector> parallelJobDataQueries = jobRegistry.getParallelJobDataQueries(clazz);
        Map<String, IJobDataInjector> parallelJobExternalDataQueries = jobRegistry.getParallelJobExternalDataQueries(clazz);
        IJobInstantiator instantiator = jobRegistry.getParallelJobInstantiator(clazz);
        if (parallelJobDataQueries == null || parallelJobExternalDataQueries == null || instantiator == null) {
            throw new IllegalStateException("Parallel job class " + clazz.getName() + " isn't registered.");
        }

        if (!parallelJobExternalDataQueries.isEmpty()) {
            Preconditions.checkArgument(externalData != null,
                    "Argument \"externalData\" must not be null since there are %d external data queries.", parallelJobExternalDataQueries.size());
            for (String key : parallelJobExternalDataQueries.keySet()) {
                Preconditions.checkArgument(externalData.containsKey(key),
                        "Missing the entry \"%s\" from \"externalData\".", key);
            }
        }

        EntityQuery query = entityManager.newQuery();
        ((IParallelJob) instantiator.instantiate()).query(query);
        List<ArchetypeDataPool> archetypes = entityManager.startQuery(query);

        List<CompletableFuture<?>> futures = new ArrayList<>();

        int threadOrdinal = 0;
        for (ArchetypeDataPool archetype : archetypes) {
            IParallelJob job = (IParallelJob) instantiator.instantiate();

            // data injection
            for (Map.Entry<JobDataQuery, IJobDataInjector> entry : parallelJobDataQueries.entrySet()) {
                IPrimitiveArray array = archetype.getArray(entry.getKey().componentClass().asSubclass(ICleanComponent.class), entry.getKey().fieldAccessChain());
                entry.getValue().inject(job, array);
            }
            if (externalData != null) {
                for (Map.Entry<String, IJobDataInjector> entry : parallelJobExternalDataQueries.entrySet()) {
                    entry.getValue().inject(job, externalData.get(entry.getKey()));
                }
            }

            ArrayRange arrayRange = archetype.getArrayRange();

            int workload = 0;
            for (int i = arrayRange.start; i < arrayRange.end; i++) {
                if (arrayRange.deprecatedIndexes.contains(i)) {
                    continue;
                }

                workload += job.estimateWorkload(i);
            }

            int futureCount = (workload + KirinoCore.KIRINO_CONFIG_HUB.targetWorkloadPerThread - 1) / KirinoCore.KIRINO_CONFIG_HUB.targetWorkloadPerThread;

            if (futureCount <= 1) {
                // run synchronously
                for (int i = arrayRange.start; i < arrayRange.end; i++) {
                    if (arrayRange.deprecatedIndexes.contains(i)) {
                        continue;
                    }

                    job.execute(entityManager, i, threadOrdinal);
                }
                threadOrdinal++;
            } else {
                // run asynchronously
                int accumulated = 0;
                int startIndex = arrayRange.start;

                for (int i = arrayRange.start; i < arrayRange.end; i++) {
                    if (arrayRange.deprecatedIndexes.contains(i)) {
                        continue;
                    }

                    accumulated += job.estimateWorkload(i);

                    if (accumulated >= KirinoCore.KIRINO_CONFIG_HUB.targetWorkloadPerThread) {
                        final int endIndexExclusive = i + 1;
                        final int finalThreadOrdinal = threadOrdinal;
                        final int finalStartIndex = startIndex;

                        futures.add(CompletableFuture.runAsync(() -> {
                            for (int j = finalStartIndex; j < endIndexExclusive; j++) {
                                if (arrayRange.deprecatedIndexes.contains(j)) {
                                    continue;
                                }

                                job.execute(entityManager, j, finalThreadOrdinal);
                            }
                        }, executor));

                        threadOrdinal++;
                        startIndex = endIndexExclusive;
                        accumulated = 0;
                    }
                }

                boolean hasRemainingWork = false;
                for (int j = startIndex; j < arrayRange.end; j++) {
                    if (!arrayRange.deprecatedIndexes.contains(j)) {
                        hasRemainingWork = true;
                        break;
                    }
                }

                if (hasRemainingWork) {
                    final int finalThreadOrdinal = threadOrdinal;
                    final int finalStartIndex = startIndex;

                    futures.add(CompletableFuture.runAsync(() -> {
                        for (int j = finalStartIndex; j < arrayRange.end; j++) {
                            if (arrayRange.deprecatedIndexes.contains(j)) {
                                continue;
                            }

                            job.execute(entityManager, j, finalThreadOrdinal);
                        }
                    }, executor));

                    threadOrdinal++;
                }
            }
        }

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        } else if (futures.size() == 1) {
            return futures.getFirst();
        } else {
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }
    }
}
