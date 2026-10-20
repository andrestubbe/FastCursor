package fastcursor.benchmark;

import fastcursor.CursorType;
import fastcursor.FastCursor;
import org.openjdk.jmh.annotations.*;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    @Setup
    public void setup() {
        FastCursor.set(CursorType.ARROW);
    }

    @TearDown
    public void tearDown() {
        FastCursor.set(CursorType.ARROW);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public boolean benchmarkSetCursorCached() {
        return FastCursor.set(CursorType.ARROW);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public boolean benchmarkSetCursorSwitch() {
        return FastCursor.set(CursorType.IBEAM);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public MemorySegment benchmarkGetCursorHandle() {
        return FastCursor.get();
    }
}
