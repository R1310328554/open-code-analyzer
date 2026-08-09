/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.transport.springmvc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.command.SentinelApiHandlerAdapter;
import com.alibaba.csp.sentinel.transport.command.SentinelApiHandlerMapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring MVC + Sentinel Transport 演示：暴露 HTTP API 供 Dashboard 拉取指标。
 * <p>连接 Dashboard：</p>
 * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-transport-spring-mvc}
 * <p>指定应用 API 端口：</p>
 * {@code -Dcsp.sentinel.api.port=10000}
 *
 * @author shenbaoyong
 */
@SpringBootApplication
@Controller
public class TransportSpringMvcDemoApplication {

    public static void main(String[] args) {
        triggerSentinelInit();
        initFlowRules();
        SpringApplication.run(TransportSpringMvcDemoApplication.class);
    }

    /** 加载 demo-hello-api 流控规则：QPS=1。 */
    public static void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("demo-hello-api");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    @GetMapping("/hello")
    @ResponseBody
    /** GET /hello：受 Sentinel 保护的问候接口。 */
    public String hello() {
        Entry entry = null;
        try {
            entry = SphU.entry("demo-hello-api");
            return "ok: " + LocalDateTime.now();
        } catch (BlockException e1) {
            return "helloBlockHandler: " + LocalDateTime.now();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    /** 异步触发 Sentinel InitExecutor 初始化。 */
    private static void triggerSentinelInit() {
        new Thread(() -> InitExecutor.doInit()).start();
    }

    @Bean
    public SentinelApiHandlerMapping sentinelApiHandlerMapping() {
        return new SentinelApiHandlerMapping();
    }

    @Bean
    public SentinelApiHandlerAdapter sentinelApiHandlerAdapter() {
        return new SentinelApiHandlerAdapter();
    }
}
