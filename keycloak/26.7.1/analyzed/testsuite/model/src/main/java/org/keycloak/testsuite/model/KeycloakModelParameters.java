/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.testsuite.model;

import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * 模型测试参数基类：限制允许的 SPI 与 ProviderFactory，并提供配置与 JUnit 规则扩展点。
 *
 * @author hmlnarik
 */
public class KeycloakModelParameters {

    /** 允许参与测试的 SPI 类型集合。 */
    private final Set<Class<? extends Spi>> allowedSpis;
    /** 允许参与测试的 ProviderFactory 类型集合。 */
    private final Set<Class<? extends ProviderFactory>> allowedFactories;

    /** 存储 provider 配置键前缀常量。 */
    protected static final String STORAGE_CONFIG = "storage.provider";

    /** 创建参数实例并指定允许的 SPI 与工厂类型。 */
    public KeycloakModelParameters(Set<Class<? extends Spi>> allowedSpis, Set<Class<? extends ProviderFactory>> allowedFactories) {
        this.allowedSpis = allowedSpis;
        this.allowedFactories = allowedFactories;
    }

    /** 判断给定 SPI 是否在允许列表中。 */
    boolean isSpiAllowed(Spi s) {
        return allowedSpis.contains(s.getClass());
    }

    /** 判断给定 ProviderFactory 是否在允许列表中。 */
    boolean isFactoryAllowed(ProviderFactory factory) {
        return allowedFactories.stream().anyMatch((c) -> c.isAssignableFrom(factory.getClass()));
    }

    /**
     * 返回指定类型的参数流；若本类未提供该类型参数则返回空流。
     * @param <T> 参数类型
     * @param clazz 参数类型 Class
     * @return 参数流
     */
    public <T> Stream<T> getParameters(Class<T> clazz) {
        return Stream.empty();
    }

    /** 在测试启动前更新 {@link Config}（子类可覆盖）。 */
    public void updateConfig(Config cf) {
    }

    /** 类级别 JUnit 规则包装（默认透传）。 */
    public Statement classRule(Statement base, Description description) {
        return base;
    }

    /** 实例级别 JUnit 规则包装（默认透传）。 */
    public Statement instanceRule(Statement base, Description description) {
        return base;
    }

    /** 测试套件开始前的钩子（子类可覆盖）。 */
    public void beforeSuite(Config cf) {

    }

    /** 测试套件结束后的钩子（子类可覆盖）。 */
    public void afterSuite() {

    }
}
