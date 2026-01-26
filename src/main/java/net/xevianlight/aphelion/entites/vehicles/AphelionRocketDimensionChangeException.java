package net.xevianlight.aphelion.entites.vehicles;

public class AphelionRocketDimensionChangeException extends RuntimeException {

    public AphelionRocketDimensionChangeException(String message) {
        super("Failed to transfer rocket to dimension " + message);
    }

    public AphelionRocketDimensionChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
