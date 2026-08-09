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
import com.alibaba.csp.sentinel.demo.sofa.rpc.service.impl.DemoServiceImpl;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;

/**
 * SOFARPC Provider 演示：在 bolt:12001 导出 {@link DemoService}。
 *
 * 对接 Sentinel Dashboard 时可添加 JVM 参数：
 * <pre>
 * -Dproject.name=DemoProvider -Dcsp.sentinel.dashboard.server=localhost:8080
 * </pre>
 *
 * @author cdfive
 */
public class DemoProvider {

    public static void main(String[] args) {
        ServerConfig serverConfig = new ServerConfig()
                .setProtocol("bolt")
                .setPort(12001)
                .setDaemon(false);

        ProviderConfig<DemoService> providerConfig = new ProviderConfig<DemoService>()
                .setInterfaceId(DemoService.class.getName())
                .setRef(new DemoServiceImpl())
                .setServer(serverConfig);

        // 是否启用 Sentinel（默认启用），亦可在 rpc-config.json 全局配置
//        providerConfig.setParameter("sofa.rpc.sentinel.enabled", "false");

        providerConfig.export();

        System.out.println("DemoProvider started!");
    }
}