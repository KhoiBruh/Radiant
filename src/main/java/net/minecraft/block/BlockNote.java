package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.ParticleTypes;
import net.minecraft.world.World;

import java.util.List;

public class BlockNote extends BlockContainer {
    private static final List<String> INSTRUMENTS = List.of("harp", "bd", "snare", "hat", "bassattack");

    public BlockNote() {
        super(Material.WOOD);
        this.setCreativeTab(CreativeTabs.TAB_REDSTONE);
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        boolean flag = worldIn.isBlockPowered(pos);
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityNote tileentitynote) {

            if (tileentitynote.previousRedstoneState != flag) {
                if (flag) {
                    tileentitynote.triggerNote(worldIn, pos);
                }

                tileentitynote.previousRedstoneState = flag;
            }
        }
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, Direction side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityNote tileentitynote) {
                tileentitynote.changePitch();
                tileentitynote.triggerNote(worldIn, pos);
                playerIn.triggerAchievement(StatList.field_181735_S);
            }

        }
        return true;
    }

    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        if (!worldIn.isRemote) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityNote tileEntityNote) {
                tileEntityNote.triggerNote(worldIn, pos);
                playerIn.triggerAchievement(StatList.field_181734_R);
            }
        }
    }

    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityNote();
    }

    private String getInstrument(int id) {
        if (id < 0 || id >= INSTRUMENTS.size()) {
            id = 0;
        }

        return INSTRUMENTS.get(id);
    }

    public boolean onBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam) {
        float f = (float) Math.pow(2.0D, (eventParam - 12) / 12.0D);
        worldIn.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "note." + this.getInstrument(eventID), 3.0F, f);
        worldIn.spawnParticle(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, eventParam / 24.0D, 0.0D, 0.0D);
        return true;
    }

    public int getRenderType() {
        return 3;
    }
}
