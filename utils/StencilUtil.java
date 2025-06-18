package org.eris.utils.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.eris.utils.msic.MinecraftInstance;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

public class StencilUtil implements MinecraftInstance {
    public static void dispose() {
        GL11.glDisable(2960);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    public static void erase(boolean invert) {
        GL11.glStencilFunc(invert ? 514 : 517, 1, 65535);
        GL11.glStencilOp(7680, 7680, 7681);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GL11.glAlphaFunc(516, 0.0f);
    }

    public static void endStencilBuffer() {
        StencilUtil.dispose();
    }

    public static void write(boolean renderClipLayer) {
        StencilUtil.checkSetupFBO();
        GL11.glClearStencil(0);
        GL11.glClear(1024);
        GL11.glEnable(2960);
        GL11.glStencilFunc(519, 1, 65535);
        GL11.glStencilOp(7680, 7680, 7681);
        if (!renderClipLayer) {
            GlStateManager.colorMask(false, false, false, false);
        }
    }

    public static void write(boolean renderClipLayer, Framebuffer fb, boolean clearStencil, boolean invert) {
        StencilUtil.checkSetupFBO(fb);
        if (clearStencil) {
            GL11.glClearStencil(0);
            GL11.glClear(1024);
            GL11.glEnable(2960);
        }
        GL11.glStencilFunc(519, invert ? 0 : 1, 65535);
        GL11.glStencilOp(7680, 7680, 7681);
        if (!renderClipLayer) {
            GlStateManager.colorMask(false, false, false, false);
        }
    }

    public static void checkSetupFBO() {
        Framebuffer fbo = mc.getFramebuffer();
        if (fbo != null && fbo.depthBuffer > -1) {
            StencilUtil.setupFBO(fbo);
            fbo.depthBuffer = -1;
        }
    }

    public static void checkSetupFBO(Framebuffer fbo) {
        if (fbo != null && fbo.depthBuffer > -1) {
            StencilUtil.setupFBO(fbo);
            fbo.depthBuffer = -1;
        }
    }

    public static void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, mc.displayWidth, mc.displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencil_depth_buffer_ID);
    }

    public static void initStencilToWrite() {
        StencilUtil.write(false);
    }

    public static void readStencilBuffer() {
        StencilUtil.erase(true);
    }
}

