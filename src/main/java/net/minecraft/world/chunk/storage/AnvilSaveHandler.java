package net.minecraft.world.chunk.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;

public class AnvilSaveHandler extends SaveHandler {
    public AnvilSaveHandler(File savesDirectory, String directoryName, boolean storePlayerdata) {
        super(savesDirectory, directoryName, storePlayerdata);
    }

    public IChunkLoader getChunkLoader(WorldProvider provider) {
        File file1 = this.getWorldDirectory();

        if (provider instanceof WorldProviderHell) {
            File file3 = new File(file1, "DIM-1");
            file3.mkdirs();
            return new AnvilChunkLoader(file3);
        } else if (provider instanceof WorldProviderEnd) {
            File file2 = new File(file1, "DIM1");
            file2.mkdirs();
            return new AnvilChunkLoader(file2);
        } else {
            return new AnvilChunkLoader(file1);
        }
    }

    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
        worldInformation.setSaveVersion(19133);
        super.saveWorldInfoWithPlayer(worldInformation, tagCompound);
    }

    public void flush() {
        try {
            ThreadedFileIOBase.getThreadedIOInstance().waitForFinish();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        RegionFileCache.clearRegionFileReferences();
    }
}
