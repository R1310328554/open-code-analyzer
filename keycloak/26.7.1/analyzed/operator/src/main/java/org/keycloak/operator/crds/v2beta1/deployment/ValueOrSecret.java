/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.operator.crds.v2beta1.deployment;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.api.model.SecretKeySelector;

/**
 * 配置项名与取值模型：支持明文 {@code value} 或 Kubernetes {@link SecretKeySelector} 引用。
 *
 * <p>用于 {@link KeycloakSpec#getAdditionalOptions()} 与 {@link KeycloakSpec#getEnv()} 等字段。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueOrSecret {
    /** 配置项或环境变量名称。 */
    private String name;
    /** 明文配置值；与 {@link #secret} 二选一。 */
    private String value;
    /** 引用 Secret 中某个键作为配置值。 */
    private SecretKeySelector secret;

    /** 无参构造，供 Jackson 反序列化。 */
    public ValueOrSecret() {
    }

    /** 使用明文值构造。 */
    public ValueOrSecret(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /** 使用 Secret 引用构造。 */
    public ValueOrSecret(String name, SecretKeySelector secret) {
        this.name = name;
        this.secret = secret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SecretKeySelector getSecret() {
        return secret;
    }

    public void setSecret(SecretKeySelector secret) {
        this.secret = secret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueOrSecret that = (ValueOrSecret) o;
        // 仅按 name 比较：同一配置项不应出现多个不同取值
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
