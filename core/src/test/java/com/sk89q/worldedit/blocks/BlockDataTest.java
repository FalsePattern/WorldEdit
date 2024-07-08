/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.blocks;

import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BlockDataTest {

    @Test
    public void testRotateFlip() {
        for (int type = 0; type < 256; ++type) {
            for (int data = 0; data < 16; ++data) {
                final String message = type + "/" + data;

                //Test r90(r-90(x))==x
                assertEquals(data, BlockData.rotate90(type, BlockData.rotate90Reverse(type, data)), message);
                //Test r-90(r90(x))==x
                assertEquals(data, BlockData.rotate90Reverse(type, BlockData.rotate90(type, data)), message);

                final int flipped = BlockData.flip(type, BlockData.flip(type, data, FlipDirection.WEST_EAST), FlipDirection.NORTH_SOUTH);

                //Test r90(r90(x))==flipNS(flipWE(x))
                assertEquals(flipped, BlockData.rotate90(type, BlockData.rotate90(type, data)), message);
                //Test r-90(r-90(x))==flipNS(flipWE(x))
                assertEquals(flipped, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, data)), message);

                //Test flipNS(flipNS(x))==x
                assertEquals(data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.NORTH_SOUTH), FlipDirection.NORTH_SOUTH), message);
                //Test flipWE(flipWE(x))==x
                assertEquals(data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.WEST_EAST), FlipDirection.WEST_EAST), message);
                //Test flipUD(flipUD(x))==x
                assertEquals(data, BlockData.flip(type, BlockData.flip(type, data, FlipDirection.UP_DOWN), FlipDirection.UP_DOWN), message);

                //Test r90(r90(r90(r90(x))))==x
                assertEquals(data, BlockData.rotate90(type, BlockData.rotate90(type, BlockData.rotate90(type, BlockData.rotate90(type, data)))), message);
                //Test r-90(r-90(r-90(r-90(x))))==x
                assertEquals(data, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, BlockData.rotate90Reverse(type, data)))), message);
            }
        }
    }

    private static final TreeSet<Integer> datasTemplate = new TreeSet<Integer>();
    static {
        for (int data = 0; data < 16; ++data) {
            datasTemplate.add(data);
        }
    }

    @Test
    public void testCycle() {
        // Test monotony and continuity
        for (int type = 0; type < 256; ++type) {
            // Cloth isn't monotonous, and thus excluded.
            if (type == BlockID.CLOTH
                    || type == BlockID.STAINED_CLAY
                    || type == BlockID.STAINED_GLASS
                    || type == BlockID.STAINED_GLASS_PANE
                    || type == BlockID.CARPET) {
                continue;
            }

            for (int data = 0; data < 16; ++data) {
                final String message = type + "/" + data;

                final int cycled = BlockData.cycle(type, data, 1);

                // If the cycle goes back (including -1), everything is ok.
                if (cycled <= data) {
                    continue;
                }

                // If there's a gap in the cycle, there's a problem.
                assertEquals(data + 1, cycled, message);
            }
        }

        // Test cyclicity forwards
        testCycle(1);

        // ...and backwards
        testCycle(-1);
    }

    private static void testCycle(final int increment) {
        // Iterate each block type and data value that wasn't part of a cycle yet.
        for (int type = 0; type < 256; ++type) {
            @SuppressWarnings("unchecked")
            final TreeSet<Integer> datas = (TreeSet<Integer>) datasTemplate.clone();
            while (!datas.isEmpty()) {
                final int start = datas.pollFirst();
                String message = type + "/" + start;
                int current = start;
                boolean first = true;
                while (true) {
                    current = BlockData.cycle(type, current, increment);

                    // If the cycle immediately goes to -1, everything is ok.
                    if (first && current == -1) break;

                    first = false;
                    message += "->" + current;

                    // If the cycle goes off limits (including -1), there's a problem.
                    assertTrue(current >= 0, message);
                    assertTrue(current < 16, message);

                    // The cycle completes, everything is ok.
                    if (current == start) break;

                    // Mark the current element as walked.
                    assertTrue(datas.remove(current), message);
                }
            }
        }
    }

}
