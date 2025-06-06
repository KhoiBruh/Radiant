package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class RenderLightningBolt extends Render<EntityLightningBolt> {
    public RenderLightningBolt(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    public void doRender(EntityLightningBolt entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 1);
        double[] adouble = new double[8];
        double[] adouble1 = new double[8];
        double d0 = 0.0D;
        double d1 = 0.0D;
        Random random = new Random(entity.boltVertex);

        for (int i = 7; i >= 0; --i) {
            adouble[i] = d0;
            adouble1[i] = d1;
            d0 += (random.nextInt(11) - 5);
            d1 += (random.nextInt(11) - 5);
        }

        for (int k1 = 0; k1 < 4; ++k1) {
            Random random1 = new Random(entity.boltVertex);

            for (int j = 0; j < 3; ++j) {
                int k = 7;
                int l = 0;

                if (j > 0) {
                    k = 7 - j;
                }

                if (j > 0) {
                    l = k - 2;
                }

                double d2 = adouble[k] - d0;
                double d3 = adouble1[k] - d1;

                for (int i1 = k; i1 >= l; --i1) {
                    double d4 = d2;
                    double d5 = d3;

                    if (j == 0) {
                        d2 += (random1.nextInt(11) - 5);
                        d3 += (random1.nextInt(11) - 5);
                    } else {
                        d2 += (random1.nextInt(31) - 15);
                        d3 += (random1.nextInt(31) - 15);
                    }

                    worldrenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);
                    float f = 0.5F;
                    float f1 = 0.45F;
                    float f2 = 0.45F;
                    float f3 = 0.5F;
                    double d6 = 0.1D + k1 * 0.2D;

                    if (j == 0) {
                        d6 *= i1 * 0.1D + 1.0D;
                    }

                    double d7 = 0.1D + k1 * 0.2D;

                    if (j == 0) {
                        d7 *= (i1 - 1) * 0.1D + 1.0D;
                    }

                    for (int j1 = 0; j1 < 5; ++j1) {
                        double d8 = x + 0.5D - d6;
                        double d9 = z + 0.5D - d6;

                        if (j1 == 1 || j1 == 2) {
                            d8 += d6 * 2.0D;
                        }

                        if (j1 == 2 || j1 == 3) {
                            d9 += d6 * 2.0D;
                        }

                        double d10 = x + 0.5D - d7;
                        double d11 = z + 0.5D - d7;

                        if (j1 == 1 || j1 == 2) {
                            d10 += d7 * 2.0D;
                        }

                        if (j1 == 2 || j1 == 3) {
                            d11 += d7 * 2.0D;
                        }

                        worldrenderer.pos(d10 + d2, y + (i1 * 16), d11 + d3).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                        worldrenderer.pos(d8 + d4, y + ((i1 + 1) * 16), d9 + d5).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                    }

                    tessellator.draw();
                }
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    protected ResourceLocation getEntityTexture(EntityLightningBolt entity) {
        return null;
    }
}
