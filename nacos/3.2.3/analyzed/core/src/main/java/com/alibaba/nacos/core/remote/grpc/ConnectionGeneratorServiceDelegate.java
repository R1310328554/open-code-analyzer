/*
 *
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.core.remote.grpc;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.LoggerUtils;
import com.alibaba.nacos.core.remote.Connection;
import com.alibaba.nacos.core.remote.ConnectionMeta;
import io.grpc.netty.shaded.io.netty.channel.Channel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

/**
 * {@link ConnectionGeneratorService} 单例委托：通过 SPI 加载指定类型的连接生成实现。
 * ConnectionGeneratorServiceDelegate.
 *
 * @author jianwei.wjw
 */

public class ConnectionGeneratorServiceDelegate {
    
    /** 连接生成器类型，默认 nacos，可通过系统属性覆盖。 */
    private String connectionGeneratorType =
        System.getProperty("nacos.core.remote.connection.generator", "nacos");
    
    /** 已加载的 SPI 连接生成器实例。 */
    private ConnectionGeneratorService connectionGeneratorService = null;
    
    /** 日志记录器。 */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ConnectionGeneratorServiceDelegate.class);
    
    /** 私有构造：扫描 SPI 并匹配 connectionGeneratorType。 */
    private ConnectionGeneratorServiceDelegate() {
        for (ConnectionGeneratorService connectionGeneratorService : NacosServiceLoader
            .load(ConnectionGeneratorService.class)) {
            if (connectionGeneratorService.getType().equals(connectionGeneratorType)) {
                this.connectionGeneratorService = connectionGeneratorService;
                LoggerUtils.printIfInfoEnabled(LOGGER, "{} has been loaded, class: {}",
                    connectionGeneratorType, connectionGeneratorService.getClass().getName());
            }
        }
        
        if (Objects.isNull(connectionGeneratorService)) {
            throw new RuntimeException("can not find implementation of "
                + ConnectionGeneratorService.class.getName() + " for type "
                + connectionGeneratorType);
        }
    }
    
    /** 单例实例。 */
    private static final ConnectionGeneratorServiceDelegate INSTANCE =
        new ConnectionGeneratorServiceDelegate();
    
    /** 获取单例委托。 */
    public static ConnectionGeneratorServiceDelegate getInstance() {
        return INSTANCE;
    }
    
    /** 委托 SPI 实现创建连接。 */
    public Connection getConnection(ConnectionMeta metaInfo, StreamObserver streamObserver,
        Channel channel) {
        return connectionGeneratorService.getConnection(metaInfo, streamObserver, channel);
    }
}
