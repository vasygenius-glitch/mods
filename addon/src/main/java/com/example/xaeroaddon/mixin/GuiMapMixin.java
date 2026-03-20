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
import net.minecraft.util.math.BlockPos;

@Mixin(GuiMap.class)
public abstract class GuiMapMixin extends ScreenBase {

    private boolean warnedChunkGen = false;

    protected GuiMapMixin(net.minecraft.client.gui.screen.Screen parent, net.minecraft.client.gui.screen.Screen escape, Text title) {
        super(parent, escape, title);
    }

    @Inject(method = "method_25426", at = @At("TAIL"), remap = false)
    private void addDimensionArrows(CallbackInfo ci) {
        GuiMap self = (GuiMap) (Object) this;

        int w = this.width;
        int h = this.height;

        // Features 1 & 2: Dimension Switchers
        ButtonWidget prevDimButton = ButtonWidget.builder(Text.literal("<"), button -> {
            cycleDimension(self, -1);
        }).dimensions(w - 70, 5, 20, 20).build();

        ButtonWidget nextDimButton = ButtonWidget.builder(Text.literal(">"), button -> {
            cycleDimension(self, 1);
        }).dimensions(w - 25, 5, 20, 20).build();

        // Feature 3: Chunk Generation
        ButtonWidget genChunksButton = ButtonWidget.builder(Text.translatable("gui.xaeroaddon.generate_chunks"), button -> {
            generateChunks();
        }).dimensions(w - 90, 30, 85, 20).build();

        // Feature 4: Teleport to Spawn
        ButtonWidget tpSpawnButton = ButtonWidget.builder(Text.literal("TP Spawn"), button -> {
            teleportToSpawn();
        }).dimensions(5, 5, 60, 20).build();

        // Feature 5: Teleport to 0,0
        ButtonWidget tpZeroButton = ButtonWidget.builder(Text.literal("TP 0,0"), button -> {
            teleportToZero();
        }).dimensions(5, 30, 60, 20).build();

        // Feature 6: Share coords in chat
        ButtonWidget shareCoordsButton = ButtonWidget.builder(Text.literal("Share Pos"), button -> {
            shareCoords();
        }).dimensions(5, 55, 60, 20).build();

        // Feature 7: Heal (Op)
        ButtonWidget healButton = ButtonWidget.builder(Text.literal("Heal"), button -> {
            healPlayer();
        }).dimensions(5, 80, 60, 20).build();

        // Feature 8: Change Gamemode
        ButtonWidget gmButton = ButtonWidget.builder(Text.literal("GMode"), button -> {
            switchGamemode();
        }).dimensions(5, 105, 60, 20).build();

        // Feature 9: Time Day
        ButtonWidget dayButton = ButtonWidget.builder(Text.literal("Day"), button -> {
            setTimeDay();
        }).dimensions(5, 130, 60, 20).build();

        // Feature 10: Weather Clear
        ButtonWidget clearButton = ButtonWidget.builder(Text.literal("Clear"), button -> {
            setWeatherClear();
        }).dimensions(5, 155, 60, 20).build();

        // Feature 11: Refresh map via teleport up and down
        ButtonWidget refreshButton = ButtonWidget.builder(Text.literal("Refresh"), button -> {
            refreshMap();
        }).dimensions(w - 90, 55, 85, 20).build();

        this.addDrawableChild(prevDimButton);
        this.addDrawableChild(nextDimButton);
        this.addDrawableChild(genChunksButton);
        this.addDrawableChild(tpSpawnButton);
        this.addDrawableChild(tpZeroButton);
        this.addDrawableChild(shareCoordsButton);
        this.addDrawableChild(healButton);
        this.addDrawableChild(gmButton);
        this.addDrawableChild(dayButton);
        this.addDrawableChild(clearButton);
        this.addDrawableChild(refreshButton);
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

            screen.method_25426();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateChunks() {
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

    private void teleportToSpawn() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
             mc.player.networkHandler.sendCommand("spawn");
             mc.player.networkHandler.sendCommand("execute as @s in overworld run tp @s 0 100 0");
             mc.player.sendMessage(Text.literal("Teleporting to spawn..."), false);
        }
    }

    private void teleportToZero() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hasPermissionLevel(2)) {
             mc.player.networkHandler.sendCommand("tp @s 0 100 0");
        } else if (mc.player != null) {
             mc.player.sendMessage(Text.translatable("message.xaeroaddon.no_permission"), false);
        }
    }

    private void shareCoords() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
             BlockPos pos = mc.player.getBlockPos();
             String msg = String.format("I am at X: %d, Y: %d, Z: %d", pos.getX(), pos.getY(), pos.getZ());
             mc.player.networkHandler.sendChatMessage(msg);
        }
    }

    private void healPlayer() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hasPermissionLevel(2)) {
             mc.player.networkHandler.sendCommand("effect give @s instant_health 1 255");
             mc.player.networkHandler.sendCommand("effect give @s saturation 1 255");
        }
    }

    private void switchGamemode() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hasPermissionLevel(2)) {
             if (mc.interactionManager != null && mc.interactionManager.getCurrentGameMode().isCreative()) {
                 mc.player.networkHandler.sendCommand("gamemode survival");
             } else {
                 mc.player.networkHandler.sendCommand("gamemode creative");
             }
        }
    }

    private void setTimeDay() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hasPermissionLevel(2)) {
             mc.player.networkHandler.sendCommand("time set day");
        }
    }

    private void setWeatherClear() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hasPermissionLevel(2)) {
             mc.player.networkHandler.sendCommand("weather clear");
        }
    }

    private void refreshMap() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hasPermissionLevel(2)) {
             mc.player.networkHandler.sendCommand("tp @s ~ ~5 ~");
        }
    }
}
