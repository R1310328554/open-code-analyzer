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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

/**
 * <p>
 * 当流控行为为 {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER}（匀速排队）时，
 * 请求以固定间隔通过。新请求到达时检查与上一请求的间隔；
 * 间隔不足则排队等待，超过 {@link FlowRule#maxQueueingTimeMs} 则直接拒绝。
 * 适用于脉冲流量：避免瞬时洪峰拖垮系统，改为匀速放行。
 *
 *
 * <p>
 * 本 demo 演示 {@link RuleConstant#CONTROL_BEHAVIOR_RATE_LIMITER} 与默认拒绝行为对比。
 * </p>
 *
 * <p>
 * {@link #initPaceFlowRule() } create rules that uses
 * {@code CONTROL_BEHAVIOR_RATE_LIMITER}.
 * <p>
 * {@link #simulatePulseFlow()} 模拟 100 个几乎同时到达的请求，匀速排队后全部通过。
 *
 * <p/>
 * Run this demo, results are as follows:
 * <pre>
 * pace behavior
 * ....
 * 1528872403887 one request pass, cost 9348 ms // every 100 ms pass one request.
 * 1528872403986 one request pass, cost 9469 ms
 * 1528872404087 one request pass, cost 9570 ms
 * 1528872404187 one request pass, cost 9642 ms
 * 1528872404287 one request pass, cost 9770 ms
 * 1528872404387 one request pass, cost 9848 ms
 * 1528872404487 one request pass, cost 9970 ms
 * ...
 * done
 * total pass:100, total block:0
 * </pre>
 *
 * 随后切换为 {@link #initDefaultFlowRule()} 默认行为，仅 10 个通过、其余立即拒绝。
 * <p/>
 * The output will be like:
 * <pre>
 * default behavior
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101279 one request pass, cost 0 ms
 * 1530500101280 one request pass, cost 1 ms
 * 1530500101280 one request pass, cost 0 ms
 * 1530500101280 one request pass, cost 0 ms
 * 1530500101280 one request pass, cost 0 ms
 * done
 * total pass:10, total block:90 // 10 requests passed, other 90 requests rejected immediately.
 * </pre>
 *
 * @author jialiang.linjl
 */
public class PaceFlowDemo {

    private static final String KEY = "abc";

    private static volatile CountDownLatch countDown;

    private static final Integer requestQps = 100;
    private static final Integer count = 10;
    private static final AtomicInteger done = new AtomicInteger();
    private static final AtomicInteger pass = new AtomicInteger();
    private static final AtomicInteger block = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("匀速排队行为（pace behavior）");
        countDown = new CountDownLatch(1);
        initPaceFlowRule();
        simulatePulseFlow();
        countDown.await();

        System.out.println("done");
        System.out.println("total pass:" + pass.get() + ", total block:" + block.get());

        System.out.println();
        System.out.println("默认快速失败行为（default behavior）");
        TimeUnit.SECONDS.sleep(5);
        done.set(0);
        pass.set(0);
        block.set(0);
        countDown = new CountDownLatch(1);
        initDefaultFlowRule();
        simulatePulseFlow();
        countDown.await();
        System.out.println("done");
        System.out.println("total pass:" + pass.get() + ", total block:" + block.get());
        System.exit(0);
    }

    private static void initPaceFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        rule1.setCount(count);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        /*
         * CONTROL_BEHAVIOR_RATE_LIMITER：超阈值请求进入队列排队，
         * 排队时间超过 {@link FlowRule#maxQueueingTimeMs} 则拒绝。
         */
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        rule1.setMaxQueueingTimeMs(20 * 1000);

        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    private static void initDefaultFlowRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource(KEY);
        rule1.setCount(count);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        // CONTROL_BEHAVIOR_DEFAULT：超阈值立即拒绝
        rule1.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    private static void simulatePulseFlow() {
        for (int i = 0; i < requestQps; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime = TimeUtil.currentTimeMillis();
                    Entry entry = null;
                    try {
                        entry = SphU.entry(KEY);
                    } catch (BlockException e1) {
                        block.incrementAndGet();
                    } catch (Exception e2) {
                        // 业务异常
                    } finally {
                        if (entry != null) {
                            entry.exit();
                            pass.incrementAndGet();
                            long cost = TimeUtil.currentTimeMillis() - startTime;
                            System.out.println(
                                TimeUtil.currentTimeMillis() + " one request pass, cost " + cost + " ms");
                        }
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(5);
                    } catch (InterruptedException e1) {
                        // 忽略中断
                    }

                    if (done.incrementAndGet() >= requestQps) {
                        countDown.countDown();
                    }
                }
            }, "Thread " + i);
            thread.start();
        }
    }
}
