package com.ustaz1505.passporttooltips;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PassportTooltips implements ModInitializer {

    public static final String PAGES_KEY = "pages";
    public static final boolean IS_DEBUG = false;

    public static int getPageCount(ItemStack stack) {
        return getPages(stack).size();
    }

    public static NbtList getPages(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        if (nbtCompound == null) {
            return new NbtList();
        }
        return nbtCompound.getList(PAGES_KEY, NbtElement.STRING_TYPE);
    }

    static Logger log = LoggerFactory.getLogger(PassportTooltips.class);

    public static String getNickname(String text) {
        int nickname_start_index = text.indexOf("Никнейм")+12;

        while (text.charAt(nickname_start_index) == ' ') {
            nickname_start_index += 1;
        }
        int nickname_end_index = nickname_start_index;
        while (text.charAt(nickname_end_index) != ' ') {
            nickname_end_index += 1;
        }
        return text.substring(nickname_start_index, nickname_end_index);
    }

    public static String getNumber(String text) {
        int number_start_index = text.indexOf("Номер:")+8;
        while (text.charAt(number_start_index) == ' ') {
            number_start_index += 1;
        }
        int number_end_index = number_start_index;
        while (text.charAt(number_end_index) != ' ') {
            number_end_index += 1;
        }
        number_end_index += 1;
        while (text.charAt(number_end_index) != ' ' && text.charAt(number_end_index) != '\n') {
            number_end_index += 1;
        }
        return text.substring(number_start_index, number_end_index);
    }

    @Override
    public void onInitialize() {
        log.info("Initializing PassportTooltips");
        ItemTooltipCallback.EVENT.register(PassportTooltips::onInjectTooltip);
    }

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
        if (!Objects.equals(stack.getItem().toString(), "writable_book") && !Objects.equals(stack.getItem().toString(), "written_book")) {
            return;
        }

        if (IS_DEBUG) { log.info("Book!"); }
        if (getPageCount(stack) != 8) {
            if (IS_DEBUG) { log.info("Not enough pages!"); }
            return;
        }

        Text page1_text;
        if (Objects.equals(stack.getItem().toString(),"written_book")) {
            try {
                page1_text = Text.Serialization.fromJson(getPages(stack).getString(1));
            } catch (Exception e) {
                if (IS_DEBUG) { log.info("JSON Parse error!"); }
                return;
            }
        } else if (Objects.equals(stack.getItem().toString(), "writable_book")) {
            page1_text = Text.literal(getPages(stack).getString(1));
        } else {
            return;
        }


        if (page1_text == null) {
            if (IS_DEBUG) { log.info("The second page is empty!"); }
            return;
        }

        if (!page1_text.toString().contains("Никнейм") || !page1_text.toString().contains("Номер:")) {
            if (IS_DEBUG) { log.info("Could not find some of the keywords on the second page!"); }
            return;
        }


        list.add(Text.literal(""));
        list.add(Text.literal("§7Никнейм: §a" + getNickname(page1_text.toString())));
        list.add(Text.literal("§7Номер: §a" + getNumber(page1_text.toString())));
    }
}