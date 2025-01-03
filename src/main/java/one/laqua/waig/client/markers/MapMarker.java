package one.laqua.waig.client.markers;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Map;
import java.util.Optional;

import net.minecraft.block.MapColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.component.type.MapDecorationsComponent.Decoration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;

public class MapMarker extends TargetMarker { 
    protected Sprite sprite = null;

    private static final Set<RegistryEntry<MapDecorationType>> SECONDARY = Set.of(
        MapDecorationTypes.FRAME, MapDecorationTypes.BANNER_WHITE, MapDecorationTypes.BANNER_ORANGE, MapDecorationTypes.BANNER_MAGENTA, MapDecorationTypes.BANNER_LIGHT_BLUE,  MapDecorationTypes.BANNER_YELLOW,  MapDecorationTypes.BANNER_LIME,  MapDecorationTypes.BANNER_PINK, MapDecorationTypes.BANNER_GRAY,  MapDecorationTypes.BANNER_LIGHT_GRAY,  MapDecorationTypes.BANNER_CYAN,  MapDecorationTypes.BANNER_PURPLE,  MapDecorationTypes.BANNER_BLUE,  MapDecorationTypes.BANNER_BROWN,  MapDecorationTypes.BANNER_GREEN,  MapDecorationTypes.BANNER_RED,  MapDecorationTypes.BANNER_BLACK);

    private static final Map<RegistryEntry<MapDecorationType>, Integer> COLORS = Map.ofEntries(
        Map.entry(MapDecorationTypes.RED_X,             MapColor.BRIGHT_RED.color),
        Map.entry(MapDecorationTypes.RED_MARKER,        MapColor.BRIGHT_RED.color),
        Map.entry(MapDecorationTypes.BLUE_MARKER,       MapColor.BLUE.color),
        Map.entry(MapDecorationTypes.TARGET_POINT,      MapColor.BRIGHT_RED.color),
        Map.entry(MapDecorationTypes.TARGET_X,          MapColor.WHITE.color),
        Map.entry(MapDecorationTypes.FRAME,             MapColor.GREEN.color),
        Map.entry(MapDecorationTypes.BANNER_WHITE,      MapColor.WHITE.color), 
        Map.entry(MapDecorationTypes.BANNER_ORANGE,     MapColor.ORANGE.color), 
        Map.entry(MapDecorationTypes.BANNER_MAGENTA,    MapColor.MAGENTA.color),
        Map.entry(MapDecorationTypes.BANNER_LIGHT_BLUE, MapColor.LIGHT_BLUE.color),
        Map.entry(MapDecorationTypes.BANNER_YELLOW,     MapColor.YELLOW.color),
        Map.entry(MapDecorationTypes.BANNER_LIME,       MapColor.LIME.color),
        Map.entry(MapDecorationTypes.BANNER_PINK,       MapColor.PINK.color),
        Map.entry(MapDecorationTypes.BANNER_GRAY,       MapColor.GRAY.color),
        Map.entry(MapDecorationTypes.BANNER_LIGHT_GRAY, MapColor.LIGHT_GRAY.color),
        Map.entry(MapDecorationTypes.BANNER_CYAN,       MapColor.CYAN.color),
        Map.entry(MapDecorationTypes.BANNER_PURPLE,     MapColor.PURPLE.color),
        Map.entry(MapDecorationTypes.BANNER_BLUE,       MapColor.BLUE.color),
        Map.entry(MapDecorationTypes.BANNER_BROWN,      MapColor.BROWN.color),
        Map.entry(MapDecorationTypes.BANNER_GREEN,      MapColor.GREEN.color),
        Map.entry(MapDecorationTypes.BANNER_RED,        MapColor.RED.color),
        Map.entry(MapDecorationTypes.BANNER_BLACK,      MapColor.BLACK.color));

    protected MapMarker(MapDecoration decoration) {
        super(getColor(decoration.type()));
        sprite = client.getMapDecorationsAtlasManager().getSprite(decoration);
    }
    protected MapMarker(PlayerEntity player, MapState mapState, Decoration decoration) {
        this(new MapDecoration(decoration.type(), (byte)0, (byte)0, (byte)0, Optional.empty())); 
        move(player, new Vec3d(decoration.x(),0,decoration.z()), mapState.dimension);
    }
    protected MapMarker(PlayerEntity player, MapState mapState, MapDecoration decoration) {
        this(decoration);
        int scale = 1 << mapState.scale;
        double x = (decoration.x() - 0.5f) / 2f * scale + mapState.centerX;
        double z = (decoration.z() - 0.5f) / 2f * scale + mapState.centerZ;
        move(player, new Vec3d(x,0,z), mapState.dimension);
    }

    public static Stream<MapMarker> enumerate(PlayerEntity player, ItemStack map) {
        MapState mapState = FilledMapItem.getMapState(map, client.world);
        if (mapState.dimension != player.getWorld().getRegistryKey()) return Stream.empty();
        MapDecorationsComponent mapDecorationsComponent = map.getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT);
        return Stream.concat(
            // Use component when possible to accurately retrieve the main decoration positions
            mapDecorationsComponent.decorations().values().stream().map(deco -> new MapMarker(player, mapState, deco)),
            // Use map state to approximate secondary decoration positions in world from their placement on the map
            StreamSupport.stream(mapState.getDecorations().spliterator(),false).filter(MapMarker::isSecondary).map(deco -> new MapMarker(player, mapState, deco))
        );
    }

    protected static boolean isSecondary(MapDecoration decoration) {
        return SECONDARY.contains(decoration.type());
    }
    protected static int getColor(RegistryEntry<MapDecorationType> type) {
        int color = type.value().mapColor();
        if (color == -1) color = COLORS.getOrDefault(type, -1);
        return 0xff000000 | color;
    }

    public void draw(DrawContext ctx, int x, int y) {
        ctx.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, x-4, y, 8, 8);
        super.draw(ctx,x,y);
    }
}