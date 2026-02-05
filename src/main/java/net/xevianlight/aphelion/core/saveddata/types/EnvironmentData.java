package net.xevianlight.aphelion.core.saveddata.types;

public record EnvironmentData (boolean oxygen, short temperature, float gravity){



    public static final boolean DEFAULT_OXYGEN = true;
    public static final short DEFAULT_TEMPERATURE = (short) 294.2611; // 70F
    public static final float DEFAULT_GRAVITY = 9.80665f; // 1G

    public static final int DEFAULT_PACKED = new EnvironmentData(DEFAULT_OXYGEN, DEFAULT_TEMPERATURE, DEFAULT_GRAVITY).pack();

    /* We can pack all of this into an int value per block position.
     * If we have to store partitionData for an entire chunk section (16^3), this amounts to 16kB per section.
     * 1000 sections touched is 16MB
     * This is acceptable for partitionData that will only exist where it is not equal to the default values
     */

    private static final int OXYGEN_BITS = 1; // Boolean. Do we have oxygen or no?
    private static final int TEMPERATURE_BITS = Short.SIZE; // 16 bits should suffice for temperature, gives 0k to 65536k range, more than enough
    private static final int GRAVITY_BITS = 15; // Leftover bits can be assigned to gravity, 32768 values

    private static final float GRAVITY_PRECISION = 100.0f; // 2 decimal precision

    private static final int OXYGEN_BIT = 0;
    private static final int TEMPERATURE_BIT = OXYGEN_BIT + OXYGEN_BITS; // next 16 bits
    private static final int GRAVITY_BIT = TEMPERATURE_BIT + TEMPERATURE_BITS; // next 15 bits


    public EnvironmentData(boolean oxygen, short temperature, float gravity) {
        this.oxygen = oxygen;
        this.temperature = temperature;
        this.gravity = gravity;
    }

    public EnvironmentData withOxygen(boolean newOxygen) {
        return new EnvironmentData(newOxygen, this.temperature, this.gravity);
    }

    public EnvironmentData withTemperature(short newTemperature) {
        return new EnvironmentData(this.oxygen, newTemperature, this.gravity);
    }

    @Deprecated
    public EnvironmentData withGravity(float newGravity) {
        return new EnvironmentData(this.oxygen, this.temperature, newGravity);
    }

    public int pack() {
        int packedData = 0;

        packedData |= (this.oxygen ? 1 : 0) << OXYGEN_BIT;
        packedData |= (this.temperature & ((1 << TEMPERATURE_BITS) - 1)) << TEMPERATURE_BIT;
        packedData |= (int) (this.gravity * GRAVITY_PRECISION) << GRAVITY_BIT;

        return packedData;
    }

    public static EnvironmentData unpack(int packedData) {
        boolean oxygen = ((packedData >> OXYGEN_BIT) & 1) == 1;
        short temperature = (short) ((packedData >> TEMPERATURE_BIT) & ((1 << TEMPERATURE_BITS) - 1));
        float gravity = ((packedData >> GRAVITY_BIT) & ((1 << GRAVITY_BITS) - 1)) / GRAVITY_PRECISION;

        return new EnvironmentData(oxygen, temperature, gravity);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EnvironmentData) obj;
        return this.oxygen == that.oxygen &&
                this.temperature == that.temperature &&
                Float.floatToIntBits(this.gravity) == Float.floatToIntBits(that.gravity);
    }

    @Override
    public String toString() {
        return "EnvironmentData[" +
                "oxygen=" + oxygen + ", " +
                "temperature=" + temperature + ", " +
                "gravity=" + gravity + ']';
    }

    public boolean hasOxygen() {
        return oxygen;
    }

    public short getTemperature() {
        return temperature;
    }

    public float getGravity() {
        return gravity;
    }

}
