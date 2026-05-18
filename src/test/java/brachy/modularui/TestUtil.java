package brachy.modularui;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class TestUtil {

    public static void bootstrap() {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
    }

    public static void repeat(int n, IntConsumer consumer) {
        for (int i = 0; i < n; i++) {
            consumer.accept(n);
        }
    }

    public static void repeatRnd(int n, Consumer<Random> consumer) {
        Random rnd = new Random();
        repeat(n, i -> consumer.accept(rnd));
    }
}
