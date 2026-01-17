package net.xevianlight.aphelion.block.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockSteel extends Block {
    public BlockSteel(Properties properties) {
        super(properties);
    }

    public static Properties getProperties() {
        return Properties
                .of()
                .sound(SoundType.NETHERITE_BLOCK)
                .destroyTime(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

}
