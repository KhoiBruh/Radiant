package net.optifine.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.RenderLayer;
import net.minecraft.world.IBlockAccess;
import net.optifine.BetterGrass;
import net.optifine.ConnectedTextures;
import net.optifine.NaturalTextures;
import net.optifine.SmartLeaves;
import net.optifine.render.RenderEnv;

import java.util.List;

public class BlockModelCustomizer {
	private static final List<BakedQuad> NO_QUADS = ImmutableList.of();

	public static IBakedModel getRenderModel(IBakedModel modelIn, IBlockState stateIn, RenderEnv renderEnv) {
		if (renderEnv.isSmartLeaves()) {
			modelIn = SmartLeaves.getLeavesModel(modelIn, stateIn);
		}

		return modelIn;
	}

	public static List<BakedQuad> getRenderQuads(List<BakedQuad> quads, IBlockAccess worldIn, IBlockState stateIn, BlockPos posIn, Direction enumfacing, RenderLayer layer, long rand, RenderEnv renderEnv) {
		if (enumfacing != null) {
			if (renderEnv.isSmartLeaves() && SmartLeaves.isSameLeaves(worldIn.getBlockState(posIn.offset(enumfacing)), stateIn)) {
				return NO_QUADS;
			}

			if (!renderEnv.isBreakingAnimation(quads) && Config.isBetterGrass()) {
				quads = BetterGrass.getFaceQuads(worldIn, stateIn, posIn, enumfacing, quads);
			}
		}

		List<BakedQuad> list = renderEnv.getListQuadsCustomizer();
		list.clear();

		for (int i = 0; i < quads.size(); ++i) {
			BakedQuad bakedquad = quads.get(i);
			BakedQuad[] abakedquad = getRenderQuads(bakedquad, worldIn, stateIn, posIn, enumfacing, rand, renderEnv);

			if (i == 0 && quads.size() == 1 && abakedquad.length == 1 && abakedquad[0] == bakedquad && bakedquad.getQuadEmissive() == null) {
				return quads;
			}

			for (BakedQuad bakedquad1 : abakedquad) {
				list.add(bakedquad1);

				if (bakedquad1.getQuadEmissive() != null) {
					renderEnv.getListQuadsOverlay(getEmissiveLayer(layer)).addQuad(bakedquad1.getQuadEmissive(), stateIn);
					renderEnv.setOverlaysRendered(true);
				}
			}
		}

		return list;
	}

	private static RenderLayer getEmissiveLayer(RenderLayer layer) {
		return layer != null && layer != RenderLayer.SOLID ? layer : RenderLayer.CUTOUT_MIPPED;
	}

	private static BakedQuad[] getRenderQuads(BakedQuad quad, IBlockAccess worldIn, IBlockState stateIn, BlockPos posIn, Direction enumfacing, long rand, RenderEnv renderEnv) {
		if (renderEnv.isBreakingAnimation(quad)) {
			return renderEnv.getArrayQuadsCtm(quad);
		} else {
			BakedQuad bakedquad = quad;

			if (Config.isConnectedTextures()) {
				BakedQuad[] abakedquad = ConnectedTextures.getConnectedTexture(worldIn, stateIn, posIn, quad, renderEnv);

				if (abakedquad.length != 1 || abakedquad[0] != quad) {
					return abakedquad;
				}
			}

			if (Config.isNaturalTextures()) {
				quad = NaturalTextures.getNaturalTexture(posIn, quad);

				if (quad != bakedquad) {
					return renderEnv.getArrayQuadsCtm(quad);
				}
			}

			return renderEnv.getArrayQuadsCtm(quad);
		}
	}
}
