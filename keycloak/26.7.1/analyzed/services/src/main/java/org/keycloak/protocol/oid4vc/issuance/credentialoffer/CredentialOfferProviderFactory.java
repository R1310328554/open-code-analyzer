/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.issuance.credentialoffer;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link CredentialOfferProvider} 的 Provider 工厂接口。
 * <p>由 SPI 加载，用于在会话中创建凭证发放 Provider 实例。</p>
 *
 * @author <a href="mailto:tdiesler@ibm.com">Thomas Diesler</a>
 */
public interface CredentialOfferProviderFactory extends ProviderFactory<CredentialOfferProvider> {

    @Override
    default void close() { }

}
