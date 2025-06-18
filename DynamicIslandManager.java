package org.eris.managers.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import org.eris.Client;
import org.eris.event.annotations.EventTarget;
import org.eris.event.impl.events.EventAddDynamicIsland;
import org.eris.event.impl.events.EventPacket;
import org.eris.event.impl.events.EventRender2D;
import org.eris.managers.Managers;
import org.eris.modules.Module;
import org.eris.modules.impl.render.HUD;
import org.eris.modules.impl.render.Notification;
import org.eris.utils.MSTimer;
import org.eris.utils.dynamicIsland.PriorityUtil;
import org.eris.utils.fontRender.FontManager;
import org.eris.utils.render.GradientUtil;
import org.eris.utils.render.RenderUtil;
import org.eris.utils.render.RoundedUtil;
import org.eris.utils.render.StencilUtil;
import org.eris.utils.render.animation.AnimationUtil;
import org.eris.utils.render.animation.Direction;
import org.eris.utils.render.animation.impl.EaseOutExpo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jon_awa
 * @since 25/5/9
 */
public class DynamicIslandManager extends Managers {
    private final List<TimedContent> timedContents;
    private final List<Content> contents;
    private float renderX, width, height;
    private float[] containerPrevSlotColor;
    private float[] containerSlotColor;

    public DynamicIslandManager() {
        this.contents = new ArrayList<>();
        this.timedContents = new ArrayList<>();
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (Notification.notiMode.is("Dynamic Island") && Client.instance.getModuleManager().getModule(Notification.class).isState()) {
            final ScaledResolution sr = new ScaledResolution(mc);

            EventAddDynamicIsland dynamicIslandEvent = new EventAddDynamicIsland(EventAddDynamicIsland.State.PRE);
            Client.instance.getEventManager().call(dynamicIslandEvent);

            if (!this.timedContents.isEmpty()) {
                for (TimedContent content : new ArrayList<>(this.timedContents)) {
                    if (!content.timer.hasTimePassed(content.time)) {
                        this.contents.add(new Content(() -> {
                            switch (content.type) {
                                case MODULE -> {
                                    if (content.module != null) {
                                        if (content.module.isState()) {
                                            content.animation.setDirection(Direction.FORWARDS);
                                        } else {
                                            content.animation.setDirection(Direction.BACKWARDS);
                                        }

                                        double animation = content.animation.getOutput();
                                        RoundedUtil.drawRoundedRect(6, 13, 26, 14, 6.5f, Color.DARK_GRAY);
                                        RoundedUtil.drawRoundedRect(7, 14, 24, 12, 6, new Color((int) (128 - 80 * animation), (int) (128 - 11 * animation), (int) (128 + 19 * animation), 255));
                                        RoundedUtil.drawRoundedRect((float) (8 + 12 * animation), 15, 10, 10, 4.5f, Color.DARK_GRAY);
                                    }
                                }

                                case SUCCESS -> {
                                    if (content.timer.hasTimePassed(content.time - 200)) {
                                        content.animation.setDirection(Direction.BACKWARDS);
                                    }

                                    double animation = content.animation.getOutput();
                                    RoundedUtil.drawRoundedRect(7, 8, 24, 24, 7, new Color(95, 143, 80, 200));

                                    StencilUtil.initStencilToWrite();
                                    RenderUtil.drawRectWH(7, 8, 24 * animation, 24, -1);
                                    StencilUtil.readStencilBuffer();
                                    FontManager.icon44.drawString("A", 10.5f, 13, -1);
                                    StencilUtil.endStencilBuffer();
                                }

                                case WARNING -> {
                                    if (content.timer.hasTimePassed(content.time - 200)) {
                                        content.animation.setDirection(Direction.BACKWARDS);
                                    }

                                    double animation = content.animation.getOutput();
                                    RoundedUtil.drawRoundedRect(7, 8, 24, 24, 7, new Color(143, 80, 80, 200));

                                    StencilUtil.initStencilToWrite();
                                    RenderUtil.drawRectWH(7, 8, 24, 24 * animation, -1);
                                    StencilUtil.readStencilBuffer();
                                    FontManager.icon44.drawString("C", 11.3f, 13, -1);
                                    StencilUtil.endStencilBuffer();
                                }

                                case INFO -> {
                                    if (content.timer.hasTimePassed(content.time - 200)) {
                                        content.animation.setDirection(Direction.BACKWARDS);
                                    }

                                    double animation = content.animation.getOutput();
                                    RoundedUtil.drawRoundedRect(7, 8, 24, 24, 7, new Color(48, 117, 147, 200));
                                    RoundedUtil.drawRoundedRect(18.2f, 11, 1.5f, (float) (18 * animation), 0.75f, Color.WHITE);
                                    RoundedUtil.drawRoundedRect(10, 19, (float) (18 * animation), 1.5f, 0.75f, Color.WHITE);
                                }
                                default -> RoundedUtil.drawRoundedRect(7, 8, 24, 24, 7, new Color(200, 200, 200, 200));
                            }

                            FontManager.axiformaBold20.drawString(content.title, 38, 9, -1);

                            if (content.type == ContentType.MODULE && content.module != null) {
                                content.description = content.module.getName() + (content.module.isState() ? " has been §2Enabled" : " has been §4Disabled");
                                FontManager.axiforma18.drawString(content.description, 38, 24, -1);
                            } else {
                                FontManager.axiforma18.drawString(content.description, 38, 24, -1);
                            }
                        }, 50 + Math.max(FontManager.axiformaBold20.getStringWidth(content.title), FontManager.axiforma18.getStringWidth(content.description)), 40, PriorityUtil.LOW));
                    } else {
                        this.timedContents.remove(content);
                    }
                }
            }

            if (mc.thePlayer.openContainer instanceof ContainerChest container) {
                this.contents.clear();

                if (containerPrevSlotColor == null || containerPrevSlotColor.length != container.inventorySlots.size() - 36) {
                    containerPrevSlotColor = new float[container.inventorySlots.size() - 36];
                }

                if (containerSlotColor == null || containerSlotColor.length != container.inventorySlots.size() - 36) {
                    containerSlotColor = new float[container.inventorySlots.size() - 36];
                }


                this.contents.add(new Content(() -> {
                    GlStateManager.pushMatrix();
                    RenderHelper.enableGUIStandardItemLighting();
                    GlStateManager.enableDepth();

                    boolean isEmpty = true;
                    for (int i = 0; i < container.inventorySlots.size() - 36; ++i) {
                        Slot slot = container.inventorySlots.get(i);

                        int x = slot.xDisplayPosition + 3;
                        int y = slot.yDisplayPosition - 10;

                        if (containerSlotColor[i] == 100) {
                            containerPrevSlotColor[i] = 0;
                        }

                        if (slot.getHasStack()) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(slot.getStack(), x - 2, y - 3);
                            mc.getRenderItem().renderItemOverlayIntoGUI(FontManager.axiforma18, slot.getStack(), x - 2, y - 3, null);
                            isEmpty = false;
                        }

                        RoundedUtil.drawRoundedRect(x - 2, y - 3, 16, 16, 7, new Color(220, 220, 220, (int) (containerSlotColor[i] = AnimationUtil.smooth(containerSlotColor[i], containerPrevSlotColor[i], 0.3f))));
                    }

                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.popMatrix();

                    if (isEmpty) {
                        FontManager.axiforma20.drawCenteredString("Empty...", 90, (int) Math.ceil((float) container.getLowerChestInventory().getSizeInventory() / 9) * 10 - 3, -1);
                    }
                }, 180, (int) Math.ceil((float) container.getLowerChestInventory().getSizeInventory() / 9) * 21, PriorityUtil.TOP));
            } else if (mc.thePlayer.openContainer instanceof ContainerFurnace || mc.thePlayer.openContainer instanceof ContainerBrewingStand) {
                this.contents.clear();

                if (containerPrevSlotColor == null || containerPrevSlotColor.length != mc.thePlayer.openContainer.inventorySlots.size() - 36) {
                    containerPrevSlotColor = new float[mc.thePlayer.openContainer.inventorySlots.size() - 36];
                }

                if (containerSlotColor == null || containerSlotColor.length != mc.thePlayer.openContainer.inventorySlots.size() - 36) {
                    containerSlotColor = new float[mc.thePlayer.openContainer.inventorySlots.size() - 36];
                }

                this.contents.add(new Content(() -> {
                    GlStateManager.pushMatrix();
                    RenderHelper.enableGUIStandardItemLighting();
                    GlStateManager.enableDepth();

                    boolean isEmpty = true;
                    for (int i = 0; i < mc.thePlayer.openContainer.inventorySlots.size() - 36; ++i) {
                        Slot slot = mc.thePlayer.openContainer.inventorySlots.get(i);

                        if (containerSlotColor[i] == 100) {
                            containerPrevSlotColor[i] = 0;
                        }

                        if (slot != null && slot.getHasStack()) {
                            mc.getRenderItem().renderItemAndEffectIntoGUI(slot.getStack(), 5 + 20 * i, 5);
                            mc.getRenderItem().renderItemOverlayIntoGUI(FontManager.axiforma18, slot.getStack(), 5 + 20 * i, 5, null);
                            isEmpty = false;
                        }

                        RoundedUtil.drawRoundedRect(3 + 20 * i, 3, 16, 16, 7, new Color(220, 220, 220, (int) (containerSlotColor[i] = AnimationUtil.smooth(containerSlotColor[i], containerPrevSlotColor[i], 0.3f))));
                    }

                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.popMatrix();

                    if (isEmpty) {
                        FontManager.axiforma20.drawCenteredString("Empty...", 90, 20, -1);
                    }
                }, 180, 50, PriorityUtil.TOP));
            }

            Client.instance.getEventManager().call(dynamicIslandEvent.setState(EventAddDynamicIsland.State.POST));

            int maxWeight = this.contents.stream().mapToInt(content -> content.weight).max().orElse(-1);
            this.contents.removeIf(content -> content.weight < maxWeight);

            final NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            final String defaultText = " | " + mc.session.getUsername() + " | " + Minecraft.getDebugFPS() + "fps" + " | " + ((playerInfo == null || playerInfo.getResponseTime() == 0) ? "Pinging..." : playerInfo.getResponseTime() + " ping");

            this.renderX = AnimationUtil.smooth(this.renderX, (sr.getScaledWidth() - this.width) / 2f, 0.2f);
            this.width = AnimationUtil.smooth(this.width, (this.contents.isEmpty() && this.timedContents.isEmpty()) ? FontManager.axiformaBold20.getStringWidth(Client.name) + FontManager.axiforma20.getStringWidth(defaultText) + 29 : this.getMaxWidth(this.contents), 0.2f);
            this.height = AnimationUtil.smooth(this.height, (this.contents.isEmpty() && this.timedContents.isEmpty()) ? FontManager.axiforma20.getHeight() + 8 : this.getTotalHeight(this.contents), 0.2f);

            RenderUtil.addBlur(() -> RoundedUtil.drawRoundedRect(this.renderX, 15, this.width, this.height, 10.5f, Color.BLACK));
            RenderUtil.addBloom(() -> RoundedUtil.drawRoundedRect(this.renderX, 15, this.width, this.height, 10.5f, Color.BLACK));
            RoundedUtil.drawRoundedRect(this.renderX, 15, this.width, this.height, 10.5f, new Color(0, 0, 0, 130));
            RenderUtil.startGlScissor((int) this.renderX, 15, (int) this.width, (int) this.height);

            if (this.contents.isEmpty() && this.timedContents.isEmpty()) {
                GradientUtil.applyGradientHorizontal(this.renderX, 14, 50, FontManager.axiformaBold20.getHeight() + 8, 1, HUD.color(1), HUD.color(4), () -> {
                    FontManager.icon60.drawString("x", this.renderX, 14, -1);
                    FontManager.axiformaBold20.drawString(Client.name, this.renderX + 22, 21.5f, -1);
                });
                FontManager.axiforma20.drawString(defaultText, this.renderX + 39, 21.5f, -1);
            } else {
                float renderY = 15;
                for (Content content : this.contents) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(this.renderX, renderY, 0);
                    content.content.run();
                    GlStateManager.popMatrix();
                    renderY += content.height;
                }
                this.contents.clear();
            }

            RenderUtil.stopGlScissor();
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getEventType() == EventPacket.EventState.SEND) {
            if (event.getPacket() instanceof C0EPacketClickWindow c0e && (mc.thePlayer.openContainer instanceof ContainerChest || mc.thePlayer.openContainer instanceof ContainerFurnace || mc.thePlayer.openContainer instanceof ContainerBrewingStand)) {
                if (containerPrevSlotColor != null && containerPrevSlotColor.length == mc.thePlayer.openContainer.inventorySlots.size() - 36) {
                    if (c0e.getSlotId() < mc.thePlayer.openContainer.inventorySlots.size() - 36 && c0e.getSlotId() >= 0) {
                        containerPrevSlotColor[c0e.getSlotId()] = 100;
                    }
                }
            }

            if (event.getPacket() instanceof C0DPacketCloseWindow) {
                containerSlotColor = null;
                containerPrevSlotColor = null;
            }
        }
    }

    private float getMaxWidth(List<Content> contents) {
        float width = 0;

        for (Content content : contents) {
            if (width < content.width) {
                width = content.width;
            }
        }

        return width;
    }

    private float getTotalHeight(List<Content> contents) {
        float height = 0;

        for (Content content : contents) {
            height += content.height;
        }

        return height;
    }

    public void addContent(Runnable content, int width, int height, int weight) {
        this.contents.add(new Content(content, width, height, weight));
    }

    public void addContent(ContentType type, String title, String text, long time) {
        this.timedContents.add(new TimedContent(type, title, text, time));
    }

    public void addContent(Module module) {
        boolean canAdd = true;
        for (TimedContent content : this.timedContents) {
            if (content.module == null) continue;
            if (content.module == module) {
                content.time = content.timer.getPassedTime() + 2000;
                canAdd = false;
                break;
            }
        }

        if (canAdd) {
            this.timedContents.add(new TimedContent(module));
        }
    }

    public void addContent(Runnable icon, String title, String description, float present, Color color) {
        this.contents.add(new Content(() -> {
            RoundedUtil.drawRoundedRect(7, 8, 24, 24, 7, RenderUtil.reAlpha(color, 255));

            if (icon != null) {
                icon.run();
            }

            FontManager.axiformaBold20.drawString(title, 38, 9, -1);
            FontManager.axiforma18.drawString(description, 38, 24, -1);
            RoundedUtil.drawRoundedRect(7, 38, this.width - 14, 5, 2, RenderUtil.reAlpha(color.darker(), 150));
            RoundedUtil.drawRoundedRect(7, 38, Math.min(present * this.width - 14, this.width - 14), 5, 2, RenderUtil.reAlpha(color, 255));
        }, 50 + Math.max(FontManager.axiformaBold20.getStringWidth(title), FontManager.axiforma18.getStringWidth(description)), 50, PriorityUtil.MEDIUM));
    }

    public enum ContentType {
        MODULE,
        SUCCESS,
        WARNING,
        INFO
    }

    public static class Content {
        private final Runnable content;
        private final int width;
        private final int height;
        private final int weight;

        private Content(Runnable content, int width, int height, int weight) {
            this.content = content;
            this.width = width;
            this.height = height;
            this.weight = weight;
        }
    }

    private static class TimedContent {
        private final ContentType type;
        private final Module module;
        private final String title;
        private final EaseOutExpo animation;
        private final MSTimer timer;
        private String description;
        private long time;

        TimedContent(ContentType contentType, String title, String description, long time) {
            this.type = contentType;
            this.module = null;
            this.title = title;
            this.description = description;
            this.animation = new EaseOutExpo(800, 1);
            this.timer = new MSTimer();
            this.time = time;
        }

        TimedContent(Module module) {
            this.type = ContentType.MODULE;
            this.module = module;
            this.title = "Module Toggled";
            this.description = module.getName() + (module.isState() ? " has been §2Enabled" : " has been §4Disabled");
            this.animation = new EaseOutExpo(300, 1);
            this.timer = new MSTimer();
            this.time = 2000;
        }
    }
}
