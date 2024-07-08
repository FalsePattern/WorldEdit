package com.sk89q.worldedit.forge;

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.util.SerializationUtil;

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;

import java.io.IOException;
import java.util.Map;

public class ForgeMappingProvider extends SerializationUtil implements SerializationUtil.MappingProvider {
    private final FMLControlledNamespacedRegistry<Block> registry = GameData.getBlockRegistry();
    @Override
    public int expectedMaxIDCount() {
        return 4096;
    }

    @Override
    public String getNameFromID(int id) {
        return registry.getNameForObject(registry.getObjectById(id));
    }

    @Override
    public int getIDFromName(String name) {
        return registry.getId(name);
    }

    @Override
    public BlockIDSerializer newBlockSerializer(int size) {
        return new RemappingBlockIDSerializer(super.newBlockSerializer(size), this);
    }

    @Override
    public BlockIDDeserializer newBlockDeserializer(Map<String, Tag> schematic) throws IOException {
        return new RemappingBlockIDDeserializer(schematic, super.newBlockDeserializer(schematic), this);
    }
}
