package com.github.tatercertified.hide_n_seek.mixin;

public interface ServerPlayerEntityInterface {

    boolean isSeeker();

    void setSeeker(boolean seeker);

    int getScore();

    void setScore(int score);

    int getHeartBeatTicks();

    void setHeartBeatTicks(int ticks);
}
