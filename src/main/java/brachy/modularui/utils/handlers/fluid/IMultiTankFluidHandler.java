package brachy.modularui.utils.handlers.fluid;

import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public interface IMultiTankFluidHandler extends IFluidHandler {

    IFluidTank getFluidTank(int index);
}
