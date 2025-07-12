package com.darkbladedev.menus.content;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.darkbladedev.utils.MessageUtils;

import gg.saki.zaiko.Menu;
import gg.saki.zaiko.Zaiko;
import gg.saki.zaiko.placeables.Placeable;
import gg.saki.zaiko.placeables.impl.Icon;
import gg.saki.zaiko.templates.impl.OuterFill;
import gg.saki.zaiko.utils.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PlayerInfoMenu extends Menu {
    private static final String INFO_MENU_TITLE = MessageUtils.mmToLegacy("<white><bold>Estado de salud");
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public PlayerInfoMenu(@NotNull Zaiko api) {
        super(api, INFO_MENU_TITLE, 6 * 9);


        Placeable fill1 = new Icon(new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name(" ").build());
        Placeable fill2 = new Icon(new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(" ").build());

        // Template to fill the outer border with alternating panes
        this.addTemplate(OuterFill.alternating(fill1, fill2, OuterFill.ALL));

        // Assigns menu settings (optional)
        this.settings()
            .playerInventoryInteraction(true)
            .transferItems(false)
            .closeable(true);

    }

    @Override
    public void build(@NotNull Player player) {
        
        // HEAD
        Icon Head = Icon.builder()
            .item(new ItemBuilder(Material.PLAYER_HEAD)
                .name("Cabeza")
                .owner(player.getName())
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_head%")
                )
                .build())
            .draggable(false)
            .removable(false)
            .build();

        // SUPERIOR PARTS
        Icon Torso = Icon.builder()
            .item(new ItemBuilder(Material.IRON_CHESTPLATE)
                .name(("Torso"))
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_torso%")
                )
                .build())
            .draggable(false)
            .removable(false)
            .build();

        Icon RightArm = Icon.builder()
            .item(new ItemBuilder(Material.BONE)
                .name(("Brazo derecho"))
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_right_arm%")
                )
                .build())
            .draggable(false)
            .removable(false)
            .build();

        Icon LeftArm = Icon.builder()
            .item(new ItemBuilder(Material.BONE)
                .name(("Brazo izquierdo"))
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_left_arm%")
                )
                .build())
            .draggable(false)
            .removable(false)
            .build();
    
        // INFERIOR PARTS
        Icon Legs = Icon.builder()
            .item(new ItemBuilder(Material.IRON_LEGGINGS)
                .name(("Piernas"))
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_legs%")
                )
                .build())
            .draggable(false)
            .removable(false)
            .build();

        Icon LeftLeg = Icon.builder()
            .item(new ItemBuilder(Material.BONE)
                .name(("Pierna izquierda"))
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_left_leg%")
                )
                .build())
            .draggable(false)
            .removable(false)
            .build();
    
        Icon RightLeg = Icon.builder()
            .item(new ItemBuilder(Material.BONE)
                .name(("Pierna derecha"))
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_right_leg%")

                )
                .build())
            .draggable(false)
            .removable(false)
            .build();
    
        // FEET
        Icon LeftFeet = Icon.builder()
            .item(new ItemBuilder(Material.BLACK_WOOL)
                .name(("Pie izquierdo"))
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_left_foot%")
                )
                .build())
            .draggable(false)
            .removable(false)
            .build();
    
        Icon RightFeet = Icon.builder()
            .item(new ItemBuilder(Material.BLACK_WOOL)
                .name(("Pie derecho"))
                .lore(
                    MessageUtils.mmToLegacy("<gray><i>Extremidad"),
                    "",
                    MessageUtils.mmToLegacy("<white>Estado: %savage-frontier_limb_right_foot%")

                )
                .build())
            .draggable(false)
            .removable(false)
            .build();

        this.place(13, Head);
        
        this.place(22, Torso);
        this.place(21, LeftArm);
        this.place(23, RightArm);

        this.place(31, Legs);
        this.place(30, LeftLeg);
        this.place(32, RightLeg);

        this.place(39, LeftFeet);
        this.place(41, RightFeet);
    }
}
