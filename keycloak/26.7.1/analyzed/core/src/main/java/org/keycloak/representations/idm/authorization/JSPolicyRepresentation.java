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
package org.keycloak.representations.idm.authorization;

/**
 * JavaScript 策略的 REST 表示，通过嵌入脚本代码实现自定义授权逻辑。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class JSPolicyRepresentation extends AbstractPolicyRepresentation {

    /** 策略执行的 JavaScript 源码。 */
    private String code;

    /** @return 策略类型，默认为 {@code js} */
    @Override
    public String getType() {
        if (super.getType() == null) {
            return "js";
        }
        return super.getType();
    }

    /** @return JavaScript 策略源码 */
    public String getCode() {
        return code;
    }

    /** @param code JavaScript 策略源码 */
    public void setCode(String code) {
        this.code = code;
    }
}
