package com.darkbladedev.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageUtils {

    public static String format(String message, Object... args) {
        return String.format(message, args);
    }

    public static Component formatComponent(String message, Object... args) {
        return MiniMessage.miniMessage().deserialize(format(message, args));
    }

    public static Component mmStringToComponent(String input) {
        MiniMessage mm = MiniMessage.miniMessage();
        return mm.deserialize(input);
    }

    public static String mmToLegacy(String input) {
    Component component = MiniMessage.miniMessage().deserialize(input);
    return LegacyComponentSerializer.legacySection().serialize(component);
}

    
}
