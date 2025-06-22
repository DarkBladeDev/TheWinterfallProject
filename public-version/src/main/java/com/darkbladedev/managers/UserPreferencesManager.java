package com.darkbladedev.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;

public class UserPreferencesManager {

    @SuppressWarnings("unused")
    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final NamespacedKey newPlayerProtectionKey;
    private final NamespacedKey statusMessagesKey;

    public UserPreferencesManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.newPlayerProtectionKey = new NamespacedKey(plugin, "new_player_protection");
        this.statusMessagesKey = new NamespacedKey(plugin, "status_messages");
    }

    // =================== GETTERS ===================

    public boolean hasNewPlayerProtection(Player player) {
        return getOrDefault(player, newPlayerProtectionKey, true);
    }

    public boolean hasStatusMessages(Player player) {
        return getOrDefault(player, statusMessagesKey, true);
    }

    // =================== SETTERS ===================

    public void setNewPlayerProtection(Player player, boolean enabled) {
        setPreference(player, newPlayerProtectionKey, enabled);
        sendFeedback(player, "Protección de nuevo jugador <color>" + (enabled ? "activada" : "desactivada") + "</color>.");
    }

    public void setStatusMessages(Player player, boolean enabled) {
        setPreference(player, statusMessagesKey, enabled);
        sendFeedback(player, "Mensajes de estado <color>" + (enabled ? "activados" : "desactivados") + "</color>.");
    }

    // =================== HELPERS ===================

    private void setPreference(Player player, NamespacedKey key, boolean value) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(key, PersistentDataType.BOOLEAN, value ? true : false);
    }

    private boolean getOrDefault(Player player, NamespacedKey key, boolean defaultValue) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.has(key, PersistentDataType.BOOLEAN)
                ? data.get(key, PersistentDataType.BOOLEAN)
                : defaultValue;
    }

    private void sendFeedback(Player player, String rawMiniMessage) {
        Component message = miniMessage.deserialize("<gray>[Preferencias]</gray> <white>" + rawMiniMessage.replace("<color>", "<yellow>").replace("</color>", "</yellow>"));
        player.sendMessage(message);
    }
} 
