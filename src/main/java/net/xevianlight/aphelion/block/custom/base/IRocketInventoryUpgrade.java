package net.xevianlight.aphelion.block.custom.base;

/**
 * Used for blocks which should provide item slots to a rockets inventory.
 * <p>Note that blocks implementing this interface should not store items themselves.
 * Rockets determine their inventory slot count from the sum of these blocks installed on them.</p>
 * <p>Keep in mind that {@code TileEntity} blocks cannot be included in a {@code RocketStructure}.</p>
 */
public interface IRocketInventoryUpgrade {
    /**
     * Used to determine how many inventory slots a rocket receives from having this block installed.
     */

    int getSlotCapacity();
}
