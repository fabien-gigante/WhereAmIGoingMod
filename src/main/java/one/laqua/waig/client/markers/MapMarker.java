package one.laqua.waig.client.markers;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;    
import java.util.Optional;

import net.minecraft.block.MapColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.registry.entry.RegistryEntry;

public class MapMarker extends DistanceMarker { 
    protected Sprite sprite = null;

    private static final Set<RegistryEntry<MapDecorationType>> SECONDARY = Set.of(
        MapDecorationTypes.FRAME, MapDecorationTypes.BANNER_WHITE, MapDecorationTypes.BANNER_ORANGE, MapDecorationTypes.BANNER_MAGENTA, MapDecorationTypes.BANNER_LIGHT_BLUE,  MapDecorationTypes.BANNER_YELLOW,  MapDecorationTypes.BANNER_LIME,  MapDecorationTypes.BANNER_PINK, MapDecorationTypes.BANNER_GRAY,  MapDecorationTypes.BANNER_LIGHT_GRAY,  MapDecorationTypes.BANNER_CYAN,  MapDecorationTypes.BANNER_PURPLE,  MapDecorationTypes.BANNER_BLUE,  MapDecorationTypes.BANNER_BROWN,  MapDecorationTypes.BANNER_GREEN,  MapDecorationTypes.BANNER_RED,  MapDecorationTypes.BANNER_BLACK);

    private static final Map<RegistryEntry<MapDecorationType>, Integer> COLORS = Map.ofEntries(
        entry(MapDecorationTypes.RED_X,             MapColor.BRIGHT_RED.color),
        entry(MapDecorationTypes.RED_MARKER,        MapColor.BRIGHT_RED.color),
        entry(MapDecorationTypes.BLUE_MARKER,       MapColor.BLUE.color),
        entry(MapDecorationTypes.TARGET_POINT,      MapColor.BRIGHT_RED.color),
        entry(MapDecorationTypes.TARGET_X,          MapColor.WHITE.color),
        entry(MapDecorationTypes.FRAME,             MapColor.GREEN.color),
        entry(MapDecorationTypes.BANNER_WHITE,      MapColor.WHITE.color), 
        entry(MapDecorationTypes.BANNER_ORANGE,     MapColor.ORANGE.color), 
        entry(MapDecorationTypes.BANNER_MAGENTA,    MapColor.MAGENTA.color),
        entry(MapDecorationTypes.BANNER_LIGHT_BLUE, MapColor.LIGHT_BLUE.color),
        entry(MapDecorationTypes.BANNER_YELLOW,     MapColor.YELLOW.color),
        entry(MapDecorationTypes.BANNER_LIME,       MapColor.LIME.color),
        entry(MapDecorationTypes.BANNER_PINK,       MapColor.PINK.color),
        entry(MapDecorationTypes.BANNER_GRAY,       MapColor.GRAY.color),
        entry(MapDecorationTypes.BANNER_LIGHT_GRAY, MapColor.LIGHT_GRAY.color),
        entry(MapDecorationTypes.BANNER_CYAN,       MapColor.CYAN.color),
        entry(MapDecorationTypes.BANNER_PURPLE,     MapColor.PURPLE.color),
        entry(MapDecorationTypes.BANNER_BLUE,       MapColor.BLUE.color),
        entry(MapDecorationTypes.BANNER_BROWN,      MapColor.BROWN.color),
        entry(MapDecorationTypes.BANNER_GREEN,      MapColor.GREEN.color),
        entry(MapDecorationTypes.BANNER_RED,        MapColor.RED.color),
        entry(MapDecorationTypes.BANNER_BLACK,      MapColor.BLACK.color));

    public MapMarker(MapDecoration decoration) {
        super(null, getColor(decoration.type()));
        sprite = client.getMapDecorationsAtlasManager().getSprite(decoration);
    }
    public MapMarker(RegistryEntry<MapDecorationType> type) {
        this(new MapDecoration(type, (byte)0, (byte)0, (byte)0, Optional.empty()));
    }

    public static boolean isSecondary(RegistryEntry<MapDecorationType> type) {
        return SECONDARY.contains(type);
    }
    private static int getColor(RegistryEntry<MapDecorationType> type) {
        int color = type.value().mapColor();
        if (color == -1) color = COLORS.getOrDefault(type, -1);
        return 0xff000000 | color;
    }

    public void draw(DrawContext ctx, int x, int y) {
        ctx.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, x-4, y, 8, 8);
        super.draw(ctx,x,y);
    }
}