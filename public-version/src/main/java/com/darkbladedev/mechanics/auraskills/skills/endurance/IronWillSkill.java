package com.darkbladedev.mechanics.auraskills.skills.endurance;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import com.darkbladedev.SavageFrontierMain;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import net.kyori.adventure.key.Key;

/**
 * Habilidad pasiva: Voluntad de Hierro
 * Otorga resistencia al cansancio extremo, reduciendo la penalización de lentitud al estar exhausto.
 */
public class IronWillSkill implements PassiveSkill {
    private static final SavageFrontierMain plugin = SavageFrontierMain.getInstance();

    @Override
    public String getName() {
        return "Voluntad de Hierro";
    }

    @Override
    public String getDescription() {
        return "Reduce la penalización de lentitud al estar con estamina agotada.";
    }

    @Override
    public int getRequiredLevel() {
        return 5; // Nivel de stat necesario para desbloquear
    }

    @Override
    public void apply(Player player) {
        // Si el jugador está exhausto, reduce la penalización de lentitud
        if (plugin.getStaminaSystem().getStaminaLevel(player) <= 2) {
            AttributeModifier modifier = player.getAttribute(Attribute.MOVEMENT_SPEED).getModifier(Key.key("savage-frontier", "stamina_modifier"));
            if (modifier != null) {
                double newSpeed = modifier.getAmount() + 0.1; // Aumenta la velocidad base en 0.1
                player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(modifier);
                player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(
                    new AttributeModifier(modifier.getKey(), newSpeed, modifier.getOperation())
                );
            }
        }
    }
}
