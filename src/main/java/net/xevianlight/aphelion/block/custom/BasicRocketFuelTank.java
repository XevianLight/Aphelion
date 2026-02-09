package net.xevianlight.aphelion.block.custom;

import net.xevianlight.aphelion.block.custom.base.BaseRocketFuelTank;

public class BasicRocketFuelTank extends BaseRocketFuelTank {
    public BasicRocketFuelTank(Properties properties) {
        super(properties);
    }

    @Override
    public int getFuelCapacity() {
        return 1000;
    }
}
