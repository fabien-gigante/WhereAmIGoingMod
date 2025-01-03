package one.laqua.waig.client.markers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class LodestoneMarker extends TextMarker {
     public LodestoneMarker(PlayerEntity player, ItemStack compass) {
        super("✠", 0xff8060e0);
        LodestoneTrackerComponent tracker = compass.get(DataComponentTypes.LODESTONE_TRACKER);
        if (tracker!=null && tracker.tracked()) move(player, tracker.target()); else Hide();
     }
     public static boolean check(ItemStack compass) { return compass.contains(DataComponentTypes.LODESTONE_TRACKER); }
}
