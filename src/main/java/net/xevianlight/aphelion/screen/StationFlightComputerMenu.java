package net.xevianlight.aphelion.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.xevianlight.aphelion.block.entity.custom.StationFlightComputerBlockEntity;
import org.jetbrains.annotations.NotNull;

public class StationFlightComputerMenu extends AbstractContainerMenu {
    public final StationFlightComputerBlockEntity blockEntity;

    public static final int DATA_TRAVELING    = 0;
    public static final int DATA_ENGINE_COUNT = 1;
    public static final int DATA_PAD_COUNT    = 2;
    public static final int DATA_COUNT        = 3;

    private final ContainerData data;

    public StationFlightComputerMenu(int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(windowId, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(DATA_COUNT));
    }

    public StationFlightComputerMenu(int windowId, Inventory inv, BlockEntity be, ContainerData data) {
        super(ModMenuTypes.STATION_FLIGHT_COMPUTER_MENU.get(), windowId);
        this.blockEntity = (StationFlightComputerBlockEntity) be;
        this.data = data;
        addDataSlots(data);
    }

    public boolean isTraveling()   { return data.get(DATA_TRAVELING) != 0; }
    public int getEngineCount()    { return data.get(DATA_ENGINE_COUNT); }
    public int getPadCount()       { return data.get(DATA_PAD_COUNT); }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        // Always valid — players can interact with the computer from anywhere on the station.
        return true;
    }
}
