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

package org.keycloak.authentication.requiredactions.util;

import java.util.Map;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.common.util.Time;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

/**
 * 邮件重发冷却管理器：通过 {@link SingleUseObjectProvider} 缓存限制验证邮件重复发送频率。
 * 冷却时长由必需操作 Provider 配置项 {@link #EMAIL_RESEND_COOLDOWN_SECONDS} 控制。
 */
public class EmailCooldownManager {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(EmailCooldownManager.class);

    /** 配置键：两次邮件重发之间的最小间隔（秒）。 */
    public static final String EMAIL_RESEND_COOLDOWN_SECONDS = "emailResendCooldownSeconds";
    /** 默认冷却间隔：30 秒。 */
    public static final int EMAIL_RESEND_COOLDOWN_DEFAULT_SECONDS = 30;
    /** 缓存条目中的过期时间戳键名。 */
    private static final String KEY_EXPIRE = "expire";

    /**
     * 查询当前用户剩余冷却时间（秒）。
     * @param context 必需操作上下文
     * @param keyPrefix 缓存键前缀
     * @return 剩余秒数；无冷却或已过期时返回 {@code null}
     */
    public static Long retrieveCooldownEntry(RequiredActionContext context, String keyPrefix) {
        SingleUseObjectProvider singleUseCache = context.getSession().singleUseObjects();
        Map<String, String> cooldownDetails = singleUseCache.get(getCacheKey(context, keyPrefix));
        if (cooldownDetails == null) {
            return null;
        }
        long remaining = (Long.parseLong(cooldownDetails.get(KEY_EXPIRE)) - Time.currentTime());
        // 避免舍入导致剩余时间为 0 的边界情况
        return remaining > 0 ? remaining : null;
    }

    /** 为当前用户写入冷却缓存条目。 */
    public static void addCooldownEntry(RequiredActionContext context, String keyPrefix) {
        SingleUseObjectProvider cache = context.getSession().singleUseObjects();
        long cooldownSeconds = getCooldownInSeconds(context);
        cache.put(getCacheKey(context, keyPrefix), cooldownSeconds, Map.of(KEY_EXPIRE, Long.toString(Time.currentTime() + cooldownSeconds)));
    }

    /** @return 管理控制台可用的邮件重发冷却配置项定义 */
    public static ProviderConfigProperty createCooldownConfigProperty() {
        ProviderConfigProperty cooldown = new ProviderConfigProperty();
        cooldown.setName(EMAIL_RESEND_COOLDOWN_SECONDS);
        cooldown.setLabel("Cooldown Between Email Resend (seconds)");
        cooldown.setHelpText("Minimum delay in seconds before another email verification email can be sent.");
        cooldown.setType(ProviderConfigProperty.STRING_TYPE);
        cooldown.setDefaultValue(String.valueOf(EMAIL_RESEND_COOLDOWN_DEFAULT_SECONDS));
        return cooldown;
    }

    /** 生成用户级冷却缓存键。 */
    private static String getCacheKey(RequiredActionContext context, String keyPrefix) {
        return keyPrefix + context.getUser().getId();
    }

    /** 从必需操作 Provider 配置读取冷却秒数，失败时返回默认值。 */
    private static long getCooldownInSeconds(RequiredActionContext context) {
        try {
            RequiredActionProviderModel model = context.getRealm().getRequiredActionProviderByAlias(context.getAction());
            if (model == null || model.getConfig() == null) {
                logger.warn("No RequiredActionProviderModel found for alias: " + context.getAction());
                return EMAIL_RESEND_COOLDOWN_DEFAULT_SECONDS;
            }

            String value = model.getConfig().getOrDefault(EMAIL_RESEND_COOLDOWN_SECONDS, String.valueOf(EMAIL_RESEND_COOLDOWN_DEFAULT_SECONDS));
            return Long.parseLong(value);
        } catch (RuntimeException e) {
            logger.error("Failed to fetch cooldown from config: ", e);
            return EMAIL_RESEND_COOLDOWN_DEFAULT_SECONDS;
        }
    }
}
