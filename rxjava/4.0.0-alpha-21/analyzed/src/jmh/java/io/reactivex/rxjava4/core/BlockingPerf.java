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

/**
 * JMH 基准：大数组 Flowable/Observable 的 blockingFirst/blockingLast 吞吐。
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class BlockingPerf {
    @Param({ "1", "1000", "1000000" })
    public int times;

    Flowable<Integer> flowable;

    Observable<Integer> observable;

    /** 按 times 填充数组并构造 fromArray 源。 */
    @Setup
    public void setup() {
        Integer[] array = new Integer[times];
        Arrays.fill(array, 777);

        flowable = Flowable.fromArray(array);

        observable = Observable.fromArray(array);
    }

    @Benchmark
    public Object flowableBlockingFirst() {
        return flowable.blockingFirst();
    }

    /** Flowable.blockingLast 基准。 */
    @Benchmark
    public Object flowableBlockingLast() {
        return flowable.blockingLast();
    }

    @Benchmark
    public Object observableBlockingLast() {
        return observable.blockingLast();
    }

    /** Observable.blockingFirst 基准。 */
    @Benchmark
    public Object observableBlockingFirst() {
        return observable.blockingFirst();
    }
}
