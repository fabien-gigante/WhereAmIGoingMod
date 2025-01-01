package one.laqua.waig.client.markers;

import java.util.Optional;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.entry.RegistryEntry;

public class MapMarker extends DistanceMarker { 
    protected Sprite sprite = null;

    public MapMarker(RegistryEntry<MapDecorationType> type) {
        super(null, 0xff000000 | type.value().mapColor());
        MapDecoration decoration = new MapDecoration(type, (byte)0, (byte)0, (byte)0, Optional.empty());
        sprite = client.getMapDecorationsAtlasManager().getSprite(decoration);
    }

    public void draw(DrawContext ctx, int x, int y) {
        ctx.drawSpriteStretched(RenderLayer::getGuiTexturedOverlay, sprite, x-4, y, 8, 8);
        super.draw(ctx,x,y);
    }
}