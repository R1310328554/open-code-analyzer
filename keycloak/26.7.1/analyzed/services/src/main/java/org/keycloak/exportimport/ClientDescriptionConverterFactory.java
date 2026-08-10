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

import org.keycloak.provider.ProviderFactory;

/**
 * {@link ClientDescriptionConverter} 的 {@link ProviderFactory} 工厂接口。
 * <p>工厂需声明是否支持给定格式的客户端描述文本，以便 Admin 导入流程选择转换器。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientDescriptionConverterFactory extends ProviderFactory<ClientDescriptionConverter> {

    /** @param description 待导入的客户端配置文本 @return 本工厂能否识别并转换该格式 */
    boolean isSupported(String description);

}
