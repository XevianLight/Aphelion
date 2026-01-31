package net.xevianlight.aphelion.client;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;

public final class ClientOxygenCache {
    public static final LongOpenHashSet OXYGEN = new LongOpenHashSet();
    public static BlockPos lastCenter = BlockPos.ZERO;
    public static long lastUpdateGameTime = -1;
}
