package foundry.veil.example.blockentity;

import foundry.veil.example.registry.VeilExampleBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ProjectorBlockEntity extends BlockEntity {

    public ProjectorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(VeilExampleBlocks.PROJECTOR_BE, blockPos, blockState);
    }

//    @Override
//    public Packet<ClientGamePacketListener> getUpdatePacket() {
//        return ClientboundBlockEntityDataPacket.create(this);
//    }
}
