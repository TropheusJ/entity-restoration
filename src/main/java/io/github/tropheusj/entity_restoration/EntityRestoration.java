package io.github.tropheusj.entity_restoration;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityRestoration implements ModInitializer {
	public static final String ID = "entity_restoration";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static final Map<UUID, Boolean> PLAYERS_KILLING = new HashMap<>();
	public static final Map<UUID, Boolean> PLAYERS_TELEPORTING = new HashMap<>();

	@Override
	public void onInitialize() {
		Commands.init();
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			UUID uuid = handler.getPlayer().getUUID();
			PLAYERS_KILLING.remove(uuid);
			PLAYERS_TELEPORTING.remove(uuid);
		});
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
