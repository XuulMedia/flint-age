package com.xuul.flint.block;

import com.google.common.collect.Lists;
import com.xuul.flint.init.ModBlocks;
import com.xuul.flint.init.ModContainerTypes;
import com.xuul.flint.init.ModRecipeTypes;
import com.xuul.flint.recipe.FlintStationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.List;

public class FlintStationContainer extends AbstractContainerMenu {

    private final DataSlot selectedRecipeIndex = DataSlot.standalone();
    private final Level level;
    private final BlockPos pos;
    private IItemHandler playerInventory;
    private List<FlintStationRecipe> recipes = Lists.newArrayList();

    private ItemStack input = ItemStack.EMPTY;
    /**
     * Stores the game time of the last time the player took items from the the crafting result slot. This is used to
     * prevent the sound from being played multiple times on the same tick.
     */
    long lastSoundTime;
    final Slot inputSlot;
    /** The inventory slot that stores the output of the crafting recipe. */
    final Slot resultSlot;
    Runnable slotUpdateListener = () -> {
    };

    public final Container container = new SimpleContainer(1) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void setChanged() {
            super.setChanged();
            FlintStationContainer.this.slotsChanged(this);
            FlintStationContainer.this.slotUpdateListener.run();
        }
    };

    final ResultContainer resultContainer = new ResultContainer();


    public FlintStationContainer(int pContainerId, BlockPos pos, Inventory inventory,  Inventory playerInventory) {
        super(ModContainerTypes.FLINT_STATION_CONTAINER.get(), pContainerId);
        this.level = inventory.player.level;
        this.pos = pos;
        this.playerInventory = new InvWrapper(playerInventory);

        this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
        this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {

            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            public void onTake(Player player, ItemStack itemStack) {
                itemStack.onCraftedBy(player.level, player, itemStack.getCount());
                FlintStationContainer.this.resultContainer.awardUsedRecipes(player);
                ItemStack itemstack = FlintStationContainer.this.inputSlot.remove(1);
                if (!itemstack.isEmpty()) {
                    FlintStationContainer.this.setupResultSlot();
                }

                super.onTake(player, itemStack);
            }
        });
        layoutPlayerInventorySlots(8, 83);
        this.addDataSlot(this.selectedRecipeIndex);
    }



    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex.get();
    }

    public List<FlintStationRecipe> getRecipes() {
        return this.recipes;
    }

    public int getNumRecipes() {
        return this.recipes.size();
    }

    public boolean hasInputItem() {
        return this.inputSlot.hasItem() && !this.recipes.isEmpty();
    }



    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, pos), player, ModBlocks.FLINT_STATION.get());
    }

    public boolean clickMenuButton(Player pPlayer, int pId) {
        if (this.isValidRecipeIndex(pId)) {
            this.selectedRecipeIndex.set(pId);
            this.setupResultSlot();
        }
        return true;
    }
    private boolean isValidRecipeIndex(int index) {
        return index >= 0 && index < this.recipes.size();
    }

    public void slotsChanged(Container inventory) {
        ItemStack itemstack = this.inputSlot.getItem();
        if (!itemstack.is(this.input.getItem())) {
            this.input = itemstack.copy();
            this.setupRecipeList(inventory, itemstack);
        }

    }

    private void setupRecipeList(Container pInventory, ItemStack pStack) {
        this.recipes.clear();
        this.selectedRecipeIndex.set(-1);
        this.resultSlot.set(ItemStack.EMPTY);
        if (!pStack.isEmpty()) {
            this.recipes = this.level.getRecipeManager().getRecipesFor(ModRecipeTypes.FLINT_STATION, pInventory, this.level);
        }
    }

    void setupResultSlot() {
        if (!this.recipes.isEmpty() && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
            FlintStationRecipe flintStationRecipe = this.recipes.get(this.selectedRecipeIndex.get());
            this.resultContainer.setRecipeUsed(flintStationRecipe);
            this.resultSlot.set(flintStationRecipe.assemble(this.container));
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    public MenuType<?> getType() {
        return ModContainerTypes.FLINT_STATION_CONTAINER.get();
    }

    public void registerUpdateListener(Runnable pListener) {
        this.slotUpdateListener = pListener;
    }

    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return pSlot.container != this.resultContainer && super.canTakeItemForPickAll(pStack, pSlot);
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            Item item = itemstack1.getItem();
            itemstack = itemstack1.copy();
            if (pIndex == 1) {
                item.onCraftedBy(itemstack1, pPlayer.level, pPlayer);
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (pIndex == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, new SimpleContainer(itemstack1), this.level).isPresent()) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (pIndex >= 2 && pIndex < 29) {
                if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (pIndex >= 29 && pIndex < 38 && !this.moveItemStackTo(itemstack1, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
            this.broadcastChanges();
        }

        return itemstack;
    }

    /*TODO see what this affects*/
//    public void removed(Player pPlayer) {
//        super.removed(pPlayer);
//        this.resultContainer.removeItemNoUpdate(1);
//        this.access.execute((p_40313_, p_40314_) -> {
//            this.clearContainer(pPlayer, this.container);
//        });
//    }

/*Setup Player inventory*/

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }


}