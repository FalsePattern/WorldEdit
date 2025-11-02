package com.sk89q.worldedit.forge.compat;

import net.minecraft.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;

public class CustomNpcHandler {

	public static boolean isNpc(Entity entity) {
		return entity instanceof EntityNPCInterface;
	}

	public static void deleteNpc(Entity entity) {
		try {
			((EntityNPCInterface) entity).delete();
		} catch (Exception e) {
			com.sk89q.worldedit.forge.ForgeWorldEdit.logger.warn("Error calling delete() method from CustomNPCs", e);
			entity.setDead();
		}
	}
}
