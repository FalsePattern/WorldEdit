package com.sk89q.worldedit.util.serialization;

import com.sk89q.jnbt.Tag;

import java.io.IOException;
import java.util.Map;

public interface BlockIOFactory {
    BlockSerializer newBlockSerializer(int size);

    BlockDeserializer newBlockDeserializer(Map<String, Tag> schematic) throws IOException;
}
