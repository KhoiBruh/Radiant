package net.minecraft.item.crafting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FurnaceRecipes {
    private static final FurnaceRecipes SMELTING_BASE = new FurnaceRecipes();
    private final Map<ItemStack, ItemStack> smeltingList = new HashMap<>();
    private final Map<ItemStack, Float> experienceList = new HashMap<>();

    public static FurnaceRecipes instance() {
        return SMELTING_BASE;
    }

    private FurnaceRecipes() {
        this.addSmeltingRecipeForBlock(Blocks.IRON_ORE, new ItemStack(Items.IRON_INGOT), 0.7F);
        this.addSmeltingRecipeForBlock(Blocks.GOLD_ORE, new ItemStack(Items.GOLD_INGOT), 1.0F);
        this.addSmeltingRecipeForBlock(Blocks.DIAMOND_ORE, new ItemStack(Items.DIAMOND), 1.0F);
        this.addSmeltingRecipeForBlock(Blocks.SAND, new ItemStack(Blocks.GLASS), 0.1F);
        this.addSmelting(Items.PORKCHOP, new ItemStack(Items.COOKED_PORKCHOP), 0.35F);
        this.addSmelting(Items.BEEF, new ItemStack(Items.COOKED_BEEF), 0.35F);
        this.addSmelting(Items.CHICKEN, new ItemStack(Items.COOKED_CHICKEN), 0.35F);
        this.addSmelting(Items.RABBIT, new ItemStack(Items.COOKED_RABBIT), 0.35F);
        this.addSmelting(Items.MUTTON, new ItemStack(Items.COOKED_MUTTON), 0.35F);
        this.addSmeltingRecipeForBlock(Blocks.COBBLESTONE, new ItemStack(Blocks.STONE), 0.1F);
        this.addSmeltingRecipe(new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.DEFAULT_META), new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.CRACKED_META), 0.1F);
        this.addSmelting(Items.CLAY_BALL, new ItemStack(Items.BRICK), 0.3F);
        this.addSmeltingRecipeForBlock(Blocks.CLAY, new ItemStack(Blocks.HARDENED_CLAY), 0.35F);
        this.addSmeltingRecipeForBlock(Blocks.CACTUS, new ItemStack(Items.DYE, 1, DyeColor.GREEN.getDyeDamage()), 0.2F);
        this.addSmeltingRecipeForBlock(Blocks.LOG, new ItemStack(Items.COAL, 1, 1), 0.15F);
        this.addSmeltingRecipeForBlock(Blocks.LOG_2, new ItemStack(Items.COAL, 1, 1), 0.15F);
        this.addSmeltingRecipeForBlock(Blocks.EMERALD_ORE, new ItemStack(Items.EMERALD), 1.0F);
        this.addSmelting(Items.POTATO, new ItemStack(Items.BAKED_POTATO), 0.35F);
        this.addSmeltingRecipeForBlock(Blocks.NETHERRACK, new ItemStack(Items.NETHERBRICK), 0.1F);
        this.addSmeltingRecipe(new ItemStack(Blocks.SPONGE, 1, 1), new ItemStack(Blocks.SPONGE, 1, 0), 0.15F);

        for (ItemFishFood.FishType itemfishfood$fishtype : ItemFishFood.FishType.values()) {
            if (itemfishfood$fishtype.canCook()) {
                this.addSmeltingRecipe(new ItemStack(Items.FISH, 1, itemfishfood$fishtype.getMetadata()), new ItemStack(Items.COOKED_FISH, 1, itemfishfood$fishtype.getMetadata()), 0.35F);
            }
        }

        this.addSmeltingRecipeForBlock(Blocks.COAL_ORE, new ItemStack(Items.COAL), 0.1F);
        this.addSmeltingRecipeForBlock(Blocks.REDSTONE_ORE, new ItemStack(Items.REDSTONE), 0.7F);
        this.addSmeltingRecipeForBlock(Blocks.LAPIS_ORE, new ItemStack(Items.DYE, 1, DyeColor.BLUE.getDyeDamage()), 0.2F);
        this.addSmeltingRecipeForBlock(Blocks.QUARTZ_ORE, new ItemStack(Items.QUARTZ), 0.2F);
    }

    public void addSmeltingRecipeForBlock(Block input, ItemStack stack, float experience) {
        this.addSmelting(Item.getItemFromBlock(input), stack, experience);
    }

    public void addSmelting(Item input, ItemStack stack, float experience) {
        this.addSmeltingRecipe(new ItemStack(input, 1, 32767), stack, experience);
    }

    public void addSmeltingRecipe(ItemStack input, ItemStack stack, float experience) {
        this.smeltingList.put(input, stack);
        this.experienceList.put(stack, experience);
    }

    public ItemStack getSmeltingResult(ItemStack stack) {
        for (Entry<ItemStack, ItemStack> entry : this.smeltingList.entrySet()) {
            if (this.compareItemStacks(stack, entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    private boolean compareItemStacks(ItemStack stack1, ItemStack stack2) {
        return stack2.getItem() == stack1.getItem() && (stack2.getMetadata() == 32767 || stack2.getMetadata() == stack1.getMetadata());
    }

    public Map<ItemStack, ItemStack> getSmeltingList() {
        return this.smeltingList;
    }

    public float getSmeltingExperience(ItemStack stack) {
        for (Entry<ItemStack, Float> entry : this.experienceList.entrySet()) {
            if (this.compareItemStacks(stack, entry.getKey())) {
                return entry.getValue();
            }
        }

        return 0.0F;
    }
}
