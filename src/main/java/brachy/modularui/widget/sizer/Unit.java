package brachy.modularui.widget.sizer;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.codec.ExtendedMutableCodec;
import brachy.modularui.api.codec.MutableCodec;
import brachy.modularui.utils.serialization.codec.CodecUtil;
import brachy.modularui.utils.serialization.codec.MutableMapCodec;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import net.minecraft.util.StringRepresentable;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.stream.Stream;

@ApiStatus.Internal
public class Unit {

    public static final int DEFAULT_COVER_CHILDREN_MIN_SIZE = 8;
    public static final int DISABLE_COVER_CHILDREN = Integer.MIN_VALUE;

    private static final MutableMapCodec<Unit> COVER_CHILDREN_CODEC = new MutableMapCodec<>() {
        @Override
        public <T> DataResult<Unit> decode(DynamicOps<T> ops, MapLike<T> input, Unit instance) {
            return CodecUtil.orElse(ops.getNumberValue(input.get("coverChildren")).flatMap(n -> {
                int i = n.intValue();
                if (i == 0) i = DISABLE_COVER_CHILDREN;
                else if (i == 1) i = -1;
                instance.setCoverChildren(i);
                return DataResult.success(instance);
            }), () -> ops.getNumberValue(input.get("coverChildrenMinSize")).map(n -> {
                instance.setCoverChildren(n.intValue());
                return instance;
            }));
        }

        @Override
        public <T> DataResult<Unit> decodeInstance(DynamicOps<T> ops, MapLike<T> input) {
            return DataResult.success(new Unit());
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of("coverChildren", "coverChildrenMinSize").map(ops::createString);
        }

        @Override
        public <T> RecordBuilder<T> encode(Unit input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (input.isCoverChildren()) {
                int i = input.getCoverChildrenMinSize();
                if (i < 0) {
                    prefix.add("coverChildren", ops.createBoolean(true));
                } else {
                    prefix.add("coverChildrenMinSize", ops.createInt(i));
                }
                return prefix;
            }
            return prefix.withErrorsFrom(DataResult.error(() -> "Can only handle cover children"));
        }
    };

    public static final MutableObjectCodec<Unit> FULL_CODEC = MutableObjectCodec.builder(Unit::new)
            .equalityTest(Unit::areEqual)
            .addOpt("autoAnchor", Unit::setAutoAnchor, Unit::isAutoAnchor, Codec.BOOL, true)
            .addOpt("value", Unit::setValue, Unit::getValue, Codec.FLOAT, 0f)
            .addOpt("measure", Unit::setMeasure, Unit::getMeasure, Measure.CODEC, Measure.PIXEL)
            .addOpt("anchor", Unit::setAnchor, Unit::getRawAnchor, Codec.FLOAT, 0f)
            .addOpt("offset", Unit::setOffset, Unit::getOffset, Codec.INT, 0)
            .addOpt("state", Unit::setState, Unit::getState, State.CODEC, State.UNUSED).neverEncode()
            .addUnencodableOpt("valueSupplier", Unit::setValue, Unit::getValueSupplier, null)
            .build();

    public static final MutableCodec<Unit> SHORT_CODEC = new MutableCodec<>() {
        @Override
        public <T> DataResult<Pair<Unit, T>> decode(DynamicOps<T> ops, T input, Unit instance) {
            return CodecUtil.orElse(ops.getNumberValue(input).map(instance::applyNumber),
                            () -> ops.getStringValue(input).flatMap(instance::applyString))
                    .map(u -> Pair.of(u, ops.empty()));
        }

        @Override
        public <T> DataResult<Pair<Unit, T>> decodeInstance(DynamicOps<T> ops, T input) {
            return DataResult.success(Pair.of(new Unit(), input));
        }

        @Override
        public <T> DataResult<T> encode(Unit input, DynamicOps<T> ops, T prefix) {
            if (input.isCoverChildren()) {
                if (input.getCoverChildrenMinSize() < 0) {
                    return DataResult.success(ops.createString("coverChildren"));
                }
                return DataResult.error(() -> "Can only handle default cover children min size");
            }
            if (FULL_CODEC.areFieldsDefaultExcept(input, f -> !f.canEncode() ||
                    f.name().equals("value") ||
                    f.name().equals("measure"))) {
                float f = input.getValue();
                String s;
                if (input.isRelative()) {
                    f *= 100;
                    s = f + "%";
                    return DataResult.success(ops.createString(s));
                }
                return DataResult.success(ops.createInt((int) f));
            }
            return DataResult.error(() -> "");
        }
    };

    public static final ExtendedMutableCodec<Unit> CODEC = FULL_CODEC.tryBefore(COVER_CHILDREN_CODEC).tryBefore(SHORT_CODEC);

    static final Unit ZERO_START = new Unit(State.START);
    static final Unit ZERO_SIZE = new Unit(State.SIZE);
    static final Unit ZERO_END = new Unit(State.END);

    @Getter
    @Setter
    private boolean autoAnchor;
    private float value;
    @Getter(AccessLevel.PRIVATE)
    private DoubleSupplier valueSupplier;
    @Getter
    @Setter
    private Measure measure;
    @Setter
    private float anchor;
    @Getter
    @Setter
    private int offset;
    @Getter
    @Setter(AccessLevel.PRIVATE)
    public State state;

    public Unit() {
        reset();
    }

    private Unit(State state) {
        reset();
        this.state = state;
    }

    public void reset() {
        this.state = State.UNUSED;
        this.autoAnchor = true;
        this.value = 0f;
        this.valueSupplier = null;
        this.measure = Measure.PIXEL;
        this.anchor = 0f;
        this.offset = 0;
    }

    public void copyPropertiesOf(Unit other) {
        copyPropertiesOf(other, false);
    }

    private void copyPropertiesOf(Unit other, boolean copyState) {
        if (copyState) this.state = other.state;
        if (this.state != State.SIZE && other.measure == Measure.COVER_CHILDREN) {
            throw new IllegalStateException("Cover children can only be applied to width and height");
        }
        this.autoAnchor = other.autoAnchor;
        this.value = other.value;
        this.valueSupplier = other.valueSupplier;
        this.measure = other.measure;
        this.anchor = other.anchor;
        this.offset = other.offset;
    }

    private Unit applyNumber(Number n) {
        float f = n.floatValue();
        int r = Math.round(f);
        if (Math.abs(f - r) < 0.00001) {
            setMeasure(Measure.PIXEL);
            setValue(r);
        } else {
            setMeasure(Measure.RELATIVE);
            setValue(f);
        }
        return this;
    }

    private DataResult<Unit> applyString(String s) {
        if ("coverChildren".equals(s)) {
            if (this.state != State.SIZE) {
                return DataResult.error(() -> "Can't apply cover children to a pos");
            }
            setMeasure(Measure.COVER_CHILDREN);
            setValue(-1);
            return DataResult.success(this);
        }
        if ("full".equals(s) || "fill".equals(s)) {
            if (this.state != State.SIZE) {
                return DataResult.error(() -> "Can't apply fill parent to a pos");
            }
            setMeasure(Measure.RELATIVE);
            setValue(1f);
            return DataResult.success(this);
        }
        boolean relative = false;
        if (s.endsWith("%")) {
            relative = true;
            s = s.substring(0, s.length() - 1);
        }
        setRelative(relative);
        try {
            float f = Float.parseFloat(s);
            if (relative) f /= 100;
            setValue(f);
            return DataResult.success(this);
        } catch (NumberFormatException e) {
            String finalS = s;
            return DataResult.error(() -> "Error parsing Unit value string [" + finalS + "]");
        }
    }

    public void setValue(float value) {
        this.value = value;
        this.valueSupplier = null;
    }

    public void setValue(DoubleSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public float getValue() {
        return this.valueSupplier == null ? this.value : (float) this.valueSupplier.getAsDouble();
    }

    public int getAbsOffset() {
        return Math.abs(this.offset);
    }

    public boolean isCloseToZero() {
        if (isRelative()) {
            return Math.abs(getValue()) < -0.01 && Math.abs(getValue()) < 5;
        }
        return Math.abs(getValue() + getOffset()) < 5;
    }

    private float getRawAnchor() {
        return this.anchor;
    }

    public float getAnchor() {
        float val = getValue();
        return isAutoAnchor() && isRelative() && val < 1 ? val : this.anchor;
    }

    public void setRelative(boolean relative) {
        this.measure = relative ? Measure.RELATIVE : Measure.PIXEL;
    }

    public boolean isRelative() {
        return this.measure == Measure.RELATIVE;
    }

    public boolean isUnused() {
        return this.state == State.UNUSED;
    }

    public boolean isCoverChildren() {
        return this.measure == Measure.COVER_CHILDREN;
    }

    public void setCoverChildren(int minSize) {
        if (minSize == DISABLE_COVER_CHILDREN) {
            setMeasure(Measure.PIXEL);
            setValue(DEFAULT_COVER_CHILDREN_MIN_SIZE);
            return;
        }
        setMeasure(Measure.COVER_CHILDREN);
        setValue(minSize);
    }

    public int getCoverChildrenMinSize() {
        if (isCoverChildren()) {
            int v = (int) getValue();
            return v < 0 ? DEFAULT_COVER_CHILDREN_MIN_SIZE : v;
        }
        return DISABLE_COVER_CHILDREN;
    }

    public boolean isEqual(Unit o) {
        return o != null &&
                this.autoAnchor == o.autoAnchor &&
                Float.compare(this.value, o.value) == 0 &&
                Float.compare(this.anchor, o.anchor) == 0 &&
                this.offset == o.offset &&
                this.measure == o.measure &&
                this.valueSupplier == o.valueSupplier &&
                this.state == o.state;
    }

    public static boolean areEqual(Unit a, Unit b) {
        return a == null ? b == null : a.isEqual(b);
    }

    public enum Measure implements StringRepresentable {

        PIXEL,
        RELATIVE,
        COVER_CHILDREN;

        public static final Codec<Measure> CODEC = StringRepresentable.fromEnum(Measure::values);

        public final String name;

        Measure() {
            this.name = name().toLowerCase(Locale.ENGLISH);
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

    public enum State implements StringRepresentable {

        UNUSED("", ""),
        START("LEFT", "TOP"),
        END("RIGHT", "BOTTOM"),
        SIZE("WIDTH", "HEIGHT");

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);

        public final String name, xText, yText;

        State(String xText, String yText) {
            this.name = name().toLowerCase(Locale.ENGLISH);
            this.xText = xText;
            this.yText = yText;
        }

        public String getText(GuiAxis axis) {
            return axis.isHorizontal() ? this.xText : this.yText;
        }


        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
