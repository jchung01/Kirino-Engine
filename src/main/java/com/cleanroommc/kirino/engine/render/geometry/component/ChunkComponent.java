package com.cleanroommc.kirino.engine.render.geometry.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;

@CleanComponent
public class ChunkComponent implements ICleanComponent {
    /**
     * X-coordinate.
     */
    public int chunkPosX;

    /**
     * Y-coordinate.
     */
    public int chunkPosY;

    /**
     * Z-coordinate.
     */
    public int chunkPosZ;

    /**
     * Whether the meshlet-gen task has been ran on this chunk.
     */
    public boolean isDirty = true;

    /**
     * The closer to the camera, the higher the priority and smaller the number.
     * <p>Domain: [0, inf]; 0 is the highest priority.</p>
     */
    public int priority = 0;
}
