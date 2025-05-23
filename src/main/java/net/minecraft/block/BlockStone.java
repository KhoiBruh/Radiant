package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StatCollector;

import java.util.List;
import java.util.Random;

public class BlockStone extends Block {
    public static final PropertyEnum<StoneType> VARIANT = PropertyEnum.create("variant", StoneType.class);

    public BlockStone() {
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, StoneType.STONE));
        this.setCreativeTab(CreativeTabs.TAB_BLOCK);
    }

    public String getLocalizedName() {
        return StatCollector.translateToLocal(this.getUnlocalizedName() + "." + StoneType.STONE.getUnlocalizedName() + ".name");
    }

    public MapColor getMapColor(IBlockState state) {
        return state.getValue(VARIANT).func_181072_c();
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(VARIANT) == StoneType.STONE ? Item.getItemFromBlock(Blocks.COBBLESTONE) : Item.getItemFromBlock(Blocks.STONE);
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (StoneType blockstone$enumtype : StoneType.values()) {
            list.add(new ItemStack(itemIn, 1, blockstone$enumtype.getMetadata()));
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, StoneType.byMetadata(meta));
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, VARIANT);
    }

    public enum StoneType implements IStringSerializable {
        STONE(0, MapColor.STONE_COLOR, "stone"),
        GRANITE(1, MapColor.DIRT_COLOR, "granite"),
        GRANITE_SMOOTH(2, MapColor.DIRT_COLOR, "smooth_granite", "graniteSmooth"),
        DIORITE(3, MapColor.QUARTZ_COLOR, "diorite"),
        DIORITE_SMOOTH(4, MapColor.QUARTZ_COLOR, "smooth_diorite", "dioriteSmooth"),
        ANDESITE(5, MapColor.STONE_COLOR, "andesite"),
        ANDESITE_SMOOTH(6, MapColor.STONE_COLOR, "smooth_andesite", "andesiteSmooth");

        private static final StoneType[] META_LOOKUP = new StoneType[values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;
        private final MapColor field_181073_l;

        StoneType(int p_i46383_3_, MapColor p_i46383_4_, String p_i46383_5_) {
            this(p_i46383_3_, p_i46383_4_, p_i46383_5_, p_i46383_5_);
        }

        StoneType(int p_i46384_3_, MapColor p_i46384_4_, String p_i46384_5_, String p_i46384_6_) {
            this.meta = p_i46384_3_;
            this.name = p_i46384_5_;
            this.unlocalizedName = p_i46384_6_;
            this.field_181073_l = p_i46384_4_;
        }

        public int getMetadata() {
            return this.meta;
        }

        public MapColor func_181072_c() {
            return this.field_181073_l;
        }

        public String toString() {
            return this.name;
        }

        public static StoneType byMetadata(int meta) {
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
            for (StoneType blockstone$enumtype : values()) {
                META_LOOKUP[blockstone$enumtype.getMetadata()] = blockstone$enumtype;
            }
        }
    }
}
