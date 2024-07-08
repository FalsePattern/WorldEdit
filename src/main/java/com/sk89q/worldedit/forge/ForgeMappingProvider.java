package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.util.IDMapping;
import lombok.val;

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;

public class ForgeMappingProvider implements IDMapping.MappingProvider {
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
}
