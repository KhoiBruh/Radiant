package net.minecraft.item;

import com.google.common.base.Predicates;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.world.World;

import java.util.List;

public class ItemArmor extends Item {
    private static final int[] MAX_DAMAGE_ARRAY = new int[]{11, 16, 15, 13};
    public static final String[] EMPTY_SLOT_NAMES = new String[]{"minecraft:items/empty_armor_slot_helmet", "minecraft:items/empty_armor_slot_chestplate", "minecraft:items/empty_armor_slot_leggings", "minecraft:items/empty_armor_slot_boots"};
    private static final IBehaviorDispenseItem DISPENSER_BEHAVIOR = new BehaviorDefaultDispenseItem() {
        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            BlockPos blockpos = source.getBlockPos().offset(BlockDispenser.getFacing(source.getBlockMetadata()));
            int i = blockpos.getX();
            int j = blockpos.getY();
            int k = blockpos.getZ();
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(i, j, k, (i + 1), (j + 1), (k + 1));
            List<EntityLivingBase> list = source.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb, Predicates.and(EntitySelectors.NOT_SPECTATING, new EntitySelectors.ArmoredMob(stack)));

            if (!list.isEmpty()) {
                EntityLivingBase entitylivingbase = list.getFirst();
                int l = entitylivingbase instanceof EntityPlayer ? 1 : 0;
                int i1 = EntityLiving.getArmorPosition(stack);
                ItemStack itemstack = stack.copy();
                itemstack.stackSize = 1;
                entitylivingbase.setCurrentItemOrArmor(i1 - l, itemstack);

                if (entitylivingbase instanceof EntityLiving entityLiving) {
                    entityLiving.setEquipmentDropChance(i1, 2.0F);
                }

                --stack.stackSize;
                return stack;
            } else {
                return super.dispenseStack(source, stack);
            }
        }
    };
    public final int armorType;
    public final int damageReduceAmount;
    public final int renderIndex;
    private final ArmorMaterial material;

    public ItemArmor(ArmorMaterial material, int renderIndex, int armorType) {
        this.material = material;
        this.armorType = armorType;
        this.renderIndex = renderIndex;
        this.damageReduceAmount = material.getDamageReductionAmount(armorType);
        this.setMaxDamage(material.getDurability(armorType));
        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.TAB_COMBAT);
        BlockDispenser.dispenseBehaviorRegistry.putObject(this, DISPENSER_BEHAVIOR);
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        if (renderPass > 0) {
            return 16777215;
        } else {
            int i = this.getColor(stack);

            if (i < 0) {
                i = 16777215;
            }

            return i;
        }
    }

    public int getItemEnchantability() {
        return this.material.getEnchantability();
    }

    public ArmorMaterial getArmorMaterial() {
        return this.material;
    }

    public boolean hasColor(ItemStack stack) {
        return this.material == ArmorMaterial.LEATHER && (stack.hasTagCompound() && (stack.getTagCompound().hasKey("display", 10) && stack.getTagCompound().getCompoundTag("display").hasKey("color", 3)));
    }

    public int getColor(ItemStack stack) {
        if (this.material != ArmorMaterial.LEATHER) {
            return -1;
        } else {
            NBTTagCompound nbttagcompound = stack.getTagCompound();

            if (nbttagcompound != null) {
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

                if (nbttagcompound1 != null && nbttagcompound1.hasKey("color", 3)) {
                    return nbttagcompound1.getInteger("color");
                }
            }

            return 10511680;
        }
    }

    public void removeColor(ItemStack stack) {
        if (this.material == ArmorMaterial.LEATHER) {
            NBTTagCompound nbttagcompound = stack.getTagCompound();

            if (nbttagcompound != null) {
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

                if (nbttagcompound1.hasKey("color")) {
                    nbttagcompound1.removeTag("color");
                }
            }
        }
    }

    public void setColor(ItemStack stack, int color) {
        if (this.material != ArmorMaterial.LEATHER) {
            throw new UnsupportedOperationException("Can't dye non-leather!");
        } else {
            NBTTagCompound nbttagcompound = stack.getTagCompound();

            if (nbttagcompound == null) {
                nbttagcompound = new NBTTagCompound();
                stack.setTagCompound(nbttagcompound);
            }

            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

            if (!nbttagcompound.hasKey("display", 10)) {
                nbttagcompound.setTag("display", nbttagcompound1);
            }

            nbttagcompound1.setInteger("color", color);
        }
    }

    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return this.material.getRepairItem() == repair.getItem() || super.getIsRepairable(toRepair, repair);
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
        int i = EntityLiving.getArmorPosition(itemStackIn) - 1;
        ItemStack itemstack = playerIn.getCurrentArmor(i);

        if (itemstack == null) {
            playerIn.setCurrentItemOrArmor(i, itemStackIn.copy());
            itemStackIn.stackSize = 0;
        }

        return itemStackIn;
    }

    public enum ArmorMaterial {
        LEATHER("leather", 5, new int[]{1, 3, 2, 1}, 15),
        CHAIN("chainmail", 15, new int[]{2, 5, 4, 1}, 12),
        IRON("iron", 15, new int[]{2, 6, 5, 2}, 9),
        GOLD("gold", 7, new int[]{2, 5, 3, 1}, 25),
        DIAMOND("diamond", 33, new int[]{3, 8, 6, 3}, 10);

        private final String name;
        private final int maxDamageFactor;
        private final int[] damageReductionAmountArray;
        private final int enchantability;

        ArmorMaterial(String name, int maxDamage, int[] reductionAmounts, int enchantability) {
            this.name = name;
            this.maxDamageFactor = maxDamage;
            this.damageReductionAmountArray = reductionAmounts;
            this.enchantability = enchantability;
        }

        public int getDurability(int armorType) {
            return ItemArmor.MAX_DAMAGE_ARRAY[armorType] * this.maxDamageFactor;
        }

        public int getDamageReductionAmount(int armorType) {
            return this.damageReductionAmountArray[armorType];
        }

        public int getEnchantability() {
            return this.enchantability;
        }

        public Item getRepairItem() {
            return this == LEATHER ? Items.LEATHER : (this == CHAIN ? Items.IRON_INGOT : (this == GOLD ? Items.GOLD_INGOT : (this == IRON ? Items.IRON_INGOT : (this == DIAMOND ? Items.DIAMOND : null))));
        }

        public String getName() {
            return this.name;
        }
    }
}
