package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class EntityDamageSourceIndirect extends EntityDamageSource {
    private final Entity indirectEntity;

    public EntityDamageSourceIndirect(String damageTypeIn, Entity source, Entity indirectEntityIn) {
        super(damageTypeIn, source);
        this.indirectEntity = indirectEntityIn;
    }

    public Entity getSourceOfDamage() {
        return this.damageSourceEntity;
    }

    public Entity getEntity() {
        return this.indirectEntity;
    }

    public IChatComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
        IChatComponent ichatcomponent = this.indirectEntity == null ? this.damageSourceEntity.getDisplayName() : this.indirectEntity.getDisplayName();
        ItemStack itemstack = this.indirectEntity instanceof EntityLivingBase entityLivingBase ? entityLivingBase.getHeldItem() : null;
        String s = "death.attack." + this.damageType;
        String s1 = s + ".item";
        return itemstack != null && itemstack.hasDisplayName() && StatCollector.canTranslate(s1) ? new ChatComponentTranslation(s1, entityLivingBaseIn.getDisplayName(), ichatcomponent, itemstack.getChatComponent()) : new ChatComponentTranslation(s, entityLivingBaseIn.getDisplayName(), ichatcomponent);
    }
}
