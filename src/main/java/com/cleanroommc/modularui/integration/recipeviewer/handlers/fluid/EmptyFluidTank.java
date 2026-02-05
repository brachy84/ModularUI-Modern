package com.cleanroommc.modularui.integration.recipeviewer.handlers.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import org.jetbrains.annotations.NotNull;

public class EmptyFluidTank implements IFluidTank {

    public static final EmptyFluidTank INSTANCE = new EmptyFluidTank();

    protected EmptyFluidTank() {}

    @Override
    public @NotNull FluidStack getFluid() {
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
    public boolean isFluidValid(@NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
