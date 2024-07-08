package com.sk89q.worldedit.util;

public class FlatNibbleArray {
    public final byte[] store;
    public FlatNibbleArray(int capacity) {
        store = new byte[(capacity >> 1) + 1];
    }

    public FlatNibbleArray(byte[] store) {
        this.store = store;
    }

    public void set(int index, int value) {
        int idx = index >> 1;
        int current;
        if ((index & 1) == 0) {
            current = (store[idx] & 0xF0) | (value & 0xF);
        } else {
            current = (store[idx] & 0x0F) | ((value & 0xF) << 4);
        }
        store[idx] = (byte) current;
    }

    public int get(int index) {
        int idx = index >> 1;
        int current;
        if ((index & 1) == 0) {
            current = store[idx] & 0x0F;
        } else {
            current = (store[idx] & 0xF0) >> 4;
        }
        return current;
    }
}
