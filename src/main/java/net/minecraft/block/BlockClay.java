package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.Random;

public class BlockClay extends Block {
    public BlockClay() {
        super(Material.CLAY);
        this.setCreativeTab(CreativeTabs.TAB_BLOCK);
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.CLAY_BALL;
    }

    public int quantityDropped(Random random) {
        return 4;
    }
}
