package com.github.tatercertified.hide_n_seek.mixin;

import com.github.tatercertified.hide_n_seek.Hide_n_Seek;
import com.github.tatercertified.hide_n_seek.command.HideNSeekCommand;
import com.github.tatercertified.hide_n_seek.interfaces.ServerPlayerEntityInterface;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityInterface {

    @Shadow public abstract void playSound(SoundEvent event, SoundCategory category, float volume, float pitch);

    @Shadow public abstract boolean changeGameMode(GameMode gameMode);

    @Shadow public abstract void sendMessage(Text message);

    public boolean isSeeker;
    public int seeker_score;
    private int heartbeat_ticks;

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (!((ServerPlayerEntityInterface)this).isSeeker()) {
            if (damageSource.getSource().isPlayer()) {
                UUID uuid = damageSource.getSource().getUuid();
                ServerPlayerEntity killer = damageSource.getSource().getServer().getPlayerManager().getPlayer(uuid);
                if (((ServerPlayerEntityInterface)killer).isSeeker()) {
                    ((ServerPlayerEntityInterface)killer).setScore(((ServerPlayerEntityInterface)killer).getScore() + 1);
                }
            }
        } else {
            HideNSeekCommand.seekers.remove((ServerPlayerEntity)(Object)this);
        }
        this.changeGameMode(GameMode.SPECTATOR);
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (Hide_n_Seek.getCurrentGameTime() >=0) {
            playHeartBeat();
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
        for (ServerPlayerEntity player : HideNSeekCommand.seekers) {
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
                this.sendMessage(Text.literal(String.valueOf(percentage_close)));
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
