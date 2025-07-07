package com.darkbladedev.mechanics.auraskills.skilltrees;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import java.util.List;

/**
 * Clase base para árboles de habilidades asociadas a una stat principal.
 */
public abstract class SkillTree {
    /**
     * Devuelve el nombre del árbol de habilidades.
     */
    public abstract String getName();

    /**
     * Devuelve la lista de habilidades pasivas raíz de este árbol.
     */
    public abstract List<PassiveSkill> getRootSkills();
}
