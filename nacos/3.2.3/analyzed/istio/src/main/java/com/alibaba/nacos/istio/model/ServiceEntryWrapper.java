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

package com.alibaba.nacos.istio.model;

import istio.mcp.v1alpha1.MetadataOuterClass.Metadata;
import istio.networking.v1alpha3.ServiceEntryOuterClass.ServiceEntry;

/**
 * MCP ServiceEntry 资源及其元数据的包装类。
 *
 * <p>将 {@link Metadata} 与 {@link ServiceEntry} 成对传递，供 MCP 聚合推送。</p>
 *
 * @author special.fy
 */
public class ServiceEntryWrapper {

    /** MCP 资源元数据（名称、版本、标签、注解）。 */
    private Metadata metadata;

    /** Istio ServiceEntry protobuf 主体。 */
    private ServiceEntry serviceEntry;

    /**
     * @param metadata     MCP 元数据
     * @param serviceEntry ServiceEntry 资源体
     */
    public ServiceEntryWrapper(Metadata metadata, ServiceEntry serviceEntry) {
        this.metadata = metadata;
        this.serviceEntry = serviceEntry;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public ServiceEntry getServiceEntry() {
        return serviceEntry;
    }
}
