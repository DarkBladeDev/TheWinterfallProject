package com.darkbladedev.CustomTypes;

import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import com.darkbladedev.SavageFrontierMain;

import io.papermc.paper.registry.RegistryAccess;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Clase que proporciona métodos para crear fuentes de daño personalizadas
 * utilizando la API de Paper para tipos de daño.
 */
public class CustomDamageTypes {
    
    private static SavageFrontierMain plugin;

    // Claves para los tipos de daño personalizados
    public static final Key BLEEDING_KEY = Key.key("savage-frontier:bleeding");
    public static final Key DEHYDRATION_KEY = Key.key("savage-frontier:dehydration");
    public static final Key DESNUTRITION_KEY = Key.key("savage-frontier:desnutrition");
    public static final Key FREEZING_KEY = Key.key("savage-frontier:freezing");
    public static final Key HYPOTHERMIA_KEY = Key.key("savage-frontier:hypothermia");
    public static final Key HYPERTHERMIA_KEY = Key.key("savage-frontier:hyperthermia");
    public static final Key FAT_DAMAGE_KEY = Key.key("savage-frontier:fat_damage");
    

        /**
     * Crea una fuente de daño custom
     * @param source La entidad que causa el daño (puede ser null)
     * @param target La entidad que recibe el daño
     * @param key La clave del damageType custom
     * @return La fuente de daño personalizada
     */
    @SuppressWarnings("removal")
    public static DamageSource DamageSourceBuilder(@Nullable Entity source, Entity target, Key key) {
        try {
            DamageType damageType = RegistryAccess.registryAccess().getRegistry(DamageType.class).get(key);
            if (damageType == null) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <dark_red>Error: <red>No se pudo encontrar el tipo de daño '" + key.asString() + "'. Usando daño genérico."));
                return DamageSource.builder(DamageType.GENERIC).build();
            }

            if (source == null) {
                source = target;
            }
            
            return DamageSource.builder(damageType)
                    .withCausingEntity(source)
                    .withDirectEntity(target)
                    .build();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al crear DamageSource de " + key.asString() + " >> " + e.getMessage()));
            return DamageSource.builder(DamageType.GENERIC).build();
        }
    }
    

    public enum CustomDamageDeathMessage {
        HYPOTHERMIA(Key.key("savage-frontier:hypothermia"), "<red>%player% ha muerto por hipotermia"),
        HYPERTHERMIA(Key.key("savage-frontier:hyperthermia"), "<red>%player% ha muerto por hipertermia"),
        DEHYDRATION(Key.key("savage-frontier:dehydration"), "<red>%player% ha muerto por deshidratación"),
        DESNUTRITION(Key.key("savage-frontier:desnutrition"), "<red>%player% ha muerto por desnutrición"),
        BLEEDING(Key.key("savage-frontier:bleeding"), "<red>%player% ha muerto desangrado"),
        FREEZING(Key.key("savage-frontier:freezing"), "<red>%player% ha muerto congelado"),
        FAT_DAMAGE(Key.key("savage-frontier:fat_damage"), "<red>%player% ha muerto por un paro cardíaco");

        CustomDamageDeathMessage(Key key, String messageID) {
            this.key = key;
            this.messageID = messageID;
        }

        public String getMessageID() {
            return messageID;
        }
        
        /**
         * Obtiene la clave del tipo de daño
         * @return Clave del tipo de daño
         */
        public Key getKey() {
            return key;
        }
        
        /**
         * Obtiene el mensaje de muerte con el nombre del jugador incluido
         * @param playerName Nombre del jugador que murió
         * @return Mensaje de muerte personalizado
         */
        public String getDeathMessage(String playerName) {
            return messageID.replace("%player%", playerName);
        }
        
        private final Key key;
        private final String messageID;
    }
}