package net.xevianlight.aphelion.block.custom.base;

/**
 * Used for blocks which should provide fluid storage capacity to a rocket.
 * <p>Note that blocks implementing this interface should not store fluids themselves.
 * Rockets determine their fluid container capacity from the sum of these blocks installed on them.</p>
 * <p>Keep in mind that {@code TileEntity} blocks cannot be included in a {@code RocketStructure}.</p>
 */
public interface IRocketFluidUpgrade {
    /**
     * Used to determine how many millibuckets of fluid storage a rocket receives from having this block is installed.
     */
    int getFluidCapacity();
}
