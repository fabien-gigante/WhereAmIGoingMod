package one.laqua.waig.client.markers;

import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

    Marker(int color) { this(color, 0); }
    Marker(int color, float yaw) { this.color = color; this.yaw = yaw; }

    public void draw(DrawContext ctx, int y)
    {
        float modAngle = MathHelper.subtractAngles(client.player.headYaw, yaw);
        float fov = WaigConfig.getHudFoVMode() == HudFoVMode.PLAYER ? client.options.getFov().getValue() : DEFAULT_FOV;
        if (modAngle < -fov || modAngle > +fov) return;
        int screenWidth = client.getWindow().getScaledWidth();
        int x = Math.round(screenWidth / 2f + (float) Math.sin(modAngle * Math.PI / 180.0d) * SCALE);
        this.draw(ctx, x, y);
    }

    public Marker at(Entity entity, Vec3d pos) {
        Vec3d vec =  pos.subtract(entity.getPos());
        this.yaw = (float) (Math.atan2(vec.getZ(), vec.getX()) * MathHelper.DEGREES_PER_RADIAN) - 90;
        return this;
    }
    public Marker at(Entity entity, BlockPos pos) { return at(entity, Vec3d.ofCenter(pos)); }
    public abstract void draw(DrawContext ctx, int x, int y);

    public static final List<Marker> HATCH_MARKS = IntStream.range(0, 64).filter(t -> t % 8 != 0)
            .mapToObj(t -> (Marker)new HatchMark(t * 360f / 64, t % 4 == 0)).toList();
    private static final String[] CARDINAL_TEXT = { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };
    public static final List<Marker> CARDINAL_MARKS = IntStream.range(0, 8)
            .mapToObj(i -> (Marker)new TextMarker(CARDINAL_TEXT[i], 0xffd0d0d0, i * 45f)).toList();
}

class HatchMark extends Marker {
    protected final boolean large;
    HatchMark(float yaw, boolean large) { super(0x80d0d0d0, yaw); this.large = large; }

    protected static void drawVerticalLineWithShadow(DrawContext ctx, int x, int y1, int y2, int color) {
        ctx.drawVerticalLine(x + 1, y1 + 1, y2 + 1, color & 0xff000000);
        ctx.drawVerticalLine(x, y1, y2, color);
    }
    public void draw(DrawContext ctx, int x, int y) {
        drawVerticalLineWithShadow(ctx, x, y + (large ? 2 : 4), y + 7, color);
    }
}

class TextMarker extends Marker {
    protected String text;
    TextMarker(String text, int color) { this(text, color, 0);}
    TextMarker(String text, int color, float yaw) { super(color, yaw); this.text = text; }

    protected static void drawCenteredText(DrawContext ctx, TextRenderer textRenderer, String text, int centerX, int y, int color) {
        ctx.drawText(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color, false);
    }
    public void draw(DrawContext ctx, int x, int y) {
        if (text != null) ctx.drawCenteredTextWithShadow(client.textRenderer, text, x, y, color);
    }
}

class DistanceMarker extends TextMarker { 
    protected float dist = 0;
    DistanceMarker(String text, int color) { super(text, color); }
    public Marker at(Entity entity, Vec3d pos) {
        super.at(entity, pos);
        this.dist = (float) pos.subtract(entity.getPos()).horizontalLength();
        return this;
    }

    public void draw(DrawContext ctx, int x, int y) {
        super.draw(ctx,x,y);
        if (WaigConfig.getHudPoIMode() == HudPoIMode.DISTANCE) {
            String label;
            if (dist>=10000) label = Math.round(dist/1000) + "k";
            else if (dist>=1000) label = Math.round(dist/100)/10f + "k";
            else label = String.valueOf(Math.round(dist));
            drawCenteredText(ctx,client.textRenderer, label, x, y + 9, color);
        }
    }
}
