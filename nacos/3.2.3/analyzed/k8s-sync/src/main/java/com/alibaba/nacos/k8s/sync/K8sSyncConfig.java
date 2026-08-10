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

package com.alibaba.nacos.k8s.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kubernetes 同步模块配置项：开关、集群外访问及 kubeconfig 路径。
 *
 * <p>通过 {@code nacos.k8s.sync.*} 属性注入，供 {@link K8sSyncServer} 与 {@link K8sSyncEnabledFilter} 使用。</p>
 *
 * @author EmanuelGi
 */
@Component
public class K8sSyncConfig {
    
    /** 是否启用 K8s 服务同步，默认关闭。 */
    @Value("${nacos.k8s.sync.enabled:false}")
    private boolean enabled = false;
    
    /** 是否在集群外运行（需加载 kubeconfig 文件）。 */
    @Value("${nacos.k8s.sync.outsideCluster:false}")
    private boolean outsideCluster = false;
    
    /** 集群外模式下的 kubeconfig 文件路径。 */
    @Value("${nacos.k8s.sync.kubeConfig:}")
    private String kubeConfig;
    
    /** 是否已启用 K8s 同步。 */
    public boolean isEnabled() {
        return enabled;
    }
    
    /** 是否为集群外部署模式。 */
    public boolean isOutsideCluster() {
        return outsideCluster;
    }
    
    /** 获取 kubeconfig 路径。 */
    public String getKubeConfig() {
        return kubeConfig;
    }
}
