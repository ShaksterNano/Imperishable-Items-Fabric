package io.github.shaksternano.imperishableitems.mixin.client.enchantment.imperishable;

import io.github.shaksternano.imperishableitems.common.enchantment.ImperishableEnchantment;
import io.github.shaksternano.imperishableitems.common.util.ImperishableBlacklistsHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(AnvilScreen.class)
abstract class AnvilScreenMixin extends ForgingScreen<AnvilScreenHandler> {

    @SuppressWarnings("unused")
    protected AnvilScreenMixin(AnvilScreenHandler handler, PlayerInventory playerInventory, Text title, Identifier texture) {
        super(handler, playerInventory, title, texture);
    }

    // An item with imperishable at 0 durability in an anvil will not have "(Broken)" at the end if its name.
    @Redirect(method = "onSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;getString()Ljava/lang/String;"))
    private String imperishableBrokenOnSlotUpdate(Text getName, ScreenHandler handler, int slotId, ItemStack stack) {
        String trimmedName = getName.getString();

        if (ImperishableBlacklistsHandler.isItemProtected(stack, ImperishableBlacklistsHandler.ProtectionType.BREAK_PROTECTION)) {
            trimmedName = ImperishableEnchantment.itemNameRemoveBroken(getName, stack);
        }

        return trimmedName;
    }

    // Putting "(Broken)" at the end of the name of an item with Imperishable at 0 durability will register as a new name.
    @Redirect(method = "onRenamed", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;getString()Ljava/lang/String;"))
    private String imperishableBrokenOnRenamed(Text getName) {
        String trimmedName = getName.getString();
        Slot slot = handler.getSlot(0);

        if (slot != null) {
            if (slot.hasStack()) {
                ItemStack stack = slot.getStack();
                if (ImperishableBlacklistsHandler.isItemProtected(stack, ImperishableBlacklistsHandler.ProtectionType.BREAK_PROTECTION)) {
                    trimmedName = ImperishableEnchantment.itemNameRemoveBroken(getName, stack);
                }
            }
        }

        return trimmedName;
    }
}
