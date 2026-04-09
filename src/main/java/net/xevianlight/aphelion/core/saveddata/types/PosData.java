package net.xevianlight.aphelion.core.saveddata.types;

import org.joml.Vector3f;

/**
 * Stores the positional and rotational data for a space station.
 *
 * <p>Rotation is stored as three 16-bit fixed-point values (pitch, yaw, roll),
 * where {@code 0} maps to {@code 0°} and {@code 65536} maps to {@code 360°}.
 * This representation allows angle arithmetic to wrap correctly via natural
 * short overflow, with no explicit modulo required.
 *
 * <p>All four fields (pitch, yaw, roll, distance) can be packed into a single
 * {@code long} for efficient NBT storage.
 *
 * @see #pack()
 * @see #packer(PosData)
 * @see #unpacker(long)
 */
public class PosData {

    public static final float AU_SCALE = 1.0f / 512.0f;

    /// Fixed-point pitch rotation. {@code 0} = 0°, {@code 32768} = 180°. <p>{@code 0.005493°} precision.
    public short pitch;
    /// Fixed-point yaw rotation. {@code 0} = 0°, {@code 32768} = 180°. <p>{@code 0.005493°} precision.
    public short yaw;
    /// Fixed-point roll rotation. {@code 0} = 0°, {@code 32768} = 180°. <p>{@code 0.005493°} precision.
    public short roll;
    /**
     * Orbital distance, stored as an unsigned 16-bit value. <p> {@code 0.00195 AU} precision.
     * <p>Defaults to {@code 1 AU}.
     */

    public PosData() {
        pitch = 0;
        yaw = 0;
        roll = 0;
    }

    @Deprecated
    public PosData(short pitch, short yaw, short roll) {
        this.pitch = fromDegrees(pitch);
        this.yaw = yaw;
        this.roll = roll;
    }

    public PosData(PosData other) {
        this.pitch = other.pitch;
        this.yaw = other.yaw;
        this.roll = other.roll;
    }

    /**
     * Packs this instance into a single {@code long} for NBT storage.
     *
     * @return the packed {@code long} representation
     * @see #packer(PosData)
     */
    public long pack() {
        return packer(this);
    }


    /**
     * Packs a {@code PosData} into a single {@code long}.
     *
     * <p>Layout (low to high bits):
     * <pre>
     *   [0..15]  pitch
     *   [16..31] yaw
     *   [32..47] roll
     *   [48..63] distance
     * </pre>
     *
     * @param data the {@code PosData} to pack
     * @return the packed {@code long}
     */
    public static long packer(PosData data) {
        return ((long) data.pitch & 0xFFFFL )
                | (((long) data.yaw & 0xFFFFL) << 16)
                | (((long) data.roll & 0xFFFFL) << 32);

    }

    /**
     * Unpacks a {@code long} into a new {@code PosData} instance.
     *
     * @param packed the {@code long} to unpack, as produced by {@link #packer(PosData)}
     * @return a new {@code PosData} with fields restored from the packed value
     */
    public static PosData unpacker(long packed) {
        PosData data = new PosData();
        data.pitch    = (short)  (packed        & 0xFFFFL);
        data.yaw      = (short) ((packed >> 16) & 0xFFFFL);
        data.roll     = (short) ((packed >> 32) & 0xFFFFL);
        return data;
    }

    /**
     * Converts a degree value to the fixed-point short representation.
     *
     * <p>{@code 0°} maps to {@code 0}, {@code 360°} maps to {@code 65536}
     * (which overflows to {@code 0}, preserving wrap-around correctness).
     *
     * @param degrees the angle in degrees
     * @return the fixed-point short representation
     */
    public static short fromDegrees(float degrees) {
        return (short) Math.round((degrees / 360.0f) * 65536.0f);
    }

    public static float pitchDegrees(PosData data) { return toDegrees(data.pitch); }
    public static float yawDegrees(PosData data) { return toDegrees(data.yaw); }
    public static float rollDegrees(PosData data) { return toDegrees(data.roll); }

    public float pitchDegrees() { return toDegrees(pitch); }
    public float yawDegrees() { return toDegrees(yaw); }
    public float rollDegrees() { return toDegrees(roll); }

    /**
     * Converts a fixed-point short to degrees.
     *
     * @param s the fixed-point value
     * @return the angle in degrees, in the range {@code [0°, 360°)}
     */
    private static float toDegrees(short s) {
        return ((s & 0xFFFF) / 65536.0f) * 360.0f;
    }

    /**
     * Converts a fixed-point short to radians.
     *
     * @param s the fixed-point value
     * @return the angle in radians, in the range {@code [0, 2π)}
     */
    private static float toRadians(short s) {
        return ((s & 0xFFFF) / 65536.0f) * (float) (2 * Math.PI);
    }

    // -------------------------------------------------------------------------
    // Euler angles (for rendering)
    // -------------------------------------------------------------------------

    /**
     * Returns the rotation as a {@link Vector3f} of Euler angles in degrees
     * ({@code x} = pitch, {@code y} = yaw, {@code z} = roll).
     *
     * @return Euler angles in degrees
     */
    public Vector3f eulerAnglesDeg() {
        return new Vector3f(pitchDegrees(), yawDegrees(), rollDegrees());
    }

    /**
     * Returns the rotation of the given {@code PosData} as a {@link Vector3f}
     * of Euler angles in degrees ({@code x} = pitch, {@code y} = yaw, {@code z} = roll).
     *
     * @param data the instance to read from
     * @return Euler angles in degrees
     */
    public static Vector3f eulerAnglesDeg(PosData data) {
        return new Vector3f(pitchDegrees(data), yawDegrees(data), rollDegrees(data));
    }

    /**
     * Returns the rotation as a {@link Vector3f} of Euler angles in radians
     * ({@code x} = pitch, {@code y} = yaw, {@code z} = roll).
     * Suitable for direct use with JOML rotation methods.
     *
     * @return Euler angles in radians
     */
    public Vector3f eulerAnglesRad() {
        return new Vector3f(toRadians(pitch), toRadians(yaw), toRadians(roll));
    }

    /**
     * Returns the rotation of the given {@code PosData} as a {@link Vector3f}
     * of Euler angles in radians ({@code x} = pitch, {@code y} = yaw, {@code z} = roll).
     * Suitable for direct use with JOML rotation methods.
     *
     * @param data the instance to read from
     * @return Euler angles in radians
     */
    public static Vector3f eulerAnglesRad(PosData data) {
        return new Vector3f(toRadians(data.pitch), toRadians(data.yaw), toRadians(data.roll));
    }

    // -------------------------------------------------------------------------
    // Setters from degrees (convenience)
    // -------------------------------------------------------------------------

    public void setPitchDegrees(float degrees) { this.pitch = fromDegrees(degrees); }
    public void setYawDegrees(float degrees)   { this.yaw   = fromDegrees(degrees); }
    public void setRollDegrees(float degrees)  { this.roll  = fromDegrees(degrees); }

    public void addPitchDegrees(float degrees) { this.pitch += fromDegrees(degrees); }
    public void addYawDegrees(float degrees)   { this.yaw   += fromDegrees(degrees); }
    public void addRollDegrees(float degrees)  { this.roll  += fromDegrees(degrees); }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Returns a human-readable representation of this {@code PosData},
     * with rotations in degrees and distance as an unsigned integer.
     *
     * @return a formatted string representation
     */
    @Override
    public String toString() {
        return "PosData{pitch=%.2f°, yaw=%.2f°, roll=%.2f°}"
                .formatted(pitchDegrees(), yawDegrees(), rollDegrees());
    }


}
