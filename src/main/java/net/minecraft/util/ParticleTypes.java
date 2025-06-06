package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;

public enum ParticleTypes {
    EXPLOSION_NORMAL("explode", 0, true),
    EXPLOSION_LARGE("largeexplode", 1, true),
    EXPLOSION_HUGE("hugeexplosion", 2, true),
    FIREWORKS_SPARK("fireworksSpark", 3, false),
    WATER_BUBBLE("bubble", 4, false),
    WATER_SPLASH("splash", 5, false),
    WATER_WAKE("wake", 6, false),
    SUSPENDED("suspended", 7, false),
    SUSPENDED_DEPTH("depthsuspend", 8, false),
    CRIT("crit", 9, false),
    CRIT_MAGIC("magicCrit", 10, false),
    SMOKE_NORMAL("smoke", 11, false),
    SMOKE_LARGE("largesmoke", 12, false),
    SPELL("spell", 13, false),
    SPELL_INSTANT("instantSpell", 14, false),
    SPELL_MOB("mobSpell", 15, false),
    SPELL_MOB_AMBIENT("mobSpellAmbient", 16, false),
    SPELL_WITCH("witchMagic", 17, false),
    DRIP_WATER("dripWater", 18, false),
    DRIP_LAVA("dripLava", 19, false),
    VILLAGER_ANGRY("angryVillager", 20, false),
    VILLAGER_HAPPY("happyVillager", 21, false),
    TOWN_AURA("townaura", 22, false),
    NOTE("note", 23, false),
    PORTAL("portal", 24, false),
    ENCHANTMENT_TABLE("enchantmenttable", 25, false),
    FLAME("flame", 26, false),
    LAVA("lava", 27, false),
    FOOTSTEP("footstep", 28, false),
    CLOUD("cloud", 29, false),
    REDSTONE("reddust", 30, false),
    SNOWBALL("snowballpoof", 31, false),
    SNOW_SHOVEL("snowshovel", 32, false),
    SLIME("slime", 33, false),
    HEART("heart", 34, false),
    BARRIER("barrier", 35, false),
    ITEM_CRACK("iconcrack_", 36, false, 2),
    BLOCK_CRACK("blockcrack_", 37, false, 1),
    BLOCK_DUST("blockdust_", 38, false, 1),
    WATER_DROP("droplet", 39, false),
    ITEM_TAKE("take", 40, false),
    MOB_APPEARANCE("mobappearance", 41, true);

    private final String particleName;
    private final int particleID;
    private final boolean shouldIgnoreRange;
    private final int argumentCount;
    private static final Int2ObjectOpenHashMap<ParticleTypes> PARTICLES = new Int2ObjectOpenHashMap<>();
    private static final String[] PARTICLE_NAMES;

    ParticleTypes(String particleNameIn, int particleIDIn, boolean p_i46011_5_, int argumentCountIn) {
        this.particleName = particleNameIn;
        this.particleID = particleIDIn;
        this.shouldIgnoreRange = p_i46011_5_;
        this.argumentCount = argumentCountIn;
    }

    ParticleTypes(String particleNameIn, int particleIDIn, boolean p_i46012_5_) {
        this(particleNameIn, particleIDIn, p_i46012_5_, 0);
    }

    public static String[] getParticleNames() {
        return PARTICLE_NAMES;
    }

    public String getParticleName() {
        return this.particleName;
    }

    public int getParticleID() {
        return this.particleID;
    }

    public int getArgumentCount() {
        return this.argumentCount;
    }

    public boolean getShouldIgnoreRange() {
        return this.shouldIgnoreRange;
    }

    public boolean hasArguments() {
        return this.argumentCount > 0;
    }

    public static ParticleTypes getParticleFromId(int particleId) {
        return PARTICLES.get(particleId);
    }

    static {
        List<String> list = new ArrayList<>();

        for (ParticleTypes enumparticletypes : values()) {
            PARTICLES.put(enumparticletypes.getParticleID(), enumparticletypes);

            if (!enumparticletypes.getParticleName().endsWith("_")) {
                list.add(enumparticletypes.getParticleName());
            }
        }

        PARTICLE_NAMES = list.toArray(new String[0]);
    }
}
