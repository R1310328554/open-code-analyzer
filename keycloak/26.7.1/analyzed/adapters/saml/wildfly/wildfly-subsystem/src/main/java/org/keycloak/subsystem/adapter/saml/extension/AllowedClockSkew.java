/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.subsystem.adapter.saml.extension;

import java.util.EnumSet;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.operations.validation.EnumValidator;
import org.jboss.as.controller.operations.validation.IntRangeValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * SAML 适配器子系统中「允许时钟偏差」（AllowedClockSkew）属性的 WildFly 管理模型定义。
 *
 * <p>包含偏差数值与单位（分钟、秒、毫秒等）两个 {@link SimpleAttributeDefinition}。</p>
 *
 * @author rmartinc
 */
abstract public class AllowedClockSkew {

    /** 时钟偏差数值（正整数，必填）。 */
    static final SimpleAttributeDefinition ALLOWED_CLOCK_SKEW_VALUE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.ALLOWED_CLOCK_SKEW_VALUE, ModelType.INT, false)
                    .setXmlName(Constants.XML.ALLOWED_CLOCK_SKEW)
                    .setAllowExpression(true)
                    .setValidator(new IntRangeValidator(1, Integer.MAX_VALUE, true, true))
                    .build();

    /** 时钟偏差单位枚举（内部使用）。 */
    static private enum AllowedClockSkewUnits {MINUTES, SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS};

    /** 时钟偏差单位（可选，默认秒）。 */
    static final SimpleAttributeDefinition ALLOWED_CLOCK_SKEW_UNIT =
            new SimpleAttributeDefinitionBuilder(Constants.Model.ALLOWED_CLOCK_SKEW_UNIT, ModelType.STRING, true)
                    .setXmlName(Constants.XML.ALLOWED_CLOCK_SKEW_UNIT)
                    .setAllowExpression(true)
                    .setDefaultValue(new ModelNode(AllowedClockSkewUnits.SECONDS.name()))
                    .setAllowedValues(AllowedClockSkewUnits.MINUTES.name(), AllowedClockSkewUnits.SECONDS.name(),
                            AllowedClockSkewUnits.MILLISECONDS.name(), AllowedClockSkewUnits.MICROSECONDS.name(),
                            AllowedClockSkewUnits.NANOSECONDS.name())
                    .setValidator(EnumValidator.create(AllowedClockSkewUnits.class, EnumSet.allOf(AllowedClockSkewUnits.class)))
                    .build();

    /** AllowedClockSkew 的全部属性定义。 */
    static final SimpleAttributeDefinition[] ATTRIBUTES = {ALLOWED_CLOCK_SKEW_UNIT, ALLOWED_CLOCK_SKEW_VALUE};
}
