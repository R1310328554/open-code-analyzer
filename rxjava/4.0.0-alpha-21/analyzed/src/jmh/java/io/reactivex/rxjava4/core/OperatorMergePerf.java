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

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.rxjava4.schedulers.Schedulers;

/**
 * JMH 基准：Flowable.merge / mergeArray 在多种嵌套与并发配置下的吞吐。
 */
@SuppressWarnings("exports")
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class OperatorMergePerf {

    // flatMap
    /** range.flatMap(just) 后 merge 单元素流基准。 */
    @Benchmark
    public void oneStreamOfNthatMergesIn1(final InputMillion input) throws InterruptedException {
        Flowable<Flowable<Integer>> os = Flowable.range(1, input.size)
                .map(Flowable::just);
        PerfSubscriber o = input.newLatchedObserver();
        Flowable.merge(os).subscribe(o);

        if (input.size == 1) {
            while (o.latch.getCount() != 0) { }
        } else {
            o.latch.await();
        }
    }

    // flatMap
    @Benchmark
    public void merge1SyncStreamOfN(final InputMillion input) throws InterruptedException {
        Flowable<Flowable<Integer>> os = Flowable.just(1).map(_ -> Flowable.range(0, input.size));
        PerfSubscriber o = input.newLatchedObserver();
        Flowable.merge(os).subscribe(o);

        if (input.size == 1) {
            while (o.latch.getCount() != 0) { }
        } else {
            o.latch.await();
        }
    }

    @Benchmark
    public void mergeNSyncStreamsOfN(final InputThousand input) throws InterruptedException {
        Flowable<Flowable<Integer>> os = input.flowable.map(_ -> Flowable.range(0, input.size));
        PerfSubscriber o = input.newLatchedObserver();
        Flowable.merge(os).subscribe(o);
        if (input.size == 1) {
            while (o.latch.getCount() != 0) { }
        } else {
            o.latch.await();
        }
    }

    /** N 路异步 range 流 merge 基准。 */
    @Benchmark
    public void mergeNAsyncStreamsOfN(final InputThousand input) throws InterruptedException {
        Flowable<Flowable<Integer>> os = input.flowable.map(_ -> Flowable.range(0, input.size).subscribeOn(Schedulers.computation()));
        PerfSubscriber o = input.newLatchedObserver();
        Flowable.merge(os).subscribe(o);
        if (input.size == 1) {
            while (o.latch.getCount() != 0) { }
        } else {
            o.latch.await();
        }
    }

    @Benchmark
    public void mergeTwoAsyncStreamsOfN(final InputThousand input) throws InterruptedException {
        PerfSubscriber o = input.newLatchedObserver();
        Flowable<Integer> ob = Flowable.range(0, input.size).subscribeOn(Schedulers.computation());
        Flowable.mergeArray(ob, ob).subscribe(o);
        if (input.size == 1) {
            while (o.latch.getCount() != 0) { }
        } else {
            o.latch.await();
        }
    }

    @Benchmark
    public void mergeNSyncStreamsOf1(final InputForMergeN input) throws InterruptedException {
        PerfSubscriber o = input.newLatchedObserver();
        Flowable.merge(input.observables).subscribe(o);
        if (input.size == 1) {
            while (o.latch.getCount() != 0) { }
        } else {
            o.latch.await();
        }
    }

    /** merge 输入：预构造 size 个 Flowable.just 列表。 */
    @State(Scope.Thread)
    public static class InputForMergeN {
        @Param({ "1", "100", "1000" })
        //        @Param({ "1000" })
        public int size;

        private Blackhole bh;
        List<Flowable<Integer>> observables;

        @Setup
        public void setup(final Blackhole bh) {
            this.bh = bh;
            observables = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                observables.add(Flowable.just(i));
            }
        }

        public PerfSubscriber newLatchedObserver() {
            return new PerfSubscriber(bh);
        }
    }

    /** 百万级递增整数输入状态。 */
    @State(Scope.Thread)
    public static class InputMillion extends InputWithIncrementingInteger {

        @Param({ "1", "1000", "1000000" })
        //        @Param({ "1000" })
        public int size;

        @Override
        public int getSize() {
            return size;
        }

    }

    @State(Scope.Thread)
    public static class InputThousand extends InputWithIncrementingInteger {

        @Param({ "1", "1000" })
        //        @Param({ "1000" })
        public int size;

        @Override
        public int getSize() {
            return size;
        }

    }
}
