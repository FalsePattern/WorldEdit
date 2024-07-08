package com.sk89q.worldedit.util.serialization;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.util.serialization.basic.BasicBlockIOFactory;
import lombok.val;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class SerializationUtil {
    private static Supplier<BlockIOFactory> instance = BasicBlockIOFactory::new;
    private static int currentPriority = -1;
    private static Throwable currentRegisterStacktrace = new Throwable();
    public static void registerConstructor(Supplier<BlockIOFactory> instance, int priority) {
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
            SerializationUtil.instance = instance;
            currentPriority = priority;
            currentRegisterStacktrace = stacktrace;
        }
    }

    public static BlockIOFactory create() {
        return instance.get();
    }

    public static <T extends Tag> T requireTag(Map<String, Tag> items, String key, Class<T> expected) throws IOException {
        if (!items.containsKey(key)) {
            throw new IOException("Schematic file is missing a \"" + key + "\" tag");
        }

        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IOException(key + " tag is not of tag type " + expected.getName());
        }

        return expected.cast(tag);
    }

    @Nullable
    public static <T extends Tag> T getTag(CompoundTag tag, Class<T> expected, String key) {
        Map<String, Tag> items = tag.getValue();

        if (!items.containsKey(key)) {
            return null;
        }

        Tag test = items.get(key);
        if (!expected.isInstance(test)) {
            return null;
        }

        return expected.cast(test);
    }
}
