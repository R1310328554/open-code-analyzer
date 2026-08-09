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

package io.reactivex.rxjava4.xmapz;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.functions.Function;

/**
 * JMH 基准：Observable concatMap(Observable::just) 与 concatMap(Single.toObservable())、专用 concatMapSingle 的吞吐对比。
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class ObservableConcatMapSinglePerf {
    @Param({ "1", "10", "100", "1000", "10000", "100000", "1000000" })
    public int count;

    Observable<Integer> observableConvert;

    Observable<Integer> observableDedicated;

    Observable<Integer> observablePlain;

    /** 按 count 填充源数组，构造 plain/convert/dedicated 三条基准链路。 */
    @Setup
    public void setup() {
        Integer[] sourceArray = new Integer[count];
        Arrays.fill(sourceArray, 777);

        Observable<Integer> source = Observable.fromArray(sourceArray);

        observablePlain = source.concatMap((Function<Integer, Observable<Integer>>) Observable::just);

        observableConvert = source.concatMap((Function<Integer, Observable<Integer>>) v -> Single.just(v).toObservable());

        observableDedicated = source.concatMapSingle((Function<Integer, Single<Integer>>) Single::just);
    }

    /** plain：concatMap(Observable::just) 基准。 */
    @Benchmark
    public Object observablePlain(Blackhole bh) {
        return observablePlain.subscribeWith(new PerfConsumer(bh));
    }

    /** convert：经 Single.toObservable 转换路径。 */
    @Benchmark
    public Object observableConvert(Blackhole bh) {
        return observableConvert.subscribeWith(new PerfConsumer(bh));
    }

    /** dedicated：concatMapSingle 专用算子。 */
    @Benchmark
    public Object observableDedicated(Blackhole bh) {
        return observableDedicated.subscribeWith(new PerfConsumer(bh));
    }
}
