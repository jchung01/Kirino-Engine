package com.cleanroommc.kirino.engine.render.camera;

import com.cleanroommc.kirino.utils.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.invoke.MethodHandle;
import java.nio.FloatBuffer;
import java.util.Objects;

public class MinecraftCamera implements ICamera {
    private final MethodHandle projectionBuffer;
    private final MethodHandle viewRotationBuffer;
    private final MethodHandle partialTicksPaused;

    @SuppressWarnings("unchecked")
    public MinecraftCamera() {
        projectionBuffer = ReflectionUtils.getFieldGetter(ActiveRenderInfo.class, "PROJECTION", "field_178813_c", FloatBuffer.class);
        viewRotationBuffer = ReflectionUtils.getFieldGetter(ActiveRenderInfo.class, "MODELVIEW", "field_178812_b", FloatBuffer.class);
        partialTicksPaused = ReflectionUtils.getFieldGetter(Minecraft.class, "renderPartialTicksPaused", "field_193996_ah", float.class);

        Objects.requireNonNull(projectionBuffer);
        Objects.requireNonNull(viewRotationBuffer);
        Objects.requireNonNull(partialTicksPaused);
    }

    public double getPartialTicks() {
        Minecraft minecraft = Minecraft.getMinecraft();
        try {
            return minecraft.isGamePaused() ? (float) partialTicksPaused.invokeExact(minecraft) : minecraft.getRenderPartialTicks();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        try {
            return new Matrix4f((FloatBuffer) projectionBuffer.invokeExact());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FloatBuffer getProjectionBuffer() {
        try {
            return (FloatBuffer) projectionBuffer.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Matrix4f getViewRotationMatrix() {
        try {
            return new Matrix4f((FloatBuffer) viewRotationBuffer.invokeExact());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FloatBuffer getViewRotationBuffer() {
        try {
            return (FloatBuffer) viewRotationBuffer.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Vector3f getWorldOffset() {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) {
            camera = Minecraft.getMinecraft().player;
        }
        double partialTicks = getPartialTicks();
        double camX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double camY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
        double camZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
        return new Vector3f((float)camX, (float)camY, (float)camZ);
    }
}
