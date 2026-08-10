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

package org.keycloak.exportimport;

import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * 客户端描述转换提供者 SPI：将任意格式的客户端配置文本转为内部 {@link ClientRepresentation}。
 * <p>供 Admin REST 导入客户端时按格式选择对应转换器实现。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientDescriptionConverter extends Provider {

    /** @param description 外部格式的客户端配置文本 @return 内部客户端表示对象 */
    ClientRepresentation convertToInternal(String description);

}
