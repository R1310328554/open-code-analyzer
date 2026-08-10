/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.vault;

import java.util.function.BiFunction;

/**
 * 保险库密钥解析器：将 realm 名与 {@code ${vault.key}} 表达式中的 key 组合为最终 vault 条目名。
 * 实现定义 realm 与 key 的拼接算法。
 *
 * {@code VaultKeyResolver} is a {@link BiFunction} whose implementation of the {@link #apply(Object, Object)} method takes
 * two {@link String}s representing the realm name and the key name (as used in {@code ${vault.key}} expressions) and returns
 * another {@link String} representing the final constructed key that is to be used when obtaining secrets from the vault.
 * <p/>
 * Implementations essentially define the algorithm that is to be used to combine the realm and the key to create the name
 * that represents an entry in the vault.
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface VaultKeyResolver extends BiFunction<String, String, String> {

}
