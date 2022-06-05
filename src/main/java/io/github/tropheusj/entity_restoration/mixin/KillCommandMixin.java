package io.github.tropheusj.entity_restoration.mixin;

import io.github.tropheusj.entity_restoration.EntityRestoration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.commands.KillCommand;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.UUID;

@Mixin(KillCommand.class)
public class KillCommandMixin {
	@Inject(method = "kill", at = @At("HEAD"), cancellable = true)
	private static void entity_restoration$checkKill(CommandSourceStack source, Collection<? extends Entity> targets, CallbackInfoReturnable<Integer> cir) {
		Entity sender = source.getEntity();
		if (sender instanceof ServerPlayer player) {
			UUID uuid = player.getUUID();
			boolean firstTry = !EntityRestoration.PLAYERS_KILLING.containsKey(uuid);
			boolean secondTry = !firstTry && !EntityRestoration.PLAYERS_KILLING.get(uuid);
			if (firstTry) {
				EntityRestoration.PLAYERS_KILLING.put(uuid, false);
				source.sendFailure(new TextComponent("Are you sure you want to kill " + targets.size() + " entities? repeat this command to confirm."));
				cir.setReturnValue(0);
			} else if (secondTry) {
				EntityRestoration.PLAYERS_KILLING.put(uuid, true);
				source.sendFailure(new TextComponent("Are you REALLY sure you want to kill " + targets.size() +
						" entities? This can't be undone! repeat this command to confirm."));
				cir.setReturnValue(0);
			} else {
				EntityRestoration.PLAYERS_KILLING.remove(uuid);
			}
		}
	}
}
