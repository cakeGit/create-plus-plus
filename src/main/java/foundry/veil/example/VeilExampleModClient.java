package foundry.veil.example;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferManager;
import foundry.veil.api.client.render.framebuffer.VeilFramebuffers;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.example.editor.VeilExampleModEditor;
import foundry.veil.fabric.event.FabricVeilRendererEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;

public class VeilExampleModClient implements ClientModInitializer {
    
    private static final ResourceLocation PROJECTOR_RESULTS = VeilExampleMod.path("projector_results");
    private static final ResourceLocation PROJECTOR_FINAL_RESULTS = VeilExampleMod.path("projector_final_results");
    private static final ResourceLocation PROJECTION_DEPTH = VeilExampleMod.path("projector_depth");
    private static final ResourceLocation PROJECTOR_RESULTS_PREVIOUS = VeilExampleMod.path("projector_results_previous");
    
    private static final Matrix4f RENDER_MODELVIEW = new Matrix4f();
    private static final Matrix4f RENDER_PROJECTION = new Matrix4f();
    private static final Quaternionf VIEW = new Quaternionf();
    private static final Vector4f OBLIQUE_PLANE = new Vector4f();
    
    private static ProjectorTexture PROJECTOR_TEXTURE;
    
    private static final List<Vec3> skibbidiprojector3 = new ArrayList<>();
    private static final List<ProjectorTexture> skibbidiprojector45 = new ArrayList<>();
    
    @Override
    public void onInitializeClient() {
        FabricVeilRendererEvent.EVENT.register(renderer -> {
            renderer.getEditorManager().add(new VeilExampleModEditor());
            VeilRenderSystem.renderer().getPostProcessingManager().add(VeilExampleMod.path("projector_flare"));
            PROJECTOR_TEXTURE = new ProjectorTexture();
            skibbidiprojector45.add(new ProjectorTexture());
            skibbidiprojector45.add(new ProjectorTexture());
        });
        
        VeilEventPlatform.INSTANCE.preVeilPostProcessing(((name, pipeline, context) -> {
            if (!name.equals(VeilExampleMod.path("projector_flare"))) return;
            ShaderProgram shader = VeilRenderSystem.setShader(VeilExampleMod.path("projector"));
            FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
            AdvancedFbo in = framebufferManager.getFramebuffer(VeilFramebuffers.POST);
            AdvancedFbo out = framebufferManager.getFramebuffer(PROJECTOR_RESULTS);
            AdvancedFbo resultsPrevious = framebufferManager.getFramebuffer(PROJECTOR_RESULTS_PREVIOUS);
            
            
            for (Vec3 skibbidiprojectorfour : skibbidiprojector3) {
                int index = skibbidiprojector3.indexOf(skibbidiprojectorfour);

//                if (index == 0) continue;
                
                shader.setup();
                shader.bind();
                shader.applyRenderSystem();
                shader.addRenderSystemTextures();
                setRenderDataForProjector(shader, skibbidiprojectorfour, index);
                context.applySamplers(shader);
                shader.applyShaderSamplers(context, 0);
                shader.setFramebufferSamplers(in);
                out.bind(true);
                context.drawScreenQuad();
                AdvancedFbo.unbind();
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.depthFunc(GL_ALWAYS);
                RenderSystem.depthMask(false);
                
                out.resolveToAdvancedFbo(resultsPrevious);

            }
            
//            ShaderProgram program = context.getShader(VeilExampleMod.path("projector_flare"));
//            if (program == null) return;
//            program.setVector("origin", new Vector3f(0, 0, 0));
//            Window window = Minecraft.getInstance().getWindow();
//            program.setFloat("aspect", (float) window.getWidth() / window.getHeight());
            
            ShaderProgram program_mix = context.getShader(VeilExampleMod.path("projector_mix"));
            AdvancedFbo projector_results = framebufferManager.getFramebuffer(PROJECTOR_FINAL_RESULTS);
            if (program_mix != null) {
                program_mix.addSampler("ProjectorLightSampler", projector_results.getColorTextureAttachment(0).getId());
            }
        }));
    }
    
    private void setRenderDataForProjector(ShaderProgram shader, Vec3 skibbidiprojectorfour, int index) {
        
        shader.setInt("isFirstLayer", index == 0 ? 1 : 0);
        
        shader.setVector("origin",
            (float) skibbidiprojectorfour.x,
            (float) skibbidiprojectorfour.y,
            (float) skibbidiprojectorfour.z);
        shader.setVector("direction", 0f, -1f, 0f);
        Window window = Minecraft.getInstance().getWindow();
        shader.setFloat("BaseAspect", (float) window.getWidth() / window.getHeight());
        
        Vector3f dir = new Vector3f(0f, -1f, 0f);
        Vector3f up = new Vector3f(0f, 0f, 1f);
        shader.setMatrix("DepthMat", new Matrix4f().setPerspective((float) (Math.PI / 2f), 1f, 0.1F, 256f).rotate(VIEW.identity().lookAlong(dir, up)));
        
        shader.setFloat("ProjectorPlaneNear", 0.1f);
        shader.setFloat("ProjectorPlaneFar", 64f);
        
        shader.setVector("projectorOneTexel", 2 / 1024f, 2 / 1024f);
        
        shader.addSampler("ProjectionDepthSampler", skibbidiprojector45.get(index).getId());
        FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
        
        AdvancedFbo projector_results_fbo = framebufferManager.getFramebuffer(PROJECTOR_RESULTS);
        shader.addSampler("ProjectionResults", projector_results_fbo.getColorTextureAttachment(0).getId());
    }
    
    public static void calculateObliqueMatrix(Matrix4fc projection, Vector4fc c, Matrix4f store) {
        Vector4f q = projection.invert(new Matrix4f()).transform(
            Math.signum(c.x()),
            Math.signum(c.y()),
            1.0f,
            1.0f,
            OBLIQUE_PLANE);
        float dot = c.dot(q);
        store.m02(c.x() * 2.0F / dot - projection.m03()).m12(c.y() * 2.0F / dot - projection.m13()).m22(c.z() * 2.0F / dot - projection.m23()).m32(c.w() * 2.0F / dot - projection.m33());
    }
    
    public static void ashbjkldg(float partialTicks) {
        if (VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            return;
        }
        FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
        AdvancedFbo fbo = framebufferManager.getFramebuffer(PROJECTION_DEPTH);
        
        if (fbo == null) return;
        for (Vec3 sdafjnjksdfgjlsdfgjklklhjdfgkljs : skibbidiprojector3) {
            fbo.bindDraw(true);
            fbo.clear();
            AdvancedFbo.unbind();
//            if (skibbidiprojector3.indexOf(sdafjnjksdfgjlsdfgjklklhjdfgkljs) == 0) continue;
//
            int index = skibbidiprojector3.indexOf(sdafjnjksdfgjlsdfgjklklhjdfgkljs);
            
//            Window window = Minecraft.getInstance().getWindow();
//            float aspect = (float) window.getWidth() / window.getHeight();
            
            Vector3f dir = new Vector3f(0f, -1f, 0f);
            Vector3f up = new Vector3f(0f, 0f, 1f);
            
            RENDER_PROJECTION.identity().setPerspective((float) (Math.PI / 2f), 1f, 0.1F, 256f);
//        RENDER_PROJECTION.identity().ortho(-1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 128.0f);
//            Vector4f plane = new Vector4f(dir.x(), dir.y(), dir.z(), -dir.dot(dir.x(), dir.y(), dir.z()));
//
//            new Quaternionf().identity().lookAlong(dir, up).transform(plane);
//        calculateObliqueMatrix(RENDER_PROJECTION, plane, RENDER_PROJECTION);
            RENDER_MODELVIEW.identity();
            VeilLevelPerspectiveRenderer.render(
                fbo,
                RENDER_MODELVIEW,
                RENDER_PROJECTION,
                new Vector3d(sdafjnjksdfgjlsdfgjklklhjdfgkljs.x, sdafjnjksdfgjlsdfgjklklhjdfgkljs.y, sdafjnjksdfgjlsdfgjklklhjdfgkljs.z),
                VIEW.identity().lookAlong(dir, up),
                256f,
                partialTicks
            );
            
            skibbidiprojector45.get(index).copy(fbo);
//            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
//            fbo.bindDraw(true);
//            glClear(GL_DEPTH_BUFFER_BIT);
//            AdvancedFbo.unbind();
        }
        
        
        
    }
    
    static {
        //{
        //      "type": "veil:blit",
        //      "shader": "veil-example-mod:projector_blur",
        //      "in": "veil-example-mod:projector_results",
        //      "out": "veil:post"
        //    },
        
        skibbidiprojector3.add(new Vec3(20, 20, 0));
        skibbidiprojector3.add(new Vec3(0, 0, 0));
        
    }
    
}