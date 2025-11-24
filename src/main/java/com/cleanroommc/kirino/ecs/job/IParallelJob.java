package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import org.jspecify.annotations.NonNull;

/**
 * Jobs will be instantiated per archetype.
 */
public interface IParallelJob {
    /**
     * Every execution must be stateless except the index.
     *
     * @param entityManager The entity manager
     * @param index The index
     */
    void execute(@NonNull EntityManager entityManager, int index, int threadOrdinal);

    void query(@NonNull EntityQuery entityQuery);

    /**
     * Return value must be greater than or equal to 1.
     *
     * <p>For example, the O(n) algorithm with input size around 10 should return 10.</p>
     *
     * @param index The index
     * @return The estimated workload
     */
    int estimateWorkload(int index);
}
