package io.github.shaksternano.imperishableitems.mixin.common.enchantment.imperishable;

import io.github.shaksternano.imperishableitems.common.enchantment.ImperishableEnchantment;
import io.github.shaksternano.imperishableitems.common.util.ImperishableBlacklistsHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.PumpkinBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
abstract class AbstractBlockStateMixin {

    @Shadow
    public abstract Block getBlock();

    // Shears with Imperishable at 0 durability have shear specific right click block actions cancelled.
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void imperishableItems$imperishableShearsUseOnBlock(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);

        if (ImperishableBlacklistsHandler.isItemProtected(stack, ImperishableBlacklistsHandler.ProtectionType.BREAK_PROTECTION)) {
            if (!player.isCreative()) {
                if (stack.getItem() instanceof ShearsItem) {
                    if (getBlock() instanceof BeehiveBlock || getBlock() instanceof PumpkinBlock) {
                        if (ImperishableEnchantment.isBrokenImperishable(stack)) {
                            cir.setReturnValue(ActionResult.PASS);
                        }
                    }
                }
            }
        }
    }
}
