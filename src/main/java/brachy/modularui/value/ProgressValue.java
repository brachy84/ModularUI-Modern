package brachy.modularui.value;

import lombok.Getter;

import net.minecraft.Util;

public class ProgressValue extends DoubleValue {

    @Getter private final int duration;

    public ProgressValue(int duration) {
        super(0);
        this.duration = duration;
    }

    @Override
    public double getDoubleValue() {
        setDoubleValue(Util.getMillis() % this.duration / (double) this.duration);
        return super.getDoubleValue();
    }
}
