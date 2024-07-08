package com.sk89q.worldedit.util.serialization;

import com.sk89q.jnbt.Tag;

import java.util.Map;

public interface BlockSerializer {
    void saveBlock(int index, int blockID, int blockData);

    void serialize(Map<String, Tag> schematic);
}
