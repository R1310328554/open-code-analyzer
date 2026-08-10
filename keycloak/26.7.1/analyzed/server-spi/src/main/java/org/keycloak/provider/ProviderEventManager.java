/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.provider;

/**
 * Provider 事件管理器：注册/注销监听器并发布事件。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ProviderEventManager {
    /** 注册事件监听器。
     * @param listener 监听器 */
    void register(ProviderEventListener listener);

    /** 注销事件监听器。
     * @param listener 监听器 */
    void unregister(ProviderEventListener listener);

    /** 发布 Provider 事件。
     * @param event 事件对象 */
    void publish(ProviderEvent event);
}
