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

package org.keycloak.testframework.remote.providers.runonserver;

import java.io.IOException;
import java.io.Serializable;

import org.keycloak.models.KeycloakSession;

/**
 * 在 Keycloak 服务器端执行并返回结果的远程可序列化任务。
 * <p>
 * 与 {@link RunOnServer} 不同，本接口的 {@link #run(KeycloakSession)} 会产出返回值，
 * 由 {@link RunOnServerRealmResourceProvider} 序列化为 JSON 传回测试进程。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface FetchOnServer extends Serializable {

    /**
     * 在服务器 {@link KeycloakSession} 上下文中执行任务并返回结果。
     *
     * @param session 当前 Keycloak 会话
     * @return 可 JSON 序列化的结果对象，若无返回值可为 {@code null}
     * @throws IOException 序列化或 I/O 失败时抛出
     */
    Object run(KeycloakSession session) throws IOException;

}
