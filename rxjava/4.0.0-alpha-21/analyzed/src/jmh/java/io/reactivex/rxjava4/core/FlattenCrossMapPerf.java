/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.core;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.rxjava4.functions.Function;

/**
 * JMH 基准：fromArray.flatMapIterable 展开大 Iterable 时
 * Flowable 与 Observable 的吞吐对比（交叉映射场景）。
 */
@SuppressWarnings("exports")
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class FlattenCrossMapPerf {
    @Param({ "1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int times;

    Flowable<Integer> flowable;

    Observable<Integer> observable;

    /** 按 times 构造外层数组与内层 Iterable，并建立 flatMapIterable 链路。 */
    @Setup
    public void setup() {
        Integer[] array = new Integer[times];
        Arrays.fill(array, 777);

        Integer[] arrayInner = new Integer[1000000 / times];
        Arrays.fill(arrayInner, 888);

        final Iterable<Integer> list = Arrays.asList(arrayInner);

        flowable = Flowable.fromArray(array).flatMapIterable((Function<Integer, Iterable<Integer>>) _ -> list);

        observable = Observable.fromArray(array).flatMapIterable((Function<Integer, Iterable<Integer>>) _ -> list);
    }

    /** Flowable flatMapIterable 基准。 */
    @Benchmark
    public void flowable(Blackhole bh) {
        flowable.subscribe(new PerfConsumer(bh));
    }

    /** Observable flatMapIterable 基准。 */
    @Benchmark
    public void observable(Blackhole bh) {
        observable.subscribe(new PerfConsumer(bh));
    }
}
