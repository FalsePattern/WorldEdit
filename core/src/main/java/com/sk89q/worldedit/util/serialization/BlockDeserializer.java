package com.sk89q.worldedit.util.serialization;

public interface BlockDeserializer {
    int length();

    int loadBlockID(int index);

    int loadBlockData(int index);
}
