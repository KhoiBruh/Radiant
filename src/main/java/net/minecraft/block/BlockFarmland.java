package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFarmland extends Block {
    public static final PropertyInteger MOISTURE = PropertyInteger.create("moisture", 0, 7);

    protected BlockFarmland() {
        super(Material.GROUND);
        this.setDefaultState(this.blockState.getBaseState().withProperty(MOISTURE, 0));
        this.setTickRandomly(true);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.9375F, 1.0F);
        this.setLightOpacity(255);
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1));
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        int i = state.getValue(MOISTURE);

        if (!this.hasWater(worldIn, pos) && !worldIn.isRainingAt(pos.up())) {
            if (i > 0) {
                worldIn.setBlockState(pos, state.withProperty(MOISTURE, i - 1), 2);
            } else if (!this.hasCrops(worldIn, pos)) {
                worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
            }
        } else if (i < 7) {
            worldIn.setBlockState(pos, state.withProperty(MOISTURE, 7), 2);
        }
    }

    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (entityIn instanceof EntityLivingBase) {
            if (!worldIn.isRemote && worldIn.rand.nextFloat() < fallDistance - 0.5F) {
                if (!(entityIn instanceof EntityPlayer) && !worldIn.getGameRules().getBoolean("mobGriefing")) {
                    return;
                }

                worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
            }

            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        }
    }

    private boolean hasCrops(World worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos.up()).getBlock();
        return block instanceof BlockCrops || block instanceof BlockStem;
    }

    private boolean hasWater(World worldIn, BlockPos pos) {
        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
            if (worldIn.getBlockState(blockpos$mutableblockpos).getBlock().getMaterial() == Material.WATER) {
                return true;
            }
        }

        return false;
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);

        if (worldIn.getBlockState(pos.up()).getBlock().getMaterial().isSolid()) {
            worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
        }
    }

    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, Direction side) {
        return switch (side) {
            case UP -> true;
            case NORTH, SOUTH, WEST, EAST -> {
                Block block = worldIn.getBlockState(pos).getBlock();
                yield !block.isOpaqueCube() && block != Blocks.FARMLAND;
            }
            default -> super.shouldSideBeRendered(worldIn, pos, side);
        };
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), rand, fortune);
    }

    public Item getItem(World worldIn, BlockPos pos) {
        return Item.getItemFromBlock(Blocks.DIRT);
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(MOISTURE, meta & 7);
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(MOISTURE);
    }

    protected BlockState createBlockState() {
        return new BlockState(this, MOISTURE);
    }
}
