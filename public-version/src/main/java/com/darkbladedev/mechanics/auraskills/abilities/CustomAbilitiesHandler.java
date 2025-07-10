package com.darkbladedev.mechanics.auraskills.abilities;

import com.darkbladedev.mechanics.events.limb.PlayerLimbDamageEvent;
import com.darkbladedev.mechanics.LimbDamageSystem.DamageState;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CustomAbilitiesHandler implements Listener {
    private final AuraSkillsApi api;

    public CustomAbilitiesHandler(AuraSkillsApi api) {
        this.api = api;
    }

    @EventHandler
    public void onPlayerLimbDamage(PlayerLimbDamageEvent event) {
        if (event.isCancelled()) return;

        SkillsUser user = api.getUser(event.getPlayer().getUniqueId());
        if (user == null) return;

        int hardBonesLevel = user.getAbilityLevel(CustomAbilities.HardBones);

        if (hardBonesLevel <= 0) return;

        // Cada nivel reduce un 2% del daño (ajustable)
        double reduction = 0.02 * hardBonesLevel;

        int oldLevel = event.getPreviousState().getLevel();
        int newLevel = event.getNewState().getLevel();

        int reducedLevel = (int) Math.round(oldLevel + ((newLevel - oldLevel) * (1.0 - reduction)));

        // Clamp entre 0 y 3
        reducedLevel = Math.max(0, Math.min(3, reducedLevel));

        // Si el nuevo nivel es menor al original, aplicamos
        if (reducedLevel < newLevel) {
            DamageState reducedState = DamageState.values()[reducedLevel];

            event.setNewState(reducedState);

        }
    }
}
