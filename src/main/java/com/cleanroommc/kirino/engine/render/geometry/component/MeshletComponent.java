package com.cleanroommc.kirino.engine.render.geometry.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;
import com.cleanroommc.kirino.engine.render.geometry.AABB;
import org.joml.Vector3f;

@CleanComponent
public class MeshletComponent implements ICleanComponent {
    // todo
    public AABB aabb;
    public Vector3f normal;
    public int pass;
    public boolean isDirty;
    public int chunkPosX;
    public int chunkPosY;
    public int chunkPosZ;
    public int handleID;
    public int handleGeneration;
}
