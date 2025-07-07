package com.darkbladedev.mechanics.auraskills.skilltrees;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import com.darkbladedev.mechanics.auraskills.skills.vitality.ThickSkinSkill;
import com.darkbladedev.mechanics.auraskills.skills.vitality.RegenerationSkill;
import com.darkbladedev.mechanics.auraskills.skills.vitality.PainToleranceSkill;
import java.util.List;
import java.util.Arrays;

/**
 * Árbol de habilidades para la stat Vitalidad (Vitality).
 */
public class VitalitySkillTree extends SkillTree {
    @Override
    public String getName() {
        return "Vitalidad";
    }

    @Override
    public List<PassiveSkill> getRootSkills() {
        return Arrays.asList(
            new ThickSkinSkill(),
            new RegenerationSkill(),
            new PainToleranceSkill()
        );
    }
}
