package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;

public class BlockRedstoneWire extends Block {
    public static final PropertyEnum<EnumAttachPosition> NORTH = PropertyEnum.create("north", EnumAttachPosition.class);
    public static final PropertyEnum<EnumAttachPosition> EAST = PropertyEnum.create("east", EnumAttachPosition.class);
    public static final PropertyEnum<EnumAttachPosition> SOUTH = PropertyEnum.create("south", EnumAttachPosition.class);
    public static final PropertyEnum<EnumAttachPosition> WEST = PropertyEnum.create("west", EnumAttachPosition.class);
    public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
    private boolean canProvidePower = true;
    private final Set<BlockPos> blocksNeedingUpdate = new HashSet<>();

    public BlockRedstoneWire() {
        super(Material.CIRCUITS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, EnumAttachPosition.NONE).withProperty(EAST, EnumAttachPosition.NONE).withProperty(SOUTH, EnumAttachPosition.NONE).withProperty(WEST, EnumAttachPosition.NONE).withProperty(POWER, 0));
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = state.withProperty(WEST, this.getAttachPosition(worldIn, pos, Direction.WEST));
        state = state.withProperty(EAST, this.getAttachPosition(worldIn, pos, Direction.EAST));
        state = state.withProperty(NORTH, this.getAttachPosition(worldIn, pos, Direction.NORTH));
        state = state.withProperty(SOUTH, this.getAttachPosition(worldIn, pos, Direction.SOUTH));
        return state;
    }

    private EnumAttachPosition getAttachPosition(IBlockAccess worldIn, BlockPos pos, Direction direction) {
        BlockPos blockpos = pos.offset(direction);
        Block block = worldIn.getBlockState(pos.offset(direction)).getBlock();

        if (!canConnectTo(worldIn.getBlockState(blockpos), direction) && (block.isBlockNormalCube() || !canConnectUpwardsTo(worldIn.getBlockState(blockpos.down())))) {
            Block block1 = worldIn.getBlockState(pos.up()).getBlock();
            return !block1.isBlockNormalCube() && block.isBlockNormalCube() && canConnectUpwardsTo(worldIn.getBlockState(blockpos.up())) ? EnumAttachPosition.UP : EnumAttachPosition.NONE;
        } else {
            return EnumAttachPosition.SIDE;
        }
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return null;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        return iblockstate.getBlock() != this ? super.colorMultiplier(worldIn, pos, renderPass) : this.colorMultiplier(iblockstate.getValue(POWER));
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) || worldIn.getBlockState(pos.down()).getBlock() == Blocks.GLOWSTONE;
    }

    private IBlockState updateSurroundingRedstone(World worldIn, BlockPos pos, IBlockState state) {
        state = this.calculateCurrentChanges(worldIn, pos, pos, state);
        List<BlockPos> list = new ArrayList<>(this.blocksNeedingUpdate);
        this.blocksNeedingUpdate.clear();

        for (BlockPos blockpos : list) {
            worldIn.notifyNeighborsOfStateChange(blockpos, this);
        }

        return state;
    }

    private IBlockState calculateCurrentChanges(World worldIn, BlockPos pos1, BlockPos pos2, IBlockState state) {
        IBlockState iblockstate = state;
        int i = state.getValue(POWER);
        int j = 0;
        j = this.getMaxCurrentStrength(worldIn, pos2, j);
        this.canProvidePower = false;
        int k = worldIn.isBlockIndirectlyGettingPowered(pos1);
        this.canProvidePower = true;

        if (k > 0 && k > j - 1) {
            j = k;
        }

        int l = 0;

        for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos1.offset(enumfacing);
            boolean flag = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();

            if (flag) {
                l = this.getMaxCurrentStrength(worldIn, blockpos, l);
            }

            if (worldIn.getBlockState(blockpos).getBlock().isNormalCube() && !worldIn.getBlockState(pos1.up()).getBlock().isNormalCube()) {
                if (flag && pos1.getY() >= pos2.getY()) {
                    l = this.getMaxCurrentStrength(worldIn, blockpos.up(), l);
                }
            } else if (!worldIn.getBlockState(blockpos).getBlock().isNormalCube() && flag && pos1.getY() <= pos2.getY()) {
                l = this.getMaxCurrentStrength(worldIn, blockpos.down(), l);
            }
        }

        if (l > j) {
            j = l - 1;
        } else if (j > 0) {
            --j;
        } else {
            j = 0;
        }

        if (k > j - 1) {
            j = k;
        }

        if (i != j) {
            state = state.withProperty(POWER, j);

            if (worldIn.getBlockState(pos1) == iblockstate) {
                worldIn.setBlockState(pos1, state, 2);
            }

            this.blocksNeedingUpdate.add(pos1);

            for (Direction enumfacing1 : Direction.values()) {
                this.blocksNeedingUpdate.add(pos1.offset(enumfacing1));
            }
        }

        return state;
    }

    private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {
        if (worldIn.getBlockState(pos).getBlock() == this) {
            worldIn.notifyNeighborsOfStateChange(pos, this);

            for (Direction enumfacing : Direction.values()) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
            }
        }
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            this.updateSurroundingRedstone(worldIn, pos, state);

            for (Direction enumfacing : Direction.Plane.VERTICAL) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
            }

            for (Direction enumfacing1 : Direction.Plane.HORIZONTAL) {
                this.notifyWireNeighborsOfStateChange(worldIn, pos.offset(enumfacing1));
            }

            for (Direction enumfacing2 : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pos.offset(enumfacing2);

                if (worldIn.getBlockState(blockpos).getBlock().isNormalCube()) {
                    this.notifyWireNeighborsOfStateChange(worldIn, blockpos.up());
                } else {
                    this.notifyWireNeighborsOfStateChange(worldIn, blockpos.down());
                }
            }
        }
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        if (!worldIn.isRemote) {
            for (Direction enumfacing : Direction.values()) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
            }

            this.updateSurroundingRedstone(worldIn, pos, state);

            for (Direction enumfacing1 : Direction.Plane.HORIZONTAL) {
                this.notifyWireNeighborsOfStateChange(worldIn, pos.offset(enumfacing1));
            }

            for (Direction enumfacing2 : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pos.offset(enumfacing2);

                if (worldIn.getBlockState(blockpos).getBlock().isNormalCube()) {
                    this.notifyWireNeighborsOfStateChange(worldIn, blockpos.up());
                } else {
                    this.notifyWireNeighborsOfStateChange(worldIn, blockpos.down());
                }
            }
        }
    }

    private int getMaxCurrentStrength(World worldIn, BlockPos pos, int strength) {
        if (worldIn.getBlockState(pos).getBlock() != this) {
            return strength;
        } else {
            int i = worldIn.getBlockState(pos).getValue(POWER);
            return i > strength ? i : strength;
        }
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (!worldIn.isRemote) {
            if (this.canPlaceBlockAt(worldIn, pos)) {
                this.updateSurroundingRedstone(worldIn, pos, state);
            } else {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.REDSTONE;
    }

    public int getStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, Direction side) {
        return !this.canProvidePower ? 0 : this.getWeakPower(worldIn, pos, state, side);
    }

    public int getWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, Direction side) {
        if (!this.canProvidePower) {
            return 0;
        } else {
            int i = state.getValue(POWER);

            if (i == 0) {
                return 0;
            } else if (side == Direction.UP) {
                return i;
            } else {
                EnumSet<Direction> enumset = EnumSet.noneOf(Direction.class);

                for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
                    if (this.func_176339_d(worldIn, pos, enumfacing)) {
                        enumset.add(enumfacing);
                    }
                }

                if (side.getAxis().isHorizontal() && enumset.isEmpty()) {
                    return i;
                } else if (enumset.contains(side) && !enumset.contains(side.rotateYCCW()) && !enumset.contains(side.rotateY())) {
                    return i;
                } else {
                    return 0;
                }
            }
        }
    }

    private boolean func_176339_d(IBlockAccess worldIn, BlockPos pos, Direction side) {
        BlockPos blockpos = pos.offset(side);
        IBlockState iblockstate = worldIn.getBlockState(blockpos);
        Block block = iblockstate.getBlock();
        boolean flag = block.isNormalCube();
        boolean flag1 = worldIn.getBlockState(pos.up()).getBlock().isNormalCube();
        return !flag1 && flag && canConnectUpwardsTo(worldIn, blockpos.up()) || (canConnectTo(iblockstate, side) || (block == Blocks.POWERED_REPEATER && iblockstate.getValue(BlockRedstoneDiode.FACING) == side || !flag && canConnectUpwardsTo(worldIn, blockpos.down())));
    }

    protected static boolean canConnectUpwardsTo(IBlockAccess worldIn, BlockPos pos) {
        return canConnectUpwardsTo(worldIn.getBlockState(pos));
    }

    protected static boolean canConnectUpwardsTo(IBlockState state) {
        return canConnectTo(state, null);
    }

    protected static boolean canConnectTo(IBlockState blockState, Direction side) {
        Block block = blockState.getBlock();

        if (block == Blocks.REDSTONE_WIRE) {
            return true;
        } else if (Blocks.UNPOWERED_REPEATER.isAssociated(block)) {
            Direction enumfacing = blockState.getValue(BlockRedstoneRepeater.FACING);
            return enumfacing == side || enumfacing.getOpposite() == side;
        } else {
            return block.canProvidePower() && side != null;
        }
    }

    public boolean canProvidePower() {
        return this.canProvidePower;
    }

    private int colorMultiplier(int powerLevel) {
        float f = powerLevel / 15.0F;
        float f1 = f * 0.6F + 0.4F;

        if (powerLevel == 0) {
            f1 = 0.3F;
        }

        float f2 = f * f * 0.7F - 0.5F;
        float f3 = f * f * 0.6F - 0.7F;

        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        if (f3 < 0.0F) {
            f3 = 0.0F;
        }

        int i = MathHelper.clamp((int) (f1 * 255.0F), 0, 255);
        int j = MathHelper.clamp((int) (f2 * 255.0F), 0, 255);
        int k = MathHelper.clamp((int) (f3 * 255.0F), 0, 255);
        return -16777216 | i << 16 | j << 8 | k;
    }

    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        int i = state.getValue(POWER);

        if (i != 0) {
            double d0 = pos.getX() + 0.5D + (rand.nextFloat() - 0.5D) * 0.2D;
            double d1 = (pos.getY() + 0.0625F);
            double d2 = pos.getZ() + 0.5D + (rand.nextFloat() - 0.5D) * 0.2D;
            float f = i / 15.0F;
            float f1 = f * 0.6F + 0.4F;
            float f2 = Math.max(0.0F, f * f * 0.7F - 0.5F);
            float f3 = Math.max(0.0F, f * f * 0.6F - 0.7F);
            worldIn.spawnParticle(ParticleTypes.REDSTONE, d0, d1, d2, f1, f2, f3);
        }
    }

    public Item getItem(World worldIn, BlockPos pos) {
        return Items.REDSTONE;
    }

    public RenderLayer getBlockLayer() {
        return RenderLayer.CUTOUT;
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(POWER, meta);
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(POWER);
    }

    protected BlockState createBlockState() {
        return new BlockState(this, NORTH, EAST, SOUTH, WEST, POWER);
    }

    enum EnumAttachPosition implements IStringSerializable {
        UP("up"),
        SIDE("side"),
        NONE("none");

        private final String name;

        EnumAttachPosition(String name) {
            this.name = name;
        }

        public String toString() {
            return this.getName();
        }

        public String getName() {
            return this.name;
        }
    }
}
