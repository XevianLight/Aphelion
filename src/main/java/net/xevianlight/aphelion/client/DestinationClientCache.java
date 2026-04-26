package net.xevianlight.aphelion.client;

import net.xevianlight.aphelion.network.packet.PlanetInfo;

import java.util.Collections;
import java.util.List;

public final class DestinationClientCache {
    // volatile: written from the netty network thread, read from the render thread
    private static volatile List<PlanetInfo> planets = Collections.emptyList();

    public static void set(List<PlanetInfo> list) { planets = List.copyOf(list); }
    public static List<PlanetInfo> get() { return planets; }
}
