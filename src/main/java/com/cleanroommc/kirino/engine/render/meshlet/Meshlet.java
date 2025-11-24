package com.cleanroommc.kirino.engine.render.meshlet;

import com.cleanroommc.kirino.engine.render.geometry.Block;

import java.util.List;

public class Meshlet {
    public final List<Block> blocks;

    public Meshlet(List<Block> blocks) {
        this.blocks = blocks;
    }
}
