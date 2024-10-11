package foundry.veil.example.mixin.client;

import foundry.veil.example.VeilExampleModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    
    @Shadow public boolean noRender;
    
    @Shadow @Nullable public ClientLevel level;
    
    @Inject(method = "runTick", at = @At("HEAD"))
    public void inject_render(boolean bl, CallbackInfo ci) {
        if (noRender || !(bl && level != null))
            return;
        VeilExampleModClient.ashbjkldg(0f);
    }

}
