package com.sk89q.worldedit.util.serialization.remapping;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.util.serialization.BlockSerializer;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

class RemappingBlockSerializer implements BlockSerializer {
    private final BlockSerializer wrapped;
    private final MappingProvider mapping;
    private final Map<Integer, String> idNameMap;
    private final Map<Integer, Integer> remapping;
    private int nextID = 1;

    public RemappingBlockSerializer(BlockSerializer wrapped, MappingProvider mapping) {
        this.wrapped = wrapped;
        this.mapping = mapping;
        this.idNameMap = new HashMap<>(mapping.expectedMaxIDCount(), 0.25f);
        this.remapping = new HashMap<>(mapping.expectedMaxIDCount(), 0.25f);
        this.remapping.put(0, 0);
    }

    @Override
    public void saveBlock(int index, int blockID, int blockData) {
        final int remappedID;
        if (!remapping.containsKey(blockID)) {
            remappedID = nextID++;
            remapping.put(blockID, remappedID);
            idNameMap.put(remappedID, mapping.getNameFromID(blockID));
        } else {
            remappedID = remapping.get(blockID);
        }
        wrapped.saveBlock(index, remappedID, blockData);
    }

    @Override
    public void serialize(Map<String, Tag> schematic) {
        wrapped.serialize(schematic);
        val idMapTags = new HashMap<String, Tag>();
        for (val idEntry : idNameMap.entrySet()) {
            idMapTags.put(idEntry.getValue(), new IntTag(idEntry.getKey()));
        }
        schematic.put("SchematicaMapping", new CompoundTag(idMapTags));
    }
}
