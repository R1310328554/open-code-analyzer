/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.pojo.instance;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.builder.InstanceBuilder;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.naming.healthcheck.RsInfo;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * 客户端心跳 {@link RsInfo} 转 {@link Instance} 的构建器。
 *
 * <p>包装 {@link InstanceBuilder}，经 SPI 加载的 {@link InstanceExtensionHandler} 链式处理扩展字段，最后由 {@link InstanceIdGeneratorManager} 生成 instanceId。</p>
 *
 * @author xiweng.yy
 */
public class BeatInfoInstanceBuilder {
    
    private final InstanceBuilder actualBuilder;
    
    private final Collection<InstanceExtensionHandler> handlers;
    
    /** 私有构造，初始化底层 Builder 与扩展处理器集合。 */
    private BeatInfoInstanceBuilder() {
        this.actualBuilder = InstanceBuilder.newBuilder();
        this.handlers = NacosServiceLoader.newServiceInstances(InstanceExtensionHandler.class);
    }
    
    /** 创建新的心跳实例构建器。 */
    public static BeatInfoInstanceBuilder newBuilder() {
        return new BeatInfoInstanceBuilder();
    }
    
    /**
     * 构建 {@link Instance} 并依次执行扩展处理器，最后写入 instanceId。
     *
     * @return new instance
     */
    public Instance build() {
        Instance result = actualBuilder.build();
        for (InstanceExtensionHandler each : handlers) {
            each.handleExtensionInfo(result);
        }
        setInstanceId(result);
        return result;
    }
    
    /** 从 HTTP 请求配置各扩展处理器的扩展信息。 */
    public BeatInfoInstanceBuilder setRequest(HttpServletRequest request) {
        for (InstanceExtensionHandler each : handlers) {
            each.configExtensionInfoFromRequest(request);
        }
        return this;
    }
    
    public BeatInfoInstanceBuilder setServiceName(String serviceName) {
        actualBuilder.setServiceName(serviceName);
        return this;
    }
    
    /** 将心跳 RsInfo 属性映射到底层 InstanceBuilder。 */
    public BeatInfoInstanceBuilder setBeatInfo(RsInfo beatInfo) {
        setAttributesToBuilder(beatInfo);
        return this;
    }
    
    private void setAttributesToBuilder(RsInfo beatInfo) {
        actualBuilder.setPort(beatInfo.getPort());
        actualBuilder.setIp(beatInfo.getIp());
        actualBuilder.setWeight(beatInfo.getWeight());
        actualBuilder.setMetadata(beatInfo.getMetadata());
        actualBuilder.setClusterName(beatInfo.getCluster());
        actualBuilder.setEphemeral(beatInfo.isEphemeral());
    }
    
    /** 调用 ID 生成管理器为实例写入唯一 instanceId。 */
    private void setInstanceId(Instance instance) {
        instance.setInstanceId(InstanceIdGeneratorManager.generateInstanceId(instance));
    }
}
