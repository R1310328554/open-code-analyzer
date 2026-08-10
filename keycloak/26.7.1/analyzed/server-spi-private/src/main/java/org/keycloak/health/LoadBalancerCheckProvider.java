/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.health;

import org.keycloak.provider.Provider;

/**
 * 负载均衡健康检查 SPI：任一实现报告组件不可用时，负载均衡端点返回 {@code DOWN} 状态。
 * <p>实现应在事件循环中非阻塞执行，避免过载时检查超时触发误切换。</p>
 */
@FunctionalInterface
public interface LoadBalancerCheckProvider extends Provider {

    /**
     * 判断本检查所代表的组件是否不可用。
     * <p>必须在事件循环中非阻塞执行；阻塞会导致过载时负载均衡侧超时，可能引发主备站点间 ping-pong 切换。</p>
     *
     * @return 组件不可用时为 {@code true}，否则为 {@code false}
     */
    boolean isDown();

    /** 默认无资源需释放。 */
    @Override
    default void close() {
        // 默认无操作
    }
}
