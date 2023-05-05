package com.github.tatercertified.hide_n_seek.mixin;

import com.github.tatercertified.hide_n_seek.interfaces.ServerPlayerEntityInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static com.github.tatercertified.hide_n_seek.Hide_n_Seek.hiders;
import static com.github.tatercertified.hide_n_seek.Hide_n_Seek.seekers;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity)(Object)this).getHealth() - amount <=0.0f && ((LivingEntity)(Object)this).getType() == EntityType.PLAYER) {
            ServerPlayerEntity killer = null;
            if (!((ServerPlayerEntityInterface)this).isSeeker()) {
                ((ServerPlayerEntity)(Object)this).getInventory().clear();
                hiders.remove((ServerPlayerEntity)(Object)this);
                if (source.getSource().isPlayer()) {
                    UUID uuid = source.getSource().getUuid();
                    killer = source.getSource().getServer().getPlayerManager().getPlayer(uuid);
                    if (((ServerPlayerEntityInterface)killer).isSeeker()) {
                        ((ServerPlayerEntityInterface)killer).setScore(((ServerPlayerEntityInterface)killer).getScore() + 1);
                    }
                }
            } else {
                ((ServerPlayerEntityInterface)this).setSeeker(false);
                seekers.remove((ServerPlayerEntity)(Object)this);
            }
            ((ServerPlayerEntity)(Object)this).changeGameMode(GameMode.SPECTATOR);
            if (killer != null) {
            source.getSource().getServer().getPlayerManager().broadcast(Text.literal(((ServerPlayerEntity)(Object)this).getDisplayName().getString() + " has been found by " + killer.getDisplayName().getString()), false);
            }
            cir.setReturnValue(false);
        }
    }
}
