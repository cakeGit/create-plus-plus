package foundry.veil.example;

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

public class VeilExampleModClient implements ClientModInitializer {
    
    private static final ResourceLocation TEST_FBO = VeilExampleMod.path("test");
    @Override
    public void onInitializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(
            VeilExampleBlocks.MAP, new SimpleBlockItemRenderer(new MapBlockEntity(BlockPos.ZERO, VeilExampleBlocks.MAP.defaultBlockState())));
        BlockEntityRenderers.register(VeilExampleBlocks.MAP_BE, MapBlockEntityRenderer::new);
        BlockEntityRenderers.register(VeilExampleBlocks.MIRROR_BE, MirrorBlockEntityRenderer::new);
        BlockEntityRenderers.register(VeilExampleBlocks.PROJECTOR_BE, ProjectorBlockEntityRenderer::new);
        FabricVeilRendererEvent.EVENT.register(renderer -> {
            renderer.getEditorManager().add(new VeilExampleModEditor());
            renderer.getPostProcessingManager().add(VeilExampleMod.path("projector"));
        });

        FabricVeilRenderLevelStageEvent.EVENT.register((stage, levelRenderer, bufferSource, poseStack, projectionMatrix, renderTick, partialTicks, camera, frustum) -> {
            if (stage == VeilRenderLevelStageEvent.Stage.AFTER_LEVEL) {
                MirrorBlockEntityRenderer.renderLevel(Minecraft.getInstance().level, projectionMatrix, partialTicks, VeilRenderBridge.create(frustum), camera);
                
                FramebufferManager framebufferManager = VeilRenderSystem.renderer().getFramebufferManager();
                AdvancedFbo fbo = framebufferManager.getFramebuffer(TEST_FBO);
                if (VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
                    return;
                }
                VeilLevelPerspectiveRenderer.render(fbo, RENDER_MODELVIEW, RENDER_PROJECTION, renderPos, VIEW.identity().lookAlong(dir, up), RENDER_DISTANCE, partialTicks);
                
            }
        });
        
        VeilEventPlatform.INSTANCE.preVeilPostProcessing(((name, pipeline, context) -> {
            if (!name.equals(VeilExampleMod.path("projector"))) return;
            ShaderProgram program = context.getShader(VeilExampleMod.path("projector"));
            if (program == null) return;
            program.setVector("origin", 0f, 0f, 0f);
            program.setVector("direction", 0f, -1f, 0f);
        }));
    }
}