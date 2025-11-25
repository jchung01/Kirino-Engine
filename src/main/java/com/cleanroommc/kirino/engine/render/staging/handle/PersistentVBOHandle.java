package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.render.staging.buffer.BufferStorage;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class PersistentVBOHandle extends StagingBufferHandle<PersistentVBOHandle> {
    private final BufferStorage.SlotHandle<VBOView> handle;

    public PersistentVBOHandle(StagingBufferManager stagingBufferManager, int offset, int maxLength, BufferStorage.SlotHandle<VBOView> handle) {
        super(stagingBufferManager, offset, maxLength);
        this.handle = handle;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(offset + byteBuffer.remaining() <= maxLength,
                "Buffer slice size must be greater than or equal to \"offset + byteBuffer.remaining()\".");

        ByteBuffer byteBuffer0 = handle.getView().getPersistentMappedBuffer();
        int oldPos = byteBuffer0.position();
        byteBuffer0.position(this.offset + offset);
        byteBuffer0.put(byteBuffer);
        byteBuffer0.position(oldPos);
    }
}
