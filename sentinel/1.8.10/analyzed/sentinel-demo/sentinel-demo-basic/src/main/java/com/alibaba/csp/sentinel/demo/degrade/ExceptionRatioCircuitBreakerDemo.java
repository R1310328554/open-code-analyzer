/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.degrade;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异常比例熔断演示：当窗口内异常占比超过阈值时打开熔断器，
 * 拒绝后续请求直至 {@link DegradeRule#getTimeWindow()} 秒后进入半开探测。
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class ExceptionRatioCircuitBreakerDemo {

    private static final String KEY = "some_service";

    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger bizException = new AtomicInteger();

    private static volatile boolean stop = false;
    private static int seconds = 120;

    public static void main(String[] args) throws Exception {
        initDegradeRule();
        registerStateChangeObserver();
        startTick();

        final int concurrency = 8;
        for (int i = 0; i < concurrency; i++) {
            Thread entryThread = new Thread(() -> {
                while (true) {
                    Entry entry = null;
                    try {
                        entry = SphU.entry(KEY);
                        sleep(ThreadLocalRandom.current().nextInt(5, 10));
                        pass.addAndGet(1);

                        // 约 45% 概率抛出业务异常
                        if (ThreadLocalRandom.current().nextInt(0, 100) > 55) {
                            // 模拟业务代码异常
                            throw new RuntimeException("oops");
                        }
                    } catch (BlockException e) {
                        block.addAndGet(1);
                        sleep(ThreadLocalRandom.current().nextInt(5, 10));
                    } catch (Throwable t) {
                        bizException.incrementAndGet();
                        // 须手动调用 Tracer 记录异常，供熔断统计
                        Tracer.traceEntry(t, entry);
                    } finally {
                        total.addAndGet(1);
                        if (entry != null) {
                            entry.exit();
                        }
                    }
                }
            });
            entryThread.setName("sentinel-simulate-traffic-task-" + i);
            entryThread.start();
        }
    }

    private static void registerStateChangeObserver() {
        EventObserverRegistry.getInstance().addStateChangeObserver("logging",
            (prevState, newState, rule, snapshotValue) -> {
                if (newState == State.OPEN) {
                    System.err.println(String.format("%s -> OPEN at %d, snapshotValue=%.2f", prevState.name(),
                        TimeUtil.currentTimeMillis(), snapshotValue));
                } else {
                    System.err.println(String.format("%s -> %s at %d", prevState.name(), newState.name(),
                        TimeUtil.currentTimeMillis()));
                }
            });
    }

    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule(KEY)
            .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
            // 异常比例阈值 50%
            .setCount(0.5d)
            .setStatIntervalMs(30000)
            .setMinRequestAmount(50)
            // 熔断打开后恢复探测间隔（秒）
            .setTimeWindow(10);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
        System.out.println("Degrade rule loaded: " + rules);
    }

    private static void sleep(int timeMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeMs);
        } catch (InterruptedException e) {
            // 忽略中断
        }
    }

    private static void startTick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-tick-task");
        timer.start();
    }

    static class TimerTask implements Runnable {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("Begin to run! Go go go!"); // 启动并发压测
            System.out.println("See corresponding metrics.log for accurate statistic data"); // 精确指标见 metrics.log

            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;
            long oldBizException = 0;
            while (!stop) {
                sleep(1000);

                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                long globalBizException = bizException.get();
                long oneSecondBizException = globalBizException - oldBizException;
                oldBizException = globalBizException;

                System.out.println(TimeUtil.currentTimeMillis() + ", oneSecondTotal:" + oneSecondTotal
                    + ", oneSecondPass:" + oneSecondPass
                    + ", oneSecondBlock:" + oneSecondBlock
                    + ", oneSecondBizException:" + oneSecondBizException);
                if (seconds-- <= 0) {
                    stop = true;
                }
            }
            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total: " + total.get() + ", pass:" + pass.get()
                + ", block:" + block.get() + ", bizException:" + bizException.get());
            System.exit(0);
        }
    }
}
