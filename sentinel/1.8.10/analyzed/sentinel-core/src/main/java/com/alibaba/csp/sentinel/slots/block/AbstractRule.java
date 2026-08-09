/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block;

import java.util.Objects;

/**
 * 规则实体的抽象基类，封装资源名、来源应用等公共字段。
 *
 * @author youji.zj
 * @author Eric Zhao
 */
public abstract class AbstractRule implements Rule {

    /**
     * 规则 ID。
     */
    private Long id;

    /**
     * 资源名称。
     */
    private String resource;

    /**
     * <p>
     * 按来源限流/授权的应用名称。
     * 默认值为 {@code default}，表示允许全部来源应用。
     * </p>
     * <p>
     * 授权规则中多个来源名可用逗号（{@code ,}）分隔。
     * </p>
     */
    private String limitApp;

    /**
     * 是否使用正则匹配资源名称。
     */
    private boolean regex;

    public Long getId() {
        return id;
    }

    public AbstractRule setId(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public String getResource() {
        return resource;
    }

    public AbstractRule setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public String getLimitApp() {
        return limitApp;
    }

    public AbstractRule setLimitApp(String limitApp) {
        this.limitApp = limitApp;
        return this;
    }

    public boolean isRegex() {
        return regex;
    }

    public AbstractRule setRegex(boolean regex) {
        this.regex = regex;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractRule)) {
            return false;
        }

        AbstractRule that = (AbstractRule)o;

        if (!Objects.equals(resource, that.resource)) {
            return false;
        }
        if (regex != that.regex) {
            return false;
        }
        if (!limitAppEquals(limitApp, that.limitApp)) {
            return false;
        }
        return true;
    }

    private boolean limitAppEquals(String str1, String str2) {
        if ("".equals(str1)) {
            return RuleConstant.LIMIT_APP_DEFAULT.equals(str2);
        } else if (RuleConstant.LIMIT_APP_DEFAULT.equals(str1)) {
            return "".equals(str2) || str2 == null || str1.equals(str2);
        }
        if (str1 == null) {
            return str2 == null || RuleConstant.LIMIT_APP_DEFAULT.equals(str2);
        }
        return str1.equals(str2);
    }

    /** 将本规则安全转型为指定子类型。 */
    public <T extends AbstractRule> T as(Class<T> clazz) {
        return (T)this;
    }

    @Override
    public int hashCode() {
        int result = resource != null ? resource.hashCode() : 0;
        if (!("".equals(limitApp) || RuleConstant.LIMIT_APP_DEFAULT.equals(limitApp) || limitApp == null)) {
            result = 31 * result + limitApp.hashCode();
        }
        result = 31 * result + (regex ? 1 : 0);
        return result;
    }
}
