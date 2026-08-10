/*
 *
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.istio.model;

import java.util.Map;

/**
 * Istio 资源快照中的服务集合容器。
 *
 * <p>键为服务全名，值为对应的 {@link IstioService}；CRD 资源扩展见 TODO。</p>
 *
 * @author RocketEngine26
 * @date 2022/8/9 16:26
 */
public class IstioResources {
    // TODO: 后续补充 VirtualService、DestinationRule 等 CRD 映射
    /** 服务名 → {@link IstioService} 映射表。 */
    private Map<String, IstioService> istioServiceMap;
    
    /**
     * @param istioServiceMap 当前快照下的全部 Istio 服务
     */
    public IstioResources(Map<String, IstioService> istioServiceMap) {
        this.istioServiceMap = istioServiceMap;
    }
    
    public Map<String, IstioService> getIstioServiceMap() {
        return istioServiceMap;
    }
    
    public void setIstioServiceMap(Map<String, IstioService> istioServiceMap) {
        this.istioServiceMap = istioServiceMap;
    }
}