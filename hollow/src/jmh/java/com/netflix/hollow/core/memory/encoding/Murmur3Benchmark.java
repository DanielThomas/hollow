package com.netflix.hollow.core.memory.encoding;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.netflix.hollow.core.memory.ByteData;
import com.sangupta.murmur.Murmur3;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.netflix.hollow.core.memory.encoding.HashCodes.MURMURHASH_SEED;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Murmur3Benchmark {
    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Param({"1", "10", "100", "1000", "10000", "100000"})
    int length;

    byte[] data;

    @Setup
    public void setup() {
        data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) random.nextInt(0x80);
        }
    }

    /**
     * Current Hollow implementation.
     */
    @Benchmark
    public int hash_hollow() {
        return HashCodes.hashCode(data);
    }

    /**
     * Yonik Seeley's port. Used by Solr and current used by Hollow in {@link HashCodes#hashCode(ByteData, long, int)}.
     *
     * http://yonik.com/murmurhash3-for-java/
     * https://raw.githubusercontent.com/yonik/java_util/master/src/util/hash/MurmurHash3.java
     */
    @Benchmark
    public int hash_yonik() {
        return YonikMurmur3.murmurhash3_x86_32(data, 0, length, MURMURHASH_SEED);
    }

    /**
     * Hive port.
     *
     * https://github.com/apache/hive/blob/master/storage-api/src/java/org/apache/hive/common/util/Murmur3.java
     */
    @Benchmark
    public int hash_hive() {
        return HiveMurmur3.hash32(data, length, MURMURHASH_SEED);
    }

    /**
     * Sandeep Gupta's port.
     *
     * https://github.com/sangupta/murmur
     */
    @Benchmark
    public long hash_sangupta() {
        return Murmur3.hash_x86_32(data, length, MURMURHASH_SEED);
    }

    /**
     * Google Guava.
     */
    @Benchmark
    public HashCode hash_guava() {
        return Hashing.murmur3_32(MURMURHASH_SEED).hashBytes(data);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Murmur3Benchmark.class.getSimpleName())
                .warmupIterations(5)
                .warmupTime(TimeValue.seconds(3))
                .measurementIterations(5)
                .measurementTime(TimeValue.seconds(3))
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
