package com.sk89q.worldedit.util.serialization.basic;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.util.serialization.FlatNibbleArray;
import com.sk89q.worldedit.util.serialization.BlockSerializer;

import java.util.Map;

class BasicBlockSerializer implements BlockSerializer {
    private final byte[] blocks;
    private FlatNibbleArray addBlocks;
    private final byte[] blockData;
    private final boolean schematicaFriendly;

    public BasicBlockSerializer(int size, boolean schematicaFriendly) {
        blocks = new byte[size];
        blockData = new byte[size];
        this.schematicaFriendly = schematicaFriendly;
    }

    @Override
    public void saveBlock(int index, int id, int data) {
        if (id > 255) {
            if (addBlocks == null) {
                addBlocks = new FlatNibbleArray(blocks.length, schematicaFriendly);
            }

            addBlocks.set(index, (id & 0xF00) >> 8);
        }

        blocks[index] = (byte) (id & 0xFF);
        blockData[index] = (byte) (data & 0xFF);
    }

    @Override
    public void serialize(Map<String, Tag> schematic) {
        schematic.put("Blocks", new ByteArrayTag(blocks));
        schematic.put("Data", new ByteArrayTag(blockData));
        if (addBlocks != null) {
            schematic.put("AddBlocks", new ByteArrayTag(addBlocks.store));
        }
    }
}
