package com.github.tatercertified.hide_n_seek.mixin;

import com.github.tatercertified.hide_n_seek.Hide_n_Seek;
import com.github.tatercertified.hide_n_seek.interfaces.ServerPlayerEntityInterface;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static com.github.tatercertified.hide_n_seek.Hide_n_Seek.hiders;
import static com.github.tatercertified.hide_n_seek.Hide_n_Seek.seekers;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityInterface {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract void playSound(SoundEvent event, SoundCategory category, float volume, float pitch);

    @Shadow public abstract boolean changeGameMode(GameMode gameMode);


    public boolean isSeeker;
    public int seeker_score;
    private int heartbeat_ticks;

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (!((ServerPlayerEntityInterface)this).isSeeker()) {
            this.getInventory().clear();
            hiders.remove((ServerPlayerEntity)(Object)this);
            if (damageSource.getSource().isPlayer()) {
                UUID uuid = damageSource.getSource().getUuid();
                ServerPlayerEntity killer = damageSource.getSource().getServer().getPlayerManager().getPlayer(uuid);
                if (((ServerPlayerEntityInterface)killer).isSeeker()) {
                    ((ServerPlayerEntityInterface)killer).setScore(((ServerPlayerEntityInterface)killer).getScore() + 1);
                }
            }
        } else {
            seekers.remove((ServerPlayerEntity)(Object)this);
        }
        this.changeGameMode(GameMode.SPECTATOR);
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (!isSeeker && Hide_n_Seek.getCurrentGameTime() > 0) {
            playHeartBeat();
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getSource().isPlayer() && ((ServerPlayerEntityInterface)source.getSource()).isSeeker() == ((ServerPlayerEntityInterface)this).isSeeker()) {
            cir.setReturnValue(false);
        }
    }


    @Override
    public boolean isSeeker() {
        return isSeeker;
    }

    @Override
    public void setSeeker(boolean seeker) {
        this.isSeeker = seeker;
    }

    @Override
    public int getScore() {
        return seeker_score;
    }

    @Override
    public void setScore(int score) {
        seeker_score = score;
    }

    @Override
    public int getHeartBeatTicks() {
        return heartbeat_ticks;
    }

    @Override
    public void setHeartBeatTicks(int ticks) {
        heartbeat_ticks = ticks;
    }

    private int closestSeekers() {
        int final_distance = Integer.MAX_VALUE;
        for (ServerPlayerEntity player : seekers) {
          float distance = player.distanceTo(((ServerPlayerEntity)(Object)this));
          if (distance < final_distance) {
              final_distance = Math.round(distance);
          }
        }
        return final_distance;
    }

    private void playHeartBeat() {
        if (((ServerPlayerEntityInterface)this).getHeartBeatTicks() == 0) {
            int distance = closestSeekers();
            if (distance <= 10) {
                float percentage_close = ((float)distance / 10);
                this.playSound(SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1F, 1F);
                if (percentage_close == 0F) {
                    percentage_close = 0.1F;
                }
                ((ServerPlayerEntityInterface)this).setHeartBeatTicks(Math.round(120 * percentage_close));
            }
        } else {
            ((ServerPlayerEntityInterface)this).setHeartBeatTicks(getHeartBeatTicks()-1);
        }
    }
}
