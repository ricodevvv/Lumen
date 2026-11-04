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
import com.ricodev.lumen.cosmetic.deatheffect.DeathEffectTypeImpl;
import com.ricodev.lumen.cosmetic.gadget.GadgetTypeImpl;
import com.ricodev.lumen.cosmetic.particleeffect.ParticleEffectTypeImpl;
import com.ricodev.lumen.cosmetic.pet.PetTypeImpl;
import com.ricodev.lumen.menu.CosmeticMenuImpl;
import com.ricodev.lumen.menu.menus.StatusMenu;
import com.ricodev.lumen.menu.menus.MorphMenu;

import com.ricodev.lumen.api.cosmetic.banner.BannerBehavior;
import com.ricodev.lumen.api.cosmetic.banner.BannerType;
import com.ricodev.lumen.cosmetic.banner.BannerTypeImpl;
import com.ricodev.lumen.api.cosmetic.emote.EmoteBehavior;
import com.ricodev.lumen.api.cosmetic.emote.EmoteType;
import com.ricodev.lumen.cosmetic.emote.EmoteTypeImpl;
import com.ricodev.lumen.api.cosmetic.status.StatusBehavior;
import com.ricodev.lumen.api.cosmetic.status.StatusType;
import com.ricodev.lumen.cosmetic.status.StatusTypeImpl;
import com.ricodev.lumen.api.cosmetic.balloon.BalloonBehavior;
import com.ricodev.lumen.api.cosmetic.balloon.BalloonType;
import com.ricodev.lumen.cosmetic.balloon.BalloonTypeImpl;
import com.ricodev.lumen.api.cosmetic.music.MusicBehavior;
import com.ricodev.lumen.api.cosmetic.music.MusicType;
import com.ricodev.lumen.cosmetic.music.MusicTypeImpl;
import com.ricodev.lumen.api.cosmetic.miniature.MiniatureBehavior;
import com.ricodev.lumen.api.cosmetic.miniature.MiniatureType;
import com.ricodev.lumen.cosmetic.miniature.MiniatureTypeImpl;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.cosmetic.mount.MountTypeImpl;
import com.ricodev.lumen.util.entity.Entities;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.cosmetic.morph.MorphTypeImpl;
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
        register(new CosmeticCategoryImpl<>(BuiltIn.MORPHS, MorphTypeImpl.BuilderImpl::new, MorphMenu::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.MOUNTS, MountTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.MINIATURES, MiniatureTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.MUSIC, MusicTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.BALLOONS, BalloonTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.STATUSES, StatusTypeImpl.BuilderImpl::new, StatusMenu::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.EMOTES, EmoteTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));
        register(new CosmeticCategoryImpl<>(BuiltIn.BANNERS, BannerTypeImpl.BuilderImpl::new, CosmeticMenuImpl::new));

        PetTypeImpl.BuilderImpl.UNSUPPORTED.clear();
        populateCosmetics();

        if (!PetTypeImpl.BuilderImpl.UNSUPPORTED.isEmpty()) {
            plugin.getJavaPlugin().getLogger().info("Turned off " + PetTypeImpl.BuilderImpl.UNSUPPORTED.size()
                    + " pets this Minecraft version doesn't have: " + String.join(", ", PetTypeImpl.BuilderImpl.UNSUPPORTED));
        }
    }

    private void populateCosmetics() {
        CosmeticRegistry<MorphType, MorphBehavior, MorphType.Builder> morphs = morphs().getCosmeticRegistry();
        morphs.register(morphs.builder("bat").entityType(Entities.type("BAT")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Bat::new).build());
        morphs.register(morphs.builder("blaze").entityType(Entities.type("BLAZE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Blaze::new).build());
        morphs.register(morphs.builder("block").entityType(Entities.type("ARMOR_STAND")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Block::new).build());
        morphs.register(morphs.builder("bunny").entityType(Entities.type("RABBIT")).readFromConfig().readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Bunny::new).build());
        morphs.register(morphs.builder("cat").entityType(Entities.type("CAT", "OCELOT")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Cat::new).build());
        morphs.register(morphs.builder("cave_spider").entityType(Entities.type("CAVE_SPIDER")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.CaveSpider::new).build());
        morphs.register(morphs.builder("chicken").entityType(Entities.type("CHICKEN")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Chicken::new).build());
        morphs.register(morphs.builder("cow").entityType(Entities.type("COW")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Cow::new).build());
        morphs.register(morphs.builder("creeper").entityType(Entities.type("CREEPER")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Creeper::new).build());
        morphs.register(morphs.builder("dog").entityType(Entities.type("WOLF")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Dog::new).build());
        morphs.register(morphs.builder("donkey").entityType(Entities.type("DONKEY", "HORSE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Donkey::new).build());
        morphs.register(morphs.builder("elder_guardian").entityType(Entities.type("ELDER_GUARDIAN", "GUARDIAN")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.ElderGuardian::new).build());
        morphs.register(morphs.builder("enderman").entityType(Entities.type("ENDERMAN")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Enderman::new).build());
        morphs.register(morphs.builder("iron_golem").entityType(Entities.type("IRON_GOLEM")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.IronGolem::new).build());
        morphs.register(morphs.builder("magma_cube").entityType(Entities.type("MAGMA_CUBE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.MagmaCube::new).build());
        morphs.register(morphs.builder("mushroom_cow").entityType(Entities.type("MOOSHROOM", "MUSHROOM_COW")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.MushroomCow::new).build());
        morphs.register(morphs.builder("olaf").entityType(Entities.type("SNOW_GOLEM", "SNOWMAN")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Olaf::new).build());
        morphs.register(morphs.builder("pig").entityType(Entities.type("PIG")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Pig::new).build());
        morphs.register(morphs.builder("piglin").entityType(Entities.type("PIGLIN", "PIG_ZOMBIE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Piglin::new).build());
        morphs.register(morphs.builder("sheep").entityType(Entities.type("SHEEP")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Sheep::new).build());
        morphs.register(morphs.builder("skeleton").entityType(Entities.type("SKELETON")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Skeleton::new).build());
        morphs.register(morphs.builder("slime").entityType(Entities.type("SLIME")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Slime::new).build());
        morphs.register(morphs.builder("spider").entityType(Entities.type("SPIDER")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Spider::new).build());
        morphs.register(morphs.builder("squid").entityType(Entities.type("SQUID")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Squid::new).build());
        morphs.register(morphs.builder("villager").entityType(Entities.type("VILLAGER")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Villager::new).build());
        morphs.register(morphs.builder("witch").entityType(Entities.type("WITCH")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Witch::new).build());
        morphs.register(morphs.builder("wither").entityType(Entities.type("WITHER")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Wither::new).build());
        morphs.register(morphs.builder("zombie").entityType(Entities.type("ZOMBIE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.morph.type.Zombie::new).build());

        CosmeticRegistry<MountType, MountBehavior, MountType.Builder> mounts = mounts().getCosmeticRegistry();
        mounts.register(mounts.builder("baby_reindeer").entityType(Entities.type("HORSE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.BabyReindeer::new).build());
        mounts.register(mounts.builder("cave_spider").entityType(Entities.type("CAVE_SPIDER")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.CaveSpider::new).build());
        mounts.register(mounts.builder("clumsy_mule").entityType(Entities.type("MULE", "HORSE")).readFromConfig().readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.ClumsyMule::new).build());
        mounts.register(mounts.builder("crazy_chicken").entityType(Entities.type("CHICKEN")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.CrazyChicken::new).build());
        mounts.register(mounts.builder("decrepit_warhorse").entityType(Entities.type("ZOMBIE_HORSE", "HORSE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.DecrepitWarhorse::new).build());
        mounts.register(mounts.builder("ethereal_dragon").entityType(Entities.type("ENDER_DRAGON")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.EtherealDragon::new).build());
        mounts.register(mounts.builder("frosty_snowman").entityType(Entities.type("SNOW_GOLEM", "SNOWMAN")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.FrostySnowman::new).build());
        mounts.register(mounts.builder("glacial_steed").entityType(Entities.type("HORSE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.GlacialSteed::new).build());
        mounts.register(mounts.builder("infernal_horror").entityType(Entities.type("SKELETON_HORSE", "HORSE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.InfernalHorror::new).build());
        mounts.register(mounts.builder("lovely_sheep").entityType(Entities.type("SHEEP")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.LovelySheep::new).build());
        mounts.register(mounts.builder("molten_snake").entityType(Entities.type("MAGMA_CUBE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.MoltenSnake::new).build());
        mounts.register(mounts.builder("pirate_ship").entityType(Entities.type("OAK_BOAT", "BOAT")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.PirateShip::new).build());
        mounts.register(mounts.builder("rudolf").entityType(Entities.type("HORSE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.Rudolf::new).build());
        mounts.register(mounts.builder("slime").entityType(Entities.type("SLIME")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.Slime::new).build());
        mounts.register(mounts.builder("unicorn").entityType(Entities.type("HORSE")).readFromConfig().factory(com.ricodev.lumen.cosmetic.mount.type.Unicorn::new).build());

        CosmeticRegistry<MiniatureType, MiniatureBehavior, MiniatureType.Builder> miniatures = miniatures().getCosmeticRegistry();
        miniatures.register(miniatures.builder("astronaut").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Astronaut::new).build());
        miniatures.register(miniatures.builder("brown_bear").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.BrownBear::new).build());
        miniatures.register(miniatures.builder("bunny").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Bunny::new).build());
        miniatures.register(miniatures.builder("chicken").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Chicken::new).build());
        miniatures.register(miniatures.builder("cow").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Cow::new).build());
        miniatures.register(miniatures.builder("devil").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Devil::new).build());
        miniatures.register(miniatures.builder("doge").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Doge::new).build());
        miniatures.register(miniatures.builder("dolphin").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Dolphin::new).build());
        miniatures.register(miniatures.builder("dwarf").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Dwarf::new).build());
        miniatures.register(miniatures.builder("elephant").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Elephant::new).build());
        miniatures.register(miniatures.builder("enderman").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Enderman::new).build());
        miniatures.register(miniatures.builder("fox").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Fox::new).build());
        miniatures.register(miniatures.builder("ghost").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Ghost::new).build());
        miniatures.register(miniatures.builder("iron_golem").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.IronGolem::new).build());
        miniatures.register(miniatures.builder("koala_bear").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.KoalaBear::new).build());
        miniatures.register(miniatures.builder("ladybug").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Ladybug::new).build());
        miniatures.register(miniatures.builder("monkey").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Monkey::new).build());
        miniatures.register(miniatures.builder("panda").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Panda::new).build());
        miniatures.register(miniatures.builder("parrot").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Parrot::new).build());
        miniatures.register(miniatures.builder("santa_claus").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.SantaClaus::new).build());
        miniatures.register(miniatures.builder("shark").readFromConfig().readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Shark::new).build());
        miniatures.register(miniatures.builder("sheep").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Sheep::new).build());
        miniatures.register(miniatures.builder("skeleton").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Skeleton::new).build());
        miniatures.register(miniatures.builder("snail").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Snail::new).build());
        miniatures.register(miniatures.builder("snowman").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Snowman::new).build());
        miniatures.register(miniatures.builder("spider").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Spider::new).build());
        miniatures.register(miniatures.builder("tiger").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Tiger::new).build());
        miniatures.register(miniatures.builder("tiki").readFromConfig().readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Tiki::new).build());
        miniatures.register(miniatures.builder("turtle").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Turtle::new).build());
        miniatures.register(miniatures.builder("vampire").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Vampire::new).build());
        miniatures.register(miniatures.builder("witch").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Witch::new).build());
        miniatures.register(miniatures.builder("zebra").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Zebra::new).build());
        miniatures.register(miniatures.builder("zombie").readFromConfig().factory(com.ricodev.lumen.cosmetic.miniature.type.Zombie::new).build());

        CosmeticRegistry<MusicType, MusicBehavior, MusicType.Builder> music = music().getCosmeticRegistry();
        for (String key : music().getConfig().getSectionKeys("cosmetics")) {
            if (!music.isRegistered(key)) {
                music.register(music.builder(key).readFromConfig().factory(com.ricodev.lumen.cosmetic.music.type.DefaultMusic::new).build());
            }
        }

        CosmeticRegistry<BalloonType, BalloonBehavior, BalloonType.Builder> balloons = balloons().getCosmeticRegistry();
        for (String key : balloons().getConfig().getSectionKeys("cosmetics")) {
            if (!balloons.isRegistered(key)) {
                balloons.register(balloons.builder(key).readFromConfig().factory(com.ricodev.lumen.cosmetic.balloon.type.DefaultBalloon::new).build());
            }
        }

        CosmeticRegistry<StatusType, StatusBehavior, StatusType.Builder> statuses = statuses().getCosmeticRegistry();
        for (String key : statuses().getConfig().getSectionKeys("cosmetics")) {
            if (!statuses.isRegistered(key)) {
                statuses.register(statuses.builder(key).readFromConfig().factory(com.ricodev.lumen.cosmetic.status.type.DefaultStatus::new).build());
            }
        }

        CosmeticRegistry<EmoteType, EmoteBehavior, EmoteType.Builder> emotes = emotes().getCosmeticRegistry();
        emotes.register(emotes.builder("in_love").readFromConfig().factory(com.ricodev.lumen.cosmetic.emote.type.InLove::new).build());
        emotes.register(emotes.builder("rage").readFromConfig().factory(com.ricodev.lumen.cosmetic.emote.type.Rage::new).build());
        emotes.register(emotes.builder("sad").readFromConfig().factory(com.ricodev.lumen.cosmetic.emote.type.Sad::new).build());
        emotes.register(emotes.builder("wink").readFromConfig().factory(com.ricodev.lumen.cosmetic.emote.type.Wink::new).build());
        for (String key : emotes().getConfig().getSectionKeys("cosmetics")) {
            if (!emotes.isRegistered(key)) {
                emotes.register(emotes.builder(key).readFromConfig().factory(com.ricodev.lumen.cosmetic.emote.type.DefaultEmote::new).build());
            }
        }

        CosmeticRegistry<BannerType, BannerBehavior, BannerType.Builder> banners = banners().getCosmeticRegistry();
        for (String key : banners().getConfig().getSectionKeys("cosmetics")) {
            if (!banners.isRegistered(key)) {
                banners.register(banners.builder(key).readFromConfig().factory(com.ricodev.lumen.cosmetic.banner.type.DefaultBanner::new).build());
            }
        }

        CosmeticRegistry<ArrowEffectType, ArrowEffectBehavior, ArrowEffectType.Builder> arrowEffects = arrowEffects().getCosmeticRegistry();
        arrowEffects.register(arrowEffects.builder("bloody_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.BloodyArrows::new).build());
        arrowEffects.register(arrowEffects.builder("candy_cane_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.CandyCaneArrows::new).build());
        arrowEffects.register(arrowEffects.builder("confetti_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.ConfettiArrows::new).build());
        arrowEffects.register(arrowEffects.builder("enchanted_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.EnchantedArrows::new).build());
        arrowEffects.register(arrowEffects.builder("flame_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.FlameArrows::new).build());
        arrowEffects.register(arrowEffects.builder("frost_lord_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.FrostLordArrows::new).build());
        arrowEffects.register(arrowEffects.builder("green_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.GreenArrows::new).build());
        arrowEffects.register(arrowEffects.builder("lava_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.LavaArrows::new).build());
        arrowEffects.register(arrowEffects.builder("love_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.LoveArrows::new).build());
        arrowEffects.register(arrowEffects.builder("magical_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.MagicalArrows::new).build());
        arrowEffects.register(arrowEffects.builder("music_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.MusicArrows::new).build());
        arrowEffects.register(arrowEffects.builder("rain_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.RainArrows::new).build());
        arrowEffects.register(arrowEffects.builder("shadow_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.ShadowArrows::new).build());
        arrowEffects.register(arrowEffects.builder("sparkly_arrows").readFromConfig().factory(com.ricodev.lumen.cosmetic.arroweffect.type.SparklyArrows::new).build());

        CosmeticRegistry<DeathEffectType, DeathEffectBehavior, DeathEffectType.Builder> deathEffects = deathEffects().getCosmeticRegistry();
        deathEffects.register(deathEffects.builder("bloody_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.BloodyDeath::new).build());
        deathEffects.register(deathEffects.builder("candy_cane_remains").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.CandyCaneRemains::new).build());
        deathEffects.register(deathEffects.builder("confetti_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.ConfettiDeath::new).build());
        deathEffects.register(deathEffects.builder("enchanted_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.EnchantedDeath::new).build());
        deathEffects.register(deathEffects.builder("flame_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.FlameDeath::new).build());
        deathEffects.register(deathEffects.builder("fall_of_the_frost_lord").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.FallOfTheFrostLord::new).build());
        deathEffects.register(deathEffects.builder("lava_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.LavaDeath::new).build());
        deathEffects.register(deathEffects.builder("lightning_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.LightningDeath::new).build());
        deathEffects.register(deathEffects.builder("magical_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.MagicalDeath::new).build());
        deathEffects.register(deathEffects.builder("last_love").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.LastLove::new).build());
        deathEffects.register(deathEffects.builder("musical_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.MusicalDeath::new).build());
        deathEffects.register(deathEffects.builder("wet_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.WetDeath::new).build());
        deathEffects.register(deathEffects.builder("sparkly_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.SparklyDeath::new).build());
        deathEffects.register(deathEffects.builder("shadow_death").readFromConfig().factory(com.ricodev.lumen.cosmetic.deatheffect.type.ShadowDeath::new).build());

        CosmeticRegistry<GadgetType, GadgetBehavior, GadgetType.Builder> gadgets = gadgets().getCosmeticRegistry();
        gadgets.register(gadgets.builder("bat_blaster").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.BatBlaster::new).build());
        gadgets.register(gadgets.builder("detonator").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.Detonator::new).build());
        gadgets.register(gadgets.builder("chicken_parachute").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.ChickenParachute::new).build());
        gadgets.register(gadgets.builder("confetti").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.Confetti::new).build());
        gadgets.register(gadgets.builder("cowboy").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.Cowboy::new).build());
        gadgets.register(gadgets.builder("ethereal_pearl").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.EtherealPearl::new).build());
        gadgets.register(gadgets.builder("explosive_sheep").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.ExplosiveSheep::new).build());
        gadgets.register(gadgets.builder("fireworks").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.Fireworks::new).build());
        gadgets.register(gadgets.builder("flame_thrower").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.FlameThrower::new).build());
        gadgets.register(gadgets.builder("flesh_hook").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.FleshHook::new).build());
        gadgets.register(gadgets.builder("grappling_hook").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.GrapplingHook::new).build());
        gadgets.register(gadgets.builder("melon_launcher").readFromConfig().readFromConfig().readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.MelonLauncher::new).build());
        gadgets.register(gadgets.builder("paintball").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.Paintball::new).build());
        gadgets.register(gadgets.builder("parkour_spiral").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.ParkourSpiral::new).build());
        gadgets.register(gadgets.builder("party_popper").readFromConfig().readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.PartyPopper::new).build());
        gadgets.register(gadgets.builder("soccer_ball").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.SoccerBall::new).build());
        gadgets.register(gadgets.builder("tnt").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.TNT::new).build());
        gadgets.register(gadgets.builder("water_gun").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.Watergun::new).build());
        gadgets.register(gadgets.builder("wither_missile").readFromConfig().factory(com.ricodev.lumen.cosmetic.gadget.type.WitherMissile::new).build());

        CosmeticRegistry<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> particleEffects = particleEffects().getCosmeticRegistry();
        particleEffects.register(particleEffects.builder("angel_wings").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.AngelWings::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("aura_of_flame").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.AuraOfFlame::new).build());
        particleEffects.register(particleEffects.builder("big_heart").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.BigHeart::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("black_hole").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.BlackHole::new).build());
        particleEffects.register(particleEffects.builder("blood_helix").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.BloodHelix::new).build());
        particleEffects.register(particleEffects.builder("bunny").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Bunny::new).build());
        particleEffects.register(particleEffects.builder("christmas_tree").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.ChristmasTree::new).build());
        particleEffects.register(particleEffects.builder("colorful_trail").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.ColorfulTrail::new).build());
        particleEffects.register(particleEffects.builder("confetti").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Confetti::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("crushed_candy_cane").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.CrushedCandyCane::new).build());
        particleEffects.register(particleEffects.builder("demon_twist").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.DemonTwist::new).build());
        particleEffects.register(particleEffects.builder("emerald_twirl").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.EmeraldTwirl::new).build());
        particleEffects.register(particleEffects.builder("enchanted").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Enchanted::new).build());
        particleEffects.register(particleEffects.builder("flame_fairy").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.FlameFairy::new).build());
        particleEffects.register(particleEffects.builder("flame_of_magic").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.FlameOfMagic::new).build());
        particleEffects.register(particleEffects.builder("flame_of_the_demons").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.FlameOfTheDemons::new).build());
        particleEffects.register(particleEffects.builder("flame_rings").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.FlameRings::new).build());
        particleEffects.register(particleEffects.builder("frost_lord").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.FrostLord::new).build());
        particleEffects.register(particleEffects.builder("in_love").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.InLove::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("jack_o_lantern").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.JackOLantern::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("legendary_aura").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.LegendaryAura::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("music").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Music::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("party_time").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.PartyTime::new).build());
        particleEffects.register(particleEffects.builder("protective_shield").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.ProtectiveShield::new).build());
        particleEffects.register(particleEffects.builder("rainbow_wings").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.RainbowWings::new).build());
        particleEffects.register(particleEffects.builder("rain_cloud").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.RainCloud::new).build());
        particleEffects.register(particleEffects.builder("santa").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Santa::new).build());
        particleEffects.register(particleEffects.builder("shadow_walk").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.ShadowWalk::new).repeatDelay(5).build());
        particleEffects.register(particleEffects.builder("skull").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Skull::new).repeatDelay(1).build());
        particleEffects.register(particleEffects.builder("snow_cloud").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.SnowCloud::new).build());
        particleEffects.register(particleEffects.builder("snowflake").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Snowflake::new).build());
        particleEffects.register(particleEffects.builder("star").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Star::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("tornado").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Tornado::new).build());
        particleEffects.register(particleEffects.builder("umbrella").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.Umbrella::new).build());
        particleEffects.register(particleEffects.builder("vampire_wings").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.VampireWings::new).repeatDelay(2).build());
        particleEffects.register(particleEffects.builder("yin_and_yang").readFromConfig().factory(com.ricodev.lumen.cosmetic.particleeffect.type.YinAndYang::new).build());

        CosmeticRegistry<PetType, PetBehavior, PetType.Builder> pets = pets().getCosmeticRegistry();
        pets.register(pets.builder("body_guard").readFromConfig().factory(com.ricodev.lumen.cosmetic.pet.type.BodyGuard::new).build());
        pets.register(pets.builder("colorful_lamb").readFromConfig().factory(com.ricodev.lumen.cosmetic.pet.type.ColorfulLamb::new).build());
        pets.register(pets.builder("elf").readFromConfig().factory(com.ricodev.lumen.cosmetic.pet.type.Elf::new).build());
        pets.register(pets.builder("santa_claus").readFromConfig().factory(com.ricodev.lumen.cosmetic.pet.type.SantaClaus::new).build());
        pets.register(pets.builder("villager").readFromConfig().factory(com.ricodev.lumen.cosmetic.pet.type.Villager::new).build());
        for (String key : pets().getConfig().getSectionKeys("cosmetics")) {
            if (!pets.isRegistered(key)) {
                pets.register(pets.builder(key).readFromConfig().factory(com.ricodev.lumen.cosmetic.pet.type.DefaultPet::new).build());
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
