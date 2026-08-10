/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.constants.OID4VCIConstants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * 对凭证中与时间相关的声明（如 iat/exp）施加关联性缓解策略。
 * <p>通过在窗口内随机化或按时间单位舍入，降低精确时间戳被用于用户关联的风险。</p>
 * <p>通过 Realm 属性配置（均为可选）：</p>
 * <ul>
 *   <li>{@code oid4vci.time.claims.strategy}：{@code off} | {@code randomize} | {@code round}（默认 off）</li>
 *   <li>{@code oid4vci.time.randomize.window.seconds}：随机化窗口秒数（默认 86400）</li>
 *   <li>{@code oid4vci.time.round.unit}：{@code SECOND} | {@code MINUTE} | {@code HOUR} | {@code DAY}（默认 SECOND）</li>
 * </ul>
 *
 * @author <a href="mailto:Rodrick.Awambeng@adorsys.com">Rodrick Awambeng</a>
 */
public class TimeClaimNormalizer {

    private static final Logger logger = Logger.getLogger(TimeClaimNormalizer.class);

    /** 时间声明归一化策略。 */
    public enum Strategy {
        OFF,
        RANDOMIZE,
        ROUND
    }

    /** 时间舍入粒度。 */
    public enum RoundUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY
    }

    private final Strategy strategy;
    private final int randomizeWindowSeconds;
    private final RoundUnit roundUnit;

    /** 默认随机化窗口：24 小时（秒）。 */
    public static final int DEFAULT_RANDOMIZE_WINDOW = 86400; // 24h default
    public static final Strategy DEFAULT_STRATEGY = Strategy.OFF;
    public static final RoundUnit DEFAULT_ROUND_UNIT = RoundUnit.SECOND;

    /** 从当前会话 Realm 读取归一化配置。 @param session Keycloak 会话 */
    public TimeClaimNormalizer(KeycloakSession session) {
        this(session.getContext().getRealm());
    }

    /** 从指定 Realm 属性解析归一化配置。 @param realm Realm 模型 */
    public TimeClaimNormalizer(RealmModel realm) {
        this.strategy = parseStrategy(realm.getAttribute(OID4VCIConstants.TIME_CLAIMS_STRATEGY));
        this.randomizeWindowSeconds = parseRandomizeWindow(realm.getAttribute(OID4VCIConstants.TIME_RANDOMIZE_WINDOW_SECONDS));
        this.roundUnit = parseRoundUnit(realm.getAttribute(OID4VCIConstants.TIME_ROUND_UNIT));
    }

    TimeClaimNormalizer(Strategy strategy, Integer randomizeWindowSeconds, RoundUnit roundUnit) {
        this.strategy = strategy == null ? DEFAULT_STRATEGY : strategy;
        this.randomizeWindowSeconds =
                randomizeWindowSeconds == null ? DEFAULT_RANDOMIZE_WINDOW : randomizeWindowSeconds;
        this.roundUnit = roundUnit == null ? DEFAULT_ROUND_UNIT : roundUnit;
    }

    /**
     * 按配置策略归一化时间点。
     * @param original 原始时间戳，可为 null
     * @return 归一化后的时间；输入为 null 时返回 null
     */
    public Instant normalize(Instant original) {
        if (original == null) {
            return null;
        }
        return switch (strategy) {
            case RANDOMIZE -> randomize(original);
            case ROUND -> round(original);
            case OFF -> original;
        };
    }

    private Instant randomize(Instant original) {
        int randomOffset = SecretGenerator.nextInt(randomizeWindowSeconds + 1);
        return original.minusSeconds(randomOffset);
    }

    private Instant round(Instant original) {
        // 刻意在 UTC 截断，确保舍入结果与时区无关
        ZonedDateTime zdt = original.atZone(ZoneOffset.UTC);
        return switch (roundUnit) {
            case SECOND -> zdt.truncatedTo(ChronoUnit.SECONDS).toInstant();
            case MINUTE -> zdt.truncatedTo(ChronoUnit.MINUTES).toInstant();
            case HOUR -> zdt.truncatedTo(ChronoUnit.HOURS).toInstant();
            case DAY -> zdt.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        };
    }

    private static Strategy parseStrategy(String value) {
        if (value == null) {
            return DEFAULT_STRATEGY;
        }
        try {
            return Strategy.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.warnf("Invalid time-claim strategy '%s'. Using default '%s'", value, DEFAULT_STRATEGY);
            return DEFAULT_STRATEGY;
        }
    }

    private static int parseRandomizeWindow(String value) {
        if (StringUtil.isBlank(value)) {
            return DEFAULT_RANDOMIZE_WINDOW;
        }
        try {
            int window = Integer.parseInt(value.trim());
            if (window <= 0) {
                logger.warnf("Randomization window is zero or negative (%d), will be using default value", window);
                return DEFAULT_RANDOMIZE_WINDOW;
            }
            return window;
        } catch (NumberFormatException ex) {
            logger.warnf("Invalid randomize window '%s'. Using default %d seconds", value, DEFAULT_RANDOMIZE_WINDOW);
            return DEFAULT_RANDOMIZE_WINDOW;
        }
    }

    private static RoundUnit parseRoundUnit(String value) {
        if (value == null) {
            return DEFAULT_ROUND_UNIT;
        }
        try {
            return RoundUnit.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.warnf("Invalid round unit '%s'. Using default '%s'", value, DEFAULT_ROUND_UNIT);
            return DEFAULT_ROUND_UNIT;
        }
    }
}
