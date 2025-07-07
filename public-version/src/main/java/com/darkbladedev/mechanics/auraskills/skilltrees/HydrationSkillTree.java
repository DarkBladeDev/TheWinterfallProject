package com.darkbladedev.mechanics.auraskills.skilltrees;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import com.darkbladedev.mechanics.auraskills.skills.hydration.EfficientDrinkerSkill;
import com.darkbladedev.mechanics.auraskills.skills.hydration.DesertWalkerSkill;
import com.darkbladedev.mechanics.auraskills.skills.hydration.RainCollectorSkill;
import java.util.List;
import java.util.Arrays;

/**
 * Árbol de habilidades para la stat Hidratación (Hydration).
 */
public class HydrationSkillTree extends SkillTree {
    @Override
    public String getName() {
        return "Hidratación";
    }

    @Override
    public List<PassiveSkill> getRootSkills() {
        return Arrays.asList(
            new EfficientDrinkerSkill(),
            new DesertWalkerSkill(),
            new RainCollectorSkill()
        );
    }
}
