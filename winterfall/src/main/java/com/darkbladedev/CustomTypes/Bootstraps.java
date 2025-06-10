package com.darkbladedev.CustomTypes;

import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageScaling;
import org.bukkit.damage.DeathMessageType;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DamageTypeKeys;
import net.kyori.adventure.key.Key;

public class Bootstraps implements PluginBootstrap{

    @Override
    public void bootstrap(BootstrapContext context) {
        // Registrar manejador para tipos de daño personalizados
        context.getLifecycleManager().registerEventHandler(RegistryEvents.DAMAGE_TYPE.freeze().newHandler(event -> {
            // Registrar tipo de daño de sangrado
            event.registry().register(
                DamageTypeKeys.create(Key.key("winterfall:bleeding")),
                b -> b.exhaustion(1.0F)
                    .deathMessageType(DeathMessageType.DEFAULT)
                    .damageEffect(DamageEffect.HURT)
                    .messageId("winterfall.death.bleeding")
                    .damageScaling(DamageScaling.NEVER)
            );
            
            // Registrar tipo de daño de deshidratación
            event.registry().register(
                DamageTypeKeys.create(Key.key("winterfall:dehydration")),
                b -> b.exhaustion(0.5F)
                    .deathMessageType(DeathMessageType.DEFAULT)
                    .damageEffect(DamageEffect.HURT)
                    .messageId("winterfall.death.dehydration")
                    .damageScaling(DamageScaling.NEVER)
            );
        }));
    }
}
