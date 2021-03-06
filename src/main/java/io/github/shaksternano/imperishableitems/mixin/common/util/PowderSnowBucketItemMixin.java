package io.github.shaksternano.imperishableitems.mixin.common.util;

import io.github.shaksternano.imperishableitems.common.ImperishableItems;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Map;

@Mixin(PowderSnowBucketItem.class)
abstract class PowderSnowBucketItemMixin extends BlockItem implements FluidModificationItem {

    @SuppressWarnings("unused")
    protected PowderSnowBucketItemMixin(Block block, Settings settings) {
        super(block, settings);
    }

    // Buckets retain their enchantments when placing powder snow.
    @SuppressWarnings("ConstantConditions")
    @ModifyArgs(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"))
    private void imperishableItems$placeTransferEnchantments(Args args, ItemUsageContext context) {
        if (ImperishableItems.getConfig().retainEnchantmentsMoreOften) {
            ItemStack stack = context.getPlayer().getStackInHand(context.getHand());
            if (stack.hasEnchantments()) {
                ItemStack getDefaultStack = args.get(1);
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
                EnchantmentHelper.set(enchantments, getDefaultStack);
            }
        }
    }
}
