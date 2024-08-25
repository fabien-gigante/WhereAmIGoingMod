package one.laqua.waig.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import one.laqua.waig.client.config.WaigConfig;
import one.laqua.waig.mixin.BossBarHudAccessor;
import one.laqua.waig.mixin.CombinedInventoryAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class CompassHud implements HudRenderCallback {

	@SuppressWarnings("unused")
	private static final float DEFAULT_FOV = 70f;
	private static final float SCALE = 100f;

	private final MinecraftClient client = MinecraftClient.getInstance();
	private final Set<Integer> compass_stacks = WaigConfig.getCompassItems();

	private static boolean visible = true;

	record Marker(String text, int color, float yaw) {
		public static final Marker SPAWN = new Marker("⌂", 0xff0080ff, 0);
		public static final Marker DEATH = new Marker("☠", 0xffffffff, 0);
		public static final Marker LODESTONE = new Marker("✠", 0xffffff00, 0);

		private static final String[] CARDINAL_TEXT = { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };
		public static final List<Marker> CARDINAL_MARKS = IntStream.range(0, 8)
				.mapToObj(i -> new Marker(CARDINAL_TEXT[i], 0xffd0d0d0, i * 45f)).toList();
		public static final List<Marker> HATCH_MARKS = IntStream.range(0, 64).filter(t -> t % 8 != 0)
				.mapToObj(t -> new Marker(t % 4 == 0 ? "," : ".", 0x80d0d0d0, t * 360f / 64)).toList();

		public Marker at(float yaw) { return new Marker(this.text, this.color, yaw); }
	}

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

	private static float getYawTo(Entity entity, BlockPos pos) {
		Vec3d vec3d = Vec3d.ofCenter(pos);
		float yaw = (float) (Math.atan2(vec3d.getZ() - entity.getZ(), vec3d.getX() - entity.getX()) * 180 / Math.PI) - 90;
		return (yaw % 360.0f + 360.0f) % 360.0f;
	}

	private static void addMarker(List<Marker> markers, Entity entity, Optional<GlobalPos> pos, Marker type) {
		if (pos.isPresent() && pos.get().dimension() == entity.getWorld().getRegistryKey())
			markers.add(type.at(getYawTo(entity, pos.get().pos())));
	}

	private List<Marker>  getTargetMarkers() {
		ClientPlayerEntity player = client.player;
		List<Marker> markers = new ArrayList<Marker>();
		Stream<ItemStack> items = getItemsToCheck(player);
		if (items == null) return markers;
		List<ItemStack> matched = items.filter(item -> compass_stacks.contains(Item.getRawId(item.getItem()))).toList();
		if (matched.isEmpty()) return markers;
		if (compass_stacks.contains(Item.getRawId(Items.COMPASS)))
			matched.stream().filter(item -> item.getItem() instanceof CompassItem).forEach(compass -> {
				if (compass.contains(DataComponentTypes.LODESTONE_TRACKER)) {
					LodestoneTrackerComponent tracker = compass.get(DataComponentTypes.LODESTONE_TRACKER);
					if (tracker.tracked()) addMarker(markers, player, tracker.target(), Marker.LODESTONE);
				} else
					markers.add(Marker.SPAWN.at(getYawTo(player, player.getWorld().getSpawnPos())));
			});
		if (compass_stacks.contains(Item.getRawId(Items.RECOVERY_COMPASS)))
			if (matched.stream().anyMatch(item -> item.getItem() == Items.RECOVERY_COMPASS))
				addMarker(markers, player, player.getLastDeathPos(), Marker.DEATH);
		return markers;
	}

	@Override
	public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
		if (!visible || client.options.hudHidden || client.player == null) return;
		int bossBarCount = ((BossBarHudAccessor) client.inGameHud.getBossBarHud()).getBossBars().size();
		int posY = 3 + bossBarCount * 19;
		for (Marker marker : Marker.HATCH_MARKS)    drawMarker(drawContext, posY, marker);
		for (Marker marker : Marker.CARDINAL_MARKS) drawMarker(drawContext, posY, marker);
		for (Marker marker : getTargetMarkers())    drawMarker(drawContext, posY, marker);
	}

	private void drawMarker(DrawContext drawContext, int y, Marker m) {
		float modAngle = (m.yaw - client.player.headYaw + 540.0f) % 360.0f - 180.0f;
		float fov = client.options.getFov().getValue(); // float fov = DEFAULT_FOV;
		if (modAngle < -fov || modAngle > +fov) return;
		int screenWidth = client.getWindow().getScaledWidth();
		int x = Math.round(screenWidth / 2f + (float) Math.sin(modAngle * Math.PI / 180.0d) * SCALE);
		if (m.text == ",") drawVerticalLineWithShadow(drawContext, x, y + 2, y + 7, m.color);
		else if (m.text == ".") drawVerticalLineWithShadow(drawContext, x, y + 4, y + 7, m.color);
		else drawContext.drawCenteredTextWithShadow(client.textRenderer, m.text, x, y, m.color);
	}

	private void drawVerticalLineWithShadow(DrawContext drawContext, int x, int y1, int y2, int color) {
		drawContext.drawVerticalLine(x + 1, y1 + 1, y2 + 1, color & 0xff000000);
		drawContext.drawVerticalLine(x, y1, y2, color);
	}

	public static void setVisible(boolean visible) {
		CompassHud.visible = visible;
	}

	public static void toggleVisibility() {
		setVisible(!visible);
	}
}
