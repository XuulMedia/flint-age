package com.xuul.flint;

import com.xuul.flint.init.ModBlocks;
import com.xuul.flint.init.ModItems;
import com.xuul.flint.init.ModTags;
import com.xuul.flint.util.CustomTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.C;

@Mod(Flint.MOD_ID)
public class Flint
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "flint";

    /*Creative Mode Tabs*/
    public static final CustomTabs FLINT_TAB = new CustomTabs("flint_tab", ModItems.CHUNK_GRANITE);
    public static final CustomTabs IRON_TAB = new CustomTabs("iron_tab", ModItems.INGOT_STEEL);


    public Flint() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);


        // Register the setup method for modloading
/*        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        modbus.addListener(ModSetup::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(ClientSetup::init));*/
    }





}
