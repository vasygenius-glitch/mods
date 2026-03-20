package com.example.xaeroaddon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import xaero.map.gui.GuiMap;
import xaero.map.MapProcessor;

@Mixin(GuiMap.class)
public interface GuiMapAccessor {
    @Accessor("mapProcessor")
    MapProcessor getMapProcessor();
}
