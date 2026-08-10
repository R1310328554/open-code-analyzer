/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.policy.provider.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.provider.PolicyProvider;

import org.jboss.logging.Logger;

/**
 * 时间策略提供者：根据当前时间（或上下文注入的时间）与策略配置的时间窗口判定是否授予访问。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TimePolicyProvider implements PolicyProvider {

    private static final Logger logger = Logger.getLogger(TimePolicyProvider.class);

    /** 默认日期时间格式。 */
    static String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 评估上下文中可覆盖当前时间的属性键。 */
    static String CONTEXT_TIME_ENTRY = "kc.time.date_time";

    /**
     * 校验 nbf/noa 边界及日/月/年/时/分字段，全部通过则 {@code grant()}，否则 {@code deny()}。
     *
     * @param evaluation 当前授权评估上下文
     */
    @Override
    public void evaluate(Evaluation evaluation) {
        Policy policy = evaluation.getPolicy();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
        logger.debugf("Time policy %s evaluating", policy.getName());
        try {
            String contextTime = null;
            EvaluationContext context = evaluation.getContext();
            if (context.getAttributes() != null && context.getAttributes().exists(CONTEXT_TIME_ENTRY)) {
                Attributes.Entry contextTimeEntry = context.getAttributes().getValue(CONTEXT_TIME_ENTRY);
                if (!contextTimeEntry.isEmpty()) {
                    contextTime = contextTimeEntry.asString(0);
                }
            }
            Date actualDate = contextTime == null ? new Date() : dateFormat.parse(contextTime);

            String notBefore = policy.getConfig().get("nbf");
            if (notBefore != null && !"".equals(notBefore)) {
                if (actualDate.before(dateFormat.parse(format(notBefore)))) {
                    logger.debugv("Provided date is before the accepted date: (nbf) ", notBefore);
                    evaluation.deny();
                    return;
                }
            }

            String notOnOrAfter = policy.getConfig().get("noa");
            if (notOnOrAfter != null && !"".equals(notOnOrAfter)) {
                if (actualDate.after(dateFormat.parse(format(notOnOrAfter)))) {
                    logger.debugf("Provided date is after the accepted date: (noa) %s", notOnOrAfter);
                    evaluation.deny();
                    return;
                }
            }

            if (isInvalid(actualDate, Calendar.DAY_OF_MONTH, "dayMonth", policy)
                    || isInvalid(actualDate, Calendar.MONTH, "month", policy)
                    || isInvalid(actualDate, Calendar.YEAR, "year", policy)
                    || isInvalid(actualDate, Calendar.HOUR_OF_DAY, "hour", policy)
                    || isInvalid(actualDate, Calendar.MINUTE, "minute", policy)) {
                logger.debugf("Invalid date provided to time policy %s", policy.getName());
                evaluation.deny();
                return;
            }

            evaluation.grant();
        } catch (Exception e) {
            throw new RuntimeException("Could not evaluate time-based policy [" + policy.getName() + "].", e);
        }
    }

    /**
     * 检查指定日历字段是否在策略配置的精确值或区间内（Calendar.MONTH 需 +1 对齐人类月份）。
     */
    private boolean isInvalid(Date actualDate, int timeConstant, String configName, Policy policy) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(actualDate);

        int dateField = calendar.get(timeConstant);

        if (Calendar.MONTH == timeConstant) {
            dateField++;
        }

        String start = policy.getConfig().get(configName);
        if (start != null) {
            String end = policy.getConfig().get(configName + "End");
            if (end != null) {
                if (dateField < Integer.parseInt(start)  || dateField > Integer.parseInt(end)) {
                    return true;
                }
            } else {
                if (dateField != Integer.parseInt(start)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 纯日期字符串（10 位）自动补全为当天 00:00:00。 */
    static String format(String notBefore) {
        String trimmed = notBefore.trim();

        if (trimmed.length() == 10) {
            notBefore = trimmed + " 00:00:00";
        }

        return notBefore;
    }

    /** 时间策略无额外资源需释放。 */
    @Override
    public void close() {

    }
}
