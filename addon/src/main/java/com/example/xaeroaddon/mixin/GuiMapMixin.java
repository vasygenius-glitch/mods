package com.example.xaeroaddon.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;

import xaero.map.gui.GuiMap;
import xaero.map.MapProcessor;
import xaero.map.world.MapWorld;
import xaero.map.world.MapDimension;
import xaero.lib.client.gui.ScreenBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(GuiMap.class)
public abstract class GuiMapMixin extends ScreenBase {

    private boolean warnedChunkGen = false;

    protected GuiMapMixin(net.minecraft.client.gui.screen.Screen parent, net.minecraft.client.gui.screen.Screen escape, Text title) {
        super(parent, escape, title);
    }

    // Remap = false since method_25426 is exactly what it's named in the jar
    @Inject(method = "method_25426", at = @At("TAIL"), remap = false)
    private void addDimensionArrows(CallbackInfo ci) {
        GuiMap self = (GuiMap) (Object) this;

        int w = this.width;

        ButtonWidget prevDimButton = ButtonWidget.builder(Text.literal("<"), button -> {
            cycleDimension(self, -1);
        }).dimensions(w - 70, 5, 20, 20).build();

        ButtonWidget nextDimButton = ButtonWidget.builder(Text.literal(">"), button -> {
            cycleDimension(self, 1);
        }).dimensions(w - 25, 5, 20, 20).build();

        ButtonWidget genChunksButton = ButtonWidget.builder(Text.translatable("gui.xaeroaddon.generate_chunks"), button -> {
            generateChunks(self);
        }).dimensions(w - 90, 30, 85, 20).build();

        this.addDrawableChild(prevDimButton);
        this.addDrawableChild(nextDimButton);
        this.addDrawableChild(genChunksButton);
    }

    private void cycleDimension(GuiMap screen, int dir) {
        try {
            MapProcessor processor = ((GuiMapAccessor) screen).getMapProcessor();
            if (processor == null) return;

            MapWorld world = processor.getMapWorld();
            if (world == null) return;

            MapDimension currentDim = world.getCurrentDimension();

            List<MapDimension> dims = new ArrayList<>();
            world.getDimensions(dims);

            if (dims.isEmpty()) return;

            RegistryKey<World> currentKey = currentDim != null ? currentDim.getDimId() : null;
            int currentIndex = -1;
            for (int i = 0; i < dims.size(); i++) {
                if (dims.get(i).getDimId() == currentKey) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex == -1) currentIndex = 0;

            int nextIndex = (currentIndex + dir) % dims.size();
            if (nextIndex < 0) nextIndex += dims.size();

            RegistryKey<World> nextKey = dims.get(nextIndex).getDimId();
            world.setCustomDimensionId(nextKey);

            screen.method_25426(); // Call init() directly as method_25426()
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateChunks(GuiMap screen) {
        if (!warnedChunkGen) {
            MinecraftClient.getInstance().player.sendMessage(Text.translatable("message.xaeroaddon.warning_admin"), false);
            warnedChunkGen = true;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hasPermissionLevel(2)) {
            int pX = mc.player.getBlockPos().getX();
            int pZ = mc.player.getBlockPos().getZ();
            String cmd = String.format("forceload add %d %d %d %d", pX - 32, pZ - 32, pX + 32, pZ + 32);
            mc.player.networkHandler.sendCommand(cmd);
            mc.player.sendMessage(Text.translatable("message.xaeroaddon.chunks_generating"), false);
            mc.player.sendMessage(Text.literal("§cRemember to '/forceload remove all' when done!§r"), false);
        } else if (mc.player != null) {
            mc.player.sendMessage(Text.translatable("message.xaeroaddon.no_permission"), false);
        }
    }
}
