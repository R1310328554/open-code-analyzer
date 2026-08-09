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
package com.alibaba.csp.sentinel.demo.dubbo.consumer;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 经典 Dubbo Consumer Spring 配置：应用、组播注册中心与 Sentinel Filter。
 *
 * @author Eric Zhao
 */
@Configuration
@DubboComponentScan
public class ConsumerConfiguration {
    /** 配置 Consumer 应用名为 demo-consumer。 */
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("demo-consumer");
        return applicationConfig;
    }

    /** 使用组播注册中心 224.5.6.7:1234。 */
    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("multicast://224.5.6.7:1234");
        return registryConfig;
    }

    /** Consumer 全局配置，默认启用 sentinel.dubbo.consumer.filter。 */
    @Bean
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        // 若不想为 Dubbo Consumer 启用 Sentinel，可取消下行注释
        // consumerConfig.setFilter("-sentinel.dubbo.consumer.filter");
        return consumerConfig;
    }

    /** 注册演示用 Consumer 包装 Bean。 */
    @Bean
    public FooServiceConsumer annotationDemoServiceConsumer() {
        return new FooServiceConsumer();
    }
}
