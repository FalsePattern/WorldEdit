package com.sk89q.worldedit.util.serialization.basic;

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.util.serialization.BlockDeserializer;
import com.sk89q.worldedit.util.serialization.BlockIOFactory;
import com.sk89q.worldedit.util.serialization.BlockSerializer;

import java.io.IOException;
import java.util.Map;

public class BasicBlockIOFactory implements BlockIOFactory {
    private final boolean schematicaFriendly;

    public BasicBlockIOFactory() {
        this(false);
    }

    public BasicBlockIOFactory(boolean schematicaFriendly) {
        this.schematicaFriendly = schematicaFriendly;
    }

    @Override
    public BlockSerializer newBlockSerializer(int size) {
        return new BasicBlockSerializer(size, schematicaFriendly);
    }

    @Override
    public BlockDeserializer newBlockDeserializer(Map<String, Tag> schematic) throws IOException {
        return new BasicBlockDeserializer(schematic);
    }
}
