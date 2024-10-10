package foundry.veil.example.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.example.VeilExampleModClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    public void inject_render(CallbackInfo ci) {
        VeilExampleModClient.ashbjkldg(0f);
    }

}
