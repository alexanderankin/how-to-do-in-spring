package test;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

@State(Scope.Benchmark)
public class ExampleBenchmark {

    static Random random = new Random();

    String[] sampleData;

    public static String[] getSampleData() {
        String[] sampleData = new String[10];

        for (int i = 0; i < sampleData.length; i++) {
            byte[] bytes = new byte[50];
            random.nextBytes(bytes);
            sampleData[i] = new String(bytes);
        }

        return sampleData;
    }

    @Setup
    public void setup() {
        sampleData = getSampleData();
    }

    @Benchmark
    public void capitalize1(Blackhole bh) {
        bh.consume(Example.capitalize1(sampleData[0]));
    }

    @Benchmark
    public void capitalize2(Blackhole bh) {
        bh.consume(Example.capitalize2(sampleData[0]));
    }
}
