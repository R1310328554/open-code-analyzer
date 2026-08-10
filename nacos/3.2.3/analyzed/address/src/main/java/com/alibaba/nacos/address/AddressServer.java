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

package com.alibaba.nacos.address;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * support address server.
 * <p>Nacos 地址服务器（Address Server）Spring Boot 启动入口：扫描 {@code com.alibaba.nacos} 包并启动独立进程，对外提供集群节点 IP 的注册、查询与删除能力，供客户端发现 Nacos 集群地址。</p>
 *
 * @author nacos
 * @since 1.1.0
 */
@SpringBootApplication(scanBasePackages = "com.alibaba.nacos")
public class AddressServer {
    
    /** 启动 Address Server 应用 */
    public static void main(String[] args) {
        
        SpringApplication.run(AddressServer.class, args);
    }
}
