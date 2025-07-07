package com.darkbladedev.mechanics.auraskills.skilltrees;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import com.darkbladedev.mechanics.auraskills.skills.fortitude.ColdResistanceSkill;
import com.darkbladedev.mechanics.auraskills.skills.fortitude.HeatResistanceSkill;
import com.darkbladedev.mechanics.auraskills.skills.fortitude.RadiationShieldSkill;
import java.util.List;
import java.util.Arrays;

/**
 * Árbol de habilidades para la stat Fortaleza (Fortitude).
 */
public class FortitudeSkillTree extends SkillTree {
    @Override
    public String getName() {
        return "Fortaleza";
    }

    @Override
    public List<PassiveSkill> getRootSkills() {
        return Arrays.asList(
            new ColdResistanceSkill(),
            new HeatResistanceSkill(),
            new RadiationShieldSkill()
        );
    }
}
