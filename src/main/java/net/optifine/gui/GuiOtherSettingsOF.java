package net.optifine.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiOtherSettingsOF extends GuiScreen implements GuiYesNoCallback {
	private static final GameSettings.Options[] enumOptions = new GameSettings.Options[]{GameSettings.Options.SHOW_FPS, GameSettings.Options.ADVANCED_TOOLTIPS, GameSettings.Options.WEATHER, GameSettings.Options.TIME, GameSettings.Options.USE_FULLSCREEN, GameSettings.Options.FULLSCREEN_MODE, GameSettings.Options.AUTOSAVE_TICKS, GameSettings.Options.SCREENSHOT_SIZE, GameSettings.Options.SHOW_GL_ERRORS};
	private final GuiScreen prevScreen;
	private final GameSettings settings;
	private final TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());
	protected String title;

	public GuiOtherSettingsOF(GuiScreen guiscreen, GameSettings gamesettings) {
		this.prevScreen = guiscreen;
		this.settings = gamesettings;
	}

	public void initGui() {
		this.title = I18n.format("of.options.otherTitle");
		this.buttonList.clear();

		for (int i = 0; i < enumOptions.length; ++i) {
			GameSettings.Options gamesettings$options = enumOptions[i];
			int j = this.width / 2 - 155 + i % 2 * 160;
			int k = this.height / 6 + 21 * (i / 2) - 12;

			if (!gamesettings$options.getEnumFloat()) {
				this.buttonList.add(new GuiOptionButtonOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options, this.settings.getKeyBinding(gamesettings$options)));
			} else {
				this.buttonList.add(new GuiOptionSliderOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options));
			}
		}

		this.buttonList.add(new GuiButton(210, this.width / 2 - 100, this.height / 6 + 168 + 11 - 44, I18n.format("of.options.other.reset")));
		this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.format("gui.done")));
	}

	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.enabled) {
			if (guibutton.id < 200 && guibutton instanceof GuiOptionButton guiOptionButton) {
				this.settings.setOptionValue(guiOptionButton.returnEnumOptions(), 1);
				guibutton.displayString = this.settings.getKeyBinding(GameSettings.Options.getEnumOptions(guibutton.id));
			}

			if (guibutton.id == 200) {
				this.mc.gameSettings.saveOptions();
				this.mc.displayGuiScreen(this.prevScreen);
			}

			if (guibutton.id == 210) {
				this.mc.gameSettings.saveOptions();
				GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("of.message.other.reset"), "", 9999);
				this.mc.displayGuiScreen(guiyesno);
			}
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			this.mc.gameSettings.resetSettings();
		}

		this.mc.displayGuiScreen(this);
	}

	public void drawScreen(int x, int y, float f) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
		super.drawScreen(x, y, f);
		this.tooltipManager.drawTooltips(x, y, this.buttonList);
	}
}
