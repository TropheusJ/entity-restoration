package io.github.tropheusj.entity_restoration;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

import static net.minecraft.commands.Commands.literal;

public class Commands {
	public static final Component NOTHING_TO_LOAD = new TextComponent("No 'entity_restoration.nbt' file was found to load from!");

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(literal("entityRestoration")
					.requires(source -> source.hasPermission(2))
					.then(literal("save")
							.executes(Commands::save))
					.then(literal("load")
							.executes(Commands::load)));
		});
	}

	public static int save(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		CompoundTag allData = new CompoundTag();
		int i = 0;
		for (Entity entity : level.getAllEntities()) {
			if (entity.isRemoved())
				continue;
			CompoundTag nbt = new CompoundTag();
			entity.save(nbt);
			allData.put("entity" + i, nbt);
			i++;
		}
		allData.putInt("entities", i);
		File out = FabricLoader.getInstance().getGameDir().resolve("entity_restoration.nbt").toFile();
		try {
			out.createNewFile();
			NbtIo.write(allData, out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		source.sendSuccess(new TextComponent("Successfully saved " + i + " entities."), true);
		return 0;
	}

	public static int load(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		File data = FabricLoader.getInstance().getGameDir().resolve("entity_restoration.nbt").toFile();
		if (!data.exists()) {
			source.sendFailure(NOTHING_TO_LOAD);
			return 0;
		}
		try {
			CompoundTag allData = NbtIo.read(data);
			int entities = allData.getInt("entities");
			int success = 0;
			for (int i = 0; i < entities; i++) {
				CompoundTag entity = allData.getCompound("entity" + i);
				Entity loaded = EntityType.loadEntityRecursive(entity, level, x -> x);
				if (loaded == null)
					continue;
				level.addFreshEntity(loaded);
				success++;
			}
			source.sendSuccess(new TextComponent("Successfully loaded " + success + " entities out of " + entities), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return 0;
	}
}
