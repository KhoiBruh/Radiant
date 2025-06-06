package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Direction;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import java.util.List;

public class BlockAnvil extends BlockFalling {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", Direction.Plane.HORIZONTAL);
    public static final PropertyInteger DAMAGE = PropertyInteger.create("damage", 0, 2);

    protected BlockAnvil() {
        super(Material.ANVIL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.NORTH).withProperty(DAMAGE, 0));
        this.setLightOpacity(0);
        this.setCreativeTab(CreativeTabs.TAB_DECORATIONS);
    }

    public boolean isFullCube() {
        return false;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        Direction enumfacing = placer.getHorizontalFacing().rotateY();
        return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, enumfacing).withProperty(DAMAGE, meta >> 2);
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, Direction side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.displayGui(new Anvil(worldIn, pos));
        }

        return true;
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(DAMAGE);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        Direction enumfacing = worldIn.getBlockState(pos).getValue(FACING);

        if (enumfacing.getAxis() == Direction.Axis.X) {
            this.setBlockBounds(0.0F, 0.0F, 0.125F, 1.0F, 1.0F, 0.875F);
        } else {
            this.setBlockBounds(0.125F, 0.0F, 0.0F, 0.875F, 1.0F, 1.0F);
        }
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(itemIn, 1, 0));
        list.add(new ItemStack(itemIn, 1, 1));
        list.add(new ItemStack(itemIn, 1, 2));
    }

    protected void onStartFalling(EntityFallingBlock fallingEntity) {
        fallingEntity.setHurtEntities(true);
    }

    public void onEndFalling(World worldIn, BlockPos pos) {
        worldIn.playAuxSFX(1022, pos, 0);
    }

    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, Direction side) {
        return true;
    }

    public IBlockState getStateForEntityRender(IBlockState state) {
        return this.getDefaultState().withProperty(FACING, Direction.SOUTH);
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, Direction.getHorizontal(meta & 3)).withProperty(DAMAGE, (meta & 15) >> 2);
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();
        i = i | state.getValue(DAMAGE) << 2;
        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, FACING, DAMAGE);
    }

    public static class Anvil implements IInteractionObject {
        private final World world;
        private final BlockPos position;

        public Anvil(World worldIn, BlockPos pos) {
            this.world = worldIn;
            this.position = pos;
        }

        public String getName() {
            return "anvil";
        }

        public boolean hasCustomName() {
            return false;
        }

        public IChatComponent getDisplayName() {
            return new ChatComponentTranslation(Blocks.ANVIL.getUnlocalizedName() + ".name");
        }

        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
            return new ContainerRepair(playerInventory, this.world, this.position, playerIn);
        }

        public String getGuiID() {
            return "minecraft:anvil";
        }
    }
}
