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
package com.alibaba.csp.sentinel.dashboard.controller;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Sentinel 功能演示控制器，用于构造调用链、循环与慢请求等测试场景。
 */
@Controller
@RequestMapping(value = "/demo", produces = MediaType.APPLICATION_JSON_VALUE)
public class DemoController {

    Logger logger = LoggerFactory.getLogger(MachineRegistryController.class);

    /** 返回演示首页视图名。 */
    @RequestMapping("/greeting")
    public String greeting() {
        return "index";
    }

    /** 构造嵌套 {@link SphU#entry} 调用链以演示链路流控。 */
    @RequestMapping("/link")
    @ResponseBody
    public String link() throws BlockException {

        Entry entry = SphU.entry("head1", EntryType.IN);

        Entry entry1 = SphU.entry("head2", EntryType.IN);
        Entry entry2 = SphU.entry("head3", EntryType.IN);
        Entry entry3 = SphU.entry("head4", EntryType.IN);

        entry3.exit();
        entry2.exit();
        entry1.exit();
        entry.exit();
        return "successfully create a call link";
    }

    /** 启动多线程循环访问资源，用于压测与规则验证。 */
    @RequestMapping("/loop")
    @ResponseBody
    public String loop(String name, int time) throws BlockException {
        for (int i = 0; i < 10; i++) {
            Thread timer = new Thread(new RunTask(name, time, false));
            timer.setName("false");
            timer.start();
        }
        return "successfully create a loop thread";
    }

    /** 启动带慢调用（sleep）的循环线程，用于 RT 熔断演示。 */
    @RequestMapping("/slow")
    @ResponseBody
    public String slow(String name, int time) throws BlockException {
        for (int i = 0; i < 10; i++) {
            Thread timer = new Thread(new RunTask(name, time, true));
            timer.setName("false");
            timer.start();
        }
        return "successfully create a loop thread";
    }

    /** 后台循环执行 {@link SphU#entry} 的任务。 */
    static class RunTask implements Runnable {
        int time;
        boolean stop = false;
        String name;
        boolean slow = false;

        public RunTask(String name, int time, boolean slow) {
            super();
            this.time = time;
            this.name = name;
            this.slow = slow;
        }

        /** 在独立上下文中循环 entry/exit，可选模拟慢请求。 */
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            ContextUtil.enter(String.valueOf(startTime));
            while (!stop) {

                long now = System.currentTimeMillis();
                if (now - startTime > time * 1000) {
                    stop = true;
                }
                Entry e1 = null;
                try {
                    e1 = SphU.entry(name);

                    if (slow) {
                        TimeUnit.MILLISECONDS.sleep(3000);
                    }

                } catch (Exception e) {
                } finally {
                    if (e1 != null) {
                        e1.exit();
                    }
                }
                Random random2 = new Random();
                try {
                    TimeUnit.MILLISECONDS.sleep(random2.nextInt(200));
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            ContextUtil.exit();
        }

    }

}
