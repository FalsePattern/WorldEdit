package com.sk89q.worldedit.extent.transform;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.math.transform.Transform;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class BlockTransformHooks implements BlockTransformHook {
    private final List<BlockTransformHook> hooks = new ArrayList<>();

    public void addHook(BlockTransformHook hook) {
        hooks.add(hook);
    }

    @Override
    public BaseBlock transformBlock(BaseBlock block, Transform transform) {
        for (val hook: hooks) {
            block = hook.transformBlock(block, transform);
        }
        return block;
    }
}
