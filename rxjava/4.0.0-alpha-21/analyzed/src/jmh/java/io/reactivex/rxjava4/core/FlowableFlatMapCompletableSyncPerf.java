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

import io.reactivex.rxjava4.core.config.*;
import io.reactivex.rxjava4.internal.functions.Functions;

/**
 * JMH 基准：Flowable flatMapCompletable 与 flatMap(Completable.toFlowable)
 * 在同步 Completable.complete 下的吞吐对比。
 */
@SuppressWarnings("exports")
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class FlowableFlatMapCompletableSyncPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    int items;

    @Param({"1", "8", "32", "128", "256"})
    int maxConcurrency;

    Completable flatMapCompletable;

    Flowable<Object> flatMap;

    /** 按 items/maxConcurrency 构造同步 flatMapCompletable 与 flatMap 变体。 */
    @Setup
    public void setup() {
        Integer[] array = new Integer[items];
        Arrays.fill(array, 777);

        flatMapCompletable = Flowable.fromArray(array)
                .flatMapCompletable(Functions.justFunction(Completable.complete()), new StandardConcurrentConfig(false, maxConcurrency));

        flatMap = Flowable.fromArray(array)
                .flatMap(Functions.justFunction(Completable.complete().toFlowable()), new StandardConcurrentBufferedConfig(false, maxConcurrency));
    }

    /** flatMap(Completable.toFlowable) 同步基准。 */
    @Benchmark
    public Object flatMap(Blackhole bh) {
        return flatMap.subscribeWith(new PerfConsumer(bh));
    }

    /** flatMapCompletable 同步基准。 */
    @Benchmark
    public Object flatMapCompletable(Blackhole bh) {
        return flatMapCompletable.subscribeWith(new PerfConsumer(bh));
    }
}
