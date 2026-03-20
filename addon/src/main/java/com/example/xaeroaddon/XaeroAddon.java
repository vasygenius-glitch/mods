package com.example.xaeroaddon;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XaeroAddon implements ModInitializer {
    public static final String MOD_ID = "xaeroaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Xaero's World Map Addon Initialized!");
    }
}
