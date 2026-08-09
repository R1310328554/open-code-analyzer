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
package com.alibaba.csp.sentinel.demo.apache.dubbo;

import java.util.Collections;

import com.alibaba.csp.sentinel.demo.apache.dubbo.provider.ProviderConfiguration;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Apache Dubbo 2.7+ 服务提供者启动类。
 * <p>启动前请添加 VM 参数：</p>
 * <pre>
 * -Djava.net.preferIPv4Stack=true
 * -Dcsp.sentinel.api.port=8720
 * -Dproject.name=dubbo-provider-demo
 * </pre>
 *
 * @author Eric Zhao
 */
public class FooProviderBootstrap {

    public static void main(String[] args) {
        // 一般无需手动调用；此处仅为 eager 初始化 Sentinel 组件
        InitExecutor.doInit();


        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ProviderConfiguration.class);
        context.refresh();

        System.out.println("Service provider is ready");
    }

}
