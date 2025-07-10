# Ideas futuras:
## 1. Agregar un sistema de moldes para la fabricación de herramientas.
### 1.1 El planteo de la idea.
    Creación de moldes para herramientas específicas.
    
    Fabricación de herramientas a partir de los moldes.

    Los moldes se fabrican con arcilla y un palo en la offhand
    
### 1.2 La implementación de la idea al código.
    Para fabricar un molde, se debe hacer click en cualquier bloque que no tenga interacción vanilla (por ej: El suelo) con un trozo de arcilla y un palo en la offhand.
    
    Modo Facil:
    Se abrirá un menú que contendrá todos los moldes disponibles.
    Al seleccionar un molde, se fabricará la herramienta correspondiente.

    Modo Avanzado:
    Se abrirá un inventario que contendrá todos los moldes disponibles.
    Al seleccionar un molde, se iniciará un minijuego en un panel donde debes darle forma al molde mediante una tablilla con piezas clickeables.
    Al clickear una pieza, la misma desaparece.
    El minijuego consiste en ubicar las piezas en el orden correcto para formar el molde.
    Si se forma el molde correctamente, se fabricará la herramienta correspondiente.

    El modo puede ser cambiado unicamente desde la configuración del plugin por un administrador.
    El palo en la offhand se desgastará al usar el molde.
    La arcilla se consumirá al usar el molde.

### 1.3 Tecnologías y métodos a usar para la implementación.
    Se hará una api interna (en el package com.darkbladedev.utils) que permitirá crear los menús basados en inventarios.
    Se hará uso de los eventos de los inventarios para detectar los clicks.
    Se hará uso de los eventos de los bloques para detectar los clicks.