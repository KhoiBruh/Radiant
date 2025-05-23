package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class EntitySheep extends EntityAnimal {
    private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new Container() {
        public boolean canInteractWith(EntityPlayer playerIn) {
            return false;
        }
    }, 2, 1);
    private static final Map<DyeColor, float[]> DYE_TO_RGB = new EnumMap<>(DyeColor.class);
    private int sheepTimer;
    private final EntityAIEatGrass entityAIEatGrass = new EntityAIEatGrass(this);

    public static float[] getDyeRgb(DyeColor dyeColor) {
        return DYE_TO_RGB.get(dyeColor);
    }

    public EntitySheep(World worldIn) {
        super(worldIn);
        this.setSize(0.9F, 1.3F);
        ((PathNavigateGround) this.getNavigator()).setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.WHEAT, false));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, this.entityAIEatGrass);
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.inventoryCrafting.setInventorySlotContents(0, new ItemStack(Items.DYE, 1, 0));
        this.inventoryCrafting.setInventorySlotContents(1, new ItemStack(Items.DYE, 1, 0));
    }

    protected void updateAITasks() {
        this.sheepTimer = this.entityAIEatGrass.getEatingGrassTimer();
        super.updateAITasks();
    }

    public void onLivingUpdate() {
        if (this.worldObj.isRemote) {
            this.sheepTimer = Math.max(0, this.sheepTimer - 1);
        }

        super.onLivingUpdate();
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
    }

    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(16, (byte) 0);
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        if (!this.getSheared()) {
            this.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, this.getFleeceColor().getMetadata()), 0.0F);
        }

        int i = this.rand.nextInt(2) + 1 + this.rand.nextInt(1 + lootingModifier);

        for (int j = 0; j < i; ++j) {
            if (this.isBurning()) {
                this.dropItem(Items.COOKED_MUTTON, 1);
            } else {
                this.dropItem(Items.MUTTON, 1);
            }
        }
    }

    protected Item getDropItem() {
        return Item.getItemFromBlock(Blocks.WOOL);
    }

    public void handleStatusUpdate(byte id) {
        if (id == 10) {
            this.sheepTimer = 40;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public float getHeadRotationPointY(float p_70894_1_) {
        return this.sheepTimer <= 0 ? 0.0F : (this.sheepTimer >= 4 && this.sheepTimer <= 36 ? 1.0F : (this.sheepTimer < 4 ? (this.sheepTimer - p_70894_1_) / 4.0F : -((this.sheepTimer - 40) - p_70894_1_) / 4.0F));
    }

    public float getHeadRotationAngleX(float p_70890_1_) {
        if (this.sheepTimer > 4 && this.sheepTimer <= 36) {
            float f = ((this.sheepTimer - 4) - p_70890_1_) / 32.0F;
            return ((float) Math.PI / 5.0F) + ((float) Math.PI * 7.0F / 100.0F) * MathHelper.sin(f * 28.7F);
        } else {
            return this.sheepTimer > 0 ? ((float) Math.PI / 5.0F) : this.rotationPitch / (180.0F / (float) Math.PI);
        }
    }

    public boolean interact(EntityPlayer player) {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.SHEARS && !this.getSheared() && !this.isChild()) {
            if (!this.worldObj.isRemote) {
                this.setSheared(true);
                int i = 1 + this.rand.nextInt(3);

                for (int j = 0; j < i; ++j) {
                    EntityItem entityitem = this.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, this.getFleeceColor().getMetadata()), 1.0F);
                    entityitem.motionY += (this.rand.nextFloat() * 0.05F);
                    entityitem.motionX += ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
                    entityitem.motionZ += ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
                }
            }

            itemstack.damageItem(1, player);
            this.playSound("mob.sheep.shear", 1.0F, 1.0F);
        }

        return super.interact(player);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("Sheared", this.getSheared());
        tagCompound.setByte("Color", (byte) this.getFleeceColor().getMetadata());
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.setSheared(tagCompund.getBoolean("Sheared"));
        this.setFleeceColor(DyeColor.byMetadata(tagCompund.getByte("Color")));
    }

    protected String getLivingSound() {
        return "mob.sheep.say";
    }

    protected String getHurtSound() {
        return "mob.sheep.say";
    }

    protected String getDeathSound() {
        return "mob.sheep.say";
    }

    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound("mob.sheep.step", 0.15F, 1.0F);
    }

    public DyeColor getFleeceColor() {
        return DyeColor.byMetadata(this.dataWatcher.getWatchableObjectByte(16) & 15);
    }

    public void setFleeceColor(DyeColor color) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(16);
        this.dataWatcher.updateObject(16, (byte) (b0 & 240 | color.getMetadata() & 15));
    }

    public boolean getSheared() {
        return (this.dataWatcher.getWatchableObjectByte(16) & 16) != 0;
    }

    public void setSheared(boolean sheared) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(16);

        if (sheared) {
            this.dataWatcher.updateObject(16, (byte) (b0 | 16));
        } else {
            this.dataWatcher.updateObject(16, (byte) (b0 & -17));
        }
    }

    public static DyeColor getRandomSheepColor(Random random) {
        int i = random.nextInt(100);
        return i < 5 ? DyeColor.BLACK : (i < 10 ? DyeColor.GRAY : (i < 15 ? DyeColor.SILVER : (i < 18 ? DyeColor.BROWN : (random.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE))));
    }

    public EntitySheep createChild(EntityAgeable ageable) {
        EntitySheep entitysheep = (EntitySheep) ageable;
        EntitySheep entitysheep1 = new EntitySheep(this.worldObj);
        entitysheep1.setFleeceColor(this.getDyeColorMixFromParents(this, entitysheep));
        return entitysheep1;
    }

    public void eatGrassBonus() {
        this.setSheared(false);

        if (this.isChild()) {
            this.addGrowth(60);
        }
    }

    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setFleeceColor(getRandomSheepColor(this.worldObj.rand));
        return livingdata;
    }

    private DyeColor getDyeColorMixFromParents(EntityAnimal father, EntityAnimal mother) {
        int i = ((EntitySheep) father).getFleeceColor().getDyeDamage();
        int j = ((EntitySheep) mother).getFleeceColor().getDyeDamage();
        this.inventoryCrafting.getStackInSlot(0).setItemDamage(i);
        this.inventoryCrafting.getStackInSlot(1).setItemDamage(j);
        ItemStack itemstack = CraftingManager.getInstance().findMatchingRecipe(this.inventoryCrafting, father.worldObj);
        int k;

        if (itemstack != null && itemstack.getItem() == Items.DYE) {
            k = itemstack.getMetadata();
        } else {
            k = this.worldObj.rand.nextBoolean() ? i : j;
        }

        return DyeColor.byDyeDamage(k);
    }

    public float getEyeHeight() {
        return 0.95F * this.height;
    }

    static {
        DYE_TO_RGB.put(DyeColor.WHITE, new float[]{1.0F, 1.0F, 1.0F});
        DYE_TO_RGB.put(DyeColor.ORANGE, new float[]{0.85F, 0.5F, 0.2F});
        DYE_TO_RGB.put(DyeColor.MAGENTA, new float[]{0.7F, 0.3F, 0.85F});
        DYE_TO_RGB.put(DyeColor.LIGHT_BLUE, new float[]{0.4F, 0.6F, 0.85F});
        DYE_TO_RGB.put(DyeColor.YELLOW, new float[]{0.9F, 0.9F, 0.2F});
        DYE_TO_RGB.put(DyeColor.LIME, new float[]{0.5F, 0.8F, 0.1F});
        DYE_TO_RGB.put(DyeColor.PINK, new float[]{0.95F, 0.5F, 0.65F});
        DYE_TO_RGB.put(DyeColor.GRAY, new float[]{0.3F, 0.3F, 0.3F});
        DYE_TO_RGB.put(DyeColor.SILVER, new float[]{0.6F, 0.6F, 0.6F});
        DYE_TO_RGB.put(DyeColor.CYAN, new float[]{0.3F, 0.5F, 0.6F});
        DYE_TO_RGB.put(DyeColor.PURPLE, new float[]{0.5F, 0.25F, 0.7F});
        DYE_TO_RGB.put(DyeColor.BLUE, new float[]{0.2F, 0.3F, 0.7F});
        DYE_TO_RGB.put(DyeColor.BROWN, new float[]{0.4F, 0.3F, 0.2F});
        DYE_TO_RGB.put(DyeColor.GREEN, new float[]{0.4F, 0.5F, 0.2F});
        DYE_TO_RGB.put(DyeColor.RED, new float[]{0.6F, 0.2F, 0.2F});
        DYE_TO_RGB.put(DyeColor.BLACK, new float[]{0.1F, 0.1F, 0.1F});
    }
}
