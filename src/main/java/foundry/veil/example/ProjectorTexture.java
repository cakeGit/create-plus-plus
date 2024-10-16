package foundry.veil.example;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import io.netty.buffer.EmptyByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL11C;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30C.*;

public class ProjectorTexture extends AbstractTexture {
    
    private boolean rendered;
    
    private int width;
    private int height;
    
    public ProjectorTexture() {
        this.setFilter(false, true);
        this.width = -1;
        this.height = -1;
    }
    
    @Override
    public void load(ResourceManager resourceManager) {
    }
    
    public void copy(AdvancedFbo fbo) {
        int id = this.getId();
        int width = fbo.getWidth();
        int height = fbo.getHeight();
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            TextureUtil.prepareImage(NativeImage.InternalGlFormat.RED, id, 1, width, height);
//            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
//            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (IntBuffer) null);
        }
        RenderSystem.bindTexture(id);
        fbo.bindRead();
//        glBindFramebuffer(GL_FRAMEBUFFER, fbo.getId());
        glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT,0, 0, width, height, 0);
//        AdvancedFbo.unbind();
        glGenerateMipmap(GL_TEXTURE_2D);
//        AdvancedFbo.unbind();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        RenderSystem.bindTexture(0);

//        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo.getId());
//        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id);
//        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
    }
    
    
}