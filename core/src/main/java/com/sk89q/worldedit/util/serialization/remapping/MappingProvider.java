package com.sk89q.worldedit.util.serialization.remapping;

public interface MappingProvider {
    int expectedMaxIDCount();

    String getNameFromID(int id);

    int getIDFromName(String name);
}
