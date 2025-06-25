package com.darkbladedev.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.NutritionSystem.NutrientType;

import java.util.*;

public class ActionBarDisplayManager implements Listener {
    private final SavageFrontierMain plugin;
    private final Map<UUID, List<StatType>> playerDisplays = new HashMap<>();
    private final Map<UUID, Boolean> playerActionBarStates = new HashMap<>();
    @SuppressWarnings("unused")
    private final int defaultSlots = 3;
    private int maxSlots;
    @SuppressWarnings("unused")
    private boolean actionBarEnabled = true;
    private BukkitTask actionBarTask;
    
    public ActionBarDisplayManager(SavageFrontierMain plugin, int maxSlots) {
        this.plugin = plugin;
        this.maxSlots = maxSlots;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startActionBarUpdater();
    }

    private void startActionBarUpdater() {
        stopActionBarUpdater(); // Detener el actualizador anterior si existe
        
        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (playerActionBarStates.getOrDefault(player.getUniqueId(), true)) {
                        sendActionBar(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // cada segundo
    }
    
    /**
     * Detiene el actualizador de la barra de acción
     */
    private void stopActionBarUpdater() {
        if (actionBarTask != null && !actionBarTask.isCancelled()) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }
    
    /**
     * Recarga la configuración del gestor de barras de acción
     * @param newMaxSlots Nuevo número máximo de slots
     */
    public void reload(int newMaxSlots) {
        this.maxSlots = newMaxSlots;
        startActionBarUpdater();
        
        // Actualizar las configuraciones de los jugadores si es necesario
        for (UUID uuid : playerDisplays.keySet()) {
            List<StatType> current = playerDisplays.get(uuid);
            if (current.size() > maxSlots) {
                playerDisplays.put(uuid, current.subList(0, maxSlots));
            }
        }
    }

    public void sendActionBar(Player player) {
        List<StatType> selected = playerDisplays.getOrDefault(player.getUniqueId(), getDefaultDisplay());
        List<String> parts = new ArrayList<>();

        for (StatType type : selected) {
            parts.add(switch (type) {
                case HEALTH -> "<red>❤ " + (int) player.getHealth() + "/" + (int) player.getAttribute(Attribute.MAX_HEALTH).getValue();
                case STAMINA -> "<green>⚡ " + getStamina(player);
                case NUTRIENTS -> "<gold>🍽 (" + getNutrients(player) + ")";
            });
        }

        player.sendActionBar(MiniMessage.miniMessage().deserialize(String.join(" <gray>| ", parts)));
    }

    private String getStamina(Player player) {
        return plugin.getStaminaSystem().getStaminaLevel(player) + "/" + plugin.getStaminaSystem().getMaxStamina();
    }

    private String getNutrients(Player player) {
        return String.valueOf(NutrientType.PROTEIN.getColorCode() + plugin.getNutritionSystem().getNutrientLevel(player, NutrientType.PROTEIN)) + "<gray>/" +
        String.valueOf(NutrientType.CARBS.getColorCode() + plugin.getNutritionSystem().getNutrientLevel(player, NutrientType.CARBS)) + "<gray>/" +
        String.valueOf(NutrientType.FAT.getColorCode() + plugin.getNutritionSystem().getNutrientLevel(player, NutrientType.FAT)) + "<gray>/" +
        String.valueOf(NutrientType.VITAMINS.getColorCode() + plugin.getNutritionSystem().getNutrientLevel(player, NutrientType.VITAMINS));
    }

    private List<StatType> getDefaultDisplay() {
        return new ArrayList<>(List.of(StatType.HEALTH, StatType.STAMINA, StatType.NUTRIENTS));
    }

    public void openDisplayMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, Component.text("Selecciona tus stats"));

        List<StatType> current = playerDisplays.getOrDefault(player.getUniqueId(), getDefaultDisplay());

        for (int i = 3; i < maxSlots; i++) {
            StatType stat = i < current.size() ? current.get(i) : StatType.HEALTH;
            ItemStack item = new ItemStack(Material.valueOf(stat.material));
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("Slot " + (i + 1) + ": " + stat.name()));
            meta.lore(List.of(Component.text("Clic para cambiar"), Component.text("Actual: " + stat.name()), Component.text("Opciones: " + stat.catalogue())));
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }

        player.openInventory(gui);
    }


    /**
     * Enables or disables the action bar for a specific player
     * @param targetPlayer The player to modify the action bar state for
     * @param enabled Whether the action bar should be enabled or disabled
     */
    public void setActionbarEnabled(Player targetPlayer, boolean enabled) {
        playerActionBarStates.put(targetPlayer.getUniqueId(), enabled);
        
        if (!enabled) {
            // Clear the action bar by sending an empty component
            targetPlayer.sendActionBar(Component.empty());
        }
    }

    /**
     * Checks if the action bar is enabled for a specific player
     * @param player The player to check
     * @return true if the action bar is enabled, false otherwise
     */
    @SuppressWarnings("unused")
    private boolean isActionBarEnabled(Player player) {
        return playerActionBarStates.getOrDefault(player.getUniqueId(), true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().title().equals(Component.text("Selecciona tus stats"))) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= maxSlots) return;

        UUID uuid = player.getUniqueId();
        List<StatType> current = playerDisplays.getOrDefault(uuid, getDefaultDisplay());
        StatType old = slot < current.size() ? current.get(slot) : StatType.HEALTH;
        StatType next = old.next();

        if (slot < current.size()) current.set(slot, next);
        else current.add(next);

        playerDisplays.put(uuid, current);
        openDisplayMenu(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        playerDisplays.putIfAbsent(e.getPlayer().getUniqueId(), getDefaultDisplay());
    }

    enum StatType {
        HEALTH("RED_STAINED_GLASS_PANE"),
        STAMINA("LIME_STAINED_GLASS_PANE"),
        NUTRIENTS("YELLOW_STAINED_GLASS_PANE");

        public final String material;

        StatType(String material) {
            this.material = material;
        }

        public StatType next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        public String catalogue() {
            return switch (this) {
                case HEALTH -> "Salud máxima y actual";
                case STAMINA -> "Energía o resistencia";
                case NUTRIENTS -> "Nutrientes (proteínas, grasas, carbohidratos, vitaminas)";
            };
        }
    }
}
