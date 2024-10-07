package foundry.veil.example.client.render;

import com.mojang.blaze3d.vertex.*;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.example.blockentity.ProjectorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectorBlockEntityRenderer implements BlockEntityRenderer<ProjectorBlockEntity> {

    public static HashMap<ProjectorBlockEntity, ProjectorInfo> PROJECTORS_TO_RENDER = new HashMap<>();
    
    public ProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ProjectorBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        if (VeilLevelPerspectiveRenderer.isRenderingPerspective()) {
            return;
        }
        
        PROJECTORS_TO_RENDER.put(blockEntity, new ProjectorInfo(blockEntity.getBlockPos().getCenter(), new Vec3(0, -1, 0)));
    }
    
    public record ProjectorInfo(Vec3 origin, Vec3 direction) {}
    
}
