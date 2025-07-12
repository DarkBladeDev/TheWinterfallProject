package com.darkbladedev.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MessageUtils {

    public static String format(String message, Object... args) {
        return String.format(message, args);
    }

    public static Component formatComponent(String message, Object... args) {
        return MiniMessage.miniMessage().deserialize(format(message, args));
    }

    public static String stringToMmString(String input) {
        MiniMessage mm = MiniMessage.miniMessage();
        return mm.serialize(mm.deserialize(input));
    }
    
}
