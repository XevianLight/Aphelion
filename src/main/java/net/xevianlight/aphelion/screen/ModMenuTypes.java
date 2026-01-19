package net.xevianlight.aphelion.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xevianlight.aphelion.Aphelion;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Aphelion.MOD_ID);

    public static DeferredHolder<MenuType<?>,MenuType<TestBlockMenu>> TEST_BLOCK_MENU =
        registerMenuType("test_block_menu", TestBlockMenu::new);

    public static DeferredHolder<MenuType<?>,MenuType<ElectricArcFurnaceMenu>> ELECTRIC_ARC_FURNACE_MENU =
            registerMenuType("electric_arc_furnace_menu", ElectricArcFurnaceMenu::new);

    public static DeferredHolder<MenuType<?>,MenuType<VacuumArcFurnaceMenu>> VACUUM_ARC_FURNACE_MENU =
            registerMenuType("vacuum_arc_furnace_menu", VacuumArcFurnaceMenu::new);

    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name,
                                                                                                              IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
