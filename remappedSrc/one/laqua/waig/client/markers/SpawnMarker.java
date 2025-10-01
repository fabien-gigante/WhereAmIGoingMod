package one.laqua.waig.client.markers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class SpawnMarker extends TextMarker { 
    public SpawnMarker(PlayerEntity player) {
        super("⭐", 0xff0080ff); 
        move(player, player.getWorld().getSpawnPos(), World.OVERWORLD); 
    }
}