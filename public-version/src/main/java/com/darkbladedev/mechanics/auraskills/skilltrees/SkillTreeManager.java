package com.darkbladedev.mechanics.auraskills.skilltrees;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;
import java.util.*;

/**
 * Gestor singleton de árboles y habilidades pasivas desbloqueadas por jugador.
 */
public class SkillTreeManager {
    private static SkillTreeManager instance;
    private final List<SkillTree> skillTrees = new ArrayList<>();

    private SkillTreeManager() {
        // Registrar todos los árboles
        skillTrees.add(new EnduranceSkillTree());
        skillTrees.add(new VitalitySkillTree());
        skillTrees.add(new FortitudeSkillTree());
        skillTrees.add(new HydrationSkillTree());
        skillTrees.add(new NutritionSkillTree());
        skillTrees.add(new RecoverySkillTree());
    }

    public static SkillTreeManager getInstance() {
        if (instance == null) {
            instance = new SkillTreeManager();
        }
        return instance;
    }

    /**
     * Devuelve todas las habilidades pasivas desbloqueadas por el jugador según su stat.
     * Debes implementar la consulta real del nivel de stat usando la API de AuraSkills.
     */
    public List<PassiveSkill> getUnlockedSkills(Player player, Map<String, Integer> playerStats) {
        List<PassiveSkill> unlocked = new ArrayList<>();
        for (SkillTree tree : skillTrees) {
            for (PassiveSkill skill : tree.getRootSkills()) {
                int playerLevel = playerStats.getOrDefault(tree.getName().toLowerCase(), 0);
                if (playerLevel >= skill.getRequiredLevel()) {
                    unlocked.add(skill);
                }
            }
        }
        return unlocked;
    }

    /**
     * Verifica si el jugador tiene una habilidad pasiva específica desbloqueada.
     */
    public boolean hasSkill(Player player, Class<? extends PassiveSkill> skillClass, Map<String, Integer> playerStats) {
        return getUnlockedSkills(player, playerStats).stream().anyMatch(skill -> skill.getClass().equals(skillClass));
    }
}
