package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;

import java.util.List;
import java.util.Random;

public class BlockDoublePlant extends BlockBush implements IGrowable {
    public static final PropertyEnum<EnumPlantType> VARIANT = PropertyEnum.create("variant", EnumPlantType.class);
    public static final PropertyEnum<EnumBlockHalf> HALF = PropertyEnum.create("half", EnumBlockHalf.class);
    public static final PropertyEnum<Direction> FACING = BlockDirectional.FACING;

    public BlockDoublePlant() {
        super(Material.VINE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumPlantType.SUNFLOWER).withProperty(HALF, EnumBlockHalf.LOWER).withProperty(FACING, Direction.NORTH));
        this.setHardness(0.0F);
        this.setStepSound(soundTypeGrass);
        this.setUnlocalizedName("doublePlant");
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public EnumPlantType getVariant(IBlockAccess worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == this) {
            iblockstate = this.getActualState(iblockstate, worldIn, pos);
            return iblockstate.getValue(VARIANT);
        } else {
            return EnumPlantType.FERN;
        }
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
    }

    public boolean isReplaceable(World worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() != this) {
            return true;
        } else {
            EnumPlantType blockdoubleplant$enumplanttype = this.getActualState(iblockstate, worldIn, pos).getValue(VARIANT);
            return blockdoubleplant$enumplanttype == EnumPlantType.FERN || blockdoubleplant$enumplanttype == EnumPlantType.GRASS;
        }
    }

    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            boolean flag = state.getValue(HALF) == EnumBlockHalf.UPPER;
            BlockPos blockpos = flag ? pos : pos.up();
            BlockPos blockpos1 = flag ? pos.down() : pos;
            Block block = flag ? this : worldIn.getBlockState(blockpos).getBlock();
            Block block1 = flag ? worldIn.getBlockState(blockpos1).getBlock() : this;

            if (block == this) {
                worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 2);
            }

            if (block1 == this) {
                worldIn.setBlockState(blockpos1, Blocks.AIR.getDefaultState(), 3);

                if (!flag) {
                    this.dropBlockAsItem(worldIn, blockpos1, state, 0);
                }
            }
        }
    }

    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(HALF) == EnumBlockHalf.UPPER) {
            return worldIn.getBlockState(pos.down()).getBlock() == this;
        } else {
            IBlockState iblockstate = worldIn.getBlockState(pos.up());
            return iblockstate.getBlock() == this && super.canBlockStay(worldIn, pos, iblockstate);
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (state.getValue(HALF) == EnumBlockHalf.UPPER) {
            return null;
        } else {
            EnumPlantType blockdoubleplant$enumplanttype = state.getValue(VARIANT);
            return blockdoubleplant$enumplanttype == EnumPlantType.FERN ? null : (blockdoubleplant$enumplanttype == EnumPlantType.GRASS ? (rand.nextInt(8) == 0 ? Items.WHEAT_SEEDS : null) : Item.getItemFromBlock(this));
        }
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(HALF) != EnumBlockHalf.UPPER && state.getValue(VARIANT) != EnumPlantType.GRASS ? state.getValue(VARIANT).getMeta() : 0;
    }

    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
        EnumPlantType blockdoubleplant$enumplanttype = this.getVariant(worldIn, pos);
        return blockdoubleplant$enumplanttype != EnumPlantType.GRASS && blockdoubleplant$enumplanttype != EnumPlantType.FERN ? 16777215 : BiomeColorHelper.getGrassColorAtPos(worldIn, pos);
    }

    public void placeAt(World worldIn, BlockPos lowerPos, EnumPlantType variant, int flags) {
        worldIn.setBlockState(lowerPos, this.getDefaultState().withProperty(HALF, EnumBlockHalf.LOWER).withProperty(VARIANT, variant), flags);
        worldIn.setBlockState(lowerPos.up(), this.getDefaultState().withProperty(HALF, EnumBlockHalf.UPPER), flags);
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos.up(), this.getDefaultState().withProperty(HALF, EnumBlockHalf.UPPER), 2);
    }

    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        if (worldIn.isRemote || player.getCurrentEquippedItem() == null || player.getCurrentEquippedItem().getItem() != Items.SHEARS || state.getValue(HALF) != EnumBlockHalf.LOWER || !this.onHarvest(worldIn, pos, state, player)) {
            super.harvestBlock(worldIn, player, pos, state, te);
        }
    }

    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (state.getValue(HALF) == EnumBlockHalf.UPPER) {
            if (worldIn.getBlockState(pos.down()).getBlock() == this) {
                if (!player.capabilities.isCreativeMode) {
                    IBlockState iblockstate = worldIn.getBlockState(pos.down());
                    EnumPlantType blockdoubleplant$enumplanttype = iblockstate.getValue(VARIANT);

                    if (blockdoubleplant$enumplanttype != EnumPlantType.FERN && blockdoubleplant$enumplanttype != EnumPlantType.GRASS) {
                        worldIn.destroyBlock(pos.down(), true);
                    } else if (!worldIn.isRemote) {
                        if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.SHEARS) {
                            this.onHarvest(worldIn, pos, iblockstate, player);
                            worldIn.setBlockToAir(pos.down());
                        } else {
                            worldIn.destroyBlock(pos.down(), true);
                        }
                    } else {
                        worldIn.setBlockToAir(pos.down());
                    }
                } else {
                    worldIn.setBlockToAir(pos.down());
                }
            }
        } else if (player.capabilities.isCreativeMode && worldIn.getBlockState(pos.up()).getBlock() == this) {
            worldIn.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 2);
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    private boolean onHarvest(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        EnumPlantType blockdoubleplant$enumplanttype = state.getValue(VARIANT);

        if (blockdoubleplant$enumplanttype != EnumPlantType.FERN && blockdoubleplant$enumplanttype != EnumPlantType.GRASS) {
            return false;
        } else {
            player.triggerAchievement(StatList.MINE_BLOCK_STAT_ARRAY[Block.getIdFromBlock(this)]);
            int i = (blockdoubleplant$enumplanttype == EnumPlantType.GRASS ? BlockTallGrass.EnumType.GRASS : BlockTallGrass.EnumType.FERN).getMeta();
            spawnAsEntity(worldIn, pos, new ItemStack(Blocks.TALL_GRASS, 2, i));
            return true;
        }
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (EnumPlantType blockdoubleplant$enumplanttype : EnumPlantType.values()) {
            list.add(new ItemStack(itemIn, 1, blockdoubleplant$enumplanttype.getMeta()));
        }
    }

    public int getDamageValue(World worldIn, BlockPos pos) {
        return this.getVariant(worldIn, pos).getMeta();
    }

    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        EnumPlantType blockdoubleplant$enumplanttype = this.getVariant(worldIn, pos);
        return blockdoubleplant$enumplanttype != EnumPlantType.GRASS && blockdoubleplant$enumplanttype != EnumPlantType.FERN;
    }

    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        spawnAsEntity(worldIn, pos, new ItemStack(this, 1, this.getVariant(worldIn, pos).getMeta()));
    }

    public IBlockState getStateFromMeta(int meta) {
        return (meta & 8) > 0 ? this.getDefaultState().withProperty(HALF, EnumBlockHalf.UPPER) : this.getDefaultState().withProperty(HALF, EnumBlockHalf.LOWER).withProperty(VARIANT, EnumPlantType.byMetadata(meta & 7));
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getValue(HALF) == EnumBlockHalf.UPPER) {
            IBlockState iblockstate = worldIn.getBlockState(pos.down());

            if (iblockstate.getBlock() == this) {
                state = state.withProperty(VARIANT, iblockstate.getValue(VARIANT));
            }
        }

        return state;
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(HALF) == EnumBlockHalf.UPPER ? 8 | state.getValue(FACING).getHorizontalIndex() : state.getValue(VARIANT).getMeta();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, HALF, VARIANT, FACING);
    }

    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    public enum EnumBlockHalf implements IStringSerializable {
        UPPER,
        LOWER;

        public String toString() {
            return this.getName();
        }

        public String getName() {
            return this == UPPER ? "upper" : "lower";
        }
    }

    public enum EnumPlantType implements IStringSerializable {
        SUNFLOWER(0, "sunflower"),
        SYRINGA(1, "syringa"),
        GRASS(2, "double_grass", "grass"),
        FERN(3, "double_fern", "fern"),
        ROSE(4, "double_rose", "rose"),
        PAEONIA(5, "paeonia");

        private static final EnumPlantType[] META_LOOKUP = new EnumPlantType[values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;

        EnumPlantType(int meta, String name) {
            this(meta, name, name);
        }

        EnumPlantType(int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMeta() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public static EnumPlantType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName() {
            return this.name;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        static {
            for (EnumPlantType blockdoubleplant$enumplanttype : values()) {
                META_LOOKUP[blockdoubleplant$enumplanttype.getMeta()] = blockdoubleplant$enumplanttype;
            }
        }
    }
}
