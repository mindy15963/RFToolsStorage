package mcjty.rftoolsstorage.blocks.basic;

import mcjty.lib.container.*;
import mcjty.rftoolsstorage.blocks.ModBlocks;
import mcjty.rftoolsstorage.craftinggrid.CraftingGridInventory;
import mcjty.rftoolsstorage.items.StorageModuleItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ModularStorageContainer extends GenericContainer {
    public static final String CONTAINER_GRID = "grid";

    public static final int SLOT_STORAGE_MODULE = 0;
    public static final int SLOT_TYPE_MODULE = 1;
    public static final int SLOT_FILTER_MODULE = 2;
    public static final int SLOT_STORAGE = 3;
    public static final int MAXSIZE_STORAGE = 300;

    private ModularStorageTileEntity modularStorageTileEntity;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, stack -> stack.getItem() instanceof StorageModuleItem), CONTAINER_CONTAINER, SLOT_STORAGE_MODULE, 5, 157, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, stack-> false /* @todo 1.14 StorageTypeItem.class*/), CONTAINER_CONTAINER, SLOT_TYPE_MODULE, 5, 175, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, stack-> false /* @todo 1.14 StorageFilterItem.class*/), CONTAINER_CONTAINER, SLOT_FILTER_MODULE, 5, 193, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_CONTAINER, SLOT_STORAGE, -500, -500, 30, 0, 10, 0);
            layoutPlayerInventorySlots(91, 157);
            layoutGridInventorySlots(CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET);
        }

        protected void layoutGridInventorySlots(int leftCol, int topRow) {
            this.addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTINPUT, leftCol, topRow, 3, 18, 3, 18);
            topRow += 58;
            this.addSlotRange(new SlotDefinition(SlotType.SLOT_GHOSTOUT), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTOUTPUT, leftCol, topRow, 1, 18);
        }

    };

    public ModularStorageTileEntity getModularStorageTileEntity() {
        return modularStorageTileEntity;
    }

    public ModularStorageContainer(int id, BlockPos pos, PlayerEntity player, ModularStorageTileEntity tileEntity) {
        super(ModBlocks.CONTAINER_MODULAR_STORAGE, id, factory, pos);
        modularStorageTileEntity = tileEntity;
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(ContainerFactory.CONTAINER_CONTAINER, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        addInventory(CONTAINER_GRID, modularStorageTileEntity.getCraftingGrid().getCraftingGridInventory());
        generateSlots();
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (CONTAINER_GRID.equals(slotFactory.getInventoryName())) {
                SlotType slotType = slotFactory.getSlotType();
                IItemHandler inventory = this.inventories.get(slotFactory.getInventoryName());
                int index = slotFactory.getIndex();
                int x = slotFactory.getX();
                int y = slotFactory.getY();
                slot = this.createSlot(slotFactory, inventory, index, x, y, slotType);
            } else if (slotFactory.getSlotType() == SlotType.SLOT_SPECIFICITEM) {
                final SlotDefinition slotDefinition = slotFactory.getSlotDefinition();
                slot = new SlotItemHandler(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return slotDefinition.itemStackMatches(stack);
                    }
                };
            } else if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV || slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            } else {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean getHasStack() {
                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + SLOT_STORAGE)) {
                            return false;
                        }
                        return super.getHasStack();
                    }

                    @Override
                    public ItemStack getStack() {
                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + SLOT_STORAGE)) {
                            return ItemStack.EMPTY;
                        }
                        return super.getStack();
                    }

                    @Override
                    public boolean canTakeStack(PlayerEntity player) {
                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + SLOT_STORAGE)) {
                            return false;
                        }
                        return super.canTakeStack(player);
                    }

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + SLOT_STORAGE)) {
                            return false;
                        }
                        // @todo 1.14
//                        if (!modularStorageTileEntity.isItemValidForSlot(getSlotIndex(), stack)) {
//                            return false;
//                        }
                        return super.isItemValid(stack);
                    }
                };
            }
            addSlot(slot);
        }
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType mode, PlayerEntity player) {
        if (index == SLOT_STORAGE_MODULE && !player.getEntityWorld().isRemote) {
            // @todo 1.14
//            modularStorageTileEntity.copyToModule();
        }
        return super.slotClick(index, button, mode, player);
    }

    @Override
    public void detectAndSendChanges() {
        // @todo 1.14
//        List<Pair<Integer, ItemStack>> differentSlots = new ArrayList<>();
//        for (int i = 0; i < this.inventorySlots.size(); ++i) {
//            ItemStack itemstack = this.inventorySlots.get(i).getStack();
//            ItemStack itemstack1 = inventoryItemStacks.get(i);
//
//            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
//                itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
//                inventoryItemStacks.set(i, itemstack1);
//                differentSlots.add(Pair.of(i, itemstack));
//                if (differentSlots.size() >= 30) {
//                    syncSlotsToListeners(differentSlots);
//                    // Make a new list so that the one we gave to syncSlots is preserved
//                    differentSlots = new ArrayList<>();
//                }
//            }
//        }
//        if (!differentSlots.isEmpty()) {
//            syncSlotsToListeners(differentSlots);
//        }
    }

    private void syncSlotsToListeners(List<Pair<Integer, ItemStack>> differentSlots) {
        String sortMode = modularStorageTileEntity.getSortMode();
        String viewMode = modularStorageTileEntity.getViewMode();
        boolean groupMode = modularStorageTileEntity.isGroupMode();
        String filter = modularStorageTileEntity.getFilter();

        // @todo 1.14
//        for (IContainerListener listener : this.listeners) {
//            if (listener instanceof PlayerEntity) {
//                PlayerEntity player = (PlayerEntity) listener;
//                RFToolsMessages.INSTANCE.sendTo(new PacketSyncSlotsToClient(
//                        modularStorageTileEntity.getPos(),
//                        sortMode, viewMode, groupMode, filter,
//                        modularStorageTileEntity.getMaxSize(),
//                        modularStorageTileEntity.getNumStacks(),
//                        differentSlots), player);
//            }
//        }
    }
}