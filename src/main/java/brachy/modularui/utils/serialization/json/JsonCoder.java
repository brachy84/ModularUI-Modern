package brachy.modularui.utils.serialization.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Optional;

public interface JsonCoder<T> {

    JsonElement encodeJson(T value);

    Optional<T> decodeJson(JsonElement json);

    static <T> JsonCoder<T> empty() {
        return (JsonCoder<T>) EMPTY;
    }

    JsonCoder<Object> EMPTY = new JsonCoder<>() {
        @Override
        public JsonElement encodeJson(Object value) {
            return null;
        }

        @Override
        public Optional<Object> decodeJson(JsonElement json) {
            return Optional.empty();
        }
    };

    JsonCoder<Integer> INT = new JsonCoder<>() {
        @Override
        public JsonElement encodeJson(Integer value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Optional<Integer> decodeJson(JsonElement json) {
            return Optional.of(json.getAsInt());
        }
    };

    JsonCoder<Float> FLOAT = new JsonCoder<>() {
        @Override
        public JsonElement encodeJson(Float value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Optional<Float> decodeJson(JsonElement json) {
            return Optional.of(json.getAsFloat());
        }
    };

    JsonCoder<Double> DOUBLE = new JsonCoder<>() {
        @Override
        public JsonElement encodeJson(Double value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Optional<Double> decodeJson(JsonElement json) {
            return Optional.of(json.getAsDouble());
        }
    };

    JsonCoder<Boolean> BOOL = new JsonCoder<>() {
        @Override
        public JsonElement encodeJson(Boolean value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Optional<Boolean> decodeJson(JsonElement json) {
            return Optional.of(json.getAsBoolean());
        }
    };

    static <T extends Enum<T>> JsonCoder<T> ofEnum(Class<T> c) {
        return ofEnum(c, c.getEnumConstants());
    }

    static <T extends Enum<T>> JsonCoder<T> ofEnum(Class<T> c, T... values) {
        return new JsonCoder<>() {
            @Override
            public JsonElement encodeJson(T value) {
                return new JsonPrimitive(value.name());
            }

            @Override
            public Optional<T> decodeJson(JsonElement json) {
                if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) throw new IllegalArgumentException();
                String name = json.getAsString();
                for (T value : values) {
                    if (value.name().equals(name)) {
                        return Optional.of(value);
                    }
                }
                return Optional.empty();
            }
        };
    }
}
