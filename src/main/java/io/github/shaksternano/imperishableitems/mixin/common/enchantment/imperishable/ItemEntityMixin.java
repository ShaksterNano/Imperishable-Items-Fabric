package io.github.shaksternano.imperishableitems.mixin.common.enchantment.imperishable;

import io.github.shaksternano.imperishableitems.common.enchantment.ImperishableEnchantment;
import io.github.shaksternano.imperishableitems.common.util.ImperishableBlacklistsHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin extends EntityMixin {

    @Shadow
    private int itemAge;

    @Shadow
    public abstract ItemStack getStack();

    // Items with Imperishable are invulnerable to all damage sources.
    @SuppressWarnings("unused")
    @Override
    protected void imperishableItems$damageImperishable(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (ImperishableBlacklistsHandler.isItemProtected(getStack(), ImperishableBlacklistsHandler.ProtectionType.DAMAGE_PROTECTION)) {
            if (ImperishableEnchantment.hasImperishable(getStack())) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void imperishableItems$checkImperishable(CallbackInfo ci) {
        if (ImperishableEnchantment.hasImperishable(getStack())) {
            // Items with Imperishable don't despawn.
            if (ImperishableBlacklistsHandler.isItemProtected(getStack(), ImperishableBlacklistsHandler.ProtectionType.DESPAWN_PROTECTION)) {
                if (!world.isClient) {
                    if (itemAge >= 1) {
                        itemAge = 0;
                    }
                } else {
                    // itemAge on the client affects an item entity's visual spin, so reset it infrequently.
                    if (itemAge >= 3000) {
                        itemAge = 0;
                    }
                }
            }

            // Items with Imperishable stop falling when they reach the world's minimum Y, and float up to the world's minimum Y if their Y coordinate is below the world's minimum Y.
            if (ImperishableBlacklistsHandler.isItemProtected(getStack(), ImperishableBlacklistsHandler.ProtectionType.VOID_PROTECTION)) {
                if (getY() == world.getBottomY()) {
                    setVelocity(Vec3d.ZERO);
                    setPosition(getX(), world.getBottomY(), getZ());
                    onGround = true;
                } else if (getY() < world.getBottomY()) {

                    Vec3d velocity = getVelocity();
                    setVelocity(velocity.x * 0.97D, velocity.y + velocity.y < 0.06D ? 0.5D : 0.0D, velocity.z * 0.97D);

                    double x = getX() + getVelocity().x;
                    double y = getY() + getVelocity().y;
                    double z = getZ() + getVelocity().z;

                    if (y >= world.getBottomY()) {
                        setVelocity(Vec3d.ZERO);
                        y = world.getBottomY();
                        onGround = true;
                    }

                    setPosition(x, y, z);
                }

                // Set the Item Entity's Y position to 64 blocks below the world's minimum Y position when below 64 blocks below the world's minimum Y position.
                if (getY() < world.getBottomY() - 64.0D) {
                    setPosition(getX(), world.getBottomY() - 64.0D, getZ());
                }
            }
        }
    }

    // Items with Imperishable don't appear on fire when in fire or lava.
    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void imperishableItems$imperishableFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (ImperishableBlacklistsHandler.isItemProtected(getStack(), ImperishableBlacklistsHandler.ProtectionType.DAMAGE_PROTECTION)) {
            if (ImperishableEnchantment.hasImperishable(getStack())) {
                cir.setReturnValue(true);
            }
        }
    }

    // Items with Imperishable don't get removed when 64 blocks below the world's minimum Y position.
    @SuppressWarnings("unused")
    @Override
    protected void imperishableItems$imperishableInVoid(CallbackInfo ci) {
        if (ImperishableBlacklistsHandler.isItemProtected(getStack(), ImperishableBlacklistsHandler.ProtectionType.VOID_PROTECTION)) {
            if (ImperishableEnchantment.hasImperishable(getStack())) {
                ci.cancel();
            }
        }
    }
}
