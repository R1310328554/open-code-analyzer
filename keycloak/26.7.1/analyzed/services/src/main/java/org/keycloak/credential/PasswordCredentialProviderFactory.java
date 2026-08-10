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
package org.keycloak.credential;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.config.MetricsOptions;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;

/**
 * 密码凭证 {@link PasswordCredentialProvider} 的 SPI 工厂。
 * <p>支持 Micrometer 指标：可配置 realm、算法、哈希强度与校验结果等标签维度。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PasswordCredentialProviderFactory implements CredentialProviderFactory<PasswordCredentialProvider> {
    /** SPI 工厂标识：{@code keycloak-password}。 */
    public static final String PROVIDER_ID = "keycloak-password";
    /** 配置项：密码校验计数器 Micrometer 标签列表（逗号分隔）。 */
    private static final String HASHES_COUNTER_TAGS = "validations-counter-tags";
    private static final String KEYCLOAK_METER_NAME_PREFIX = "keycloak.";
    /** Micrometer 计数器名称：密码哈希/校验次数。 */
    private static final String LOGIN_PASSWORD_VERIFY_METER_NAME = KEYCLOAK_METER_NAME_PREFIX + "credentials.password.hashing";
    private static final String LOGIN_PASSWORD_VERIFY_METER_DESCRIPTION = "Password validations";

    /** 指标标签：Realm 名称。 */
    public static final String METER_REALM_TAG = "realm";
    /** 指标标签：哈希算法 ID。 */
    public static final String METER_ALGORITHM_TAG = "algorithm";
    /** 指标标签：哈希迭代次数/强度。 */
    public static final String METER_HASHING_STRENGTH_TAG = "hashing_strength";
    /** 指标标签：校验结果（成功/失败）。 */
    public static final String METER_VALIDATION_OUTCOME_TAG = "outcome";
    private static final String HASHES_COUNTER_TAGS_DEFAULT_VALUE = String.format("%s,%s,%s,%s", METER_REALM_TAG, METER_ALGORITHM_TAG, METER_HASHING_STRENGTH_TAG, METER_VALIDATION_OUTCOME_TAG);

    /** 是否启用密码校验 Micrometer 指标。 */
    private boolean metricsEnabled;
    private boolean withRealmInMetric;
    private boolean withAlgorithmInMetric;
    private boolean withHashingStrengthInMetric;
    private boolean withOutcomeInMetric;

    /** 懒加载的密码校验计数器提供者。 */
    private Meter.MeterProvider<Counter> meterProvider;

    @Override
    /** @param session 当前 Keycloak 会话 @return 带指标配置的密码凭证提供者 */
    public PasswordCredentialProvider create(KeycloakSession session) {
        return new PasswordCredentialProvider(session, meterProvider, metricsEnabled, withRealmInMetric, withAlgorithmInMetric, withHashingStrengthInMetric, withOutcomeInMetric);
    }

    @Override
    /** 读取 metrics 开关及 {@link #HASHES_COUNTER_TAGS} 决定各维度标签是否上报。 */
    public void init(Config.Scope config) {
        metricsEnabled = config.root().getBoolean(MetricsOptions.METRICS_ENABLED.getKey(), false);
        if (metricsEnabled) {
            meterProvider = Counter.builder(LOGIN_PASSWORD_VERIFY_METER_NAME)
                    .description(LOGIN_PASSWORD_VERIFY_METER_DESCRIPTION)
                    .baseUnit("validations")
                    .withRegistry(Metrics.globalRegistry);

            Set<String> tags = Arrays.stream(config.get(HASHES_COUNTER_TAGS, HASHES_COUNTER_TAGS_DEFAULT_VALUE).split(",")).collect(Collectors.toSet());
            withRealmInMetric = tags.contains(METER_REALM_TAG);
            withAlgorithmInMetric = tags.contains(METER_ALGORITHM_TAG);
            withHashingStrengthInMetric = tags.contains(METER_HASHING_STRENGTH_TAG);
            withOutcomeInMetric = tags.contains(METER_VALIDATION_OUTCOME_TAG);
        }
    }

    @Override
    /** @return {@link #PROVIDER_ID} */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 暴露密码校验计数器可配置标签的 SPI 元数据。 */
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                    .name(HASHES_COUNTER_TAGS)
                    .type("string")
                    .helpText("Comma-separated list of tags to be used when publishing password validation counter metric.")
                    .options(METER_REALM_TAG, METER_ALGORITHM_TAG, METER_HASHING_STRENGTH_TAG, METER_VALIDATION_OUTCOME_TAG)
                    .defaultValue(HASHES_COUNTER_TAGS_DEFAULT_VALUE)
                    .add()
                .build();
    }
}
