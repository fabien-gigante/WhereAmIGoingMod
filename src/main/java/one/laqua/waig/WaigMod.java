package one.laqua.waig;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WaigMod implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "waig";
    public static final String MOD_NAME = "Where Am I Going";

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        HudRenderCallback.EVENT.register(CompassHud::onHudRender);
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}
