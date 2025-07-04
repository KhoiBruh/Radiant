package net.optifine;

import net.minecraft.src.Config;
import net.optifine.shaders.Shaders;

import java.util.BitSet;

public class SmartAnimations {
	private static final BitSet SPRITES_RENDERED = new BitSet();
	private static final BitSet TEXTURES_RENDERED = new BitSet();
	private static boolean active;

	public static boolean isActive() {
		return active && !Shaders.isShadowPass;
	}

	public static void update() {
		active = Config.getGameSettings().ofSmartAnimations;
	}

	public static void spriteRendered(int animationIndex) {
		if (animationIndex >= 0) {
			SPRITES_RENDERED.set(animationIndex);
		}
	}

	public static void spritesRendered(BitSet animationIndexes) {
		if (animationIndexes != null) {
			SPRITES_RENDERED.or(animationIndexes);
		}
	}

	public static boolean isSpriteRendered(int animationIndex) {
		return animationIndex >= 0 && SPRITES_RENDERED.get(animationIndex);
	}

	public static void resetSpritesRendered() {
		SPRITES_RENDERED.clear();
	}

	public static void textureRendered(int textureId) {
		if (textureId >= 0) {
			TEXTURES_RENDERED.set(textureId);
		}
	}

	public static boolean isTextureRendered(int texId) {
		return texId >= 0 && TEXTURES_RENDERED.get(texId);
	}

	public static void resetTexturesRendered() {
		TEXTURES_RENDERED.clear();
	}
}
