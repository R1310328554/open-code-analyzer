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
package com.alibaba.csp.sentinel.demo.flow;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** 正则资源名 QPS 流控演示：规则 {@code /A/.*} 匹配以 /A/ 开头的资源。 */
public class FlowQpsRegexDemo {

    private static final String KEY = "/A/.*";

    private static Map<String, AtomicInteger> passMap = new ConcurrentHashMap<>();
    private static Map<String, AtomicInteger> blockMap = new ConcurrentHashMap<>();
    private static Map<String, AtomicInteger> totalMap = new ConcurrentHashMap<>();

    private static final List<String> resourceNameList = Arrays.asList("/A/a", "/A/c", "/B/a");

    private static volatile boolean stop = false;

    private static final int threadCount = 10;

    private static int seconds = 60 + 40;

    public static void main(String[] args) throws Exception {
        initFlowQpsRule();

        tick();
        // 为各资源启动压测线程
        simulateTraffic();

        System.out.println("===== 开始正则流控演示");
        System.out.println("以 /A/ 为前缀的资源每秒最多通过 20 个请求");

    }

    private static void initFlowQpsRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        // QPS 阈值 20，regex=true 启用正则匹配
        rule1.setCount(20);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setRegex(true);
        rule1.setLimitApp("default");
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    private static void simulateTraffic() {
        for (String resourceName : resourceNameList) {
            passMap.put(resourceName, new AtomicInteger(0));
            blockMap.put(resourceName, new AtomicInteger(0));
            totalMap.put(resourceName, new AtomicInteger(0));
            for (int i = 0; i < threadCount; i++) {
                Thread t = new Thread(new FlowQpsRegexDemo.RunTask(resourceName));
                t.setName("simulate-traffic-Task-" + resourceName);
                t.start();
            }
        }
    }

    private static void tick() {
        Thread timer = new Thread(new FlowQpsRegexDemo.TimerTask());
        timer.setName("sentinel-timer-task");
        timer.start();
    }

    static class TimerTask implements Runnable {

        private final Map<String, Long> oldTotalMap = new HashMap<>();
        private final Map<String, Long> oldPassMap = new HashMap<>();
        private final Map<String, Long> oldBlockMap = new HashMap<>();

        TimerTask() {
            for (String resource : resourceNameList) {
                oldTotalMap.put(resource, 0L);
                oldPassMap.put(resource, 0L);
                oldBlockMap.put(resource, 0L);
            }
        }

        @Override
        public void run() {
            System.out.println("begin to statistic!!!");

            while (!stop) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                for (String resource : resourceNameList) {
                    long oldTotal = oldTotalMap.get(resource);
                    long oldPass = oldPassMap.get(resource);
                    long oldBlock = oldBlockMap.get(resource);
                    AtomicInteger pass = passMap.get(resource);
                    AtomicInteger block = blockMap.get(resource);
                    AtomicInteger total = totalMap.get(resource);
                    long globalTotal = total.get();
                    long oneSecondTotal = globalTotal - oldTotal;
                    oldTotalMap.put(resource, globalTotal);

                    long globalPass = pass.get();
                    long oneSecondPass = globalPass - oldPass;
                    oldPassMap.put(resource, globalPass);

                    long globalBlock = block.get();
                    long oneSecondBlock = globalBlock - oldBlock;
                    oldBlockMap.put(resource, globalBlock);

                    System.out.println(seconds + " " + resource + " send qps is: " + oneSecondTotal);
                    System.out.println(TimeUtil.currentTimeMillis() + ", total:" + oneSecondTotal
                            + ", pass:" + oneSecondPass
                            + ", block:" + oneSecondBlock);
                }

                if (seconds-- <= 0) {
                    stop = true;
                }
            }
            System.exit(0);
        }
    }

    static class RunTask implements Runnable {

        private final String resourceName;
        private final AtomicInteger pass;
        private final AtomicInteger block;
        private final AtomicInteger total;

        RunTask(String resourceName) {
            this.resourceName = resourceName;
            pass = passMap.get(resourceName);
            block = blockMap.get(resourceName);
            total = totalMap.get(resourceName);
        }

        @Override
        public void run() {
            while (!stop) {
                Entry entry = null;

                try {
                    entry = SphU.entry(resourceName);
                    // 获取令牌成功
                    pass.addAndGet(1);
                } catch (BlockException e1) {
                    block.incrementAndGet();
                } catch (Exception e2) {
                    // 业务异常
                } finally {
                    total.incrementAndGet();
                    if (entry != null) {
                        entry.exit();
                    }
                }

                Random random2 = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(50));
                } catch (InterruptedException e) {
                    // 忽略中断
                }
            }
        }
    }

}
