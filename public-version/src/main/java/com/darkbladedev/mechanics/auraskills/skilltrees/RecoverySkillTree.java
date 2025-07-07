package com.darkbladedev.mechanics.auraskills.skilltrees;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import com.darkbladedev.mechanics.auraskills.skills.recovery.QuickHealerSkill;
import com.darkbladedev.mechanics.auraskills.skills.recovery.AdrenalineRushSkill;
import com.darkbladedev.mechanics.auraskills.skills.recovery.MedicSkill;
import java.util.List;
import java.util.Arrays;

/**
 * Árbol de habilidades para la stat Recuperación (Recovery).
 */
public class RecoverySkillTree extends SkillTree {
    @Override
    public String getName() {
        return "Recuperación";
    }

    @Override
    public List<PassiveSkill> getRootSkills() {
        return Arrays.asList(
            new QuickHealerSkill(),
            new AdrenalineRushSkill(),
            new MedicSkill()
        );
    }
}
