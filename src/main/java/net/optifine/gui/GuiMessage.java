package net.optifine.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.src.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMessage extends GuiScreen {
	protected final String confirmButtonText;
	private final GuiScreen parentScreen;
	private final String messageLine1;
	private final String messageLine2;
	private final List listLines2 = new ArrayList<>();
	private int ticksUntilEnable;

	public GuiMessage(GuiScreen parentScreen, String line1, String line2) {
		this.parentScreen = parentScreen;
		this.messageLine1 = line1;
		this.messageLine2 = line2;
		this.confirmButtonText = I18n.format("gui.done");
	}

	public void initGui() {
		this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 74, this.height / 6 + 96, this.confirmButtonText));
		this.listLines2.clear();
		this.listLines2.addAll(this.fontRendererObj.listFormattedStringToWidth(this.messageLine2, this.width - 50));
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		Config.getMinecraft().displayGuiScreen(this.parentScreen);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, this.messageLine1, this.width / 2, 70, 16777215);
		int i = 90;

		for (Object s : this.listLines2) {
			this.drawCenteredString(this.fontRendererObj, (String) s, this.width / 2, i, 16777215);
			i += this.fontRendererObj.FONT_HEIGHT;
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void setButtonDelay(int ticksUntilEnable) {
		this.ticksUntilEnable = ticksUntilEnable;

		for (GuiButton guibutton : this.buttonList) {
			guibutton.enabled = false;
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		super.updateScreen();

		if (--this.ticksUntilEnable == 0) {
			for (GuiButton guibutton : this.buttonList) {
				guibutton.enabled = true;
			}
		}
	}
}
