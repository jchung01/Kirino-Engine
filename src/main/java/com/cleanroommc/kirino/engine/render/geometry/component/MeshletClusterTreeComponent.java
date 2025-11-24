package com.cleanroommc.kirino.engine.render.geometry.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;

@CleanComponent
public class MeshletClusterTreeComponent implements ICleanComponent {
    public int handleID;
    public int handleGeneration;
}
