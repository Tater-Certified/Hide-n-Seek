package com.github.tatercertified.hide_n_seek.mixin;

import net.minecraft.util.math.BlockPos;

public interface MinecraftServerInterface {
    int getCurrentGameTime();

    void setCurrentGameTime(int time);

    int getDuration();

    void setDuration(int duration);

    int getTimeLeft();

    void setTimeLeft(int time);

    void setCountdown(int countdown);

    void reset();

    void releaseSeekers();

    void setMapTeleport(BlockPos pos);

    void setLobbyTeleport(BlockPos pos);
}
