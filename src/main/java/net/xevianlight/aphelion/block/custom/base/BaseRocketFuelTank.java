package net.xevianlight.aphelion.block.custom.base;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BaseRocketFuelTank extends Block implements IRocketFuelUpgrade {
    public BaseRocketFuelTank(Properties properties) {
        super(properties);
    }

    public static Properties getProperties() {
        return Properties
                .of()
                .sound(SoundType.METAL)
                .destroyTime(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    @Override
    public int getFuelCapacity() {
        return 0;
    }
}
