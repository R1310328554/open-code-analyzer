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
package com.alibaba.csp.sentinel.demo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

/**
 * Sentinel 异步 {@link AsyncEntry} 用法示例：演示异步 entry 与同步 entry 嵌套、
 * {@link ContextUtil#runOnContext} 绑定异步上下文及调用链结构。
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class AsyncEntryDemo {

    /** 模拟异步 RPC：延迟 3 秒后在另一线程回调 handler。 */
    private void invoke(String arg, Consumer<String> handler) {
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
                String resp = arg + ": " + System.currentTimeMillis();
                handler.accept(resp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void anotherAsync() {
        try {
            // 创建异步 entry，资源名 test-another-async
            final AsyncEntry entry = SphU.asyncEntry("test-another-async");

            CompletableFuture.runAsync(() -> {
                ContextUtil.runOnContext(entry.getAsyncContext(), () -> {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                        // 在异步 entry 内嵌套同步 entry
                        anotherSyncInAsync();

                        System.out.println("Async result: 666");
                    } catch (InterruptedException e) {
                        // 忽略中断
                    } finally {
                        entry.exit();
                    }
                });
            });
        } catch (BlockException ex) {
            ex.printStackTrace();
        }
    }

    private void fetchSync() {
        Entry entry = null;
        try {
            entry = SphU.entry("test-sync");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void fetchSyncInAsync() {
        Entry entry = null;
        try {
            entry = SphU.entry("test-sync-in-async");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void anotherSyncInAsync() {
        Entry entry = null;
        try {
            entry = SphU.entry("test-another-sync-in-async");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void directlyAsync() {
        try {
            final AsyncEntry entry = SphU.asyncEntry("test-async-not-nested");

            this.invoke("abc", result -> {
                // 若回调内无嵌套 entry，可不使用 ContextUtil.runOnContext()
                try {
                    // 在此处理异步结果（无其他 entry）
                } finally {
                    // 退出异步 entry
                    entry.exit();
                }
            });
        } catch (BlockException e) {
            // 请求被限流，处理 BlockException
            e.printStackTrace();
        }
    }

    private void doAsyncThenSync() {
        try {
            // 先调用异步资源 test-async
            final AsyncEntry entry = SphU.asyncEntry("test-async");
            this.invoke("abc", resp -> {
                // 回调线程与发起 asyncEntry 的线程不同
                // 须在异步上下文中执行，使嵌套 entry 挂到父异步 entry 调用链
                ContextUtil.runOnContext(entry.getAsyncContext(), () -> {
                    try {
                        // 在回调中多次触发 anotherAsync()
                        for (int i = 0; i < 7; i++) {
                            anotherAsync();
                        }

                        System.out.println(resp);

                        // 再在异步上下文中做同步 entry
                        fetchSyncInAsync();
                    } finally {
                        // Exit the async entry.
                        entry.exit();
                    }
                });
            });
            // 随后调用同步资源 test-sync
            fetchSync();
        } catch (BlockException ex) {
            // Request blocked, handle the exception.
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        initFlowRule();

        AsyncEntryDemo service = new AsyncEntryDemo();

        // 预期调用链：
        //
        // EntranceNode: machine-root
        // -EntranceNode: async-context
        // --test-top
        // ---test-sync
        // ---test-async
        // ----test-another-async
        // -----test-another-sync-in-async
        // ----test-sync-in-async
        ContextUtil.enter("async-context", "originA");
        Entry entry = null;
        try {
            entry = SphU.entry("test-top");
            System.out.println("Do something..."); // 触发异步+同步嵌套调用
            service.doAsyncThenSync();
        } catch (BlockException ex) {
            // Request blocked, handle the exception.
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }

        TimeUnit.SECONDS.sleep(20);
    }

    private static void initFlowRule() {
        // 规则 1 的 limitApp 为 originB，与当前 originA 不匹配，不生效
        FlowRule rule1 = new FlowRule()
            .setResource("test-another-sync-in-async")
            .setLimitApp("originB")
            .as(FlowRule.class)
            .setCount(4)
            .setGrade(RuleConstant.FLOW_GRADE_QPS);
        // 规则 2 对 test-another-async 限 QPS=5，会生效
        FlowRule rule2 = new FlowRule()
            .setResource("test-another-async")
            .setLimitApp("default")
            .as(FlowRule.class)
            .setCount(5)
            .setGrade(RuleConstant.FLOW_GRADE_QPS);
        List<FlowRule> ruleList = Arrays.asList(rule1, rule2);
        FlowRuleManager.loadRules(ruleList);
    }
}
