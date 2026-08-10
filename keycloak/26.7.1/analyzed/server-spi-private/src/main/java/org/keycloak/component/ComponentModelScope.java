/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.component;

import java.util.Set;

import org.keycloak.Config.AbstractScope;
import org.keycloak.Config.Scope;

/**
 * 将 {@link ComponentModel} 配置与底层 {@link Scope} 合并的配置作用域。
 * <p>组件属性优先于工厂级配置；支持嵌套 scope 前缀。</p>
 *
 * @author hmlnarik
 */
public class ComponentModelScope extends AbstractScope {

    private final Scope origScope;
    private final ComponentModel componentConfig;
    private final String prefix;

    /** 以空前缀将组件配置叠加到原始 Scope 上。 */
    public ComponentModelScope(Scope origScope, ComponentModel componentConfig) {
        this(origScope, componentConfig, "");
    }

    /** 以指定键前缀构造组件配置作用域。 */
    public ComponentModelScope(Scope origScope, ComponentModel componentConfig, String prefix) {
        this.origScope = origScope;
        this.componentConfig = componentConfig;
        this.prefix = prefix;
    }

    /** 返回组件 ID。 */
    public String getComponentId() {
        return componentConfig.getId();
    }

    /** 返回组件显示名称。 */
    public String getComponentName() {
        return componentConfig.getName();
    }

    /** 读取组件附注（note）属性。 */
    public <T> T getComponentNote(String key) {
        return componentConfig.getNote(key);
    }

    /** 返回父组件 ID。 */
    public String getComponentParentId() {
        return componentConfig.getParentId();
    }

    /** 返回组件子类型。 */
    public String getComponentSubType() {
        return componentConfig.getSubType();
    }

    @Override
    public String get(String key) {
        final String res = componentConfig.get(prefix + key, null);
        return (res == null) ? origScope.get(key) : res;
    }

    @Override
    public Scope scope(String... scope) {
        return new ComponentModelScope(origScope.scope(scope), componentConfig, String.join(".", scope) + ".");
    }

    @Override
    public Set<String> getPropertyNames() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** 返回底层 {@link ComponentModel}。 */
    public ComponentModel getComponentModel() {
        return componentConfig;
    }

    @Override
    public Scope root() {
        return this.origScope.root();
    }

}
