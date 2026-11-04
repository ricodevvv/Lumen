/*
 * This file is part of Lumen, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2025-2026 FilleDev and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ricodev.lumen.cosmetic.registry;

import org.bukkit.entity.EntityType;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.cosmetic.CosmeticBehavior;
import com.ricodev.lumen.api.cosmetic.CosmeticType;
import com.ricodev.lumen.api.cosmetic.arroweffect.ArrowEffectBehavior;
import com.ricodev.lumen.api.cosmetic.arroweffect.ArrowEffectType;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffectBehavior;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffectType;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffectBehavior;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffectType;
import com.ricodev.lumen.api.cosmetic.pet.PetBehavior;
import com.ricodev.lumen.api.cosmetic.pet.PetType;
import com.ricodev.lumen.api.cosmetic.registry.CategoryRegistries;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticRegistry;
import com.ricodev.lumen.cosmetic.arroweffect.ArrowEffectTypeImpl;
import com.ricodev.lumen.cosmetic.arroweffect.type.*;
import com.ricodev.lumen.cosmetic.deatheffect.DeathEffectTypeImpl;
import com.ricodev.lumen.cosmetic.deatheffect.type.*;
import com.ricodev.lumen.cosmetic.gadget.GadgetTypeImpl;
import com.ricodev.lumen.cosmetic.gadget.type.*;
import com.ricodev.lumen.cosmetic.particleeffect.ParticleEffectTypeImpl;
import com.ricodev.lumen.cosmetic.particleeffect.type.*;
import com.ricodev.lumen.cosmetic.particleeffect.type.Bunny;
import com.ricodev.lumen.cosmetic.particleeffect.type.Confetti;
import com.ricodev.lumen.cosmetic.pet.PetTypeImpl;
import com.ricodev.lumen.cosmetic.pet.type.*;
import com.ricodev.lumen.cosmetic.pet.type.SantaClaus;
import com.ricodev.lumen.menu.CosmeticMenuImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CategoryRegistriesImpl implements CategoryRegistries {

    private final Map<String, CosmeticCategory<?, ?, ?>> categories = new HashMap<>();

    public CategoryRegistriesImpl(Lumen plugin) {
        register(new CosmeticCategoryImpl<>(BuiltIn.ARROW_EFFECTS, ArrowEffectTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.DEATH_EFFECTS, DeathEffectTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.GADGETS, GadgetTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.PARTICLE_EFFECTS, ParticleEffectTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.PETS, PetTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));

        PetTypeImpl.BuilderImpl.UNSUPPORTED.clear();
        populateCosmetics();

        if (!PetTypeImpl.BuilderImpl.UNSUPPORTED.isEmpty()) {
            plugin.getJavaPlugin().getLogger().info("Turned off " + PetTypeImpl.BuilderImpl.UNSUPPORTED.size()
                    + " pets this Minecraft version doesn't have: " + String.join(", ", PetTypeImpl.BuilderImpl.UNSUPPORTED));
        }
    }

    private void populateCosmetics() {
        CosmeticRegistry<ArrowEffectType, ArrowEffectBehavior, ArrowEffectType.Builder> arrowEffects = arrowEffects().getCosmeticRegistry();
        arrowEffects.register(arrowEffects.builder("bloody_arrows").readFromConfig().factory(BloodyArrows::new).build());
        arrowEffects.register(arrowEffects.builder("candy_cane_arrows").readFromConfig().factory(CandyCaneArrows::new).build());
        arrowEffects.register(arrowEffects.builder("confetti_arrows").readFromConfig().factory(ConfettiArrows::new).build());
        arrowEffects.register(arrowEffects.builder("enchanted_arrows").readFromConfig().factory(EnchantedArrows::new).build());
        arrowEffects.register(arrowEffects.builder("flame_arrows").readFromConfig().factory(FlameArrows::new).build());
        arrowEffects.register(arrowEffects.builder("frost_lord_arrows").readFromConfig().factory(FrostLordArrows::new).build());
        arrowEffects.register(arrowEffects.builder("green_arrows").readFromConfig().factory(GreenArrows::new).build());
        arrowEffects.register(arrowEffects.builder("lava_arrows").readFromConfig().factory(LavaArrows::new).build());
        arrowEffects.register(arrowEffects.builder("love_arrows").readFromConfig().factory(LoveArrows::new).build());
        arrowEffects.register(arrowEffects.builder("magical_arrows").readFromConfig().factory(MagicalArrows::new).build());
        arrowEffects.register(arrowEffects.builder("music_arrows").readFromConfig().factory(MusicArrows::new).build());
        arrowEffects.register(arrowEffects.builder("rain_arrows").readFromConfig().factory(RainArrows::new).build());
        arrowEffects.register(arrowEffects.builder("shadow_arrows").readFromConfig().factory(ShadowArrows::new).build());
        arrowEffects.register(arrowEffects.builder("sparkly_arrows").readFromConfig().factory(SparklyArrows::new).build());

        CosmeticRegistry<DeathEffectType, DeathEffectBehavior, DeathEffectType.Builder> deathEffects = deathEffects().getCosmeticRegistry();
        deathEffects.register(deathEffects.builder("bloody_death").readFromConfig().factory(BloodyDeath::new).build());
        deathEffects.register(deathEffects.builder("candy_cane_remains").readFromConfig().factory(CandyCaneRemains::new).build());
        deathEffects.register(deathEffects.builder("confetti_death").readFromConfig().factory(ConfettiDeath::new).build());
        deathEffects.register(deathEffects.builder("enchanted_death").readFromConfig().factory(EnchantedDeath::new).build());
        deathEffects.register(deathEffects.builder("flame_death").readFromConfig().factory(FlameDeath::new).build());
        deathEffects.register(deathEffects.builder("fall_of_the_frost_lord").readFromConfig().factory(FallOfTheFrostLord::new).build());
        deathEffects.register(deathEffects.builder("lava_death").readFromConfig().factory(LavaDeath::new).build());
        deathEffects.register(deathEffects.builder("lightning_death").readFromConfig().factory(LightningDeath::new).build());
        deathEffects.register(deathEffects.builder("magical_death").readFromConfig().factory(MagicalDeath::new).build());
        deathEffects.register(deathEffects.builder("last_love").readFromConfig().factory(LastLove::new).build());
        deathEffects.register(deathEffects.builder("musical_death").readFromConfig().factory(MusicalDeath::new).build());
        deathEffects.register(deathEffects.builder("wet_death").readFromConfig().factory(WetDeath::new).build());
        deathEffects.register(deathEffects.builder("sparkly_death").readFromConfig().factory(SparklyDeath::new).build());
        deathEffects.register(deathEffects.builder("shadow_death").readFromConfig().factory(ShadowDeath::new).build());

        CosmeticRegistry<GadgetType, GadgetBehavior, GadgetType.Builder> gadgets = gadgets().getCosmeticRegistry();
        gadgets.register(gadgets.builder("bat_blaster").readFromConfig().factory(BatBlaster::new).build());
        gadgets.register(gadgets.builder("detonator").readFromConfig().factory(Detonator::new).build());
        gadgets.register(gadgets.builder("chicken_parachute").readFromConfig().factory(ChickenParachute::new).build());
        gadgets.register(gadgets.builder("confetti").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.Confetti::new).build());
        gadgets.register(gadgets.builder("cowboy").readFromConfig().factory(Cowboy::new).build());
        gadgets.register(gadgets.builder("ethereal_pearl").readFromConfig().factory(EtherealPearl::new).build());
        gadgets.register(gadgets.builder("explosive_sheep").readFromConfig().factory(ExplosiveSheep::new).build());
        gadgets.register(gadgets.builder("fireworks").readFromConfig().factory(Fireworks::new).build());
        gadgets.register(gadgets.builder("flame_thrower").readFromConfig().factory(FlameThrower::new).build());
        gadgets.register(gadgets.builder("flesh_hook").readFromConfig().factory(FleshHook::new).build());
        gadgets.register(gadgets.builder("grappling_hook").readFromConfig().factory(GrapplingHook::new).build());
        gadgets.register(gadgets.builder("melon_launcher").readFromConfig().readFromConfig().readFromConfig().factory(MelonLauncher::new).build());
        gadgets.register(gadgets.builder("paintball").readFromConfig().factory(Paintball::new).build());
        gadgets.register(gadgets.builder("parkour_spiral").readFromConfig().factory(ParkourSpiral::new).build());
        gadgets.register(gadgets.builder("party_popper").readFromConfig().readFromConfig().factory(PartyPopper::new).build());
        gadgets.register(gadgets.builder("soccer_ball").readFromConfig().factory(SoccerBall::new).build());
        gadgets.register(gadgets.builder("tnt").readFromConfig().factory(TNT::new).build());
        gadgets.register(gadgets.builder("water_gun").readFromConfig().factory(Watergun::new).build());
        gadgets.register(gadgets.builder("wither_missile").readFromConfig().factory(WitherMissile::new).build());

        CosmeticRegistry<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> particleEffects = particleEffects().getCosmeticRegistry();
        particleEffects.register(particleEffects.builder("angel_wings").readFromConfig().factory(AngelWings::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("aura_of_flame").readFromConfig().factory(AuraOfFlame::new).build());
        particleEffects.register(particleEffects.builder("big_heart").readFromConfig().factory(BigHeart::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("black_hole").readFromConfig().factory(BlackHole::new).build());
        particleEffects.register(particleEffects.builder("blood_helix").readFromConfig().factory(BloodHelix::new).build());
        particleEffects.register(particleEffects.builder("bunny").readFromConfig().factory(Bunny::new).build());
        particleEffects.register(particleEffects.builder("christmas_tree").readFromConfig().factory(ChristmasTree::new).build());
        particleEffects.register(particleEffects.builder("colorful_trail").readFromConfig().factory(ColorfulTrail::new).build());
        particleEffects.register(particleEffects.builder("confetti").readFromConfig().factory(Confetti::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("crushed_candy_cane").readFromConfig().factory(CrushedCandyCane::new).build());
        particleEffects.register(particleEffects.builder("demon_twist").readFromConfig().factory(DemonTwist::new).build());
        particleEffects.register(particleEffects.builder("emerald_twirl").readFromConfig().factory(EmeraldTwirl::new).build());
        particleEffects.register(particleEffects.builder("enchanted").readFromConfig().factory(Enchanted::new).build());
        particleEffects.register(particleEffects.builder("flame_fairy").readFromConfig().factory(FlameFairy::new).build());
        particleEffects.register(particleEffects.builder("flame_of_magic").readFromConfig().factory(FlameOfMagic::new).build());
        particleEffects.register(particleEffects.builder("flame_of_the_demons").readFromConfig().factory(FlameOfTheDemons::new).build());
        particleEffects.register(particleEffects.builder("flame_rings").readFromConfig().factory(FlameRings::new).build());
        particleEffects.register(particleEffects.builder("frost_lord").readFromConfig().factory(FrostLord::new).build());
        particleEffects.register(particleEffects.builder("in_love").readFromConfig().factory(InLove::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("jack_o_lantern").readFromConfig().factory(JackOLantern::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("legendary_aura").readFromConfig().factory(LegendaryAura::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("music").readFromConfig().factory(Music::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("party_time").readFromConfig().factory(PartyTime::new).build());
        particleEffects.register(particleEffects.builder("protective_shield").readFromConfig().factory(ProtectiveShield::new).build());
        particleEffects.register(particleEffects.builder("rainbow_wings").readFromConfig().factory(RainbowWings::new).build());
        particleEffects.register(particleEffects.builder("rain_cloud").readFromConfig().factory(RainCloud::new).build());
        particleEffects.register(particleEffects.builder("santa").readFromConfig().factory(Santa::new).build());
        particleEffects.register(particleEffects.builder("shadow_walk").readFromConfig().factory(ShadowWalk::new).repeatDelay(5).build());
        particleEffects.register(particleEffects.builder("skull").readFromConfig().factory(Skull::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("snow_cloud").readFromConfig().factory(SnowCloud::new).build());
        particleEffects.register(particleEffects.builder("snowflake").readFromConfig().factory(Snowflake::new).build());
        particleEffects.register(particleEffects.builder("star").readFromConfig().factory(Star::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("tornado").readFromConfig().factory(Tornado::new).build());
        particleEffects.register(particleEffects.builder("umbrella").readFromConfig().factory(Umbrella::new).build());
        particleEffects.register(particleEffects.builder("vampire_wings").readFromConfig().factory(VampireWings::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("yin_and_yang").readFromConfig().factory(YinAndYang::new).build());

        CosmeticRegistry<PetType, PetBehavior, PetType.Builder> pets = pets().getCosmeticRegistry();
        pets.register(pets.builder("body_guard").readFromConfig().factory(BodyGuard::new).build());
        pets.register(pets.builder("colorful_lamb").readFromConfig().factory(ColorfulLamb::new).build());
        pets.register(pets.builder("elf").readFromConfig().factory(Elf::new).build());
        pets.register(pets.builder("santa_claus").readFromConfig().factory(SantaClaus::new).build());
        pets.register(pets.builder("villager").readFromConfig().factory(com.ricodev.lumen.cosmetic.pet.type.Villager::new).build());
        for (String key : pets().getConfig().getSectionKeys("cosmetics")) {
            if (!pets.isRegistered(key)) {
                pets.register(pets.builder(key).readFromConfig().factory(DefaultPet::new).build());
            }
        }
    }

    @Override
    public <T extends CosmeticType<T, B>, B extends CosmeticBehavior<T>, U extends CosmeticType.Builder<T, B, U>>
    void register(CosmeticCategory<T, B, U> category) {
        categories.put(category.getKey().toLowerCase(), category);
    }

    @Override
    public CosmeticCategory<?, ?, ?> getCategoryRaw(String key) {
        return categories.get(key.toLowerCase());
    }

    @Override
    public Collection<CosmeticCategory<?, ?, ?>> getCategories() {
        return categories.values();
    }
}
