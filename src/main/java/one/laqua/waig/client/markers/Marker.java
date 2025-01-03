package one.laqua.waig.client.markers;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.util.math.MathHelper;
import one.laqua.waig.client.config.HudFoVMode;
import one.laqua.waig.client.config.HudPoIMode;
import one.laqua.waig.client.config.WaigConfig;

public abstract class Marker {
    protected int color;
    protected float yaw = 0;

	private static final float DEFAULT_FOV = 70f;
	private static final float SCALE = 100f;

    protected static final MinecraftClient client = MinecraftClient.getInstance();

    protected Marker(int color) { this(color, 0); }
    protected Marker(int color, float yaw) { this.color = color; this.yaw = yaw; }

    protected static void drawCenteredText(DrawContext ctx, TextRenderer textRenderer, String text, int centerX, int y, int color) {
        ctx.drawText(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color, false);
    }
    public void draw(DrawContext ctx, int y)
    {
        float modAngle = MathHelper.subtractAngles(client.player.headYaw, yaw);
        float fov = WaigConfig.getHudFoVMode() == HudFoVMode.PLAYER ? client.options.getFov().getValue() : DEFAULT_FOV;
        if (modAngle < -fov || modAngle > +fov) return;
        int screenWidth = client.getWindow().getScaledWidth();
        int x = Math.round(screenWidth / 2f + (float) Math.sin(modAngle * Math.PI / 180.0d) * SCALE);
        this.draw(ctx, x, y);
    }

    protected void move(PlayerEntity player, Vec3d pos) {
        Vec3d vec =  pos.subtract(player.getPos());
        this.yaw = (float) (Math.atan2(vec.getZ(), vec.getX()) * MathHelper.DEGREES_PER_RADIAN) - 90;
    }
    public boolean isVisible() { return true; }

    public abstract void draw(DrawContext ctx, int x, int y);

    public static final List<Marker> HATCH_MARKS = IntStream.range(0, 64).filter(t -> t % 8 != 0)
            .mapToObj(t -> (Marker)new HatchMark(t * 360f / 64, t % 4 == 0)).toList();
    private static final String[] CARDINAL_TEXT = "S SW W NW N NE E SE".split(" ");
    public static final List<Marker> CARDINAL_MARKS = IntStream.range(0, 8)
            .mapToObj(i -> (Marker)new TextMarker(CARDINAL_TEXT[i], 0xffd0d0d0, i * 45f)).toList();
}

class HatchMark extends Marker {
    protected final boolean large;
    protected HatchMark(float yaw, boolean large) { super(0x80d0d0d0, yaw); this.large = large; }

    protected static void drawVerticalLineWithShadow(DrawContext ctx, int x, int y1, int y2, int color) {
        ctx.drawVerticalLine(x + 1, y1 + 1, y2 + 1, color & 0xff000000);
        ctx.drawVerticalLine(x, y1, y2, color);
    }
    public void draw(DrawContext ctx, int x, int y) {
        drawVerticalLineWithShadow(ctx, x, y + (large ? 2 : 4), y + 7, color);
    }
}

class TargetMarker extends Marker { 
    protected float dist = Float.POSITIVE_INFINITY;
    
    protected TargetMarker(int color, float yaw) { super(color, yaw); }
    protected TargetMarker(int color) { super(color); }

    protected void move(PlayerEntity player, Vec3d pos) {
        super.move(player, pos);
        this.dist = (float) pos.subtract(player.getPos()).horizontalLength();
    }
    protected void move(PlayerEntity player, Vec3d pos, RegistryKey<World> dimension) {
        if (player.getWorld().getRegistryKey() == dimension) move(player, pos); else Hide();
    }
    protected void move(PlayerEntity player, BlockPos pos, RegistryKey<World> dimension) {
        move(player, Vec3d.ofCenter(pos), dimension);
    }
    protected void move(PlayerEntity player, Optional<GlobalPos> pos) {
        if (pos.isPresent()) move(player, pos.get().pos(), pos.get().dimension()); else Hide();
    }
    public boolean isVisible() { return !Float.isNaN(dist); }
    protected void Hide() { dist = Float.NaN; }

    public void draw(DrawContext ctx, int x, int y) {
        if (WaigConfig.getHudPoIMode() != HudPoIMode.DISTANCE || !Float.isFinite(dist)) return;
        String label;
        if (dist>=10000) label = Math.round(dist/1000) + "k";
        else if (dist>=1000) label = Math.round(dist/100)/10f + "k";
        else label = String.valueOf(Math.round(dist));
        drawCenteredText(ctx,client.textRenderer, label, x, y + 9, color);
    }
}

class TextMarker extends TargetMarker {
    protected String text;
    protected TextMarker(String text, int color) { this(text, color, 0);} 
    protected TextMarker(String text, int color, float yaw) { super(color, yaw); this.text = text; } 

    public void draw(DrawContext ctx, int x, int y) {
        if (text != null) ctx.drawCenteredTextWithShadow(client.textRenderer, text, x, y, color);
        super.draw(ctx, x, y);
    }
}