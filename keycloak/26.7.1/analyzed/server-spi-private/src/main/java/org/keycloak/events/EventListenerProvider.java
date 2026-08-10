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

package org.keycloak.events;

import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.provider.Provider;

/**
 * 事件监听器提供者 SPI：在 Keycloak 运行期间接收用户事件与管理事件。
 * <p>事件类型：</p>
 * <ul>
 *     <li>{@link Event} — 用户操作（登录、注册等）</li>
 *     <li>{@link org.keycloak.events.admin.AdminEvent} — 管理员操作（客户端创建/更新等）</li>
 * </ul>
 * <p>{@code onEvent} 与 {@code onAdminEvent} 通常在活动事务内调用；JPA 实现可将明细写入同一事务。 不可回滚的副作用（如写日志文件）应通过 {@link org.keycloak.models.KeycloakTransactionManager#enlistAfterCompletion(KeycloakTransaction)} 在事务提交后执行。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface EventListenerProvider extends Provider {

    /**
     * 用户事件发生时的回调（如登录、注册）。
     * <p>避免执行无法随事务回滚的操作，详见类级 JavaDoc。</p>
     * 
     * @param event to be triggered
     */
    void onEvent(Event event);

    /**
     * 管理事件发生时的回调（如客户端更新/删除）。
     * <p>避免执行无法随事务回滚的操作，详见类级 JavaDoc。</p>
     *
     * @param event to be triggered
     * @param includeRepresentation when false, event listener should NOT include representation field in the resulting
     *                              action
     */
    void onEvent(AdminEvent event, boolean includeRepresentation);

}
