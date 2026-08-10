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

package com.alibaba.nacos.istio.server;

import com.alibaba.nacos.istio.mcp.NacosMcpService;
import com.alibaba.nacos.istio.misc.IstioConfig;
import com.alibaba.nacos.istio.misc.Loggers;
import com.alibaba.nacos.istio.xds.NacosXdsService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Istio MCP/xDS gRPC 服务端：注册 MCP 与 xDS 服务并管理生命周期。
 *
 * <p>受 {@link IstioConfig#isServerEnabled()} 控制；关闭时跳过启动。</p>
 *
 * @author special.fy
 */
@Service
public class IstioServer {
    
    /** 底层 gRPC {@link Server} 实例。 */
    private Server server;
    
    @Autowired
    private IstioConfig istioConfig;
    
    @Autowired
    private ServerInterceptor serverInterceptor;
    
    @Autowired
    private NacosMcpService nacosMcpService;
    
    @Autowired
    private NacosXdsService nacosXdsService;
    
    /**
     * 启动 Istio gRPC 服务（MCP + xDS），并注册 JVM 关闭钩子。
     *
     * @throws IOException 端口绑定或启动失败
     */
    @PostConstruct
    public void start() throws IOException {
        
        if (!istioConfig.isServerEnabled()) {
            Loggers.MAIN.info("The Nacos Istio server is disabled.");
            return;
        }
        
        Loggers.MAIN.info("Nacos Istio server, starting Nacos Istio server...");
        
        // 为 MCP 与 xDS 服务挂载同一拦截器（记录远端地址与方法名）
        server = ServerBuilder.forPort(istioConfig.getServerPort())
            .addService(ServerInterceptors.intercept(nacosMcpService, serverInterceptor))
            .addService(ServerInterceptors.intercept(nacosXdsService, serverInterceptor))
            .build();
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            
            @Override
            public void run() {
                
                IstioServer.this.stop();
            }
        });
    }
    
    /**
     * 优雅关闭 gRPC 服务。
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
