/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.testsuite.arquillian;

/**
 * 负载均衡器控制接口，供 Arquillian 集成测试启停后端 Keycloak 节点。
 *
 * @author hmlnarik
 */
public interface LoadBalancerController {

    /** 启用全部后端节点。 */
    void enableAllBackendNodes();

    /** 禁用全部后端节点。 */
    void disableAllBackendNodes();

    /**
     * 按名称启用指定后端节点。
     *
     * @param nodeName 节点名称
     */
    void enableBackendNodeByName(String nodeName);

    /**
     * 按名称禁用指定后端节点。
     *
     * @param nodeName 节点名称
     */
    void disableBackendNodeByName(String nodeName);

}
