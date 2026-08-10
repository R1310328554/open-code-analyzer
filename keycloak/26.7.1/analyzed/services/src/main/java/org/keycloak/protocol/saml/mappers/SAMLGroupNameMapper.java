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

package org.keycloak.protocol.saml.mappers;

import org.keycloak.models.GroupModel;
import org.keycloak.models.ProtocolMapperModel;

/**
 * SAML 组名称映射器接口。
 * <p>实现此接口的映射器可将 {@link GroupModel} 映射为 SAML 断言中的自定义组名。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface SAMLGroupNameMapper {
    /**
     * 将组映射为新名称。
     * @param model 映射器配置
     * @param group 用户组
     * @return 映射后的组名，不匹配时 null
     */
    String mapName(ProtocolMapperModel model, GroupModel group);
}
