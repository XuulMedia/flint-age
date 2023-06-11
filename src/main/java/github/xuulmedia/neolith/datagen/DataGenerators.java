package github.xuulmedia.neolith.datagen;

import github.xuulmedia.neolith.Neolith;
import github.xuulmedia.neolith.datagen.loot.BlockLootTableProvider;
import github.xuulmedia.neolith.datagen.loot.EntityLootTableProvider;
import github.xuulmedia.neolith.datagen.loot.ModLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Neolith.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider  = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));


        ModBlockTagProvider blockTagProvider = new ModBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        ModItemTagProvider itemTagProvider =  new ModItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), existingFileHelper);

//       generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(),itemTagProvider);


//        generator.addProvider(event.includeServer(), new BlockLootTableProvider(packOutput));
//        generator.addProvider(event.includeServer(), new EntityLootTableProvider(packOutput));
//        generator.addProvider(event.includeServer(), new ModBiomeTags(generator, event.getExistingFileHelper()));
//        generator.addProvider(event.includeServer(), new ModStructureSetTags(generator, event.getExistingFileHelper()));



        generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput, "en_us"));
    }


}
