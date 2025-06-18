package com.darkbladedev.CustomTypes;

import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageScaling;
import org.bukkit.damage.DeathMessageType;
import org.bukkit.inventory.EquipmentSlotGroup;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DamageTypeKeys;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Bootstraps implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        // Registrar manejador para tipos de daño personalizados
        context.getLifecycleManager().registerEventHandler(RegistryEvents.DAMAGE_TYPE.freeze().newHandler(event -> {
            // Registrar tipo de daño de sangrado
            event.registry().register(
                DamageTypeKeys.create(Key.key("savage-frontier:bleeding")),
                b -> b.exhaustion(1.0F)
                    .deathMessageType(DeathMessageType.DEFAULT)
                    .damageEffect(DamageEffect.HURT)
                    .messageId(CustomDamageTypes.CustomDamageDeathMessage.BLEEDING.getMessageID())
                    .damageScaling(DamageScaling.NEVER)
            );
            
            // Registrar tipo de daño de deshidratación
            event.registry().register(
                DamageTypeKeys.create(Key.key("savage-frontier:dehydration")),
                b -> b.exhaustion(0.5F)
                    .deathMessageType(DeathMessageType.DEFAULT)
                    .damageEffect(DamageEffect.HURT)
                    .messageId(CustomDamageTypes.CustomDamageDeathMessage.DEHYDRATION.getMessageID())
                    .damageScaling(DamageScaling.NEVER)
            );
            
            // Registrar tipo de daño de congelación
            event.registry().register(
                DamageTypeKeys.create(Key.key("savage-frontier:freezing")),
                b -> b.exhaustion(0.5F)
                    .deathMessageType(DeathMessageType.DEFAULT)
                    .damageEffect(DamageEffect.HURT)
                    .messageId(CustomDamageTypes.CustomDamageDeathMessage.FREEZING.getMessageID())
                    .damageScaling(DamageScaling.NEVER)
            );
            
            // Registrar tipo de daño de desnutrición
            event.registry().register(
                DamageTypeKeys.create(Key.key("savage-frontier:desnutrition")),
                b -> b.exhaustion(0.0F)
                    .deathMessageType(DeathMessageType.DEFAULT)
                    .damageEffect(DamageEffect.HURT)
                    .messageId(CustomDamageTypes.CustomDamageDeathMessage.DESNUTRITION.getMessageID())
                    .damageScaling(DamageScaling.NEVER)
            );

            // Registrar tipo de daño de hipotermia
            event.registry().register(
                DamageTypeKeys.create(Key.key("savage-frontier:hypothermia")),
                b -> b.exhaustion(0.7F)
                    .deathMessageType(DeathMessageType.DEFAULT)
                    .damageEffect(DamageEffect.FREEZING)
                    .messageId(CustomDamageTypes.CustomDamageDeathMessage.HYPOTHERMIA.getMessageID())
                    .damageScaling(DamageScaling.NEVER)
            );

            // Registrar tipo de daño de hipertermia
            event.registry().register(
                DamageTypeKeys.create(Key.key("savage-frontier:hyperthermia")),
                b -> b.exhaustion(0.7F)
                    .deathMessageType(DeathMessageType.DEFAULT)
                    .damageEffect(DamageEffect.BURNING)
                    .messageId(CustomDamageTypes.CustomDamageDeathMessage.HYPERTHERMIA.getMessageID())
                    .damageScaling(DamageScaling.NEVER)
            );
        }));

        // Register Enchantments handler
        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
            // Register Congelation Ench.
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.CONGELATION_KEY),
                b -> b.maxLevel(1)
                    .anvilCost(10)
                    .activeSlots(EquipmentSlotGroup.MAINHAND)
                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(5, 3))
                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 5))
                    .weight(5)
                    
                    .description(MiniMessage.miniMessage().deserialize(
                        "<aqua>Congelación</aqua>"
                        ))
                    
                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                    
                );

            // Register Rad. protection Ench.
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.RADIATION_PROTECTION_KEY),
                b -> b.maxLevel(3)
                   .anvilCost(15)
                   .weight(15)
                   .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(5, 3))
                   .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 5))

                   .description(MiniMessage.miniMessage().deserialize(
                        "<gradient:#66cded:#02f938>Proteccion contra radiacion</gradient>"
                    ))

                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.LEG_ARMOR))
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.CHEST_ARMOR)) 
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.FOOT_ARMOR))
                  .activeSlots(EquipmentSlotGroup.ARMOR)
            );

            // Register Cold protection Ench.
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.COLD_PROTECTION_KEY),
                b -> b.maxLevel(3)
                   .anvilCost(15)
                   .weight(15)
                   .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(5, 3))
                   .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 5))

                   .description(MiniMessage.miniMessage().deserialize(
                        "<gradient:#3734da:#36ccde>Proteccion contra el Frio</gradient>"
                    ))

                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.LEG_ARMOR))
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.CHEST_ARMOR)) 
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.FOOT_ARMOR))
                  .activeSlots(EquipmentSlotGroup.ARMOR)
            );

            // Register Heat protection Ench.
            event.registry().register(
                EnchantmentKeys.create(CustomEnchantments.HEAT_PROTECTION_KEY),
                b -> b.maxLevel(3)
                   .anvilCost(15)
                   .weight(15)
                   .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(5, 3))
                   .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 5))

                   .description(MiniMessage.miniMessage().deserialize(
                        "<gradient:#eee846:#f63232>Proteccion contra el Calor</gradient>"
                    ))

                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.LEG_ARMOR))
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.CHEST_ARMOR)) 
                  .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.FOOT_ARMOR))
                  .activeSlots(EquipmentSlotGroup.ARMOR)
            );
        }));
    }
}
