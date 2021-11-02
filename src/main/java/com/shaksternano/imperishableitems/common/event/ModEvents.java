package com.shaksternano.imperishableitems.common.event;

import com.shaksternano.imperishableitems.common.ImperishableItems;
import com.shaksternano.imperishableitems.common.enchantments.ImperishableEnchantment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;

import java.util.ArrayList;

public final class ModEvents {

    private ModEvents() {}

    @Environment(EnvType.CLIENT)
    public static void registerClientEvents() {
        // Adds a message to the tooltip of an item with Imperishable at 0 durability.
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (ImperishableItems.getConfig().imperishablePreventsBreaking) {
                if (ImperishableEnchantment.isBrokenImperishable(stack)) {
                    boolean inserted = false;

                    if (context.isAdvanced()) {
                        if (lines instanceof ArrayList<Text> arrayLines) {
                            int index = 0;
                            while (index < arrayLines.size() && !inserted) {
                                Text line = arrayLines.get(index);
                                if (line instanceof TranslatableText) {
                                    if (((TranslatableText) line).getKey().equals("item.durability")) {
                                        arrayLines.add(index, new TranslatableText("item.tooltip." + ImperishableEnchantment.TRANSLATION_KEY + ".broken").formatted(Formatting.RED));
                                        arrayLines.add(index, LiteralText.EMPTY);
                                        inserted = true;
                                    }
                                }

                                index++;
                            }
                        }
                    }

                    if (!context.isAdvanced() || !inserted) {
                        lines.add(LiteralText.EMPTY);
                        lines.add(new TranslatableText("item.tooltip." + ImperishableEnchantment.TRANSLATION_KEY + ".broken").formatted(Formatting.RED));
                    }
                }
            }
        });
    }

    public static void registerEvents() {
        // Item specific right click actions are cancelled if the item has Imperishable and is at 0 durability.
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (ImperishableItems.getConfig().imperishablePreventsBreaking) {
                if (!player.isCreative() && !player.isSpectator()) {
                    // Still allow a wearable item to be equipped even if the item is broken.
                    if (!(stack.getItem() instanceof Wearable)) {
                        if (ImperishableEnchantment.isBrokenImperishable(stack)) {
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }

            return TypedActionResult.pass(stack);
        });
    }
}