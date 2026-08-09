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
package com.alibaba.csp.sentinel.demo.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

/**
 * 系统保护规则演示：对系统负载、CPU、平均 RT、入口 QPS、并发线程数设全局阈值。
 *
 * @author jialiang.linjl
 */
public class SystemGuardDemo {

    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger total = new AtomicInteger();

    private static volatile boolean stop = false;
    private static final int threadCount = 100;

    private static int seconds = 60 + 40;

    public static void main(String[] args) throws Exception {

        tick();
        initSystemRule();

        for (int i = 0; i < threadCount; i++) {
            Thread entryThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Entry entry = null;
                        try {
                            entry = SphU.entry("methodA", EntryType.IN);
                            pass.incrementAndGet();
                            try {
                                TimeUnit.MILLISECONDS.sleep(20);
                            } catch (InterruptedException e) {
                                // 忽略中断
                            }
                        } catch (BlockException e1) {
                            block.incrementAndGet();
                            try {
                                TimeUnit.MILLISECONDS.sleep(20);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        } catch (Exception e2) {
                            // 业务异常
                        } finally {
                            total.incrementAndGet();
                            if (entry != null) {
                                entry.exit();
                            }
                        }
                    }
                }

            });
            entryThread.setName("working-thread");
            entryThread.start();
        }
    }

    private static void initSystemRule() {
        SystemRule rule = new SystemRule();
        // 最高系统负载 3.0
        rule.setHighestSystemLoad(3.0);
        // 最高 CPU 使用率 60%
        rule.setHighestCpuUsage(0.6);
        // 全局平均 RT 上限 10 ms
        rule.setAvgRt(10);
        // 入口总 QPS 上限 20
        rule.setQps(20);
        // 最大并发工作线程 10
        rule.setMaxThread(10);

        SystemRuleManager.loadRules(Collections.singletonList(rule));
    }

    private static void tick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    static class TimerTask implements Runnable {
        @Override
        public void run() {
            System.out.println("begin to statistic!!!");
            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;
            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                System.out.println(seconds + ", " + TimeUtil.currentTimeMillis() + ", total:"
                    + oneSecondTotal + ", pass:"
                    + oneSecondPass + ", block:" + oneSecondBlock);
                if (seconds-- <= 0) {
                    stop = true;
                }
            }
            System.exit(0);
        }
    }
}
