package net.xevianlight.aphelion.block.custom;

import net.xevianlight.aphelion.block.custom.base.BaseRocketContainer;
import net.xevianlight.aphelion.block.custom.base.BaseRocketFuelTank;

public class BasicRocketContainer extends BaseRocketContainer {
    public BasicRocketContainer(Properties properties) {
        super(properties);
    }

    @Override
    public int getSlotCapacity() {
        return 9;
    }
}
