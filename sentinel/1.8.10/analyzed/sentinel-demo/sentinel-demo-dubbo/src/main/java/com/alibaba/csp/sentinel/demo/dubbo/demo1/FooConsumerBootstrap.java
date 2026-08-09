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
package com.alibaba.csp.sentinel.demo.dubbo.demo1;

import com.alibaba.csp.sentinel.demo.dubbo.consumer.ConsumerConfiguration;
import com.alibaba.csp.sentinel.demo.dubbo.consumer.FooServiceConsumer;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 经典 Dubbo Consumer 启动类：循环调用 sayHello 演示流控阻塞。
 * <p>启动前请添加 VM 参数：</p>
 * <pre>
 * -Djava.net.preferIPv4Stack=true
 * -Dcsp.sentinel.api.port=8721
 * -Dproject.name=dubbo-consumer-demo
 * </pre>
 *
 * @author Eric Zhao
 */
public class FooConsumerBootstrap {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext consumerContext = new AnnotationConfigApplicationContext();
        consumerContext.register(ConsumerConfiguration.class);
        consumerContext.refresh();

        FooServiceConsumer service = consumerContext.getBean(FooServiceConsumer.class);

        for (int i = 0; i < 15; i++) {
            try {
                String message = service.sayHello("Eric");
                System.out.println("Success: " + message);
            } catch (SentinelRpcException ex) {
                // 被 Sentinel 限流/熔断时抛出 SentinelRpcException
                System.out.println("Blocked");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
