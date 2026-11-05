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
package com.ricodev.lumen.util.entity;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.entity.EntityTracker;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.util.MetadataUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * {@link CosmeticEntity} backed by a real entity with no AI. ProCosmetics drove these through
 * NMS; here everything goes through the Bukkit API (plus {@link Compats}) so 1.8 works too.
 */
public class CosmeticEntityImpl implements CosmeticEntity {

    private static final int HELMET = 0;
    private static final int CHEST = 1;
    private static final int LEGS = 2;
    private static final int BOOTS = 3;
    private static final int MAIN_HAND = 4;
    private static final int OFF_HAND = 5;

    private final World world;
    private final EntityType type;
    private final EntityTracker tracker;
    private final boolean keepAI;
    private final ItemStack[] equipment = new ItemStack[6];
    private final EulerAngle[] poses = new EulerAngle[6];
    private final boolean[] equipmentSet = new boolean[6];

    private Entity entity;
    private Location location;
    private Component customName;
    private Entity leashHolder;
    private Boolean creeperPowered;
    private ItemStack itemStack;
    private Vector pendingVelocity;
    private float rideSpeed = 0.3f;
    private boolean itemHolder;
    private double verticalSpeed;

    public CosmeticEntityImpl(World world, EntityType type, @Nullable EntityTracker tracker, boolean keepAI) {
        this.world = world;
        this.type = type;
        this.tracker = tracker != null ? tracker : new EntityTrackerImpl();
        this.keepAI = keepAI;
        this.tracker.addEntity(this);
    }

    CosmeticEntityImpl(Entity existing) {
        this.world = existing.getWorld();
        this.type = existing.getType();
        this.tracker = new EntityTrackerImpl();
        this.keepAI = true;
        this.entity = existing;
        this.location = existing.getLocation();
        this.tracker.addEntity(this);
    }

    void spawnIfNeeded() {
        if (entity == null && location != null) {
            spawn();
        }
    }

    void despawn() {
        if (entity != null) {
            if (entity instanceof LivingEntity && leashHolder != null) {
                ((LivingEntity) entity).setLeashHolder(null);
            }
            entity.remove();
            entity = null;
        }
    }

    void respawn() {
        despawn();
        spawnIfNeeded();
    }

    private void spawn() {
        Location spawnAt = location.clone();

        if (spawnAt.getWorld() == null) {
            spawnAt.setWorld(world);
        }
        if (isItem()) {
            ItemStack item = itemStack != null ? itemStack : XMaterial.STONE.parseItem();
            Item dropped = spawnAt.getWorld().dropItem(spawnAt, item);
            dropped.setPickupDelay(Integer.MAX_VALUE);
            entity = dropped;
        } else {
            entity = spawnAt.getWorld().spawnEntity(spawnAt, type);
        }
        MetadataUtil.setCustomEntity(entity);
        Compats.get().setSilent(entity, true);

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            living.setRemoveWhenFarAway(false);

            if (!keepAI) {
                Compats.get().setAI(living, false);
                Compats.get().setGravity(living, false);
            }
        }
        if (entity instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) entity;
            stand.setGravity(false);

            if (itemHolder) {
                stand.setVisible(false);
                stand.setSmall(true);
                stand.setMarker(true);
                stand.setBasePlate(false);
            }
        }
        applyPending();

        for (UUID uuid : ((EntityTrackerImpl) tracker).getAntiViewers()) {
            Player player = entity.getServer().getPlayer(uuid);

            if (player != null) {
                Compats.get().hideEntity(LumenPlugin.getPlugin(), player, entity);
            }
        }
    }

    private void applyPending() {
        for (int slot = 0; slot < equipment.length; slot++) {
            if (equipmentSet[slot]) {
                applyEquipment(slot);
            }
            if (poses[slot] != null) {
                applyPose(slot);
            }
        }
        if (customName != null) {
            applyName();
        }
        if (leashHolder != null && entity instanceof LivingEntity) {
            ((LivingEntity) entity).setLeashHolder(leashHolder);
        }
        if (creeperPowered != null && entity instanceof Creeper) {
            ((Creeper) entity).setPowered(creeperPowered);
        }
        if (itemStack != null && entity instanceof Item) {
            ((Item) entity).setItemStack(itemStack);
        }
        if (pendingVelocity != null) {
            entity.setVelocity(pendingVelocity);
            pendingVelocity = null;
        }
    }

    private boolean isItem() {
        return type.name().equals("DROPPED_ITEM") || type.name().equals("ITEM");
    }

    @Override
    public void setPositionRotation(Location location) {
        this.location = location.clone();

        if (entity == null) {
            if (!isItem() || tracker.isTracking()) {
                spawn();
            }
        } else {
            entity.teleport(this.location);
        }
    }

    @Override
    public void sendPositionRotationPacket(Location location) {
        this.location = location.clone();

        if (entity != null && Compats.get().getPassengers(entity).isEmpty()) {
            entity.teleport(this.location);
        }
    }

    @Override
    public @Nullable Location getPreviousLocation() {
        if (entity != null && entity.isValid()) {
            return entity.getLocation();
        }
        return location == null ? null : location.clone();
    }

    @Override
    public Location getPreviousLocation(Location out) {
        Location current = getPreviousLocation();

        if (current != null) {
            out.setWorld(current.getWorld());
            out.setX(current.getX());
            out.setY(current.getY());
            out.setZ(current.getZ());
            out.setYaw(current.getYaw());
            out.setPitch(current.getPitch());
        }
        return out;
    }

    @Override
    public void setYaw(float yaw) {
        if (location != null) {
            location.setYaw(yaw);
            sendPositionRotationPacket(location);
        }
    }

    @Override
    public void setPitch(float pitch) {
        if (location != null) {
            location.setPitch(pitch);
            sendPositionRotationPacket(location);
        }
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        Vector velocity = new Vector(x, y, z);

        if (entity == null) {
            pendingVelocity = velocity;
            return;
        }
        Compats.get().setGravity(entity, true);
        entity.setVelocity(velocity);
    }

    @Override
    public void move(Vector vector) {
        Location current = getPreviousLocation();

        if (current == null) {
            return;
        }
        current.add(vector);

        if (entity == null || Compats.get().getPassengers(entity).isEmpty()) {
            sendPositionRotationPacket(current);
            return;
        }

        if (location != null) {
            current.setYaw(location.getYaw());
            current.setPitch(location.getPitch());
        }
        location = current.clone();
        Compats.get().teleportWithPassengers(entity, current);
    }

    @Override
    public void follow(Player player) {
        if (entity != null && Compats.get().getPassengers(entity).isEmpty()) {
            Entities.stepTowards(entity, player.getLocation(), 2.5d, 0.32d);
            location = entity.getLocation();
        }
    }

    /**
     * Pushes the mount with velocity instead of teleporting it, because a vehicle with a
     * passenger can't be teleported on most versions.
     */
    @Override
    public void moveRide(Player player) {
        if (entity == null) {
            return;
        }
        RideInput.Input input = RideInput.get(player.getUniqueId());
        float forward = input.getForward();
        float strafe = input.getStrafe();

        if (forward < 0.0f) {
            forward *= 0.5f;
        }
        double yaw = Math.toRadians(player.getLocation().getYaw());
        double dirX = -Math.sin(yaw);
        double dirZ = Math.cos(yaw);
        double x = (dirX * forward + dirZ * strafe) * rideSpeed;
        double z = (dirZ * forward - dirX * strafe) * rideSpeed;
        boolean onGround = isOnGround();

        if (onGround) {
            verticalSpeed = input.isJump() ? 0.5d : 0.0d;
        } else {
            verticalSpeed = Math.max(-0.8d, verticalSpeed - 0.08d);
        }
        Compats.get().setGravity(entity, true);
        entity.setVelocity(new Vector(x, onGround && !input.isJump() ? entity.getVelocity().getY() : verticalSpeed, z));
        location = entity.getLocation();
    }

    private boolean isOnGround() {
        Block below = entity.getLocation().subtract(0.0d, 0.1d, 0.0d).getBlock();
        return below.getType().isSolid();
    }

    @Override
    public float getRideSpeed() {
        return rideSpeed;
    }

    @Override
    public void setRideSpeed(float speed) {
        this.rideSpeed = speed;
    }

    private void setEquipment(int slot, @Nullable ItemStack itemStack) {
        equipment[slot] = itemStack;
        equipmentSet[slot] = true;

        if (entity != null) {
            applyEquipment(slot);
        }
    }

    @SuppressWarnings("deprecation")
    private void applyEquipment(int slot) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        EntityEquipment entityEquipment = ((LivingEntity) entity).getEquipment();

        if (entityEquipment == null) {
            return;
        }
        ItemStack item = equipment[slot];

        switch (slot) {
            case HELMET:
                entityEquipment.setHelmet(item);
                break;
            case CHEST:
                entityEquipment.setChestplate(item);
                break;
            case LEGS:
                entityEquipment.setLeggings(item);
                break;
            case BOOTS:
                entityEquipment.setBoots(item);
                break;
            case MAIN_HAND:
                Compats.get().setMainHand((LivingEntity) entity, item);
                break;
            default:
                Compats.get().setOffHand((LivingEntity) entity, item);
                break;
        }
    }

    /**
     * Makes this armor stand an invisible holder for one item on its head, which is what block
     * and item display entities turn into on versions that don't have them.
     */
    public CosmeticEntityImpl asItemHolder(@Nullable ItemStack itemStack) {
        itemHolder = true;

        if (itemStack != null) {
            setHelmet(itemStack);
        }
        return this;
    }

    @Override
    public void setMainHand(@Nullable ItemStack itemStack) {
        setEquipment(MAIN_HAND, itemStack);
    }

    @Override
    public void setOffHand(@Nullable ItemStack itemStack) {
        setEquipment(OFF_HAND, itemStack);
    }

    @Override
    public void setHelmet(@Nullable ItemStack itemStack) {
        setEquipment(HELMET, itemStack);
    }

    @Override
    public void setChestplate(@Nullable ItemStack itemStack) {
        setEquipment(CHEST, itemStack);
    }

    @Override
    public void setLeggings(@Nullable ItemStack itemStack) {
        setEquipment(LEGS, itemStack);
    }

    @Override
    public void setBoots(@Nullable ItemStack itemStack) {
        setEquipment(BOOTS, itemStack);
    }

    private void setPose(int part, float x, float y, float z) {
        poses[part] = new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));

        if (entity != null) {
            applyPose(part);
        }
    }

    private void applyPose(int part) {
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        ArmorStand stand = (ArmorStand) entity;
        EulerAngle pose = poses[part];

        switch (part) {
            case 0:
                stand.setHeadPose(pose);
                break;
            case 1:
                stand.setBodyPose(pose);
                break;
            case 2:
                stand.setLeftArmPose(pose);
                break;
            case 3:
                stand.setRightArmPose(pose);
                break;
            case 4:
                stand.setLeftLegPose(pose);
                break;
            default:
                stand.setRightLegPose(pose);
                break;
        }
    }

    @Override
    public void setHeadPose(float x, float y, float z) {
        setPose(0, x, y, z);
    }

    @Override
    public void setBodyPose(float x, float y, float z) {
        setPose(1, x, y, z);
    }

    @Override
    public void setLeftArmPose(float x, float y, float z) {
        setPose(2, x, y, z);
    }

    @Override
    public void setRightArmPose(float x, float y, float z) {
        setPose(3, x, y, z);
    }

    @Override
    public void setLeftLegPose(float x, float y, float z) {
        setPose(4, x, y, z);
    }

    @Override
    public void setRightLegPose(float x, float y, float z) {
        setPose(5, x, y, z);
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        customName = component == null ? Component.empty() : component;

        if (entity != null) {
            applyName();
        }
    }

    private void applyName() {
        LumenPlugin.getPlugin().getPlatformAdapter().setCustomName(entity, customName);
        entity.setCustomNameVisible(!customName.equals(Component.empty()));
    }

    @Override
    public void setLeashHolder(@Nullable Entity holder) {
        leashHolder = holder;

        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setLeashHolder(holder);
        }
    }

    @Override
    public void setCreeperPowered(boolean powered) {
        creeperPowered = powered;

        if (entity instanceof Creeper) {
            ((Creeper) entity).setPowered(powered);
        }
    }

    @Override
    public void setCreeperIgnited(boolean ignited) {
        if (ignited && entity instanceof Creeper) {
            try {
                entity.getClass().getMethod("ignite").invoke(entity);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
    }

    @Override
    public void setHurtTicks(int hurtTicks) {
        if (hurtTicks > 0 && entity instanceof LivingEntity) {
            Compats.get().playHurtAnimation((LivingEntity) entity);
        }
    }

    @Override
    public void setNoClip(boolean noClip) {
    }

    @Override
    public void setEntityItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;

        if (entity instanceof Item) {
            ((Item) entity).setItemStack(itemStack);
        }
    }

    @Override
    public void setHorseStanding(boolean standing) {
    }

    @Override
    public void setGuardianTarget(int targetId) {
    }

    @Override
    public void sendEntityMetadataPacket() {
    }

    @Override
    public void sendEntityEquipmentPacket() {
    }

    @Override
    public void sendEntityEventPacket(byte eventId) {
        if (entity != null) {
            broadcast(new WrapperPlayServerEntityStatus(entity.getEntityId(), eventId));
        }
    }

    @Override
    public void sendSwingAnimationPacket(boolean mainHand) {
        if (entity != null) {
            broadcast(new WrapperPlayServerEntityAnimation(entity.getEntityId(), mainHand
                    ? WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM
                    : WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND));
        }
    }

    private void broadcast(PacketWrapper<?> packet) {
        Location at = entity.getLocation();

        for (Player player : entity.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(at) < 48.0d * 48.0d) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
        }
    }

    @Override
    public void addCollision(Player player) {
    }

    @Override
    public void removeCollision(Player player) {
    }

    @Override
    public void removePathfinder() {
        if (entity instanceof LivingEntity) {
            Compats.get().setAI((LivingEntity) entity, false);
        }
    }

    @Override
    public int getId() {
        return entity == null ? -1 : entity.getEntityId();
    }

    /**
     * Spawns on first access when a position is already known, so code that configures the
     * Bukkit entity right after placing it keeps working.
     */
    @Override
    public @Nullable Entity getBukkitEntity() {
        if (entity == null && location != null) {
            spawn();
        }
        return entity;
    }

    @Override
    public EntityTracker getTracker() {
        return tracker;
    }
}
