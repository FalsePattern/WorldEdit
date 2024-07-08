package com.sk89q.worldedit.util.serialization.remapping;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.util.serialization.BlockDeserializer;
import com.sk89q.worldedit.util.serialization.SerializationUtil;
import lombok.val;
import lombok.var;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class RemappingBlockDeserializer implements BlockDeserializer {
    private static final Logger log = Logger.getLogger(RemappingBlockDeserializer.class.getCanonicalName());

    private final BlockDeserializer wrapped;
    private final Map<Integer, Integer> remappingTable;

    public RemappingBlockDeserializer(Map<String, Tag> schematic, BlockDeserializer wrapped, MappingProvider mapping) throws IOException {
        this.wrapped = wrapped;

        String idMapKey;
        if (schematic.containsKey("IDMap")) {
            idMapKey = "IDMap";
        } else if (schematic.containsKey("SchematicaMapping")) {
            idMapKey = "SchematicaMapping";
        } else {
            idMapKey = null;
        }

        // ID remapping
        if (idMapKey != null) {
            val idMap = SerializationUtil.requireTag(schematic, idMapKey, CompoundTag.class).getValue();
            remappingTable = new HashMap<>(idMap.size(), 0.25f);
            for (val entry : idMap.entrySet()) {
                val name = entry.getKey();
                val nbtEntry = entry.getValue();
                int originalID;
                if (nbtEntry instanceof IntTag) {
                    originalID = ((IntTag) nbtEntry).getValue();
                } else if (nbtEntry instanceof ShortTag) {
                    originalID = ((ShortTag) nbtEntry).getValue() & 0xFFFF;
                } else if (nbtEntry instanceof ByteTag) {
                    originalID = ((ByteTag) nbtEntry).getValue() & 0xFF;
                } else {
                    originalID = 0;
                }
                var blockID = mapping.getIDFromName(name);
                if (blockID == -1 || (blockID == 0 && originalID != 0)) {
                    log.log(Level.WARNING, "Missing ID mapping for " + name + "! Replacing with air.");
                    blockID = 0;
                }
                remappingTable.put(originalID, blockID);
            }
        } else {
            remappingTable = null;
        }
    }

    @Override
    public int length() {
        return wrapped.length();
    }

    @Override
    public int loadBlockID(int index) {
        val id = wrapped.loadBlockID(index);
        return remappingTable != null ? remappingTable.getOrDefault(id, id) : id;
    }

    @Override
    public int loadBlockData(int index) {
        return wrapped.loadBlockData(index);
    }
}
