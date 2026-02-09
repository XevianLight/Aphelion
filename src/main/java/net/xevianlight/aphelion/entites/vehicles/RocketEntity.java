package net.xevianlight.aphelion.entites.vehicles;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.entity.energy.ModEnergyStorage;
import net.xevianlight.aphelion.core.init.ModEntities;
import net.xevianlight.aphelion.fluid.ModFluids;
import net.xevianlight.aphelion.util.RocketStructure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RocketEntity extends VehicleEntity implements IEntityWithComplexSpawn {

    RocketStructure structure = new RocketStructure(s -> {
        s.add(0,0,0, Blocks.NETHERITE_BLOCK.defaultBlockState());
        s.add(0,1,0, Blocks.NETHERITE_BLOCK.defaultBlockState());
        s.add(0,2,0, Blocks.NETHERITE_BLOCK.defaultBlockState());
    });

    public enum FlightPhase { IDLE, PREPARE, ASCEND, TRANSIT, DESCEND, LANDED}
    private ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, Aphelion.id("test"));
    private @Nullable BlockPos targetPos = null;

    private double landingPosX;
    private double landingPosZ;

    private static final double TELEPORT_Y = 600.0;
    private static final double ASCEND_ACCEL = 0.0125;
    private static final double DESCEND_SPEED = 1;

    private double yVel = 0.0;

    private static final EntityDataAccessor<Byte> FLIGHT_PHASE =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BYTE);

    private static final EntityDataAccessor<CompoundTag> STRUCTURE_TAG =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private final FluidTank FUEL_TANK = newFuelTank(0);
    private final FluidTank FLUID_STORAGE = newFluidTank(0);
    private final ModEnergyStorage ENERGY_STORAGE = newEnergyStorage(0, 0);
    private final ItemStackHandler INVENTORY = new ItemStackHandler(0);

    public ItemStackHandler getInventory() {
        return INVENTORY;
    }

    private static @NotNull FluidTank newFluidTank(int capacity) {
        return new FluidTank(capacity) {
            @Override
            public boolean isFluidValid(@NotNull FluidStack stack) {
                return true;
            }
        };
    }

    private static @NotNull FluidTank newFuelTank(int capacity) {
        return new FluidTank(capacity) {
            @Override
            public boolean isFluidValid(@NotNull FluidStack stack) {
                return stack.is(FluidTags.WATER);
            }
        };
    }

    private static @NotNull ModEnergyStorage newEnergyStorage(int capacity, int transfer) {
        return new ModEnergyStorage(capacity, transfer) {
            @Override
            public void onEnergyChanged() {

            }
        };
    }


    public static RocketEntity spawnRocket(Level level, BlockPos pos, RocketStructure structure) {
        if (level.isClientSide) return null;

        RocketEntity rocket = new RocketEntity(ModEntities.ROCKET.get(), level);

        rocket.moveTo(
                pos.getX() + 0.5f,
                pos.getY(),
                pos.getZ() + 0.5f,
                0.0f,
                0.0f
        );

        rocket.setStructure(structure);
        level.addFreshEntity(rocket);

        rocket.FUEL_TANK.setFluid(new FluidStack(ModFluids.OIL.get(), 1000));
        rocket.INVENTORY.setSize(rocket.INVENTORY.getSlots() + 1);
        rocket.INVENTORY.insertItem(0, new ItemStack(Items.DIAMOND, 1), false);

        return rocket;
    }

    private void recalculateCapacitiesFromStructure() {
        int inv = RocketStructure.calculateInventoryCapacity(structure);
        int fuelCap = RocketStructure.calculateFuelCapacity(structure);
        int fluidCap = RocketStructure.calculateFluidCapacity(structure);
        int energyCap = RocketStructure.calculateEnergyCapacity(structure);

        INVENTORY.setSize(inv);
        FUEL_TANK.setCapacity(fuelCap);
        FLUID_STORAGE.setCapacity(fluidCap);
        ENERGY_STORAGE.setCapacity(energyCap);
    }

    public void launchTo(ResourceKey<Level> dim, @Nullable BlockPos pos) {
        if (level().isClientSide) return;

        this.targetDim = dim;
        this.targetPos = pos;

        setPhase(FlightPhase.PREPARE);
    }

    public void launch() {
        if (level().isClientSide) return;
        if (targetDim == null) return;
        if (targetDim == this.level().dimension() && targetPos == null) return;
        setPhase(FlightPhase.PREPARE);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {

            switch (getPhase()) {
                case IDLE, LANDED -> tickIdle();
                case PREPARE -> tickPrepare();
                case ASCEND -> tickAscend();
                case TRANSIT -> {
                    try {
                        tickTransit();
                    } catch (AphelionRocketDimensionChangeException e) {
                        Aphelion.LOGGER.error("Rocket dimension transfer failed!", e);
                        setPhase(FlightPhase.IDLE);
                        resetDeltaMovement();
                    }
                }
                case DESCEND -> tickDescend();
            }

        }

        move(MoverType.SELF, getDeltaMovement());
    }

    private void tickIdle() {
        resetDeltaMovement();
    }

    private void tickPrepare() {
        setPhase(FlightPhase.ASCEND);
    }

    private void tickAscend() {
        setDeltaMovement(0, yVel, 0);
        yVel += ASCEND_ACCEL;

        if (getY() >= TELEPORT_Y) {
            resetDeltaMovement();
            setPhase(FlightPhase.TRANSIT);
        }
    }

    private void tickTransit() throws AphelionRocketDimensionChangeException {
        if (!(level() instanceof ServerLevel src)) {
            setPhase(FlightPhase.IDLE);
            return;
        }
        if (targetDim == null) {
            setPhase(FlightPhase.IDLE);
            return;
        }

        ServerLevel dst = src.getServer().getLevel(targetDim);
        if (dst == null) {
            setPhase(FlightPhase.IDLE);
            return;
        }

        // Set destination position to rockets current position OR defined landing coordinates
        if (targetPos != null) {
            landingPosX = targetPos.getX() + 0.5d;
            landingPosZ = targetPos.getZ() + 0.5d;
        } else {
            landingPosX = getX();
            landingPosZ = getZ();
        }

        double arrivalY = TELEPORT_Y;

        var passengers = List.copyOf(getPassengers());

        // Dismount before changing dims
        ejectPassengers();

        RocketEntity movedRocket = (RocketEntity) this.changeDimension(new DimensionTransition(
                dst,
                new Vec3(landingPosX, TELEPORT_Y, landingPosZ),
                Vec3.ZERO,
                getYRot(),
                getXRot(),
                DimensionTransition.PLAY_PORTAL_SOUND
        ));

        if (movedRocket == null) {
            throw new AphelionRocketDimensionChangeException(targetDim.location().toString());
        }

        movedRocket.moveTo(landingPosX, arrivalY, landingPosZ, getYRot(), getXRot());
        movedRocket.resetDeltaMovement();

        for (Entity p : passengers) {
            if (p instanceof ServerPlayer sp) {
                sp.teleportTo(dst, landingPosX, arrivalY, landingPosZ, sp.getYRot(), sp.getXRot());
                sp.startRiding(movedRocket, true);
            } else {
                Entity movedP = p.changeDimension(new DimensionTransition(
                        dst,
                        new Vec3(landingPosX, TELEPORT_Y, landingPosZ),
                        Vec3.ZERO,
                        getYRot(),
                        getXRot(),
                        DimensionTransition.PLAY_PORTAL_SOUND
                ));
                if (movedP !=  null) {
                    movedP.moveTo(landingPosX, arrivalY, landingPosZ, movedP.getYRot(), movedP.getXRot());
                    movedP.startRiding(movedRocket, true);
                }
            }
        }

        movedRocket.setPhase(FlightPhase.DESCEND);
        movedRocket.targetDim = targetDim;
        movedRocket.targetPos = targetPos;
        movedRocket.landingPosX = landingPosX;
        movedRocket.landingPosZ = landingPosZ;
        yVel = 0;
    }

    private void tickDescend() {
        if (isOnGround()) {
            resetDeltaMovement();
            setPhase(FlightPhase.LANDED);
            yVel = 0;
            return;
        }
        setDeltaMovement(0, -DESCEND_SPEED, 0);
    }

    private boolean isOnGround() {
        BlockPos below = BlockPos.containing(getX(), getBoundingBox().minY - 0.01, getZ());
        return !level().getBlockState(below).isAir();
    }

    private void resetDeltaMovement() {
        setDeltaMovement(0,0,0);
    }

    public RocketStructure getStructure() {
        return structure;
    }

    public RocketEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STRUCTURE_TAG, new CompoundTag());
        builder.define(FLIGHT_PHASE, (byte) FlightPhase.IDLE.ordinal());
    }

    public FlightPhase getPhase() {
        int idx = this.entityData.get(FLIGHT_PHASE);
        FlightPhase[] values = FlightPhase.values();
        if (idx < 0 || idx >= values.length) return FlightPhase.IDLE;
        return values[idx];
    }

    public void setPhase(FlightPhase phase) {
        this.entityData.set(FLIGHT_PHASE, (byte) phase.ordinal());
    }

    @Override
    protected @NotNull Item getDropItem() {
        return Items.AIR;
    }

    public void setStructure(RocketStructure structure) {
        double x = getX(), y = getY(), z = getZ();
        float yRot = getYRot(), xRot = getXRot();

        this.structure.clear();
        this.structure.load(structure.save());

        this.refreshDimensions();
        this.setBoundingBox(this.makeBoundingBox());

        this.moveTo(x, y, z, yRot, xRot);

        if (!level().isClientSide) {
            this.entityData.set(STRUCTURE_TAG, this.structure.save());
        }

        recalculateCapacitiesFromStructure();
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (FLIGHT_PHASE.equals(key)) {
            FlightPhase phase = getPhase();
            handleClientFlightPhaseChange(phase);
        }

        if (STRUCTURE_TAG.equals(key)) {
            CompoundTag tag = this.entityData.get(STRUCTURE_TAG);
            this.applyStructureTag(tag);
        }
    }

    private RocketEngineSound ascendLoopSound;

    private void handleClientFlightPhaseChange(FlightPhase phase) {
        switch (phase) {
            case IDLE, PREPARE, TRANSIT, DESCEND, LANDED -> {
                    Aphelion.LOGGER.info("Rocket state updated to {}", phase);
                if (ascendLoopSound != null) {
                    ascendLoopSound.killSound();
                    ascendLoopSound = null;
                }
            }
            case ASCEND -> {
                if (ascendLoopSound == null || ascendLoopSound.isStopped()) {
                    ascendLoopSound = new RocketEngineSound(this);
                    Minecraft.getInstance().getSoundManager().play(ascendLoopSound);
                }
            }

        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        HolderLookup.Provider registries = level().registryAccess();

        if (tag.contains("RocketStructure")) {
            CompoundTag rocketTag = tag.getCompound("RocketStructure");
            structure.load(rocketTag);
            recalculateCapacitiesFromStructure();

            // Immediately apply correct bbox on load (server + client)
            double x = getX(), y = getY(), z = getZ();
            float yRot = getYRot(), xRot = getXRot();

            refreshDimensions();
            setBoundingBox(makeBoundingBox());

            moveTo(x, y, z, yRot, xRot);
        }
        if (tag.contains("TargetDim", Tag.TAG_STRING)) {
            ResourceLocation rl = ResourceLocation.tryParse(tag.getString("TargetDim"));
            if (rl != null) {
                targetDim = ResourceKey.create(Registries.DIMENSION, rl);
            } else {
                targetDim = null;
            }
        } else {
            targetDim = null;
        }
        if (tag.contains("TargetPos", Tag.TAG_LONG)) {
            targetPos = BlockPos.of(tag.getLong("TargetPos"));
        } else {
            targetPos = null;
        }

        landingPosX = tag.getDouble("LandingX");
        landingPosZ = tag.getDouble("LandingZ");
        yVel = tag.getDouble("yVelocity");

        if (tag.contains("FlightPhase", Tag.TAG_BYTE)) {
            setPhase(FlightPhase.values()[tag.getByte("FlightPhase")]);
        }

        if (tag.contains("Inventory", CompoundTag.TAG_COMPOUND)){
            INVENTORY.deserializeNBT(registries, tag.getCompound("Inventory"));
        }

        FUEL_TANK.readFromNBT(registries, tag);
        FLUID_STORAGE.readFromNBT(registries, tag);

        if (tag.contains("Energy", CompoundTag.TAG_COMPOUND)) {
            ENERGY_STORAGE.deserializeNBT(registries, tag.getCompound("Energy"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        HolderLookup.Provider registries = level().registryAccess();
        tag.put("RocketStructure", structure.save());
        if (targetDim != null)
            tag.putString("TargetDim", targetDim.location().toString());
        if (targetPos != null)
            tag.putLong("TargetPos", targetPos.asLong());
        tag.putByte("FlightPhase", (byte) getPhase().ordinal());

        tag.putDouble("LandingX", landingPosX);
        tag.putDouble("LandingZ", landingPosZ);
        tag.putDouble("yVelocity", yVel);
        tag.put("Inventory", INVENTORY.serializeNBT(registries));
        tag = FUEL_TANK.writeToNBT(registries, tag);
        tag = FLUID_STORAGE.writeToNBT(registries, tag);
        tag.put("Energy", ENERGY_STORAGE.serializeNBT(registries));
    }

    public @Nullable BlockPos getTargetPos() {
        return targetPos;
    }

    public void setTargetPos(@Nullable BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    public ResourceKey<Level> getTargetDim() {
        return targetDim;
    }

    public void setTargetDim(ResourceKey<Level> targetDim) {
        this.targetDim = targetDim;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushedByFluid(@NotNull FluidType type) {
        return false;
    }

    @Override @NotNull
    protected AABB makeBoundingBox() {
        if (structure == null) {
            double half = 0.5;
            return new AABB(
                    getX() - half, getY() - half + 0.5, getZ() - half,
                    getX() + half, getY() + half + 0.5, getZ() + half
            );
        }
        return computeWorldAABBFromStructure();
    }

    @Override @NotNull
    public AABB getBoundingBoxForCulling() {
        if (structure == null)
            return super.getBoundingBoxForCulling();
        return computeWorldAABBFromStructure();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        RocketStructure.Extents e = structure.computeExtents();
        AABB local = e.toLocalAABB(); // local coords
        float w = (float) Math.max(local.getXsize(), local.getZsize());
        float h = (float) local.getYsize();
        return EntityDimensions.scalable(w, h).withEyeHeight(Math.max(0.1f, h - 0.2f));
    }


    public Vec3 getSeatWorldPos(int seatIndex) {
        int packed = structure.packedSeatAt(seatIndex);

        int dx = RocketStructure.unpackX(packed);
        int dy = RocketStructure.unpackY(packed);
        int dz = RocketStructure.unpackZ(packed);

        return this.position().add(dx, dy, dz);
    }

    @Override
    public void positionRider(@NotNull Entity passenger, @NotNull MoveFunction move) {
        if (!this.hasPassenger(passenger)) return;

        Vec3 seat = getSeatWorldPos(0); // primary seat
        move.accept(passenger, seat.x, seat.y, seat.z);

    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(structure.save());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag != null) {
            structure.load(tag);
            refreshDimensions();
        }
    }

    public void applyStructureTag(CompoundTag structureTag) {
        this.structure.load(structureTag);
        this.refreshDimensions();
    }

    private AABB computeWorldAABBFromStructure() {
        RocketStructure.Extents e = structure.computeExtents();
        AABB local = e.toLocalAABB(); // [min, max+1] in local structure coords

        return local.move(getX(), getY(), getZ());
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        return passenger instanceof Player && getPassengers().isEmpty();
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!level().isClientSide) {
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    public boolean disassemble() {
        if (level().isClientSide) return false;
        if (!(level() instanceof ServerLevel server)) return false;

        // In rare instances we can disassemble a rocket AFTER it has been killed.
        // This usually only happens if another class instance still has a reference to this object stored and calls rocketEntity.disassemble().
        // This SHOULD fix that
        if (!this.isAlive()) return false;          // dead
        if (this.isRemoved()) return false;         // discarded / removed

        ejectPassengers();

        BlockPos origin = BlockPos.containing(getX(), getY(), getZ());

        // Place blocks
        for (int i = 0; i < structure.size(); i++) {
            int packed = structure.packedPosAt(i);

            int dx = RocketStructure.unpackX(packed);
            int dy = RocketStructure.unpackY(packed);
            int dz = RocketStructure.unpackZ(packed);

            BlockPos wp = origin.offset(dx, dy, dz);
            var stateToPlace = structure.stateAt(i);

            // Don't place air (shouldn't exist in structure anyway, but safe)
            if (stateToPlace.isAir()) continue;

            // Safety: don't overwrite existing blocks
            if (!server.getBlockState(wp).isAir()) {
                continue;
            }

            server.setBlock(wp, stateToPlace, 3);
        }

        // Remove the rocket entity
        discard(); // preferred over kill() for "just remove this entity"
        return true;
    }

    public static void dropItemStackHandler(ServerLevel level, Vec3 pos, ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack toDrop = stack.copy();
                handler.setStackInSlot(i, ItemStack.EMPTY);

                ItemEntity ent = new ItemEntity(level, pos.x, pos.y, pos.z, toDrop);
                level.addFreshEntity(ent);
            }
        }
    }

    @Override
    public void kill() {
        if (!level().isClientSide())
            dropItemStackHandler((ServerLevel) level(), position(), INVENTORY);
        super.kill();
    }

    @Override
    public void onRemovedFromLevel() {
        if (!level().isClientSide())
            dropItemStackHandler((ServerLevel) level(), position(), INVENTORY);
        super.onRemovedFromLevel();
    }
}