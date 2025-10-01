package one.laqua.waig.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import one.laqua.waig.client.config.HudPoIMode;
import one.laqua.waig.client.config.WaigConfig;
import one.laqua.waig.mixin.BossBarHudAccessor;
import one.laqua.waig.client.markers.*;

@Environment(EnvType.CLIENT)
public class CompassHud implements HudElement {

	private static final MinecraftClient client = MinecraftClient.getInstance();
	private final Set<Item> compass_stacks = WaigConfig.getCompassItems();
	private static boolean visible = true;

	private Stream<ItemStack> getItemsToCheck(PlayerEntity p) {
		switch (WaigConfig.getHudShowMode()) {
			case INVENTORY:
				return Stream.concat(
						p.getInventory().getMainStacks().stream(),
						EquipmentSlot.VALUES.stream().map(slot -> p.getEquippedStack(slot))
	                );
			case HAND:
				return Stream.of(p.getOffHandStack(), p.getMainHandStack());
			case ALWAYS:
			default:
				return null;
		}
	}

	private static void addMarker(List<Marker> markers, Marker marker) {
		if (marker.isVisible())	markers.add(marker);
	}

	private List<Marker> getTargetMarkers() {
		List<Marker> markers = new ArrayList<Marker>();
		PlayerEntity player = client.player;
		Stream<ItemStack> items = getItemsToCheck(player);
		if (items == null) return markers;
		List<ItemStack> matched = items.filter(item -> compass_stacks.contains(item.getItem())).toList();
		if (matched.isEmpty()) return null;
		if (WaigConfig.getHudPoIMode() == HudPoIMode.HIDDEN) return markers;
		if (compass_stacks.contains(Items.COMPASS))
			matched.stream().filter(item -> item.getItem() instanceof CompassItem).forEach(compass -> {
				if (LodestoneMarker.check(compass)) addMarker(markers, new LodestoneMarker(player, compass));
				else addMarker(markers, new SpawnMarker(player));
			});
		if (compass_stacks.contains(Items.RECOVERY_COMPASS))
			if (matched.stream().anyMatch(item -> item.getItem() == Items.RECOVERY_COMPASS))
				addMarker(markers, new DeathMarker(player));
		if (compass_stacks.contains(Items.FILLED_MAP))
			matched.stream().filter(item -> item.getItem() instanceof FilledMapItem).forEach(map -> {
				MapMarker.enumerate(player, map).forEach(marker -> addMarker(markers, marker) );
			});
		return markers;
	}

	@Override
	public void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
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
