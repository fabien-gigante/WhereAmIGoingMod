package one.laqua.waig.client.markers;

import net.minecraft.entity.player.PlayerEntity;

public class DeathMarker extends TextMarker { 
    public DeathMarker(PlayerEntity player) { 
        super("☠", 0xffffffff);
         move(player, player.getLastDeathPos()); 
    } 
}
