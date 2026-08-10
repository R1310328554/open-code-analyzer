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

package org.keycloak.adapters.spi;

/**
 * 适配器单次认证尝试的结果枚举。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public enum AuthOutcome {
    /** 尚未尝试认证。 */
    NOT_ATTEMPTED,
    /** 认证失败（凭证无效或协议错误）。 */
    FAILED,
    /** 已成功认证并建立安全上下文。 */
    AUTHENTICATED,
    /** 已处理请求但用户未认证（如 Bearer-only 资源）。 */
    NOT_AUTHENTICATED,
    /** 用户已登出。 */
    LOGGED_OUT
}
