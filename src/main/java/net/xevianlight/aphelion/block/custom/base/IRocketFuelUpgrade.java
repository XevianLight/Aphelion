package net.xevianlight.aphelion.block.custom.base;

import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Used for blocks which should provide fuel storage capacity to a rocket.
 * <p>Note that blocks implementing this interface should not store fuel themselves.
 * Rockets determine their fuel container capacity from the sum of these blocks installed on them.</p>
 * <p>Keep in mind that {@code TileEntity} blocks cannot be included in a {@code RocketStructure}.</p>
 */
public interface IRocketFuelUpgrade {
    /**
     * Used to determine how many millibuckets of fuel storage a rocket receives from having this block is installed.
     */
    int getFuelCapacity();
}
