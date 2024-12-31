package one.laqua.waig.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import one.laqua.waig.client.config.HudPoIMode;
import one.laqua.waig.client.config.WaigConfig;
import one.laqua.waig.mixin.BossBarHudAccessor;
import one.laqua.waig.mixin.CombinedInventoryAccessor;
import one.laqua.waig.client.markers.*;

@Environment(EnvType.CLIENT)
public class CompassHud implements HudRenderCallback {

	private static final MinecraftClient client = MinecraftClient.getInstance();
	private final Set<Integer> compass_stacks = WaigConfig.getCompassItems();

	private static boolean visible = true;


	private Stream<ItemStack> getItemsToCheck(ClientPlayerEntity p) {
		switch (WaigConfig.getHudShowMode()) {
			case INVENTORY:
				return ((CombinedInventoryAccessor) p.getInventory()).getCombinedInventory().stream().flatMap(e -> e.stream());
			case HAND:
				return Stream.of(p.getOffHandStack(), p.getMainHandStack());
			case ALWAYS:
			default:
				return null;
		}
	}

	private static void addMarker(List<Marker> markers, Marker marker, Entity entity, Vec3d pos) { markers.add(marker.at(entity, pos)); }
	private static void addMarker(List<Marker> markers, Marker marker, Entity entity, Optional<GlobalPos> pos) {
		if (pos.isPresent() && pos.get().dimension() == entity.getWorld().getRegistryKey())
			markers.add(marker.at(entity, pos.get().pos()));
	}

	private List<Marker> getTargetMarkers() {
		List<Marker> markers = new ArrayList<Marker>();
		ClientPlayerEntity player = client.player;
		Stream<ItemStack> items = getItemsToCheck(player);
		if (items == null) return markers;
		List<ItemStack> matched = items.filter(item -> compass_stacks.contains(Item.getRawId(item.getItem()))).toList();
		if (matched.isEmpty()) return null;
		if (WaigConfig.getHudPoIMode() == HudPoIMode.HIDDEN) return markers;
		if (compass_stacks.contains(Item.getRawId(Items.COMPASS)))
			matched.stream().filter(item -> item.getItem() instanceof CompassItem).forEach(compass -> {
				if (compass.contains(DataComponentTypes.LODESTONE_TRACKER)) {
					LodestoneTrackerComponent tracker = compass.get(DataComponentTypes.LODESTONE_TRACKER);
					if (tracker.tracked()) addMarker(markers, new LodestoneMarker(), player, tracker.target());
				} else
					addMarker(markers, new SpawnMarker(), player, Optional.of(new GlobalPos(World.OVERWORLD, player.getWorld().getSpawnPos())));
			});
		if (compass_stacks.contains(Item.getRawId(Items.RECOVERY_COMPASS)))
			if (matched.stream().anyMatch(item -> item.getItem() == Items.RECOVERY_COMPASS))
				addMarker(markers, new DeathMarker(), player, player.getLastDeathPos());

		if (compass_stacks.contains(Item.getRawId(Items.FILLED_MAP)) && player.getWorld().getRegistryKey()==World.OVERWORLD)
			matched.stream().filter(item -> item.getItem() instanceof FilledMapItem).forEach(map -> {
				MapDecorationsComponent mapDecorationsComponent = map.getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT);
				mapDecorationsComponent.decorations().forEach((id, decoration) -> {
					addMarker(markers, new MapMarker(), player, new Vec3d(decoration.x(),0,decoration.z()));
				}) ;
			});
		return markers;
	}

	@Override
	public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
		if (!visible || client.options.hudHidden || client.player == null) return;
		List<Marker> markers =  getTargetMarkers();
		if (markers == null) return;
		int bossBarCount = ((BossBarHudAccessor) client.inGameHud.getBossBarHud()).getBossBars().size();
		int posY = 3 + bossBarCount * 19;
		for (Marker marker : Marker.HATCH_MARKS)	marker.draw(drawContext, posY);
		for (Marker marker : Marker.CARDINAL_MARKS)	marker.draw(drawContext, posY);
		for (Marker marker : markers)				marker.draw(drawContext, posY);
	}

	public static void setVisible(boolean visible) {
		CompassHud.visible = visible;
	}

	public static void toggleVisibility() {
		setVisible(!visible);
	}
}
