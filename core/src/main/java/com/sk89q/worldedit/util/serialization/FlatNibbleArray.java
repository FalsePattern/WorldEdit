package com.sk89q.worldedit.util.serialization;

public class FlatNibbleArray {
    public final byte[] store;
    private final boolean schematicaFriendly;
    public FlatNibbleArray(int capacity, boolean schematicaFriendly) {
        store = new byte[(capacity >> 1) + 1];
        this.schematicaFriendly = schematicaFriendly;
    }

    public FlatNibbleArray(byte[] store, boolean schematicaFriendly) {
        this.store = store;
        this.schematicaFriendly = schematicaFriendly;
    }

    public void set(int index, int value) {
        int idx = index >> 1;
        int current;
        if ((index & 1) == 0 ^ schematicaFriendly) {
            current = (store[idx] & 0xF0) | (value & 0xF);
        } else {
            current = (store[idx] & 0x0F) | ((value & 0xF) << 4);
        }
        store[idx] = (byte) current;
    }

    public int get(int index) {
        int idx = index >> 1;
        if (idx < 0 || idx >= store.length)
            return 0;
        int current;
        if ((index & 1) == 0 ^ schematicaFriendly) {
            current = store[idx] & 0x0F;
        } else {
            current = (store[idx] & 0xF0) >> 4;
        }
        return current;
    }
}
