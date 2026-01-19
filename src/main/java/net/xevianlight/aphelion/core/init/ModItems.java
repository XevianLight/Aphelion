package net.xevianlight.aphelion.core.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.custom.*;
import net.xevianlight.aphelion.item.*;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Aphelion.MOD_ID);

    public static final DeferredItem<Item> TEST_ITEM = ITEMS.register("test_item", TestItem::new);
    public static final DeferredItem<Item> INGOT_ALUMINUM = ITEMS.register("ingot_aluminum", IngotAluminum::new);
    public static final DeferredItem<Item> INGOT_STEEL = ITEMS.register("ingot_steel", IngotSteel::new);
    public static final DeferredItem<Item> INGOT_TITANIUM = ITEMS.register("ingot_titanium", IngotTitanium::new);
    public static final DeferredItem<Item> INGOT_URANIUM = ITEMS.register("ingot_uranium", IngotUranium::new);
    public static final DeferredItem<Item> INGOT_COBALT = ITEMS.register("ingot_cobalt", IngotCobalt::new);
    public static final DeferredItem<Item> INGOT_TUNGSTEN = ITEMS.register("ingot_tungsten", IngotTungsten::new);
    public static final DeferredItem<Item> INGOT_NEODYMIUM = ITEMS.register("ingot_neodymium", IngotNeodymium::new);
    public static final DeferredItem<Item> INGOT_IRIDIUM = ITEMS.register("ingot_iridium", IngotIridium::new);

public static final DeferredItem<Item> MUSIC_DISC_BIT_SHIFT = ITEMS.register("music_disc_bit_shift",
        () -> new Item(new Item.Properties().jukeboxPlayable(ModSounds.BIT_SHIFT_KEY).stacksTo(1).rarity(Rarity.RARE)));


    // Block Items

    public static final DeferredItem<BlockItem> TEST_BLOCK = ITEMS.register("test_block", () -> new BlockItem(ModBlocks.TEST_BLOCK.get(), TestBlock.getItemProperties().stacksTo(9999)));
    public static final DeferredItem<BlockItem> DIMENSION_CHANGER = ITEMS.register("dimension_changer", () -> new BlockItem(ModBlocks.DIMENSION_CHANGER.get(), DimensionChangerBlock.getItemProperties()));
    public static final DeferredItem<BlockItem> ELECTRIC_ARC_FURNACE = ITEMS.register("electric_arc_furnace", () -> new BlockItem(ModBlocks.ELECTRIC_ARC_FURNACE.get(), ElectricArcFurnace.getItemProperties()));
    public static final DeferredItem<BlockItem> BLOCK_STEEL = ITEMS.register("block_steel", () -> new BlockItem(ModBlocks.BLOCK_STEEL.get(), BlockSteel.getItemProperties()));
    public static final DeferredItem<BlockItem> ARC_FURNACE_CASING_BLOCK = ITEMS.register("arc_furnace_casing", () -> new BlockItem(ModBlocks.ARC_FURNACE_CASING_BLOCK.get(), ArcFurnaceCasingBlock.getItemProperties()));
    public static final DeferredItem<BlockItem> VACUUM_ARC_FURNACE_CONTROLLER = ITEMS.register("vacuum_arc_furnace_controller", () -> new BlockItem(ModBlocks.VACUUM_ARC_FURNACE_CONTROLLER.get(), VacuumArcFurnaceController.getItemProperties()));
}
