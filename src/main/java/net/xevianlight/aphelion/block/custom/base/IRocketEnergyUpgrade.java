package net.xevianlight.aphelion.block.custom.base;

/**
 * Used for blocks which should provide energy storage capacity to a rocket.
 * <p>Note that blocks implementing this interface should not store energy themselves.
 * Rockets determine their energy capacity from the sum of these blocks installed on them.</p>
 * <p>Keep in mind that {@code TileEntity} blocks cannot be included in a {@code RocketStructure}.</p>
 */
public interface IRocketEnergyUpgrade {
    /**
     * Used to determine how much FE of energy storage a rocket receives from having this block is installed.
     */
    int getEnergyCapacity();

    /**
     * Used to determine how much FE transfer rate bonus a rocket receives from having this block is installed. This is added onto the base rockets energy transfer limit.
     */
    default int getMaxTransferBonus() {
        return 0;
    };
}
