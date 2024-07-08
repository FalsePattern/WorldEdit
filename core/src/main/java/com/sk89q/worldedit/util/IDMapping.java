package com.sk89q.worldedit.util;

import lombok.val;

import java.util.function.Supplier;

public class IDMapping {
    private static Supplier<MappingProvider> instance = () -> new MappingProvider() {
        @Override
        public int expectedMaxIDCount() {
            return 4096;
        }

        @Override
        public String getNameFromID(int id) {
            return Integer.toString(id);
        }

        @Override
        public int getIDFromName(String name) {
            try {
                return Integer.parseInt(name);
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
    };
    private static int currentPriority = -1;
    private static Throwable currentRegisterStacktrace = new Throwable();
    public static void register(Supplier<MappingProvider> instance, int priority) {
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
            IDMapping.instance = instance;
            currentPriority = priority;
            currentRegisterStacktrace = stacktrace;
        }
    }

    public static MappingProvider create() {
        return instance.get();
    }

    public interface MappingProvider {
        int expectedMaxIDCount();
        String getNameFromID(int id);
        int getIDFromName(String name);
    }
}
