/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.issuance.mappers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oid4vc.issuance.TimeClaimNormalizer;
import org.keycloak.protocol.oid4vc.model.CredentialSubject;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

/**
 * 将签发时间映射到凭证主体的协议映射器，默认声明名为 {@code iat}。
 * <p>可通过 {@link #CLAIM_NAME} 配置声明名；{@link #VALUE_SOURCE} 选择计算当前时间或读取 VC 已有签发日期； {@link #TRUNCATE_TO_TIME_UNIT_KEY} 支持按 {@link ChronoUnit} 截断以降低凭证关联风险。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class OID4VCIssuedAtTimeClaimMapper extends OID4VCMapper {

    /** 协议映射器 Provider ID。 */
    public static final String MAPPER_ID = "oid4vc-issued-at-time-claim-mapper";

    // 使用 ChronoUnit 枚举截断时间粒度；未配置则不截断。
    /** 配置键：时间截断粒度（ChronoUnit 名称）。 */
    public static final String TRUNCATE_TO_TIME_UNIT_KEY = "truncateToTimeUnit";

    // 值来源：COMPUTE 计算当前时间，VC 读取凭证签发日期；默认 COMPUTE。
    /** 配置键：时间值来源（{@code COMPUTE} 或 {@code VC}）。 */
    public static final String VALUE_SOURCE = "valueSource";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();
    private static final Logger log = Logger.getLogger(OID4VCIssuedAtTimeClaimMapper.class);

    static {
        ProviderConfigProperty subjectPropertyNameConfig = new ProviderConfigProperty();
        subjectPropertyNameConfig.setName(CLAIM_NAME);
        subjectPropertyNameConfig.setLabel("Time Claim Name");
        subjectPropertyNameConfig.setHelpText("Name of this time claim. Default is iat");
        subjectPropertyNameConfig.setType(ProviderConfigProperty.STRING_TYPE);
        subjectPropertyNameConfig.setDefaultValue("iat");
        CONFIG_PROPERTIES.add(subjectPropertyNameConfig);

        ProviderConfigProperty truncateToTimeUnit = new ProviderConfigProperty();
        truncateToTimeUnit.setName(TRUNCATE_TO_TIME_UNIT_KEY);
        truncateToTimeUnit.setLabel("Truncate To Time Unit");
        truncateToTimeUnit.setHelpText("Truncate time to the start of the selected unit. Supported: SECONDS, MINUTES, HOURS, HALF_DAYS, DAYS, WEEKS, MONTHS, YEARS. Such as to prevent correlation of credentials based on this time value.");
        truncateToTimeUnit.setType(ProviderConfigProperty.LIST_TYPE);
        truncateToTimeUnit.setOptions(List.of("SECONDS", "MINUTES", "HOURS", "HALF_DAYS", "DAYS", "WEEKS", "MONTHS", "YEARS"));
        CONFIG_PROPERTIES.add(truncateToTimeUnit);

        ProviderConfigProperty valueSource = new ProviderConfigProperty();
        valueSource.setName(VALUE_SOURCE);
        valueSource.setLabel("Source of Value");
        valueSource.setHelpText("Tells the protocol mapper where to get the information. For now: COMPUTE or VC. Default is COMPUTE, in which this protocol mapper computes the current time in seconds. With value `VC`, the time is read from the verifiable credential issuance date field.");
        valueSource.setType(ProviderConfigProperty.LIST_TYPE);
        valueSource.setOptions(List.of("COMPUTE", "VC"));
        valueSource.setDefaultValue("COMPUTE");
        CONFIG_PROPERTIES.add(valueSource);
    }

    @Override
    protected List<ProviderConfigProperty> getIndividualConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    /** 默认不将此声明纳入凭证元数据。 */

    @Override
    public boolean includeInMetadata() {
        return Optional.ofNullable(mapperModel.getConfig().get(CredentialScopeModel.VC_INCLUDE_IN_METADATA))
                       .map(Boolean::parseBoolean)
                       .orElse(false);
    }

    public void setClaim(VerifiableCredential verifiableCredential,
                         UserSessionModel userSessionModel) {
        // 解析时间来源、归一化并按需截断后写入 subject
        List<String> attributePath = getMetadataAttributePath();
        String propertyName = attributePath.get(attributePath.size() - 1);
        if (propertyName == null) {
            log.errorf("Invalid configuration: missing config-property '%s' for mapper '%s' of type '%s'. Mapper is ignored.",
                      CLAIM_NAME,
                      mapperModel.getName(),
                      MAPPER_ID);
            return;
        }

        Instant iat = Optional.ofNullable(mapperModel.getConfig())
                .flatMap(config -> Optional.ofNullable(config.get(VALUE_SOURCE)))
                .filter(valueSource -> Objects.equals(valueSource, "COMPUTE"))
                .map(valueSource -> Instant.ofEpochSecond(Time.currentTime()))
                .orElseGet(() -> Optional.ofNullable(verifiableCredential.getIssuanceDate())
                        .orElse(Instant.ofEpochSecond(Time.currentTime())));

        Instant normalizedIat = new TimeClaimNormalizer(userSessionModel.getRealm())
                .normalize(iat);

        // 可选按 ChronoUnit 截断；未配置则使用归一化后的 iat
        Instant iatTrunc = Optional.ofNullable(mapperModel.getConfig())
                .map(config -> config.get(TRUNCATE_TO_TIME_UNIT_KEY))
                .filter(val -> !val.isEmpty())
                .map(ChronoUnit::valueOf)
                .map(normalizedIat::truncatedTo)
                .orElse(normalizedIat);

        CredentialSubject credentialSubject = verifiableCredential.getCredentialSubject();
        credentialSubject.setClaims(propertyName, iatTrunc.getEpochSecond());
    }

    @Override
    public void setClaim(Map<String, Object> claims, UserSessionModel userSessionModel) {
        // 时间写入 VC credentialSubject，Map 重载为空操作
    }

    @Override
    public String getDisplayType() {
        return "Issuance Date Claim Mapper";
    }

    @Override
    public String getHelpText() {
        return "Allows to set the issuance date credential subject.";
    }

    @Override
    public ProtocolMapper create(KeycloakSession session) {
        return new OID4VCIssuedAtTimeClaimMapper();
    }

    @Override
    public String getId() {
        return MAPPER_ID;
    }
}
