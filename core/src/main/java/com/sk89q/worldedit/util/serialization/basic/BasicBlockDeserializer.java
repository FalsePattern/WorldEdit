package com.sk89q.worldedit.util.serialization.basic;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.util.serialization.FlatNibbleArray;
import com.sk89q.worldedit.util.serialization.BlockDeserializer;
import com.sk89q.worldedit.util.serialization.SerializationUtil;

import java.io.IOException;
import java.util.Map;

class BasicBlockDeserializer implements BlockDeserializer {
    private final byte[] blocks;
    private final byte[] blockData;
    private final FlatNibbleArray addBlocks;

    public BasicBlockDeserializer(Map<String, Tag> schematic) throws IOException {
        blocks = SerializationUtil.requireTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        blockData = SerializationUtil.requireTag(schematic, "Data", ByteArrayTag.class).getValue();

        String addBlocksKey;
        if (schematic.containsKey("Add")) {
            addBlocksKey = "Add";
        } else if (schematic.containsKey("AddBlocks")) {
            addBlocksKey = "AddBlocks";
        } else {
            addBlocksKey = null;
        }

        if (addBlocksKey != null) {
            addBlocks = new FlatNibbleArray(SerializationUtil.requireTag(schematic, addBlocksKey, ByteArrayTag.class).getValue(), schematic.containsKey("SchematicaMapping"));
        } else {
            addBlocks = null;
        }
    }

    @Override
    public int length() {
        return blocks.length;
    }

    @Override
    public int loadBlockID(int index) {
        if (index < 0 || index >= blocks.length) {
            return 0;
        }

        if (addBlocks == null) {
            return blocks[index] & 0xFF;
        }

        return (blocks[index] & 0xFF) | (addBlocks.get(index) << 8);
    }

    @Override
    public int loadBlockData(int index) {
        if (index < 0 || index >= blockData.length) {
            return 0;
        }

        return blockData[index] & 0xFF;
    }
}
