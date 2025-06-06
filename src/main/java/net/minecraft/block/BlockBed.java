package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Random;

public class BlockBed extends BlockDirectional {
    public static final PropertyEnum<EnumPartType> PART = PropertyEnum.create("part", EnumPartType.class);
    public static final PropertyBool OCCUPIED = PropertyBool.create("occupied");

    public BlockBed() {
        super(Material.CLOTH);
        this.setDefaultState(this.blockState.getBaseState().withProperty(PART, EnumPartType.FOOT).withProperty(OCCUPIED, Boolean.FALSE));
        this.setBedBounds();
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, Direction side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            if (state.getValue(PART) != EnumPartType.HEAD) {
                pos = pos.offset(state.getValue(FACING));
                state = worldIn.getBlockState(pos);

                if (state.getBlock() != this) {
                    return true;
                }
            }

            if (worldIn.provider.canRespawnHere() && worldIn.getBiomeGenForCoords(pos) != BiomeGenBase.HELL) {
                if (state.getValue(OCCUPIED)) {
                    EntityPlayer entityplayer = this.getPlayerInBed(worldIn, pos);

                    if (entityplayer != null) {
                        playerIn.addChatComponentMessage(new ChatComponentTranslation("tile.bed.occupied"));
                        return true;
                    }

                    state = state.withProperty(OCCUPIED, Boolean.FALSE);
                    worldIn.setBlockState(pos, state, 4);
                }

                EntityPlayer.EnumStatus entityplayer$enumstatus = playerIn.trySleep(pos);

                if (entityplayer$enumstatus == EntityPlayer.EnumStatus.OK) {
                    state = state.withProperty(OCCUPIED, Boolean.TRUE);
                    worldIn.setBlockState(pos, state, 4);
                } else {
                    if (entityplayer$enumstatus == EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW) {
                        playerIn.addChatComponentMessage(new ChatComponentTranslation("tile.bed.noSleep"));
                    } else if (entityplayer$enumstatus == EntityPlayer.EnumStatus.NOT_SAFE) {
                        playerIn.addChatComponentMessage(new ChatComponentTranslation("tile.bed.notSafe"));
                    }

                }
                return true;
            } else {
                worldIn.setBlockToAir(pos);
                BlockPos blockpos = pos.offset(state.getValue(FACING).getOpposite());

                if (worldIn.getBlockState(blockpos).getBlock() == this) {
                    worldIn.setBlockToAir(blockpos);
                }

                worldIn.newExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, true);
                return true;
            }
        }
    }

    private EntityPlayer getPlayerInBed(World worldIn, BlockPos pos) {
        for (EntityPlayer entityplayer : worldIn.playerEntities) {
            if (entityplayer.isPlayerSleeping() && entityplayer.playerLocation.equals(pos)) {
                return entityplayer;
            }
        }

        return null;
    }

    public boolean isFullCube() {
        return false;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        this.setBedBounds();
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        Direction enumfacing = state.getValue(FACING);

        if (state.getValue(PART) == EnumPartType.HEAD) {
            if (worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getBlock() != this) {
                worldIn.setBlockToAir(pos);
            }
        } else if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() != this) {
            worldIn.setBlockToAir(pos);

            if (!worldIn.isRemote) {
                this.dropBlockAsItem(worldIn, pos, state, 0);
            }
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(PART) == EnumPartType.HEAD ? null : Items.BED;
    }

    private void setBedBounds() {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5625F, 1.0F);
    }

    public static BlockPos getSafeExitLocation(World worldIn, BlockPos pos, int tries) {
        Direction enumfacing = worldIn.getBlockState(pos).getValue(FACING);
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        for (int l = 0; l <= 1; ++l) {
            int i1 = i - enumfacing.getFrontOffsetX() * l - 1;
            int j1 = k - enumfacing.getFrontOffsetZ() * l - 1;
            int k1 = i1 + 2;
            int l1 = j1 + 2;

            for (int i2 = i1; i2 <= k1; ++i2) {
                for (int j2 = j1; j2 <= l1; ++j2) {
                    BlockPos blockpos = new BlockPos(i2, j, j2);

                    if (hasRoomForPlayer(worldIn, blockpos)) {
                        if (tries <= 0) {
                            return blockpos;
                        }

                        --tries;
                    }
                }
            }
        }

        return null;
    }

    protected static boolean hasRoomForPlayer(World worldIn, BlockPos pos) {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && !worldIn.getBlockState(pos).getBlock().getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getBlock().getMaterial().isSolid();
    }

    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (state.getValue(PART) == EnumPartType.FOOT) {
            super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
        }
    }

    public int getMobilityFlag() {
        return 1;
    }

    public RenderLayer getBlockLayer() {
        return RenderLayer.CUTOUT;
    }

    public Item getItem(World worldIn, BlockPos pos) {
        return Items.BED;
    }

    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (player.capabilities.isCreativeMode && state.getValue(PART) == EnumPartType.HEAD) {
            BlockPos blockpos = pos.offset(state.getValue(FACING).getOpposite());

            if (worldIn.getBlockState(blockpos).getBlock() == this) {
                worldIn.setBlockToAir(blockpos);
            }
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        Direction enumfacing = Direction.getHorizontal(meta);
        return (meta & 8) > 0 ? this.getDefaultState().withProperty(PART, EnumPartType.HEAD).withProperty(FACING, enumfacing).withProperty(OCCUPIED, (meta & 4) > 0) : this.getDefaultState().withProperty(PART, EnumPartType.FOOT).withProperty(FACING, enumfacing);
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getValue(PART) == EnumPartType.FOOT) {
            IBlockState iblockstate = worldIn.getBlockState(pos.offset(state.getValue(FACING)));

            if (iblockstate.getBlock() == this) {
                state = state.withProperty(OCCUPIED, iblockstate.getValue(OCCUPIED));
            }
        }

        return state;
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();

        if (state.getValue(PART) == EnumPartType.HEAD) {
            i |= 8;

            if (state.getValue(OCCUPIED)) {
                i |= 4;
            }
        }

        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, FACING, PART, OCCUPIED);
    }

    public enum EnumPartType implements IStringSerializable {
        HEAD("head"),
        FOOT("foot");

        private final String name;

        EnumPartType(String name) {
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
