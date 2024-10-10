package foundry.veil.example;

import com.mojang.blaze3d.platform.Window;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.framebuffer.FramebufferManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.example.blockentity.MapBlockEntity;
import foundry.veil.example.client.render.MapBlockEntityRenderer;
import foundry.veil.example.client.render.MirrorBlockEntityRenderer;
import foundry.veil.example.client.render.ProjectorBlockEntityRenderer;
import foundry.veil.example.client.render.SimpleBlockItemRenderer;
import foundry.veil.example.editor.VeilExampleModEditor;
import foundry.veil.example.registry.VeilExampleBlocks;
import foundry.veil.fabric.event.FabricVeilRenderLevelStageEvent;
import foundry.veil.fabric.event.FabricVeilRendererEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class VeilExampleModClient implements ClientModInitializer {
    
    private static final ResourceLocation TEST_FBO = VeilExampleMod.path("test");
//
//    private static final Matrix4f RENDER_MODELVIEW = new Matrix4f();
//    private static final Matrix4f RENDER_PROJECTION = new Matrix4f();
//    private static final Quaternionf VIEW = new Quaternionf();
    
    @Override
    public void onInitializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(
            VeilExampleBlocks.MAP, new SimpleBlockItemRenderer(new MapBlockEntity(BlockPos.ZERO, VeilExampleBlocks.MAP.defaultBlockState())));
        BlockEntityRenderers.register(VeilExampleBlocks.MAP_BE, MapBlockEntityRenderer::new);
        BlockEntityRenderers.register(VeilExampleBlocks.MIRROR_BE, MirrorBlockEntityRenderer::new);
//        BlockEntityRenderers.register(VeilExampleBlocks.PROJECTOR_BE, ProjectorBlockEntityRenderer::new);
        FabricVeilRendererEvent.EVENT.register(renderer -> {
            renderer.getEditorManager().add(new VeilExampleModEditor());
            //TODO remove when manually activating
//            renderer.getPostProcessingManager().add(VeilExampleMod.path("projector"));
        });

        FabricVeilRenderLevelStageEvent.EVENT.register((stage, levelRenderer, bufferSource, poseStack, projectionMatrix, renderTick, partialTicks, camera, frustum) -> {
            if (stage == VeilRenderLevelStageEvent.Stage.AFTER_LEVEL) {
                MirrorBlockEntityRenderer.renderLevel(Minecraft.getInstance().level, projectionMatrix, partialTicks, VeilRenderBridge.create(frustum), camera);
                
                
//                FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
//                AdvancedFbo fbo = framebufferManager.getFramebuffer(TEST_FBO);
//
//                if (fbo == null) return;
//
//                Window window = Minecraft.getInstance().getWindow();
//                float aspect = (float) window.getWidth() / window.getHeight();
//                float fov = projectionMatrix.perspectiveFov();
//
//
//
//                Vector3f dir = new Vector3f(0f, -1f, 0f);
//                Vector3f up = new Vector3f(0f, 0f, 1f);
//
//                RENDER_PROJECTION.setPerspective(fov, aspect, 0.3F, 128 * 4);
//                VeilLevelPerspectiveRenderer.render(fbo, RENDER_MODELVIEW, RENDER_PROJECTION, new Vector3d(0, 0, 0), VIEW.identity().lookAlong(dir, up), 64, partialTicks);

            }
        });
        
        //TODO
        // ok so from what i can see
        // you gotta call the post processor for each one after generating its depthmap by directly invoking the postprocessing manager, ignoring the default pipeline system,
        // then copy the results buffer by running a finalise processor
        
//        VeilEventPlatform.INSTANCE.preVeilPostProcessing(((name, pipeline, context) -> {
//            if (!name.equals(VeilExampleMod.path("projector"))) return;
//            ShaderProgram program = context.getShader(VeilExampleMod.path("projector"));
//            if (program == null) return;
//            program.setVector("origin", 0f, 0f, 0f);
//            program.setVector("direction", 0f, -1f, 0f);
//        }));
    }
}