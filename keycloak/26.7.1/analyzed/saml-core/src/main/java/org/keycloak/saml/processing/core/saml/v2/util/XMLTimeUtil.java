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
package org.keycloak.saml.processing.core.saml.v2.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.common.util.Time;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.util.SecurityActions;
import org.keycloak.saml.common.util.SystemPropertiesUtil;

/**
 * XML 时间处理工具类。
 * <p>提供 {@link XMLGregorianCalendar} 的加减、解析、有效性校验及时区处理。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 6, 2009
 */
public class XMLTimeUtil {

    /** 日志记录器。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * 在指定日历值上增加毫秒数。
     *
     * @param value 待更新的日历对象
     * @param millis 增加的毫秒数
     *
     * @return 更新后的日历副本；输入为 {@code null} 时返回 {@code null}
     */
    public static XMLGregorianCalendar add(XMLGregorianCalendar value, long millis) {
        if (value == null) {
            return null;
        }

        XMLGregorianCalendar newVal = (XMLGregorianCalendar) value.clone();

        if (millis == 0) {
            return newVal;
        }

        Duration duration;
        duration = DATATYPE_FACTORY.get().newDuration(millis);
        newVal.add(duration);
        return newVal;
    }

    /**
     * 从日历值中减去指定毫秒数。
     *
     * @param value 待更新的日历对象
     * @param millis 减去的毫秒数（正数）
     *
     * @return 更新后的日历副本
     */
    public static XMLGregorianCalendar subtract(XMLGregorianCalendar value, long millis) {
        return add(value, - millis);
    }

    /**
     * 返回指定时区的 {@link XMLGregorianCalendar} 签发时刻。
     * <p>若时区无效则回退为 GMT。</p>
     *
     * @param timezone 时区标识
     *
     * @return 签发时刻
     */
    public static XMLGregorianCalendar getIssueInstant(String timezone) {
        TimeZone tz = TimeZone.getTimeZone(timezone);
        DatatypeFactory dtf;
        dtf = DATATYPE_FACTORY.get();

        GregorianCalendar gc = new GregorianCalendar(tz);
        XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar(gc);

        Long offsetMilis = TimeUnit.MILLISECONDS.convert(Time.getOffset(), TimeUnit.SECONDS);
        if (offsetMilis != 0) {
            if (logger.isDebugEnabled()) logger.debug(XMLTimeUtil.class.getName() + " timeOffset: " + offsetMilis);
            xgc.add(parseAsDuration(offsetMilis.toString()));
        }
        if (logger.isDebugEnabled()) logger.debug(XMLTimeUtil.class.getName() + " issueInstant: " + xgc.toString());
        return xgc;
    }

    /** 获取当前时刻的签发时间。 */
    public static XMLGregorianCalendar getIssueInstant() {
        return getIssueInstant(getCurrentTimeZoneID());
    }

    /** 获取当前系统时区 ID（可通过系统属性覆盖）。 */
    public static String getCurrentTimeZoneID() {
        String timezonePropertyValue = SecurityActions.getSystemProperty(GeneralConstants.TIMEZONE, "GMT");

        TimeZone timezone;
        if (GeneralConstants.TIMEZONE_DEFAULT.equals(timezonePropertyValue)) {
            timezone = TimeZone.getDefault();
        } else {
            timezone = TimeZone.getTimeZone(timezonePropertyValue);
        }

        return timezone.getID();
    }

    /**
     * 将分钟数转换为毫秒数。
     *
     * @param valueInMins 分钟数
     *
     * @return 对应毫秒数
     */
    public static long inMilis(int valueInMins) {
        return (long) valueInMins * 60 * 1000;
    }

    /**
     * 校验当前时刻是否在 NotBefore 与 NotOnOrAfter 边界内。
     *
     * @param now 当前时刻
     * @param notbefore 生效起始时刻
     * @param notOnOrAfter 失效时刻（不含）
     *
     * @return 是否在有效时间窗口内
     */
    public static boolean isValid(XMLGregorianCalendar now, XMLGregorianCalendar notbefore, XMLGregorianCalendar notOnOrAfter) {
        int val;

        if (notbefore != null) {
            val = notbefore.compare(now);

            if (val == DatatypeConstants.INDETERMINATE || val == DatatypeConstants.GREATER)
                return false;
        }

        if (notOnOrAfter != null) {
            val = notOnOrAfter.compare(now);

            if (val != DatatypeConstants.GREATER)
                return false;
        }

        return true;
    }

    /**
     * 将字符串解析为 {@code Duration}。
     * <p>支持 ISO 8601 周期（如 P10M）或纯数字毫秒值。</p>
     *
     * @param timeValue 时间字符串
     *
     * @return 解析后的 Duration 对象
     */
    public static Duration parseAsDuration(String timeValue) {
        if (timeValue == null) {
            PicketLinkLoggerFactory.getLogger().nullArgumentError("duration time");
        }

        DatatypeFactory factory = DATATYPE_FACTORY.get();

        try {
            // 判断是否为 ISO 8601 周期；否则按数值毫秒解析
            if (timeValue.startsWith("P")) {
                return factory.newDuration(timeValue);
            } else {
                return factory.newDuration(Long.parseLong(timeValue));
            }
        } catch (Exception e) {
            throw logger.samlMetaDataFailedToCreateCacheDuration(timeValue);
        }
    }

    /**
     * 将 XML 时间字符串解析为 {@code XMLGregorianCalendar}。
     *
     * @param timeString XML 时间字符串
     *
     * @return 解析后的日历对象
     */
    public static XMLGregorianCalendar parse(String timeString) {
        DatatypeFactory factory = DATATYPE_FACTORY.get();
        return factory.newXMLGregorianCalendar(timeString);
    }

    /** 线程本地 DatatypeFactory 缓存。 */
    private static final ThreadLocal<DatatypeFactory> DATATYPE_FACTORY = new ThreadLocal<DatatypeFactory>() {
        @Override
        protected DatatypeFactory initialValue() {
            try {
                return newDatatypeFactory();
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    };

    /**
     * 创建新的 {@link DatatypeFactory} 实例。
     *
     * @return DatatypeFactory 实例
     *
     * @throws DatatypeConfigurationException 工厂创建失败时抛出
     */
    private static DatatypeFactory newDatatypeFactory() throws DatatypeConfigurationException {
        boolean tccl_jaxp = SystemPropertiesUtil.getSystemProperty(GeneralConstants.TCCL_JAXP, "false")
                .equalsIgnoreCase("true");
        ClassLoader prevTCCL = SecurityActions.getTCCL();
        try {
            if (tccl_jaxp) {
                SecurityActions.setTCCL(XMLTimeUtil.class.getClassLoader());
            }
            return DatatypeFactory.newInstance();
        } finally {
            if (tccl_jaxp) {
                SecurityActions.setTCCL(prevTCCL);
            }
        }
    }
}
