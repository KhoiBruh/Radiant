package net.minecraft.tileentity;

import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import java.util.ArrayList;
import java.util.List;

public class TileEntityBanner extends TileEntity {
    private int baseColor;
    private NBTTagList patterns;
    private boolean field_175119_g;
    private List<BannerPattern> patternList;
    private List<DyeColor> colorList;
    private String patternResourceLocation;

    public void setItemValues(ItemStack stack) {
        this.patterns = null;

        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("BlockEntityTag", 10)) {
            NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("BlockEntityTag");

            if (nbttagcompound.hasKey("Patterns")) {
                this.patterns = (NBTTagList) nbttagcompound.getTagList("Patterns", 10).copy();
            }

            if (nbttagcompound.hasKey("Base", 99)) {
                this.baseColor = nbttagcompound.getInteger("Base");
            } else {
                this.baseColor = stack.getMetadata() & 15;
            }
        } else {
            this.baseColor = stack.getMetadata() & 15;
        }

        this.patternList = null;
        this.colorList = null;
        this.patternResourceLocation = "";
        this.field_175119_g = true;
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        setBaseColorAndPatterns(compound, this.baseColor, this.patterns);
    }

    public static void setBaseColorAndPatterns(NBTTagCompound compound, int baseColorIn, NBTTagList patternsIn) {
        compound.setInteger("Base", baseColorIn);

        if (patternsIn != null) {
            compound.setTag("Patterns", patternsIn);
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.baseColor = compound.getInteger("Base");
        this.patterns = compound.getTagList("Patterns", 10);
        this.patternList = null;
        this.colorList = null;
        this.patternResourceLocation = null;
        this.field_175119_g = true;
    }

    public Packet<?> getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.pos, 6, nbttagcompound);
    }

    public int getBaseColor() {
        return this.baseColor;
    }

    public static int getBaseColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag", false);
        return nbttagcompound != null && nbttagcompound.hasKey("Base") ? nbttagcompound.getInteger("Base") : stack.getMetadata();
    }

    public static int getPatterns(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag", false);
        return nbttagcompound != null && nbttagcompound.hasKey("Patterns") ? nbttagcompound.getTagList("Patterns", 10).tagCount() : 0;
    }

    public List<BannerPattern> getPatternList() {
        this.initializeBannerData();
        return this.patternList;
    }

    public NBTTagList getPatterns() {
        return this.patterns;
    }

    public List<DyeColor> getColorList() {
        this.initializeBannerData();
        return this.colorList;
    }

    public String getPatternResourceLocation() {
        this.initializeBannerData();
        return this.patternResourceLocation;
    }

    private void initializeBannerData() {
        if (this.patternList == null || this.colorList == null || this.patternResourceLocation == null) {
            if (!this.field_175119_g) {
                this.patternResourceLocation = "";
            } else {
                this.patternList = new ArrayList<>();
                this.colorList = new ArrayList<>();
                this.patternList.add(BannerPattern.BASE);
                this.colorList.add(DyeColor.byDyeDamage(this.baseColor));
                this.patternResourceLocation = "b" + this.baseColor;

                if (this.patterns != null) {
                    for (int i = 0; i < this.patterns.tagCount(); ++i) {
                        NBTTagCompound nbttagcompound = this.patterns.getCompoundTagAt(i);
                        BannerPattern tileentitybanner$enumbannerpattern = BannerPattern.getPatternByID(nbttagcompound.getString("Pattern"));

                        if (tileentitybanner$enumbannerpattern != null) {
                            this.patternList.add(tileentitybanner$enumbannerpattern);
                            int j = nbttagcompound.getInteger("Color");
                            this.colorList.add(DyeColor.byDyeDamage(j));
                            this.patternResourceLocation = this.patternResourceLocation + tileentitybanner$enumbannerpattern.getPatternID() + j;
                        }
                    }
                }
            }
        }
    }

    public static void removeBannerData(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getSubCompound("BlockEntityTag", false);

        if (nbttagcompound != null && nbttagcompound.hasKey("Patterns", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getTagList("Patterns", 10);

            if (nbttaglist.tagCount() > 0) {
                nbttaglist.removeTag(nbttaglist.tagCount() - 1);

                if (nbttaglist.hasNoTags()) {
                    stack.getTagCompound().removeTag("BlockEntityTag");

                    if (stack.getTagCompound().hasNoTags()) {
                        stack.setTagCompound(null);
                    }
                }
            }
        }
    }

    public enum BannerPattern {
        BASE("base", "b"),
        SQUARE_BOTTOM_LEFT("square_bottom_left", "bl", "   ", "   ", "#  "),
        SQUARE_BOTTOM_RIGHT("square_bottom_right", "br", "   ", "   ", "  #"),
        SQUARE_TOP_LEFT("square_top_left", "tl", "#  ", "   ", "   "),
        SQUARE_TOP_RIGHT("square_top_right", "tr", "  #", "   ", "   "),
        STRIPE_BOTTOM("stripe_bottom", "bs", "   ", "   ", "###"),
        STRIPE_TOP("stripe_top", "ts", "###", "   ", "   "),
        STRIPE_LEFT("stripe_left", "ls", "#  ", "#  ", "#  "),
        STRIPE_RIGHT("stripe_right", "rs", "  #", "  #", "  #"),
        STRIPE_CENTER("stripe_center", "cs", " # ", " # ", " # "),
        STRIPE_MIDDLE("stripe_middle", "ms", "   ", "###", "   "),
        STRIPE_DOWNRIGHT("stripe_downright", "drs", "#  ", " # ", "  #"),
        STRIPE_DOWNLEFT("stripe_downleft", "dls", "  #", " # ", "#  "),
        STRIPE_SMALL("small_stripes", "ss", "# #", "# #", "   "),
        CROSS("cross", "cr", "# #", " # ", "# #"),
        STRAIGHT_CROSS("straight_cross", "sc", " # ", "###", " # "),
        TRIANGLE_BOTTOM("triangle_bottom", "bt", "   ", " # ", "# #"),
        TRIANGLE_TOP("triangle_top", "tt", "# #", " # ", "   "),
        TRIANGLES_BOTTOM("triangles_bottom", "bts", "   ", "# #", " # "),
        TRIANGLES_TOP("triangles_top", "tts", " # ", "# #", "   "),
        DIAGONAL_LEFT("diagonal_left", "ld", "## ", "#  ", "   "),
        DIAGONAL_RIGHT("diagonal_up_right", "rd", "   ", "  #", " ##"),
        DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud", "   ", "#  ", "## "),
        DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud", " ##", "  #", "   "),
        CIRCLE_MIDDLE("circle", "mc", "   ", " # ", "   "),
        RHOMBUS_MIDDLE("rhombus", "mr", " # ", "# #", " # "),
        HALF_VERTICAL("half_vertical", "vh", "## ", "## ", "## "),
        HALF_HORIZONTAL("half_horizontal", "hh", "###", "###", "   "),
        HALF_VERTICAL_MIRROR("half_vertical_right", "vhr", " ##", " ##", " ##"),
        HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb", "   ", "###", "###"),
        BORDER("border", "bo", "###", "# #", "###"),
        CURLY_BORDER("curly_border", "cbo", new ItemStack(Blocks.VINE)),
        CREEPER("creeper", "cre", new ItemStack(Items.SKULL, 1, 4)),
        GRADIENT("gradient", "gra", "# #", " # ", " # "),
        GRADIENT_UP("gradient_up", "gru", " # ", " # ", "# #"),
        BRICKS("bricks", "bri", new ItemStack(Blocks.BRICK_BLOCK)),
        SKULL("skull", "sku", new ItemStack(Items.SKULL, 1, 1)),
        FLOWER("flower", "flo", new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.FlowerType.OXEYE_DAISY.getMeta())),
        MOJANG("mojang", "moj", new ItemStack(Items.GOLDEN_APPLE, 1, 1));

        private final String patternName;
        private final String patternID;
        private final String[] craftingLayers;
        private ItemStack patternCraftingStack;

        BannerPattern(String name, String id) {
            this.craftingLayers = new String[3];
            this.patternName = name;
            this.patternID = id;
        }

        BannerPattern(String name, String id, ItemStack craftingItem) {
            this(name, id);
            this.patternCraftingStack = craftingItem;
        }

        BannerPattern(String name, String id, String craftingTop, String craftingMid, String craftingBot) {
            this(name, id);
            this.craftingLayers[0] = craftingTop;
            this.craftingLayers[1] = craftingMid;
            this.craftingLayers[2] = craftingBot;
        }

        public String getPatternName() {
            return this.patternName;
        }

        public String getPatternID() {
            return this.patternID;
        }

        public String[] getCraftingLayers() {
            return this.craftingLayers;
        }

        public boolean hasValidCrafting() {
            return this.patternCraftingStack != null || this.craftingLayers[0] != null;
        }

        public boolean hasCraftingStack() {
            return this.patternCraftingStack != null;
        }

        public ItemStack getCraftingStack() {
            return this.patternCraftingStack;
        }

        public static BannerPattern getPatternByID(String id) {
            for (BannerPattern tileentitybanner$enumbannerpattern : values()) {
                if (tileentitybanner$enumbannerpattern.patternID.equals(id)) {
                    return tileentitybanner$enumbannerpattern;
                }
            }

            return null;
        }
    }
}
