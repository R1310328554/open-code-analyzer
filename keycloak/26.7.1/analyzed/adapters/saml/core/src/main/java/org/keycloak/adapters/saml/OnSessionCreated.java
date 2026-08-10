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

package org.keycloak.adapters.saml;

/**
 * SAML 会话创建后的回调接口。
 *
 * <p>应用可实现此接口，在适配器成功建立 SAML 会话后执行自定义逻辑
 *（例如审计、会话属性初始化等）。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface OnSessionCreated {

    /**
     * SAML 会话创建完成时调用。
     *
     * @param samlSession 新创建的 SAML 会话
     */
    void onSessionCreated(SamlSession samlSession);
}
