package net.xevianlight.aphelion.core.saveddata.types;

// A record seems more useful here since this class is really only to help us interpret these values as they are written to the disk as an integer.
// We never store GravityData for any reason other than to test its values or to pack it into an integer.
public record GravityData (float gravity, float radius) {

    public static final float DEFAULT_GRAVITY = 9.80665f; // 1G
    public static final float GRAVITY_PRECISION = 100.0f;
    public static final float RADIUS_PRECISION = 100.0f;
    public static final float MAX_RADIUS = Short.MAX_VALUE / RADIUS_PRECISION;
    public static final float MAX_GRAVITY = Short.MAX_VALUE / GRAVITY_PRECISION;

    public GravityData {
        gravity = Math.clamp(gravity, 0f, MAX_GRAVITY);
        radius  = Math.clamp(radius,  0f, MAX_RADIUS);
    }

    public int pack() {
        int g = Math.round(gravity * GRAVITY_PRECISION) & 0xFFFF;
        int r = Math.round(radius * RADIUS_PRECISION) & 0xFFFF;

        return g | (r << 16);
    }

    public float getGravity() {
        return gravity;
    }

    public float getRadius() {
        return radius;
    }

    public static GravityData unpack(int packed) {
        float gravity = (packed & 0xFFFF) / GRAVITY_PRECISION;
        float radius = ((packed >>> 16) & 0xFFFF) / RADIUS_PRECISION;

        return new GravityData(gravity, radius);
    }
}
