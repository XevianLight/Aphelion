package net.xevianlight.aphelion.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.screen.renderer.EnergyDisplayTooltipArea;
import net.xevianlight.aphelion.util.MouseUtil;

import java.util.Optional;

public class VacuumArcFurnaceScreen extends AbstractContainerScreen<VacuumArcFurnaceMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "textures/gui/vacuum_arc_furnace/gui.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID,"textures/gui/base/arrow_progress.png");

    private EnergyDisplayTooltipArea energyInfoArea;

    public VacuumArcFurnaceScreen(VacuumArcFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        // Gets rid of labels
        this.inventoryLabelY = 73;
        this.titleLabelY = 5;
        this.titleLabelX = 35;

        assignEnergyInfoArea();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
        if (menu.blockEntity.getBlockState().getValue(BlockStateProperties.LIT)) {
            guiGraphics.blit(GUI_TEXTURE, x + 54, y + 14, 176, 75, 13, 18);
        }

        renderEnergyBar(guiGraphics, x , y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ARROW_TEXTURE,x + 88, y + 35, 0, 0, menu.getScaledArrowProgress(), 16, 24, 16);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        int stored = menu.blockEntity.getTrueEnergyStorage(null).getEnergyStored();
        int max = menu.blockEntity.getTrueEnergyStorage(null).getMaxEnergyStored();

        int h = (max <= 0) ? 0 : (int) Math.round((stored / (double) max) * 42.0);
        h = Math.max(0, Math.min(42, h));
        int w = 14;

        int drawX = x + 9;
        int drawY = y + 9 + (42 - h);     // move up as it fills
        int u = 176;
        int v = 15 + (42 - h);            // sample lower part of bar texture

        guiGraphics.blit(GUI_TEXTURE, drawX, drawY, u, v, w, h);
    }

    private void assignEnergyInfoArea() {
        energyInfoArea = new EnergyDisplayTooltipArea(((width - imageWidth) / 2) + 9,
                ((height - imageHeight) / 2 ) + 9, menu.blockEntity.getTrueEnergyStorage(null), 14, 42);
    }

    private void renderEnergyAreaTooltip(GuiGraphics guiGraphics, int pMouseX, int pMouseY, int x, int y) {
        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 9, 9, 14, 42)) {
            guiGraphics.renderTooltip(this.font, energyInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - x, pMouseY - y);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderEnergyAreaTooltip(guiGraphics, mouseX, mouseY, x, y);
    }

    public static boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
    }
}
