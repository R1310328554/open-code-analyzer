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
package org.keycloak.subsystem.adapter.saml.extension;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 * 添加密钥（Key）资源的管理操作处理器。
 *
 * <p>将 CLI/XML 添加操作写入 {@link Configuration} 内存配置树。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
class KeyAddHandler extends AbstractAddStepHandler {

    /** 使用 Key 全部属性定义构造添加处理器。 */
    KeyAddHandler() {
        super(KeyDefinition.ALL_ATTRIBUTES);
    }

    /** 运行时将 Key 模型合并到全局配置树。 */
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        Configuration.INSTANCE.updateModel(operation, model);
    }
}
