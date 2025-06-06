package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.RenderLayer;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Random;

public class BlockIce extends BlockBreakable {
    public BlockIce() {
        super(Material.ICE, false);
        this.slipperiness = 0.98F;
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.TAB_BLOCK);
    }

    public RenderLayer getBlockLayer() {
        return RenderLayer.TRANSLUCENT;
    }

    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        player.triggerAchievement(StatList.MINE_BLOCK_STAT_ARRAY[Block.getIdFromBlock(this)]);
        player.addExhaustion(0.025F);

        if (this.canSilkHarvest() && EnchantmentHelper.getSilkTouchModifier(player)) {
            ItemStack itemstack = this.createStackedBlock(state);

            if (itemstack != null) {
                spawnAsEntity(worldIn, pos, itemstack);
            }
        } else {
            if (worldIn.provider.doesWaterVaporize()) {
                worldIn.setBlockToAir(pos);
                return;
            }

            int i = EnchantmentHelper.getFortuneModifier(player);
            this.dropBlockAsItem(worldIn, pos, state, i);
            Material material = worldIn.getBlockState(pos.down()).getBlock().getMaterial();

            if (material.blocksMovement() || material.isLiquid()) {
                worldIn.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
            }
        }
    }

    public int quantityDropped(Random random) {
        return 0;
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.getLightFor(LightType.BLOCK, pos) > 11 - this.getLightOpacity()) {
            if (worldIn.provider.doesWaterVaporize()) {
                worldIn.setBlockToAir(pos);
            } else {
                this.dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
                worldIn.setBlockState(pos, Blocks.WATER.getDefaultState());
            }
        }
    }

    public int getMobilityFlag() {
        return 0;
    }
}
