package one.laqua.waig.client.markers;

import java.util.Set;

import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;

public class MapMarker extends DistanceMarker { 
    static final Set<RegistryEntry<MapDecorationType>> MAP_X = Set.of(MapDecorationTypes.RED_X, MapDecorationTypes.TARGET_X, MapDecorationTypes.TARGET_POINT);
    static final Set<RegistryEntry<MapDecorationType>> MAP_VILLAGE = Set.of(MapDecorationTypes.VILLAGE_DESERT, MapDecorationTypes.VILLAGE_PLAINS, MapDecorationTypes.VILLAGE_SAVANNA, MapDecorationTypes.VILLAGE_SNOWY, MapDecorationTypes.VILLAGE_TAIGA);
    static final Set<RegistryEntry<MapDecorationType>> MAP_RUINS = Set.of(MapDecorationTypes.MANSION, MapDecorationTypes.MONUMENT, MapDecorationTypes.JUNGLE_TEMPLE, MapDecorationTypes.SWAMP_HUT, MapDecorationTypes.TRIAL_CHAMBERS);

    public MapMarker() { super("◇", 0xff7f7f7f); }
    public MapMarker(RegistryEntry<MapDecorationType> type) {
        this();
        if (type instanceof Reference<MapDecorationType> map) color = 0xff000000 | map.value().mapColor();
        if (MAP_X.contains(type)) { text= "✕"; color = 0xffff0000; } else if (MAP_VILLAGE.contains(type)) text= "⛺︎"; else if (MAP_RUINS.contains(type)) text= "◆";
    } 
}