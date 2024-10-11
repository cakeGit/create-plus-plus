package foundry.veil.example;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.example.client.render.SimpleBlockItemRenderer;
import foundry.veil.example.editor.VeilExampleModEditor;
import foundry.veil.example.registry.VeilExampleBlocks;
import foundry.veil.fabric.event.FabricVeilRenderLevelStageEvent;
import foundry.veil.fabric.event.FabricVeilRendererEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class VeilExampleModClient implements ClientModInitializer {
    
    private static final ResourceLocation PROJECTION_DEPTH = VeilExampleMod.path("projection_depth");
    
    private static final Matrix4f RENDER_MODELVIEW = new Matrix4f();
    private static final Matrix4f RENDER_PROJECTION = new Matrix4f();
    private static final Quaternionf VIEW = new Quaternionf();
    private static final Vector4f OBLIQUE_PLANE = new Vector4f();
    private static final Matrix4f TRANSFORM = new Matrix4f();
    
    private static ProjectorTexture PROJECTOR_TEXTURE;
    
    @Override
    public void onInitializeClient() {
//        BlockEntityRenderers.register(VeilExampleBlocks.PROJECTOR_BE, ProjectorBlockEntityRenderer::new);
        FabricVeilRendererEvent.EVENT.register(renderer -> {
            renderer.getEditorManager().add(new VeilExampleModEditor());
            VeilRenderSystem.renderer().getPostProcessingManager().add(VeilExampleMod.path("projector"));
            PROJECTOR_TEXTURE = new ProjectorTexture();
        });
        
        FabricVeilRenderLevelStageEvent.EVENT.register((stage, levelRenderer, bufferSource, poseStack, projectionMatrix, renderTick, partialTicks, camera, frustum) -> {
            if (stage == VeilRenderLevelStageEvent.Stage.AFTER_LEVEL) {
            
            }
        });
        
        VeilEventPlatform.INSTANCE.preVeilPostProcessing(((name, pipeline, context) -> {
            if (!name.equals(VeilExampleMod.path("projector"))) return;

            ShaderProgram program = context.getShader(VeilExampleMod.path("projector"));
            if (program == null) return;
            program.setVector("origin", 0f, 0f, 0f);
            program.setVector("direction", 0f, -1f, 0f);
            Window window = Minecraft.getInstance().getWindow();
            program.setFloat("BaseAspect", (float) window.getWidth() / window.getHeight());


            PoseStack poseStack = new PoseStack();

            Vector3f dir = new Vector3f(0f, -1f, 0f);
            Vector3f up = new Vector3f(0f, 0f, 1f);
            poseStack.mulPoseMatrix(TRANSFORM.set(new Matrix4f()));
            poseStack.mulPose(VIEW.identity().lookAlong(dir, up));

            Matrix4f pose = poseStack.last().pose();
            program.setMatrix("DepthMatrix", pose);
            program.setFloat("ProjectorPlaneNear", 0.0f);
            program.setFloat("ProjectorPlaneFar", 128.0f);

            program.setVector("projectorOneTexel", 2/1024f, 2/1024f);
            
            program.addSampler("ProjectionDepthSampler", PROJECTOR_TEXTURE.getId());
        }));
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
        
        Window window = Minecraft.getInstance().getWindow();
        float aspect = (float) window.getWidth() / window.getHeight();
        
        Vector3f dir = new Vector3f(0f, -1f, 0f);
        Vector3f up = new Vector3f(0f, 0f, 1f);
        
//        RENDER_PROJECTION.setPerspective((float) (Math.PI / 2f), 1f, 0.3F, 128 * 4);
        RENDER_PROJECTION.identity().ortho(-1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 128.0f);
        Vector4f plane = new Vector4f(dir.x(), dir.y(), dir.z(), -dir.dot(dir.x(), dir.y(), dir.z()));
        
        new Quaternionf().lookAlong(dir, up).transform(plane);
//        calculateObliqueMatrix(RENDER_PROJECTION, plane, RENDER_PROJECTION);
        
        VeilLevelPerspectiveRenderer.render(
            fbo,
            RENDER_MODELVIEW,
            RENDER_PROJECTION,
            new Vector3d(0, 0, 0),
            VIEW.identity().lookAlong(dir, up),
            64.0F,
            partialTicks
        );
        
        PROJECTOR_TEXTURE.copy(fbo);
    }
    
    
}