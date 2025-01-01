package one.laqua.waig.client.markers;

import java.util.Set;
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

    private static final Set<RegistryEntry<MapDecorationType>> RED_TYPES = Set.of(MapDecorationTypes.RED_X, MapDecorationTypes.RED_MARKER, MapDecorationTypes.TARGET_POINT);

    private static int getColor(RegistryEntry<MapDecorationType> type) {
        int color = type.value().mapColor();
        if (color == -1 && RED_TYPES.contains(type)) color = MapColor.BRIGHT_RED.color;
        return 0xff000000 | color;
    }

    public MapMarker(RegistryEntry<MapDecorationType> type) {
        super(null, getColor(type));
        MapDecoration decoration = new MapDecoration(type, (byte)0, (byte)0, (byte)0, Optional.empty());
        sprite = client.getMapDecorationsAtlasManager().getSprite(decoration);
    }

    public void draw(DrawContext ctx, int x, int y) {
        ctx.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, x-4, y, 8, 8);
        super.draw(ctx,x,y);
    }
}