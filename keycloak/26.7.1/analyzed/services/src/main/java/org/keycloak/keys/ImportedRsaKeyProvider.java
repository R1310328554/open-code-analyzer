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

package org.keycloak.keys;

import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.models.RealmModel;


/**
 * 导入 RSA 密钥提供者：从组件配置加载外部 RSA 公私钥及证书链。
 * <p>继承 {@link AbstractRsaKeyProvider}，加载后通过 {@link KeyNoteUtils} 缓存密钥并跟踪证书过期。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ImportedRsaKeyProvider extends AbstractRsaKeyProvider {

    /** @param realm 当前领域 @param model 含 PEM 密钥材料的组件配置 */
    public ImportedRsaKeyProvider(RealmModel realm, ComponentModel model) {
        super(realm, model);

        // 导入密钥需检查证书 notAfter 过期时间并写入 model note
        KeyNoteUtils.attachKeyNotes(model, KeyWrapper.class.getName(), this.key);
    }
}
