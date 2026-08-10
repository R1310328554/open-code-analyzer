/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.keycloak.theme.beans;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * 邮件中链接过期时间的格式化方法。
 * <p>将分钟数转换为本地化的「X 天/小时/分钟/秒」可读字符串，供 FreeMarker 模板在邮件正文中展示。</p>
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class LinkExpirationFormatterMethod implements TemplateMethodModelEx {

    protected final Properties messages;
    protected final Locale locale;

    /** 绑定消息 bundle 与区域设置。 */
    public LinkExpirationFormatterMethod(Properties messages, Locale locale) {
        this.messages = messages;
        this.locale = locale;
    }

    @SuppressWarnings("rawtypes")
    /** 接收分钟数参数，转换为本地化时间段描述。 */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        Object val = arguments.isEmpty() ? null : arguments.get(0);
        if (val == null)
            return "";

        try {
            // 输入值为分钟，与 EmailTemplateProvider 约定一致
            return format(Long.parseLong(val.toString().trim()) * 60);
        } catch (NumberFormatException e) {
            // 非数字则原样返回
            return val.toString();
        }

    }

    /** 将秒数归一化为最大合适单位并拼接本地化单位名。 */
    protected String format(long valueInSeconds) {

        String unitKey = "seconds";
        long value = valueInSeconds;

        if (value > 0 && value % 60 == 0) {
            unitKey = "minutes";
            value = value / 60;
            if (value % 60 == 0) {
                unitKey = "hours";
                value = value / 60;
                if (value % 24 == 0) {
                    unitKey = "days";
                    value = value / 24;
                }
            }
        }

        return value + " " + MessageFormat.format(messages.getProperty("linkExpirationFormatter.timePeriodUnit." + unitKey), value);
    }
}
