package net.minecraft.client.multiplayer;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.EntityCategory;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ChunkProviderClient implements IChunkProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Chunk blankChunk;
    private final Long2ObjectOpenHashMap<Chunk> chunkMapping = new Long2ObjectOpenHashMap<>(8192);
    private final List<Chunk> chunkListing = new ArrayList<>();
    private final World worldObj;

    public ChunkProviderClient(World worldIn) {
        this.blankChunk = new EmptyChunk(worldIn, 0, 0);
        this.worldObj = worldIn;
    }

    public boolean chunkExists(int x, int z) {
        return true;
    }

    public void unloadChunk(int x, int z) {
        Chunk chunk = this.provideChunk(x, z);

        if (!chunk.isEmpty()) {
            chunk.onChunkUnload();
        }

        this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(x, z));
        this.chunkListing.remove(chunk);
    }

    public Chunk loadChunk(int chunkX, int chunkZ) {
        Chunk chunk = new Chunk(this.worldObj, chunkX, chunkZ);
        this.chunkMapping.put(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);
        this.chunkListing.add(chunk);
        chunk.setChunkLoaded(true);
        return chunk;
    }

    public Chunk provideChunk(int x, int z) {
        Chunk chunk = this.chunkMapping.get(ChunkCoordIntPair.chunkXZ2Int(x, z));
        return chunk == null ? this.blankChunk : chunk;
    }

    public boolean saveChunks(boolean saveAllChunks, IProgressUpdate progressCallback) {
        return true;
    }

    public void saveExtraData() {
    }

    public boolean unloadQueuedChunks() {
        long i = System.currentTimeMillis();

        for (Chunk chunk : this.chunkListing) {
            chunk.func_150804_b(System.currentTimeMillis() - i > 5L);
        }

        if (System.currentTimeMillis() - i > 100L) {
            LOGGER.info("Warning: Clientside chunk ticking took {} ms", new Object[]{System.currentTimeMillis() - i});
        }

        return false;
    }

    public boolean canSave() {
        return false;
    }

    public void populate(IChunkProvider chunkProvider, int x, int z) {
    }

    public boolean populateChunk(IChunkProvider chunkProvider, Chunk chunkIn, int x, int z) {
        return false;
    }

    public String makeString() {
        return "MultiplayerChunkCache: " + this.chunkMapping.size() + ", " + this.chunkListing.size();
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EntityCategory creatureType, BlockPos pos) {
        return null;
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return null;
    }

    public int getLoadedChunkCount() {
        return this.chunkListing.size();
    }

    public void recreateStructures(Chunk chunkIn, int x, int z) {
    }

    public Chunk provideChunk(BlockPos blockPosIn) {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
