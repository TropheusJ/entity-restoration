package io.github.tropheusj.entity_restoration;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityRestoration implements ModInitializer {
	public static final String ID = "entity_restoration";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		Commands.init();
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
