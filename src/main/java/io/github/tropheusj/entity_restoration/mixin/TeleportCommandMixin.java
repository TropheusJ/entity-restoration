package io.github.tropheusj.entity_restoration.mixin;

import java.util.Collection;
import java.util.UUID;

import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.server.commands.TeleportCommand;

import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.tropheusj.entity_restoration.EntityRestoration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {
	@Inject(method = "teleportToEntity", at = @At("HEAD"), cancellable = true)
	private static void entity_restoration$checkTpEntity(CommandSourceStack source, Collection<? extends Entity> targets, Entity destination, CallbackInfoReturnable<Integer> cir) {
		entity_restoration$checkTp(source, targets, cir);
	}

	@Inject(method = "teleportToPos", at = @At("HEAD"), cancellable = true)
	private static void entity_restoration$checkTpPos(CommandSourceStack source, Collection<? extends Entity> targets, ServerLevel level, Coordinates position, @Nullable Coordinates rotation, @Coerce Object facing, CallbackInfoReturnable<Integer> cir) {
		entity_restoration$checkTp(source, targets, cir);
	}

	/**
	 * @return true if tp should succeed
	 */
	@Unique
	private static boolean entity_restoration$checkTp(CommandSourceStack source, Collection<? extends Entity> targets, CallbackInfoReturnable<Integer> cir) {
		Entity sender = source.getEntity();
		int targetCount = targets.size();
		if (sender instanceof ServerPlayer player && targetCount != 1) {
			UUID uuid = player.getUUID();
			boolean firstTry = !EntityRestoration.PLAYERS_TELEPORTING.containsKey(uuid);
			boolean secondTry = !firstTry && !EntityRestoration.PLAYERS_TELEPORTING.get(uuid);
			if (firstTry) {
				EntityRestoration.PLAYERS_TELEPORTING.put(uuid, false);
				source.sendFailure(new TextComponent("Are you sure you want to teleport " + targetCount + " entities? repeat this command to confirm."));
				cir.setReturnValue(0);
			} else if (secondTry) {
				EntityRestoration.PLAYERS_TELEPORTING.put(uuid, true);
				source.sendFailure(new TextComponent("Are you REALLY sure you want to teleport " + targetCount +
						" entities? This can't be undone! repeat this command to confirm."));
				cir.setReturnValue(0);
			} else {
				EntityRestoration.PLAYERS_TELEPORTING.remove(uuid);
				return true;
			}
			return false;
		}
		return true;
	}
}
