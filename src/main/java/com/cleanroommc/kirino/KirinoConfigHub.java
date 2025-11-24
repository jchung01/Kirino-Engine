package com.cleanroommc.kirino;

public final class KirinoConfigHub {
    KirinoConfigHub() {
    }

    public boolean enable = true;
    public boolean enableRenderDelegate = true;
    public boolean enableHDR = true;
    public boolean enablePostProcessing = true;

    public float chunkPriorityFalloffDistance = 46f;

    public int targetWorkloadPerThread = 5000;
}
