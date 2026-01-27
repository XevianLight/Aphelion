package net.xevianlight.aphelion.entites.vehicles;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.xevianlight.aphelion.core.init.ModSounds;

public class RocketEngineSound extends AbstractTickableSoundInstance {
    private final RocketEntity rocket;

    public RocketEngineSound (RocketEntity rocket) {
        super(ModSounds.ROCKET_ENGINE.get(), SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.rocket = rocket;

        this.looping = true;
        this.delay = 0;
        this.volume = 1;
        this.pitch = 1.0f;

        this.x = rocket.getX();
        this.y = rocket.getY();
        this.z = rocket.getZ();
    }

    @Override
    public void tick() {
        if (rocket.isRemoved() || rocket.getPhase() != RocketEntity.FlightPhase.ASCEND) {
            this.stop();
            return;
        }

        // follow entity
        this.x = rocket.getX();
        this.y = rocket.getY();
        this.z = rocket.getZ();
    }

    public void killSound() {
        stop();
    }

}
