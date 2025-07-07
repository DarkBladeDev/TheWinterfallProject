package com.darkbladedev.mechanics.auraskills.skilltrees;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import com.darkbladedev.mechanics.auraskills.skills.nutrition.EfficientEaterSkill;
import com.darkbladedev.mechanics.auraskills.skills.nutrition.MetabolicBoostSkill;
import com.darkbladedev.mechanics.auraskills.skills.nutrition.IronStomachSkill;
import java.util.List;
import java.util.Arrays;

/**
 * Árbol de habilidades para la stat Nutrición (Nutrition).
 */
public class NutritionSkillTree extends SkillTree {
    @Override
    public String getName() {
        return "Nutrición";
    }

    @Override
    public List<PassiveSkill> getRootSkills() {
        return Arrays.asList(
            new EfficientEaterSkill(),
            new MetabolicBoostSkill(),
            new IronStomachSkill()
        );
    }
}
