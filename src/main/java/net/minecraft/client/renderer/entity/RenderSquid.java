package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;

public class RenderSquid extends RenderLiving<EntitySquid> {
    private static final ResourceLocation SQUID_TEXTURES = new ResourceLocation("textures/entity/squid.png");

    public RenderSquid(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    protected ResourceLocation getEntityTexture(EntitySquid entity) {
        return SQUID_TEXTURES;
    }

    protected void rotateCorpse(EntitySquid bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        float f = bat.prevSquidPitch + (bat.squidPitch - bat.prevSquidPitch) * partialTicks;
        float f1 = bat.prevSquidYaw + (bat.squidYaw - bat.prevSquidYaw) * partialTicks;
        GlStateManager.translate(0.0F, 0.5F, 0.0F);
        GlStateManager.rotate(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -1.2F, 0.0F);
    }

    protected float handleRotationFloat(EntitySquid livingBase, float partialTicks) {
        return livingBase.lastTentacleAngle + (livingBase.tentacleAngle - livingBase.lastTentacleAngle) * partialTicks;
    }
}
