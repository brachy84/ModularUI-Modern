package brachy.modularui.integration.recipeviewer.handlers.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import org.jspecify.annotations.NonNull;

public class EmptyFluidTank implements IFluidTank {

    public static final EmptyFluidTank INSTANCE = new EmptyFluidTank();

    protected EmptyFluidTank() {}

    @Override
    public @NonNull FluidStack getFluid() {
        return FluidStack.EMPTY;
    }

    @Override
    public int getFluidAmount() {
        return 0;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public boolean isFluidValid(@NonNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(@NonNull FluidStack resource, @NonNull FluidAction action) {
        return 0;
    }

    @Override
    public @NonNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NonNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
