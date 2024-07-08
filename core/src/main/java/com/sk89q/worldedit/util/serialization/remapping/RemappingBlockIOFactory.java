package com.sk89q.worldedit.util.serialization.remapping;

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.util.serialization.BlockDeserializer;
import com.sk89q.worldedit.util.serialization.BlockIOFactory;
import com.sk89q.worldedit.util.serialization.BlockSerializer;
import com.sk89q.worldedit.util.serialization.basic.BasicBlockIOFactory;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class RemappingBlockIOFactory implements BlockIOFactory {
    private final BlockIOFactory wrapped;
    private final Supplier<MappingProvider> mapping;

    public RemappingBlockIOFactory(Supplier<MappingProvider> mappingConstructor) {
        this(new BasicBlockIOFactory(true), mappingConstructor);
    }

    public RemappingBlockIOFactory(BlockIOFactory wrapped, Supplier<MappingProvider> mappingConstructor) {
        this.wrapped = wrapped;
        this.mapping = mappingConstructor;
    }
    @Override
    public BlockSerializer newBlockSerializer(int size) {
        return new RemappingBlockSerializer(wrapped.newBlockSerializer(size), mapping.get());
    }

    @Override
    public BlockDeserializer newBlockDeserializer(Map<String, Tag> schematic) throws IOException {
        return new RemappingBlockDeserializer(schematic, wrapped.newBlockDeserializer(schematic), mapping.get());
    }

}
