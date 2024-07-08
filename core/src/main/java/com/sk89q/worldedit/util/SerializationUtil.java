package com.sk89q.worldedit.util;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import lombok.val;
import lombok.var;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerializationUtil {
    private static final Logger log = Logger.getLogger(SerializationUtil.class.getCanonicalName());
    private static Supplier<SerializationUtil> instance = SerializationUtil::new;
    private static int currentPriority = -1;
    private static Throwable currentRegisterStacktrace = new Throwable();
    public static void register(Supplier<SerializationUtil> instance, int priority) {
        if (priority >= currentPriority) {
            val stacktrace = new Throwable();
            if (priority == currentPriority) {
                System.err.println("Identical priorities for different mapping provider registrations!");
                System.err.println("Priority: " + priority);
                System.err.println("Previous registration: ");
                currentRegisterStacktrace.printStackTrace();
                System.err.println("New registration: ");
                stacktrace.printStackTrace();
            }
            SerializationUtil.instance = instance;
            currentPriority = priority;
            currentRegisterStacktrace = stacktrace;
        }
    }

    public static SerializationUtil create() {
        return instance.get();
    }

    public BlockIDSerializer newBlockSerializer(int size) {
        return new BasicBlockIDSerializer(size);
    }

    public BlockIDDeserializer newBlockDeserializer(Map<String, Tag> schematic) throws IOException {
        return new BasicBlockIDDeserializer(schematic);
    }

    public interface MappingProvider {
        int expectedMaxIDCount();
        String getNameFromID(int id);
        int getIDFromName(String name);
    }

    public interface BlockIDSerializer {
        void saveBlock(int index, int blockID, int blockData);
        void serialize(Map<String, Tag> schematic);
    }

    public interface BlockIDDeserializer {
        int length();
        int loadBlockID(int index);
        int loadBlockData(int index);
    }

    public static class BasicBlockIDSerializer implements BlockIDSerializer {
        private byte[] blocks;
        private FlatNibbleArray addBlocks;
        private byte[] blockData;

        public BasicBlockIDSerializer(int size) {
            blocks = new byte[size];
            blockData = new byte[size];
        }
        @Override
        public void saveBlock(int index, int id, int data) {
            if (id > 255) {
                if (addBlocks == null)
                    addBlocks = new FlatNibbleArray(blocks.length);

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

    public static class RemappingBlockIDSerializer implements BlockIDSerializer {
        private final BlockIDSerializer wrapped;
        private final MappingProvider mapping;
        private final Map<Integer, String> idMap;
        public RemappingBlockIDSerializer(BlockIDSerializer wrapped, MappingProvider mapping) {
            this.wrapped = wrapped;
            this.mapping = mapping;
            this.idMap = new HashMap<>(mapping.expectedMaxIDCount(), 0.25f);
        }

        @Override
        public void saveBlock(int index, int blockID, int blockData) {
            if (!idMap.containsKey(blockID)) {
                idMap.put(blockID, mapping.getNameFromID(blockID));
            }
            wrapped.saveBlock(index, blockID, blockData);
        }

        @Override
        public void serialize(Map<String, Tag> schematic) {
            wrapped.serialize(schematic);
            val idMapTags = new HashMap<String, Tag>();
            for (val idEntry: idMap.entrySet()) {
                idMapTags.put(idEntry.getValue(), new IntTag(idEntry.getKey()));
            }
            schematic.put("SchematicaMapping", new CompoundTag(idMapTags));
        }
    }

    public static class BasicBlockIDDeserializer implements BlockIDDeserializer {
        private byte[] blocks;
        private FlatNibbleArray addBlocks;
        private byte[] blockData;

        public BasicBlockIDDeserializer(Map<String, Tag> schematic) throws IOException {
            blocks = requireTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            blockData = requireTag(schematic, "Data", ByteArrayTag.class).getValue();

            String addBlocksKey;
            if (schematic.containsKey("Add"))
                addBlocksKey = "Add";
            else if (schematic.containsKey("AddBlocks"))
                addBlocksKey = "AddBlocks";
            else
                addBlocksKey = null;

            if (addBlocksKey != null) {
                addBlocks = new FlatNibbleArray(requireTag(schematic, addBlocksKey, ByteArrayTag.class).getValue());
            }
        }

        @Override
        public int length() {
            return blocks.length;
        }

        @Override
        public int loadBlockID(int index) {
            if (index < 0 || index >= blocks.length)
                return 0;

            if (addBlocks == null)
                return blocks[index] & 0xFF;

            return (blocks[index] & 0xFF) | (addBlocks.get(index) << 8);
        }

        @Override
        public int loadBlockData(int index) {
            if (index < 0 || index >= blockData.length)
                return 0;

            return blockData[index] & 0xFF;
        }
    }

    public static class RemappingBlockIDDeserializer implements BlockIDDeserializer {
        private final BlockIDDeserializer wrapped;
        private final Map<Integer, Integer> remappingTable;

        public RemappingBlockIDDeserializer(Map<String, Tag> schematic, BlockIDDeserializer wrapped, MappingProvider mapping) throws IOException {
            this.wrapped = wrapped;

            String idMapKey;
            if (schematic.containsKey("IDMap"))
                idMapKey = "IDMap";
            else if (schematic.containsKey("SchematicaMapping"))
                idMapKey = "SchematicaMapping";
            else
                idMapKey = null;

            // ID remapping
            if (idMapKey != null) {
                val idMap = requireTag(schematic, idMapKey, CompoundTag.class).getValue();
                remappingTable = new HashMap<>(idMap.size(), 0.25f);
                for (val entry: idMap.entrySet()) {
                    val name = entry.getKey();
                    val nbtEntry = entry.getValue();
                    int originalID;
                    if (nbtEntry instanceof IntTag) {
                        originalID = ((IntTag)nbtEntry).getValue();
                    } else if (nbtEntry instanceof ShortTag) {
                        originalID = ((ShortTag)nbtEntry).getValue() & 0xFFFF;
                    } else if (nbtEntry instanceof ByteTag) {
                        originalID = ((ByteTag)nbtEntry).getValue() & 0xFF;
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

    public static <T extends Tag> T requireTag(Map<String, Tag> items, String key, Class<T> expected) throws IOException {
        if (!items.containsKey(key)) {
            throw new IOException("Schematic file is missing a \"" + key + "\" tag");
        }

        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IOException(key + " tag is not of tag type " + expected.getName());
        }

        return expected.cast(tag);
    }

    @Nullable
    public static <T extends Tag> T getTag(CompoundTag tag, Class<T> expected, String key) {
        Map<String, Tag> items = tag.getValue();

        if (!items.containsKey(key)) {
            return null;
        }

        Tag test = items.get(key);
        if (!expected.isInstance(test)) {
            return null;
        }

        return expected.cast(test);
    }
}
