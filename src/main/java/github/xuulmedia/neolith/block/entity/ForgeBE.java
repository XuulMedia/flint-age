package github.xuulmedia.neolith.block.entity;

import github.xuulmedia.neolith.gui.menu.ForgeMenu;
import github.xuulmedia.neolith.init.ModBlockEntities;
import github.xuulmedia.neolith.recipe.ForgeRecipe;
import github.xuulmedia.neolith.util.AdaptedItemHandler;
import github.xuulmedia.neolith.util.HeatingFuelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ForgeBE extends AbstractHeatingBlockEntity implements MenuProvider {
    public static final String DISPLAY_NAME = "Forge";
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_INPUT_COUNT = 1;

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_FUEL_COUNT = 1;

    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_OUTPUT_COUNT = 1;

    public static final int SLOT_COUNT  = SLOT_INPUT_COUNT + SLOT_FUEL_COUNT + SLOT_OUTPUT_COUNT;


    public ForgeBE(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.FORGE.get(), pWorldPosition, pBlockState);
//        this.itemHandler = createItemHandler(STACK_SIZE);
        this.inputItems = createItemHandler(SLOT_INPUT_COUNT);
        this.fuelItems = createItemHandler(SLOT_FUEL_COUNT);
        this.outputItems = createItemHandler(SLOT_OUTPUT_COUNT);
        this.lazyItemHandler =  LazyOptional.of(() -> new CombinedInvWrapper(inputItems, inputItems, outputItems, fuelItems));


        this.inputItemHandler = LazyOptional.of(() -> new AdaptedItemHandler(inputItems) {
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        });

        this.outputItemHandler = LazyOptional.of(() -> new AdaptedItemHandler(outputItems) {
            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return stack;
            }
        });

        this.fuelItemHandler = LazyOptional.of(() -> new AdaptedItemHandler(fuelItems) {
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        });

    }

    @Override
    protected int getFuelSlotIndex() {
        return SLOT_FUEL;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new ForgeMenu(pContainerId, pInventory, this, this.data);
    }


    public Component getDisplayName() {
        return Component.translatable(DISPLAY_NAME);
    }


    public static void tick(Level level, BlockPos pos, BlockState state, ForgeBE forgeBE) {
        HeatingFuelContainer heatContainer = new HeatingFuelContainer(forgeBE.fuelItems.getSlots()) {
            @Override
            public int getFuelSlot() {
                return SLOT_FUEL;
            }
        };

        for (int i = 0; i < forgeBE.fuelItems.getSlots(); i++) {
            heatContainer.setItem(i, forgeBE.fuelItems.getStackInSlot(i));
        }



        RecipeManager recipeManager = level.getRecipeManager();
        RecipeType<ForgeRecipe> type = ForgeRecipe.Type.INSTANCE;
        Optional<ForgeRecipe> match = recipeManager.getRecipeFor(type, heatContainer, level);

        AbstractHeatingBlockEntity.tickHeat(level, pos, state, forgeBE, heatContainer);

        ItemStack currentOutputStack = forgeBE.outputItems.getStackInSlot(SLOT_OUTPUT);

        boolean recipeOk = false;
        if(match.isPresent()){
            ForgeRecipe recipe = match.get();
            boolean ingredientTest = recipe.getIngredient().test(forgeBE.inputItems.getStackInSlot(SLOT_INPUT));
            boolean canInsertItem = canInsertItemIntoOutputSlot(forgeBE, match.get().getResultItem(level.registryAccess()));

            boolean spaceCheck = canInsertAmountIntoOutputSlot(forgeBE);
            boolean heatCheck = isHeatHighEnough(forgeBE, match);
            if(ingredientTest && canInsertItem && heatCheck && spaceCheck){
                recipeOk = true;
            }
        }
        if(recipeOk){
            forgeBE.progress++;
            if(forgeBE.progress > forgeBE.maxProgress) {
                forgeBE.progress = 0;
                if(currentOutputStack.isEmpty()){
                    forgeBE.outputItems.setStackInSlot(SLOT_OUTPUT, match.get().getResultItem(level.registryAccess()));
                } else{
                    currentOutputStack.grow(1);
                }
                forgeBE.inputItems.getStackInSlot(SLOT_INPUT).shrink(1);
           }

        }
    }


    private static boolean canInsertItemIntoOutputSlot(ForgeBE forgeBE, ItemStack resultItem){
        return forgeBE.outputItems.getStackInSlot(SLOT_OUTPUT).isEmpty() || forgeBE.outputItems.getStackInSlot(SLOT_OUTPUT).getItem() == resultItem.getItem();
    }
    private  static boolean canInsertAmountIntoOutputSlot(ForgeBE forgeBE) {
        return forgeBE.outputItems.getStackInSlot(SLOT_OUTPUT).getMaxStackSize() > forgeBE.outputItems.getStackInSlot(SLOT_OUTPUT).getCount();
    }
    protected static boolean isHeatHighEnough(ForgeBE forgeBE, Optional<ForgeRecipe> match){
        return  match.get().getHeatRequired() <= forgeBE.heat;
    }

}
