package com.github.tatercertified.hide_n_seek.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.tatercertified.hide_n_seek.Hide_n_Seek.hiders;

@Mixin(CompassItem.class)
public class CompassMixin extends Item {

    public CompassMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CompassItem;hasLodestone(Lnet/minecraft/item/ItemStack;)Z", shift = At.Shift.BEFORE))
    private void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (stack.getNbt() != null && stack.getNbt().getBoolean("tracker")) {
            NbtCompound pos = new NbtCompound();
            ServerPlayerEntity closest = closestHider((ServerPlayerEntity) entity);
            pos.putInt("X", closest.getBlockX());
            pos.putInt("Y", closest.getBlockY());
            pos.putInt("Z", closest.getBlockZ());
            stack.getOrCreateNbt().put("LodestonePos", pos);
            stack.getOrCreateNbt().putBoolean("LodestoneTracked", true);
        }
    }

    private ServerPlayerEntity closestHider(ServerPlayerEntity seeker) {
        int final_distance = Integer.MAX_VALUE;
        ServerPlayerEntity closest = null;
        for (ServerPlayerEntity player : hiders) {
            float distance = player.distanceTo(seeker);
            if (distance < final_distance) {
                final_distance = Math.round(distance);
                closest = player;
            }
        }
        return closest;
    }
}
