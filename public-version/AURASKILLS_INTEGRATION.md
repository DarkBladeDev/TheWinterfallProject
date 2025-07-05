# Integración con AuraSkills

Este documento describe la integración del plugin Savage Frontier con AuraSkills, permitiendo una experiencia de ActionBar unificada y personalizable.

## Características

### 1. Detección Automática
- El plugin detecta automáticamente si AuraSkills está instalado y habilitado
- Se inicializa la integración solo si AuraSkills está disponible
- Funciona de forma independiente si AuraSkills no está presente

### 2. Modos de Operación

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

### 3. Configuración

```yaml
actionbar:
  max_slots: 3
  auraskills_integration:
    enabled: true                    # Habilita la integración
    allow_toggle: true              # Permite alternar entre ActionBars
    default_priority: 'savage'      # Prioridad por defecto: 'savage' o 'auraskills'
    combine_mode: false             # Modo combinado
```

### 4. Comandos

| Comando | Descripción | Permiso |
|---------|-------------|----------|
| `/actionbar toggle` | Alterna entre ActionBars | `savage.actionbar.menu` |
| `/actionbar status` | Muestra el estado actual | `savage.actionbar.menu` |
| `/actionbar combine` | Alterna modo combinado (Admin) | `savage.admin.actionbar` |
| `/actionbar menu` | Abre menú de configuración | `savage.actionbar.menu` |

### 5. Permisos

- `savage.actionbar.menu` - Permite usar comandos básicos de ActionBar
- `savage.admin.actionbar` - Permite configurar opciones administrativas

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