package com.darkbladedev.mechanics.auraskills.skilltrees;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import com.darkbladedev.mechanics.auraskills.skills.endurance.IronWillSkill;
import com.darkbladedev.mechanics.auraskills.skills.endurance.SecondWindSkill;
import com.darkbladedev.mechanics.auraskills.skills.endurance.MarathonerSkill;
import java.util.List;
import java.util.Arrays;

/**
 * Árbol de habilidades para la stat Resistencia (Endurance).
 */
public class EnduranceSkillTree extends SkillTree {
    @Override
    public String getName() {
        return "Resistencia";
    }

    @Override
    public List<PassiveSkill> getRootSkills() {
        // Aquí puedes agregar más habilidades raíz
        return Arrays.asList(
            (PassiveSkill) new IronWillSkill(),
            (PassiveSkill) new SecondWindSkill(),
            (PassiveSkill) new MarathonerSkill()
        );
    }
}
