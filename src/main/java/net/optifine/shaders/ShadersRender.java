package net.optifine.shaders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.RenderLayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.IntBuffer;

public class ShadersRender {
	private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");

	public static void setFrustrumPosition(ICamera frustum, double x, double y, double z) {
		frustum.setPosition(x, y, z);
	}

	public static void setupTerrain(RenderGlobal renderGlobal, Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
		renderGlobal.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
	}

	public static void beginTerrainSolid() {
		if (Shaders.isRenderingWorld) {
			Shaders.fogEnabled = true;
			Shaders.useProgram(Shaders.PROGRAM_TERRAIN);
		}
	}

	public static void beginTerrainCutoutMipped() {
		if (Shaders.isRenderingWorld) {
			Shaders.useProgram(Shaders.PROGRAM_TERRAIN);
		}
	}

	public static void beginTerrainCutout() {
		if (Shaders.isRenderingWorld) {
			Shaders.useProgram(Shaders.PROGRAM_TERRAIN);
		}
	}

	public static void endTerrain() {
		if (Shaders.isRenderingWorld) {
			Shaders.useProgram(Shaders.PROGRAM_TEXTURED_LIT);
		}
	}

	public static void beginTranslucent() {
		if (Shaders.isRenderingWorld) {
			if (Shaders.usedDepthBuffers >= 2) {
				GlStateManager.setActiveTexture(33995);
				Shaders.checkGLError("pre copy depth");
				GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, Shaders.renderWidth, Shaders.renderHeight);
				Shaders.checkGLError("copy depth");
				GlStateManager.setActiveTexture(33984);
			}

			Shaders.useProgram(Shaders.PROGRAM_WATER);
		}
	}

	public static void endTranslucent() {
		if (Shaders.isRenderingWorld) {
			Shaders.useProgram(Shaders.PROGRAM_TEXTURED_LIT);
		}
	}

	public static void renderHand0(EntityRenderer er, float par1, int par2) {
		if (!Shaders.isShadowPass) {
			boolean flag = Shaders.isItemToRenderMainTranslucent();
			boolean flag1 = Shaders.isItemToRenderOffTranslucent();

			if (!flag || !flag1) {
				Shaders.readCenterDepth();
				Shaders.beginHand(false);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				Shaders.setSkipRenderHands(flag, flag1);
				er.renderHand(par1, par2, true, false, false);
				Shaders.endHand();
				Shaders.setHandsRendered(!flag, !flag1);
				Shaders.setSkipRenderHands(false, false);
			}
		}
	}

	public static void renderHand1(EntityRenderer er, float par1, int par2) {
		if (!Shaders.isShadowPass && !Shaders.isBothHandsRendered()) {
			Shaders.readCenterDepth();
			GlStateManager.enableBlend();
			Shaders.beginHand(true);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Shaders.setSkipRenderHands(Shaders.isHandRenderedMain(), Shaders.isHandRenderedOff());
			er.renderHand(par1, par2, true, false, true);
			Shaders.endHand();
			Shaders.setHandsRendered(true, true);
			Shaders.setSkipRenderHands(false, false);
		}
	}

	public static void renderItemFP(ItemRenderer itemRenderer, float par1, boolean renderTranslucent) {
		Shaders.setRenderingFirstPersonHand(true);
		GlStateManager.depthMask(true);

		if (renderTranslucent) {
			GlStateManager.depthFunc(519);
			GL11.glPushMatrix();
			IntBuffer intbuffer = Shaders.activeDrawBuffers;
			Shaders.setDrawBuffers(Shaders.DRAW_BUFFERS_NONE);
			Shaders.renderItemKeepDepthMask = true;
			itemRenderer.renderItemInFirstPerson(par1);
			Shaders.renderItemKeepDepthMask = false;
			Shaders.setDrawBuffers(intbuffer);
			GL11.glPopMatrix();
		}

		GlStateManager.depthFunc(515);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		itemRenderer.renderItemInFirstPerson(par1);
		Shaders.setRenderingFirstPersonHand(false);
	}

	public static void renderFPOverlay(EntityRenderer er, float par1, int par2) {
		if (!Shaders.isShadowPass) {
			Shaders.beginFPOverlay();
			er.renderHand(par1, par2, false, true, false);
			Shaders.endFPOverlay();
		}
	}

	public static void beginBlockDamage() {
		if (Shaders.isRenderingWorld) {
			Shaders.useProgram(Shaders.PROGRAM_DAMAGED_BLOCK);

			if (Shaders.PROGRAM_DAMAGED_BLOCK.getId() == Shaders.PROGRAM_TERRAIN.getId()) {
				Shaders.setDrawBuffers(Shaders.DRAW_BUFFERS_COLOR_ATT_0);
				GlStateManager.depthMask(false);
			}
		}
	}

	public static void endBlockDamage() {
		if (Shaders.isRenderingWorld) {
			GlStateManager.depthMask(true);
			Shaders.useProgram(Shaders.PROGRAM_TEXTURED_LIT);
		}
	}

	public static void renderShadowMap(EntityRenderer entityRenderer, int pass, float partialTicks, long finishTimeNano) {
		if (Shaders.usedShadowDepthBuffers > 0 && --Shaders.shadowPassCounter <= 0) {
			Minecraft minecraft = Minecraft.getMinecraft();
			RenderGlobal renderglobal = minecraft.renderGlobal;
			Shaders.isShadowPass = true;
			Shaders.shadowPassCounter = Shaders.shadowPassInterval;
			Shaders.preShadowPassThirdPersonView = minecraft.gameSettings.thirdPersonView;
			minecraft.gameSettings.thirdPersonView = 1;
			Shaders.checkGLError("pre shadow");
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			EXTFramebufferObject.glBindFramebufferEXT(36160, Shaders.sfb);
			Shaders.checkGLError("shadow bind sfb");
			entityRenderer.setupCameraTransform(partialTicks, 2);
			Shaders.setCameraShadow(partialTicks);
			Shaders.checkGLError("shadow camera");
			Shaders.useProgram(Shaders.PROGRAM_SHADOW);
			GL20.glDrawBuffers(Shaders.SFB_DRAW_BUFFERS);
			Shaders.checkGLError("shadow drawbuffers");
			GL11.glReadBuffer(0);
			Shaders.checkGLError("shadow readbuffer");
			EXTFramebufferObject.glFramebufferTexture2DEXT(36160, 36096, 3553, Shaders.SFB_DEPTH_TEXTURES.get(0), 0);

			if (Shaders.usedShadowColorBuffers != 0) {
				EXTFramebufferObject.glFramebufferTexture2DEXT(36160, 36064, 3553, Shaders.SFB_COLOR_TEXTURES.get(0), 0);
			}

			Shaders.checkFramebufferStatus("shadow fb");
			GL11.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glClear(Shaders.usedShadowColorBuffers != 0 ? GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT : GL11.GL_DEPTH_BUFFER_BIT);
			Shaders.checkGLError("shadow clear");
			ClippingHelper clippinghelper = ClippingHelperShadow.getInstance();
			Frustum frustum = new Frustum(clippinghelper);
			Entity entity = minecraft.getRenderViewEntity();
			double xPos = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
			double yPos = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
			double zPos = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
			frustum.setPosition(xPos, yPos, zPos);
			GlStateManager.shadeModel(7425);
			GlStateManager.enableDepth();
			GlStateManager.depthFunc(515);
			GlStateManager.depthMask(true);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.disableCull();
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			int i;
			i = entityRenderer.frameCount;
			entityRenderer.frameCount = i + 1;
			renderglobal.setupTerrain(entity, partialTicks, frustum, i, minecraft.player.isSpectator());
			GlStateManager.matrixMode(5888);
			GlStateManager.pushMatrix();
			GlStateManager.disableAlpha();
			renderglobal.renderBlockLayer(RenderLayer.SOLID, partialTicks, 2, entity);
			Shaders.checkGLError("shadow terrain solid");
			GlStateManager.enableAlpha();
			renderglobal.renderBlockLayer(RenderLayer.CUTOUT_MIPPED, partialTicks, 2, entity);
			Shaders.checkGLError("shadow terrain cutoutmipped");
			minecraft.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			renderglobal.renderBlockLayer(RenderLayer.CUTOUT, partialTicks, 2, entity);
			Shaders.checkGLError("shadow terrain cutout");
			minecraft.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
			GlStateManager.shadeModel(7424);
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.matrixMode(5888);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();

			renderglobal.renderEntities(entity, frustum, partialTicks);
			Shaders.checkGLError("shadow entities");
			GlStateManager.matrixMode(5888);
			GlStateManager.popMatrix();
			GlStateManager.depthMask(true);
			GlStateManager.disableBlend();
			GlStateManager.enableCull();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.alphaFunc(516, 0.1F);

			if (Shaders.usedShadowDepthBuffers >= 2) {
				GlStateManager.setActiveTexture(33989);
				Shaders.checkGLError("pre copy shadow depth");
				GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, Shaders.shadowMapWidth, Shaders.shadowMapHeight);
				Shaders.checkGLError("copy shadow depth");
				GlStateManager.setActiveTexture(33984);
			}

			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.shadeModel(7425);
			Shaders.checkGLError("shadow pre-translucent");
			GL20.glDrawBuffers(Shaders.SFB_DRAW_BUFFERS);
			Shaders.checkGLError("shadow drawbuffers pre-translucent");
			Shaders.checkFramebufferStatus("shadow pre-translucent");

			if (Shaders.isRenderShadowTranslucent()) {
				renderglobal.renderBlockLayer(RenderLayer.TRANSLUCENT, partialTicks, 2, entity);
				Shaders.checkGLError("shadow translucent");
			}

			GlStateManager.shadeModel(7424);
			GlStateManager.depthMask(true);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GL11.glFlush();
			Shaders.checkGLError("shadow flush");
			Shaders.isShadowPass = false;
			minecraft.gameSettings.thirdPersonView = Shaders.preShadowPassThirdPersonView;

			if (Shaders.hasGlGenMipmap) {
				if (Shaders.usedShadowDepthBuffers >= 1) {
					if (Shaders.SHADOW_MIPMAP_ENABLED[0]) {
						GlStateManager.setActiveTexture(33988);
						GlStateManager.bindTexture(Shaders.SFB_DEPTH_TEXTURES.get(0));
						GL30.glGenerateMipmap(3553);
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.SHADOW_FILTER_NEAREST[0] ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR);
					}

					if (Shaders.usedShadowDepthBuffers >= 2 && Shaders.SHADOW_MIPMAP_ENABLED[1]) {
						GlStateManager.setActiveTexture(33989);
						GlStateManager.bindTexture(Shaders.SFB_DEPTH_TEXTURES.get(1));
						GL30.glGenerateMipmap(3553);
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.SHADOW_FILTER_NEAREST[1] ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR);
					}

					GlStateManager.setActiveTexture(33984);
				}

				if (Shaders.usedShadowColorBuffers >= 1) {
					if (Shaders.SHADOW_COLOR_MIPMAP_ENABLED[0]) {
						GlStateManager.setActiveTexture(33997);
						GlStateManager.bindTexture(Shaders.SFB_COLOR_TEXTURES.get(0));
						GL30.glGenerateMipmap(3553);
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.SHADOW_COLOR_FILTER_NEAREST[0] ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR);
					}

					if (Shaders.usedShadowColorBuffers >= 2 && Shaders.SHADOW_COLOR_MIPMAP_ENABLED[1]) {
						GlStateManager.setActiveTexture(33998);
						GlStateManager.bindTexture(Shaders.SFB_COLOR_TEXTURES.get(1));
						GL30.glGenerateMipmap(3553);
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.SHADOW_COLOR_FILTER_NEAREST[1] ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR);
					}

					GlStateManager.setActiveTexture(33984);
				}
			}

			Shaders.checkGLError("shadow postprocess");
			EXTFramebufferObject.glBindFramebufferEXT(36160, Shaders.dfb);
			GL11.glViewport(0, 0, Shaders.renderWidth, Shaders.renderHeight);
			Shaders.activeDrawBuffers = null;
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			Shaders.useProgram(Shaders.PROGRAM_TERRAIN);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			Shaders.checkGLError("shadow end");
		}
	}

	public static void preRenderChunkLayer(RenderLayer blockLayerIn) {
		if (Shaders.isRenderBackFace(blockLayerIn)) {
			GlStateManager.disableCull();
		}

		if (OpenGlHelper.useVbo()) {
			GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
			GL20.glEnableVertexAttribArray(Shaders.MID_TEX_COORD_ATTRIB);
			GL20.glEnableVertexAttribArray(Shaders.TANGENT_ATTRIB);
			GL20.glEnableVertexAttribArray(Shaders.ENTITY_ATTRIB);
		}
	}

	public static void postRenderChunkLayer(RenderLayer blockLayerIn) {
		if (OpenGlHelper.useVbo()) {
			GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			GL20.glDisableVertexAttribArray(Shaders.MID_TEX_COORD_ATTRIB);
			GL20.glDisableVertexAttribArray(Shaders.TANGENT_ATTRIB);
			GL20.glDisableVertexAttribArray(Shaders.ENTITY_ATTRIB);
		}

		if (Shaders.isRenderBackFace(blockLayerIn)) {
			GlStateManager.enableCull();
		}
	}

	public static void setupArrayPointersVbo() {
		int i = 14;
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 56, 0L);
		GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 56, 12L);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 56, 16L);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glTexCoordPointer(2, GL11.GL_SHORT, 56, 24L);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		GL11.glNormalPointer(GL11.GL_BYTE, 56, 28L);
		GL20.glVertexAttribPointer(Shaders.MID_TEX_COORD_ATTRIB, 2, GL11.GL_FLOAT, false, 56, 32L);
		GL20.glVertexAttribPointer(Shaders.TANGENT_ATTRIB, 4, GL11.GL_SHORT, false, 56, 40L);
		GL20.glVertexAttribPointer(Shaders.ENTITY_ATTRIB, 3, GL11.GL_SHORT, false, 56, 48L);
	}

	public static void beaconBeamBegin() {
		Shaders.useProgram(Shaders.PROGRAM_BEACON_BEAM);
	}

	public static void beaconBeamStartQuad1() {
	}

	public static void beaconBeamStartQuad2() {
	}

	public static void beaconBeamDraw1() {
	}

	public static void beaconBeamDraw2() {
		GlStateManager.disableBlend();
	}

	public static void renderEnchantedGlintBegin() {
		Shaders.useProgram(Shaders.PROGRAM_ARMOR_GLINT);
	}

	public static void renderEnchantedGlintEnd() {
		if (Shaders.isRenderingWorld) {
			if (Shaders.isRenderingFirstPersonHand() && Shaders.isRenderBothHands()) {
				Shaders.useProgram(Shaders.PROGRAM_HAND);
			} else {
				Shaders.useProgram(Shaders.PROGRAM_ENTITIES);
			}
		} else {
			Shaders.useProgram(Shaders.PROGRAM_NONE);
		}
	}

	public static boolean renderEndPortal(TileEntityEndPortal te, double x, double y, double z, float partialTicks, int destroyStage, float offset) {
		if (!Shaders.isShadowPass && Shaders.activeProgram.getId() == 0) {
			return false;
		} else {
			GlStateManager.disableLighting();
			Config.getTextureManager().bindTexture(END_PORTAL_TEXTURE);
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
			float f = 0.5F;
			float f1 = f * 0.15F;
			float f2 = f * 0.3F;
			float f3 = f * 0.4F;
			float f4 = 0.0F;
			float f5 = 0.2F;
			float f6 = (System.currentTimeMillis() % 100000L) / 100000.0F;
			int i = 240;
			worldrenderer.pos(x, y + offset, z + 1.0D).color(f1, f2, f3, 1.0F).tex(f4 + f6, f4 + f6).lightmap(i, i).endVertex();
			worldrenderer.pos(x + 1.0D, y + offset, z + 1.0D).color(f1, f2, f3, 1.0F).tex(f4 + f6, f5 + f6).lightmap(i, i).endVertex();
			worldrenderer.pos(x + 1.0D, y + offset, z).color(f1, f2, f3, 1.0F).tex(f5 + f6, f5 + f6).lightmap(i, i).endVertex();
			worldrenderer.pos(x, y + offset, z).color(f1, f2, f3, 1.0F).tex(f5 + f6, f4 + f6).lightmap(i, i).endVertex();
			tessellator.draw();
			GlStateManager.enableLighting();
			return true;
		}
	}
}
