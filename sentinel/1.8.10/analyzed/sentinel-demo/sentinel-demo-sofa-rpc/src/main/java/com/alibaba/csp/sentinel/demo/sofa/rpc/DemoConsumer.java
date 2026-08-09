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
package com.alibaba.csp.sentinel.demo.sofa.rpc;

import com.alibaba.csp.sentinel.demo.sofa.rpc.service.DemoService;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;

import java.util.concurrent.TimeUnit;

/**
 * SOFARPC Consumer 演示：bolt 直连 Provider 循环调用 {@link DemoService}。
 *
 * 对接 Sentinel Dashboard 时可添加 JVM 参数：
 * <pre>
 * -Dproject.name=DemoProvider -Dcsp.sentinel.dashboard.server=localhost:8080
 * </pre>
 *
 * @author cdfive
 */
public class DemoConsumer {

    public static void main(String[] args) throws Exception {
        ApplicationConfig application = new ApplicationConfig().setAppName("DemoConsumer");

        ConsumerConfig<DemoService> consumerConfig = new ConsumerConfig<DemoService>()
            .setApplication(application)
            .setInterfaceId(DemoService.class.getName())
            .setProtocol("bolt")
            .setDirectUrl("bolt://127.0.0.1:12001")
            .setInvokeType(RpcConstants.INVOKER_TYPE_SYNC);

        // 是否启用 Sentinel（默认启用），亦可在 rpc-config.json 全局配置
//        consumerConfig.setParameter("sofa.rpc.sentinel.enabled", "false");

        DemoService helloService = consumerConfig.refer();

        System.out.println("DemoConsumer started!");

        long sleepMs = 5;
        int total = 5000;
        int index = 0;
        System.out.println("Total call " + total + " times and sleep " + sleepMs + "ms after each call.");

        while (true) {
            try {
                index++;
                String result = helloService.sayHello(index, "SOFARPC", 2020);
                System.out.println("[" + index + "][Consumer]receive response: " + result);
            } catch (Exception e) {
                System.out.println("[" + index + "][Consumer]receive exception: " + e.getMessage());
            }

            TimeUnit.MILLISECONDS.sleep(sleepMs);

            if (index == total) {
                break;
            }
        }

        System.out.println("DemoConsumer exit!");
        System.exit(0);
    }
}