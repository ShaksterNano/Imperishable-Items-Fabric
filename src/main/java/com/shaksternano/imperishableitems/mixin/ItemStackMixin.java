package com.shaksternano.imperishableitems.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.shaksternano.imperishableitems.ImperishableItems;
import com.shaksternano.imperishableitems.registry.ModEnchantments;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    private ItemStackMixin() {}

    @Shadow public abstract void setDamage(int damage);

    @Shadow public abstract int getMaxDamage();

    @Shadow public abstract Item getItem();

    @Shadow public abstract int getDamage();

    @Shadow public abstract boolean isDamageable();

    @Inject(method = "damage(ILjava/util/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void imperishableDurability(int amount, Random random, @Nullable ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir, int i) {
        if (ImperishableItems.config.imperishablePreventsBreaking) {
            if (!(getItem() instanceof ElytraItem)) {
                if (isDamageable()) {
                    ItemStack stack = (ItemStack) (Object) this;

                    if (EnchantmentHelper.getLevel(ModEnchantments.IMPERISHABLE, stack) > 0) {
                        if (i > getMaxDamage()) {
                            setDamage(getMaxDamage());
                        } else {
                            if (player != null) {
                                if (getDamage() < getMaxDamage() && i == getMaxDamage()) {
                                    player.world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, player.getSoundCategory(), 0.8F, 0.8F + player.world.random.nextFloat() * 0.4F);
                                }
                            }

                            setDamage(i);
                        }

                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }

    @Inject(method = "isSuitableFor", at = @At("HEAD"), cancellable = true)
    private void imperishableSuitableFor(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (ImperishableItems.config.imperishablePreventsBreaking) {
            if (isDamageable()) {
                ItemStack stack = (ItemStack) (Object) this;

                if (EnchantmentHelper.getLevel(ModEnchantments.IMPERISHABLE, stack) > 0) {
                    if (stack.getDamage() >= stack.getMaxDamage()) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }

    @Inject(method = "getMiningSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void imperishableNoDurabilitySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (ImperishableItems.config.imperishablePreventsBreaking) {
            if (isDamageable()) {
                ItemStack stack = (ItemStack) (Object) this;

                if (EnchantmentHelper.getLevel(ModEnchantments.IMPERISHABLE, stack) > 0) {
                    if (stack.getDamage() >= stack.getMaxDamage()) {
                        cir.setReturnValue(1.0F);
                    }
                }
            }
        }
    }

    @Inject(method = "getAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void imperishableAttributeModifiers(EquipmentSlot equipmentSlot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        if (ImperishableItems.config.imperishablePreventsBreaking) {
            if (isDamageable()) {
                ItemStack stack = (ItemStack) (Object) this;

                if (EnchantmentHelper.getLevel(ModEnchantments.IMPERISHABLE, stack) > 0) {
                    if (stack.getDamage() >= stack.getMaxDamage()) {
                        cir.setReturnValue(ImmutableMultimap.of());
                    }
                }
            }
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void imperishableUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (ImperishableItems.config.imperishablePreventsBreaking) {
            if (!user.isCreative()) {
                if (!(getItem() instanceof Wearable)) {
                    if (isDamageable()) {
                        ItemStack stack = (ItemStack) (Object) this;

                        if (EnchantmentHelper.getLevel(ModEnchantments.IMPERISHABLE, stack) > 0) {
                            if (stack.getDamage() >= stack.getMaxDamage()) {
                                cir.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void imperishableUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (ImperishableItems.config.imperishablePreventsBreaking) {
            PlayerEntity player = context.getPlayer();
            boolean userIsCreative = false;
            if (player != null) {
                if (player.isCreative()) {
                    userIsCreative = true;
                }
            }

            if (!userIsCreative) {
                if (isDamageable()) {
                    ItemStack stack = (ItemStack) (Object) this;

                    if (EnchantmentHelper.getLevel(ModEnchantments.IMPERISHABLE, stack) > 0) {
                        if (stack.getDamage() >= stack.getMaxDamage()) {
                            cir.setReturnValue(ActionResult.PASS);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    private void imperishableUseOnEntity(PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (ImperishableItems.config.imperishablePreventsBreaking) {
            if (!user.isCreative()) {
                if (isDamageable()) {
                    ItemStack stack = (ItemStack) (Object) this;

                    if (EnchantmentHelper.getLevel(ModEnchantments.IMPERISHABLE, stack) > 0) {
                        if (stack.getDamage() >= stack.getMaxDamage()) {
                            cir.setReturnValue(ActionResult.PASS);
                        }
                    }
                }
            }
        }
    }
}