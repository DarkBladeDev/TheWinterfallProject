# Integración con AuraSkills

Este documento describe la integración del plugin Savage Frontier con AuraSkills, permitiendo una experiencia de ActionBar unificada y personalizable.

## Características

### 1. Detección Automática
- El plugin detecta automáticamente si AuraSkills está instalado y habilitado
- Se inicializa la integración solo si AuraSkills está disponible
- Funciona de forma independiente si AuraSkills no está presente

### 2. Sistema de Estamina y Traits
- Integración con el sistema de estamina para aplicar modificadores basados en estadísticas
- Trait de capacidad de estamina: aumenta la capacidad máxima de estamina
- Trait de recuperación de estamina: aumenta la velocidad de recuperación
- Verificación periódica automática para asegurar que los traits estén correctamente aplicados
- Comando administrativo para verificar y corregir traits manualmente

### 3. Modos de Operación

#### Modo Alternado (Por defecto)
- Los jugadores pueden alternar entre la ActionBar de AuraSkills y Savage Frontier
- Comando: `/actionbar toggle`
- Configuración individual por jugador
- Prioridad por defecto configurable

#### Modo Combinado
- Combina ambas ActionBars en una sola visualización
- Muestra información de ambos plugins simultáneamente
- Comando de administrador: `/actionbar combine`
- Formato: `❤ Vida | ⚡ Stamina | 🍎 Nutrientes | ✦ Mana`

### 4. Configuración

```yaml
actionbar:
  max_slots: 3
  auraskills_integration:
    enabled: true                    # Habilita la integración
    allow_toggle: true              # Permite alternar entre ActionBars
    default_priority: 'savage'      # Prioridad por defecto: 'savage' o 'auraskills'
    combine_mode: false             # Modo combinado

stamina:
  # ... otras opciones de stamina ...
  # Intervalo en minutos para verificar y corregir automáticamente los traits de estamina
  # Establece a 0 para deshabilitar la verificación periódica
  verify-traits-interval: 60        # Verificación automática cada 60 minutos
```

### 5. Comandos

| Comando | Descripción | Permiso |
|---------|-------------|----------|
| `/actionbar toggle` | Alterna entre ActionBars | `savage.actionbar.menu` |
| `/actionbar status` | Muestra el estado actual | `savage.actionbar.menu` |
| `/actionbar combine` | Alterna modo combinado (Admin) | `savage.admin.actionbar` |
| `/actionbar menu` | Abre menú de configuración | `savage.actionbar.menu` |
| `/checkstaminatraits <jugador>` | Verifica y corrige traits de estamina para un jugador | `savage.admin` |
| `/checkstaminatraits all` | Verifica y corrige traits de estamina para todos los jugadores en línea | `savage.admin` |

### 6. Permisos

- `savage.actionbar.menu` - Permite usar comandos básicos de ActionBar
- `savage.admin.actionbar` - Permite configurar opciones administrativas de ActionBar
- `savage.admin` - Permite usar comandos administrativos, incluyendo verificación de traits

## Funcionamiento Técnico

### Clases Principales

1. **AuraSkillsIntegration**
   - Maneja la detección y configuración de AuraSkills
   - Controla las preferencias de los jugadores
   - Verifica el estado de la ActionBar de AuraSkills

2. **ActionBarCombiner**
   - Combina información de ambos plugins
   - Formatea la visualización unificada
   - Maneja la lógica del modo combinado

3. **ActionBarDisplayManager**
   - Coordina la visualización de ActionBars
   - Decide qué ActionBar mostrar según la configuración
   - Integra con el sistema de eventos

4. **StaminaSystemExpansion**
   - Integra el sistema de estamina con AuraSkills
   - Gestiona los traits de capacidad y recuperación de estamina
   - Verifica y corrige automáticamente los traits de los jugadores
   - Proporciona comandos administrativos para gestión de traits

### Flujo de Decisión

```
¿Está habilitado combine_mode?
├─ Sí → Mostrar ActionBar combinada
└─ No → ¿Jugador prefiere AuraSkills?
    ├─ Sí → Mostrar ActionBar de AuraSkills
    └─ No → Mostrar ActionBar de Savage Frontier
```

## Compatibilidad

- **AuraSkills**: Versión 2.3.3+
- **Minecraft**: 1.21+
- **Java**: 17+
- **Spigot/Paper**: Última versión estable

## Configuración Recomendada

### Para Servidores RPG
```yaml
auraskills_integration:
  enabled: true
  allow_toggle: true
  default_priority: 'auraskills'
  combine_mode: false
```

### Para Servidores Survival Hardcore
```yaml
auraskills_integration:
  enabled: true
  allow_toggle: false
  default_priority: 'savage'
  combine_mode: true
```

## Solución de Problemas

### AuraSkills no detectado
1. Verificar que AuraSkills esté instalado y habilitado
2. Comprobar la versión de AuraSkills (2.3.3+)
3. Revisar los logs del servidor para errores de dependencias

### ActionBar no se muestra
1. Verificar permisos del jugador
2. Comprobar configuración de `enabled: true`
3. Usar `/actionbar status` para diagnosticar

### Conflictos de visualización
1. Deshabilitar una de las ActionBars temporalmente
2. Usar modo combinado si ambas son necesarias
3. Ajustar la prioridad por defecto

## API para Desarrolladores

```java
// Obtener la integración
AuraSkillsIntegration integration = plugin.getActionBarDisplayManager().getAuraSkillsIntegration();

// Verificar estado
boolean isEnabled = integration.isAuraSkillsEnabled();
boolean allowToggle = integration.isToggleAllowed();

// Configurar preferencias del jugador
integration.setPlayerAuraSkillsActionBar(player, true);
boolean usingAuraSkills = integration.isPlayerUsingAuraSkillsActionBar(player);

// Recargar configuración
integration.reload();
```

## Changelog

### v1.0.0
- Integración inicial con AuraSkills
- Modo alternado entre ActionBars
- Comandos básicos de gestión
- Configuración por jugador

### v1.1.0
- Modo combinado de ActionBars
- Comando `/actionbar combine`
- Mejoras en el comando `/actionbar status`
- Documentación completa

### v1.2.0
- Integración del sistema de estamina con AuraSkills
- Traits de capacidad y recuperación de estamina
- Comando `/checkstaminatraits` para verificar y corregir traits
- Verificación periódica automática de traits
- Mejoras en la estabilidad de la integración