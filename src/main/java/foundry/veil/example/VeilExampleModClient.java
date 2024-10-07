package foundry.veil.example;

import foundry.veil.Veil;
import foundry.veil.api.client.registry.PostPipelineStageRegistry;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.VeilRenderer;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilPostProcessingEvent;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.example.blockentity.MapBlockEntity;
import foundry.veil.example.client.render.MapBlockEntityRenderer;
import foundry.veil.example.client.render.MirrorBlockEntityRenderer;
import foundry.veil.example.client.render.ProjectorBlockEntityRenderer;
import foundry.veil.example.client.render.SimpleBlockItemRenderer;
import foundry.veil.example.editor.VeilExampleModEditor;
import foundry.veil.example.registry.VeilExampleBlocks;
import foundry.veil.fabric.event.FabricVeilPostProcessingEvent;
import foundry.veil.fabric.event.FabricVeilRenderLevelStageEvent;
import foundry.veil.fabric.event.FabricVeilRendererEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;

public class VeilExampleModClient implements ClientModInitializer {

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
            }
        });
        
        VeilEventPlatform.INSTANCE.preVeilPostProcessing(((name, pipeline, context) -> {
            if (!name.equals(VeilExampleMod.path("projector"))) return;
            ShaderProgram program = context.getShader(VeilExampleMod.path("projector"));
            if (program == null) return;
            program.setFloat("seteone", 1f);
            program.setVector("origin", 0f, 0f, 0f);
            program.setVector("direction", 0f, -1f, 0f);
        }));
    }
}