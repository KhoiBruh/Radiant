package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockStairs extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", Direction.Plane.HORIZONTAL);
    public static final PropertyEnum<EnumHalf> HALF = PropertyEnum.create("half", EnumHalf.class);
    public static final PropertyEnum<StairsShape> SHAPE = PropertyEnum.create("shape", StairsShape.class);
    private static final int[][] field_150150_a = new int[][]{{4, 5}, {5, 7}, {6, 7}, {4, 6}, {0, 1}, {1, 3}, {2, 3}, {0, 2}};
    private final Block modelBlock;
    private final IBlockState modelState;
    private boolean hasRaytraced;
    private int rayTracePass;

    protected BlockStairs(IBlockState modelState) {
        super(modelState.getBlock().blockMaterial);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.NORTH).withProperty(HALF, EnumHalf.BOTTOM).withProperty(SHAPE, StairsShape.STRAIGHT));
        this.modelBlock = modelState.getBlock();
        this.modelState = modelState;
        this.setHardness(this.modelBlock.blockHardness);
        this.setResistance(this.modelBlock.blockResistance / 3.0F);
        this.setStepSound(this.modelBlock.stepSound);
        this.setLightOpacity(255);
        this.setCreativeTab(CreativeTabs.TAB_BLOCK);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        if (this.hasRaytraced) {
            this.setBlockBounds(0.5F * (this.rayTracePass % 2), 0.5F * (this.rayTracePass / 4 % 2), 0.5F * (this.rayTracePass / 2 % 2), 0.5F + 0.5F * (this.rayTracePass % 2), 0.5F + 0.5F * (this.rayTracePass / 4 % 2), 0.5F + 0.5F * (this.rayTracePass / 2 % 2));
        } else {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    public void setBaseCollisionBounds(IBlockAccess worldIn, BlockPos pos) {
        if (worldIn.getBlockState(pos).getValue(HALF) == EnumHalf.TOP) {
            this.setBlockBounds(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
        } else {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        }
    }

    public static boolean isBlockStairs(Block blockIn) {
        return blockIn instanceof BlockStairs;
    }

    public static boolean isSameStair(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return isBlockStairs(block) && iblockstate.getValue(HALF) == state.getValue(HALF) && iblockstate.getValue(FACING) == state.getValue(FACING);
    }

    public int func_176307_f(IBlockAccess blockAccess, BlockPos pos) {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        Direction enumfacing = iblockstate.getValue(FACING);
        EnumHalf blockstairs$enumhalf = iblockstate.getValue(HALF);
        boolean flag = blockstairs$enumhalf == EnumHalf.TOP;

        if (enumfacing == Direction.EAST) {
            IBlockState iblockstate1 = blockAccess.getBlockState(pos.east());
            Block block = iblockstate1.getBlock();

            if (isBlockStairs(block) && blockstairs$enumhalf == iblockstate1.getValue(HALF)) {
                Direction enumfacing1 = iblockstate1.getValue(FACING);

                if (enumfacing1 == Direction.NORTH && !isSameStair(blockAccess, pos.south(), iblockstate)) {
                    return flag ? 1 : 2;
                }

                if (enumfacing1 == Direction.SOUTH && !isSameStair(blockAccess, pos.north(), iblockstate)) {
                    return flag ? 2 : 1;
                }
            }
        } else if (enumfacing == Direction.WEST) {
            IBlockState iblockstate2 = blockAccess.getBlockState(pos.west());
            Block block1 = iblockstate2.getBlock();

            if (isBlockStairs(block1) && blockstairs$enumhalf == iblockstate2.getValue(HALF)) {
                Direction enumfacing2 = iblockstate2.getValue(FACING);

                if (enumfacing2 == Direction.NORTH && !isSameStair(blockAccess, pos.south(), iblockstate)) {
                    return flag ? 2 : 1;
                }

                if (enumfacing2 == Direction.SOUTH && !isSameStair(blockAccess, pos.north(), iblockstate)) {
                    return flag ? 1 : 2;
                }
            }
        } else if (enumfacing == Direction.SOUTH) {
            IBlockState iblockstate3 = blockAccess.getBlockState(pos.south());
            Block block2 = iblockstate3.getBlock();

            if (isBlockStairs(block2) && blockstairs$enumhalf == iblockstate3.getValue(HALF)) {
                Direction enumfacing3 = iblockstate3.getValue(FACING);

                if (enumfacing3 == Direction.WEST && !isSameStair(blockAccess, pos.east(), iblockstate)) {
                    return flag ? 2 : 1;
                }

                if (enumfacing3 == Direction.EAST && !isSameStair(blockAccess, pos.west(), iblockstate)) {
                    return flag ? 1 : 2;
                }
            }
        } else if (enumfacing == Direction.NORTH) {
            IBlockState iblockstate4 = blockAccess.getBlockState(pos.north());
            Block block3 = iblockstate4.getBlock();

            if (isBlockStairs(block3) && blockstairs$enumhalf == iblockstate4.getValue(HALF)) {
                Direction enumfacing4 = iblockstate4.getValue(FACING);

                if (enumfacing4 == Direction.WEST && !isSameStair(blockAccess, pos.east(), iblockstate)) {
                    return flag ? 1 : 2;
                }

                if (enumfacing4 == Direction.EAST && !isSameStair(blockAccess, pos.west(), iblockstate)) {
                    return flag ? 2 : 1;
                }
            }
        }

        return 0;
    }

    public int func_176305_g(IBlockAccess blockAccess, BlockPos pos) {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        Direction enumfacing = iblockstate.getValue(FACING);
        EnumHalf blockstairs$enumhalf = iblockstate.getValue(HALF);
        boolean flag = blockstairs$enumhalf == EnumHalf.TOP;

        if (enumfacing == Direction.EAST) {
            IBlockState iblockstate1 = blockAccess.getBlockState(pos.west());
            Block block = iblockstate1.getBlock();

            if (isBlockStairs(block) && blockstairs$enumhalf == iblockstate1.getValue(HALF)) {
                Direction enumfacing1 = iblockstate1.getValue(FACING);

                if (enumfacing1 == Direction.NORTH && !isSameStair(blockAccess, pos.north(), iblockstate)) {
                    return flag ? 1 : 2;
                }

                if (enumfacing1 == Direction.SOUTH && !isSameStair(blockAccess, pos.south(), iblockstate)) {
                    return flag ? 2 : 1;
                }
            }
        } else if (enumfacing == Direction.WEST) {
            IBlockState iblockstate2 = blockAccess.getBlockState(pos.east());
            Block block1 = iblockstate2.getBlock();

            if (isBlockStairs(block1) && blockstairs$enumhalf == iblockstate2.getValue(HALF)) {
                Direction enumfacing2 = iblockstate2.getValue(FACING);

                if (enumfacing2 == Direction.NORTH && !isSameStair(blockAccess, pos.north(), iblockstate)) {
                    return flag ? 2 : 1;
                }

                if (enumfacing2 == Direction.SOUTH && !isSameStair(blockAccess, pos.south(), iblockstate)) {
                    return flag ? 1 : 2;
                }
            }
        } else if (enumfacing == Direction.SOUTH) {
            IBlockState iblockstate3 = blockAccess.getBlockState(pos.north());
            Block block2 = iblockstate3.getBlock();

            if (isBlockStairs(block2) && blockstairs$enumhalf == iblockstate3.getValue(HALF)) {
                Direction enumfacing3 = iblockstate3.getValue(FACING);

                if (enumfacing3 == Direction.WEST && !isSameStair(blockAccess, pos.west(), iblockstate)) {
                    return flag ? 2 : 1;
                }

                if (enumfacing3 == Direction.EAST && !isSameStair(blockAccess, pos.east(), iblockstate)) {
                    return flag ? 1 : 2;
                }
            }
        } else if (enumfacing == Direction.NORTH) {
            IBlockState iblockstate4 = blockAccess.getBlockState(pos.south());
            Block block3 = iblockstate4.getBlock();

            if (isBlockStairs(block3) && blockstairs$enumhalf == iblockstate4.getValue(HALF)) {
                Direction enumfacing4 = iblockstate4.getValue(FACING);

                if (enumfacing4 == Direction.WEST && !isSameStair(blockAccess, pos.west(), iblockstate)) {
                    return flag ? 1 : 2;
                }

                if (enumfacing4 == Direction.EAST && !isSameStair(blockAccess, pos.east(), iblockstate)) {
                    return flag ? 2 : 1;
                }
            }
        }

        return 0;
    }

    public boolean func_176306_h(IBlockAccess blockAccess, BlockPos pos) {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        Direction enumfacing = iblockstate.getValue(FACING);
        EnumHalf blockstairs$enumhalf = iblockstate.getValue(HALF);
        boolean flag = blockstairs$enumhalf == EnumHalf.TOP;
        float f = 0.5F;
        float f1 = 1.0F;

        if (flag) {
            f = 0.0F;
            f1 = 0.5F;
        }

        float f2 = 0.0F;
        float f3 = 1.0F;
        float f4 = 0.0F;
        float f5 = 0.5F;
        boolean flag1 = true;

        if (enumfacing == Direction.EAST) {
            f2 = 0.5F;
            f5 = 1.0F;
            IBlockState iblockstate1 = blockAccess.getBlockState(pos.east());
            Block block = iblockstate1.getBlock();

            if (isBlockStairs(block) && blockstairs$enumhalf == iblockstate1.getValue(HALF)) {
                Direction enumfacing1 = iblockstate1.getValue(FACING);

                if (enumfacing1 == Direction.NORTH && !isSameStair(blockAccess, pos.south(), iblockstate)) {
                    f5 = 0.5F;
                    flag1 = false;
                } else if (enumfacing1 == Direction.SOUTH && !isSameStair(blockAccess, pos.north(), iblockstate)) {
                    f4 = 0.5F;
                    flag1 = false;
                }
            }
        } else if (enumfacing == Direction.WEST) {
            f3 = 0.5F;
            f5 = 1.0F;
            IBlockState iblockstate2 = blockAccess.getBlockState(pos.west());
            Block block1 = iblockstate2.getBlock();

            if (isBlockStairs(block1) && blockstairs$enumhalf == iblockstate2.getValue(HALF)) {
                Direction enumfacing2 = iblockstate2.getValue(FACING);

                if (enumfacing2 == Direction.NORTH && !isSameStair(blockAccess, pos.south(), iblockstate)) {
                    f5 = 0.5F;
                    flag1 = false;
                } else if (enumfacing2 == Direction.SOUTH && !isSameStair(blockAccess, pos.north(), iblockstate)) {
                    f4 = 0.5F;
                    flag1 = false;
                }
            }
        } else if (enumfacing == Direction.SOUTH) {
            f4 = 0.5F;
            f5 = 1.0F;
            IBlockState iblockstate3 = blockAccess.getBlockState(pos.south());
            Block block2 = iblockstate3.getBlock();

            if (isBlockStairs(block2) && blockstairs$enumhalf == iblockstate3.getValue(HALF)) {
                Direction enumfacing3 = iblockstate3.getValue(FACING);

                if (enumfacing3 == Direction.WEST && !isSameStair(blockAccess, pos.east(), iblockstate)) {
                    f3 = 0.5F;
                    flag1 = false;
                } else if (enumfacing3 == Direction.EAST && !isSameStair(blockAccess, pos.west(), iblockstate)) {
                    f2 = 0.5F;
                    flag1 = false;
                }
            }
        } else if (enumfacing == Direction.NORTH) {
            IBlockState iblockstate4 = blockAccess.getBlockState(pos.north());
            Block block3 = iblockstate4.getBlock();

            if (isBlockStairs(block3) && blockstairs$enumhalf == iblockstate4.getValue(HALF)) {
                Direction enumfacing4 = iblockstate4.getValue(FACING);

                if (enumfacing4 == Direction.WEST && !isSameStair(blockAccess, pos.east(), iblockstate)) {
                    f3 = 0.5F;
                    flag1 = false;
                } else if (enumfacing4 == Direction.EAST && !isSameStair(blockAccess, pos.west(), iblockstate)) {
                    f2 = 0.5F;
                    flag1 = false;
                }
            }
        }

        this.setBlockBounds(f2, f, f4, f3, f1, f5);
        return flag1;
    }

    public boolean func_176304_i(IBlockAccess blockAccess, BlockPos pos) {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        Direction enumfacing = iblockstate.getValue(FACING);
        EnumHalf blockstairs$enumhalf = iblockstate.getValue(HALF);
        boolean flag = blockstairs$enumhalf == EnumHalf.TOP;
        float f = 0.5F;
        float f1 = 1.0F;

        if (flag) {
            f = 0.0F;
            f1 = 0.5F;
        }

        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = 0.5F;
        float f5 = 1.0F;
        boolean flag1 = false;

        if (enumfacing == Direction.EAST) {
            IBlockState iblockstate1 = blockAccess.getBlockState(pos.west());
            Block block = iblockstate1.getBlock();

            if (isBlockStairs(block) && blockstairs$enumhalf == iblockstate1.getValue(HALF)) {
                Direction enumfacing1 = iblockstate1.getValue(FACING);

                if (enumfacing1 == Direction.NORTH && !isSameStair(blockAccess, pos.north(), iblockstate)) {
                    f4 = 0.0F;
                    f5 = 0.5F;
                    flag1 = true;
                } else if (enumfacing1 == Direction.SOUTH && !isSameStair(blockAccess, pos.south(), iblockstate)) {
                    f4 = 0.5F;
                    f5 = 1.0F;
                    flag1 = true;
                }
            }
        } else if (enumfacing == Direction.WEST) {
            IBlockState iblockstate2 = blockAccess.getBlockState(pos.east());
            Block block1 = iblockstate2.getBlock();

            if (isBlockStairs(block1) && blockstairs$enumhalf == iblockstate2.getValue(HALF)) {
                f2 = 0.5F;
                f3 = 1.0F;
                Direction enumfacing2 = iblockstate2.getValue(FACING);

                if (enumfacing2 == Direction.NORTH && !isSameStair(blockAccess, pos.north(), iblockstate)) {
                    f4 = 0.0F;
                    f5 = 0.5F;
                    flag1 = true;
                } else if (enumfacing2 == Direction.SOUTH && !isSameStair(blockAccess, pos.south(), iblockstate)) {
                    f4 = 0.5F;
                    f5 = 1.0F;
                    flag1 = true;
                }
            }
        } else if (enumfacing == Direction.SOUTH) {
            IBlockState iblockstate3 = blockAccess.getBlockState(pos.north());
            Block block2 = iblockstate3.getBlock();

            if (isBlockStairs(block2) && blockstairs$enumhalf == iblockstate3.getValue(HALF)) {
                f4 = 0.0F;
                f5 = 0.5F;
                Direction enumfacing3 = iblockstate3.getValue(FACING);

                if (enumfacing3 == Direction.WEST && !isSameStair(blockAccess, pos.west(), iblockstate)) {
                    flag1 = true;
                } else if (enumfacing3 == Direction.EAST && !isSameStair(blockAccess, pos.east(), iblockstate)) {
                    f2 = 0.5F;
                    f3 = 1.0F;
                    flag1 = true;
                }
            }
        } else if (enumfacing == Direction.NORTH) {
            IBlockState iblockstate4 = blockAccess.getBlockState(pos.south());
            Block block3 = iblockstate4.getBlock();

            if (isBlockStairs(block3) && blockstairs$enumhalf == iblockstate4.getValue(HALF)) {
                Direction enumfacing4 = iblockstate4.getValue(FACING);

                if (enumfacing4 == Direction.WEST && !isSameStair(blockAccess, pos.west(), iblockstate)) {
                    flag1 = true;
                } else if (enumfacing4 == Direction.EAST && !isSameStair(blockAccess, pos.east(), iblockstate)) {
                    f2 = 0.5F;
                    f3 = 1.0F;
                    flag1 = true;
                }
            }
        }

        if (flag1) {
            this.setBlockBounds(f2, f, f4, f3, f1, f5);
        }

        return flag1;
    }

    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        this.setBaseCollisionBounds(worldIn, pos);
        super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        boolean flag = this.func_176306_h(worldIn, pos);
        super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);

        if (flag && this.func_176304_i(worldIn, pos)) {
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        }

        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        this.modelBlock.randomDisplayTick(worldIn, pos, state, rand);
    }

    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        this.modelBlock.onBlockClicked(worldIn, pos, playerIn);
    }

    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
        this.modelBlock.onBlockDestroyedByPlayer(worldIn, pos, state);
    }

    public int getMixedBrightnessForBlock(IBlockAccess worldIn, BlockPos pos) {
        return this.modelBlock.getMixedBrightnessForBlock(worldIn, pos);
    }

    public float getExplosionResistance(Entity exploder) {
        return this.modelBlock.getExplosionResistance(exploder);
    }

    public RenderLayer getBlockLayer() {
        return this.modelBlock.getBlockLayer();
    }

    public int tickRate(World worldIn) {
        return this.modelBlock.tickRate(worldIn);
    }

    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        return this.modelBlock.getSelectedBoundingBox(worldIn, pos);
    }

    public Vec3 modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3 motion) {
        return this.modelBlock.modifyAcceleration(worldIn, pos, entityIn, motion);
    }

    public boolean isCollidable() {
        return this.modelBlock.isCollidable();
    }

    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return this.modelBlock.canCollideCheck(state, hitIfLiquid);
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return this.modelBlock.canPlaceBlockAt(worldIn, pos);
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.onNeighborBlockChange(worldIn, pos, this.modelState, Blocks.AIR);
        this.modelBlock.onBlockAdded(worldIn, pos, this.modelState);
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        this.modelBlock.breakBlock(worldIn, pos, this.modelState);
    }

    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn) {
        this.modelBlock.onEntityCollidedWithBlock(worldIn, pos, entityIn);
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        this.modelBlock.updateTick(worldIn, pos, state, rand);
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, Direction side, float hitX, float hitY, float hitZ) {
        return this.modelBlock.onBlockActivated(worldIn, pos, this.modelState, playerIn, Direction.DOWN, 0.0F, 0.0F, 0.0F);
    }

    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
        this.modelBlock.onBlockDestroyedByExplosion(worldIn, pos, explosionIn);
    }

    public MapColor getMapColor(IBlockState state) {
        return this.modelBlock.getMapColor(this.modelState);
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState iblockstate = super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
        iblockstate = iblockstate.withProperty(FACING, placer.getHorizontalFacing()).withProperty(SHAPE, StairsShape.STRAIGHT);
        return facing != Direction.DOWN && (facing == Direction.UP || hitY <= 0.5D) ? iblockstate.withProperty(HALF, EnumHalf.BOTTOM) : iblockstate.withProperty(HALF, EnumHalf.TOP);
    }

    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
        MovingObjectPosition[] amovingobjectposition = new MovingObjectPosition[8];
        IBlockState iblockstate = worldIn.getBlockState(pos);
        int i = iblockstate.getValue(FACING).getHorizontalIndex();
        boolean flag = iblockstate.getValue(HALF) == EnumHalf.TOP;
        int[] aint = field_150150_a[i + (flag ? 4 : 0)];
        this.hasRaytraced = true;

        for (int j = 0; j < 8; ++j) {
            this.rayTracePass = j;

            if (Arrays.binarySearch(aint, j) < 0) {
                amovingobjectposition[j] = super.collisionRayTrace(worldIn, pos, start, end);
            }
        }

        for (int k : aint) {
            amovingobjectposition[k] = null;
        }

        MovingObjectPosition movingobjectposition1 = null;
        double d1 = 0.0D;

        for (MovingObjectPosition movingobjectposition : amovingobjectposition) {
            if (movingobjectposition != null) {
                double d0 = movingobjectposition.hitVec.squareDistanceTo(end);

                if (d0 > d1) {
                    movingobjectposition1 = movingobjectposition;
                    d1 = d0;
                }
            }
        }

        return movingobjectposition1;
    }

    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState().withProperty(HALF, (meta & 4) > 0 ? EnumHalf.TOP : EnumHalf.BOTTOM);
        iblockstate = iblockstate.withProperty(FACING, Direction.getFront(5 - (meta & 3)));
        return iblockstate;
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;

        if (state.getValue(HALF) == EnumHalf.TOP) {
            i |= 4;
        }

        i = i | 5 - state.getValue(FACING).getIndex();
        return i;
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (this.func_176306_h(worldIn, pos)) {
            switch (this.func_176305_g(worldIn, pos)) {
                case 0:
                    state = state.withProperty(SHAPE, StairsShape.STRAIGHT);
                    break;

                case 1:
                    state = state.withProperty(SHAPE, StairsShape.INNER_RIGHT);
                    break;

                case 2:
                    state = state.withProperty(SHAPE, StairsShape.INNER_LEFT);
            }
        } else {
            switch (this.func_176307_f(worldIn, pos)) {
                case 0:
                    state = state.withProperty(SHAPE, StairsShape.STRAIGHT);
                    break;

                case 1:
                    state = state.withProperty(SHAPE, StairsShape.OUTER_RIGHT);
                    break;

                case 2:
                    state = state.withProperty(SHAPE, StairsShape.OUTER_LEFT);
            }
        }

        return state;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, FACING, HALF, SHAPE);
    }

    public enum EnumHalf implements IStringSerializable {
        TOP("top"),
        BOTTOM("bottom");

        private final String name;

        EnumHalf(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }

    public enum StairsShape implements IStringSerializable {
        STRAIGHT("straight"),
        INNER_LEFT("inner_left"),
        INNER_RIGHT("inner_right"),
        OUTER_LEFT("outer_left"),
        OUTER_RIGHT("outer_right");

        private final String name;

        StairsShape(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }
}
