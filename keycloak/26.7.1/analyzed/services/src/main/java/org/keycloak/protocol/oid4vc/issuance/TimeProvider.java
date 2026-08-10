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

package org.keycloak.protocol.oid4vc.issuance;

/**
 * 提供当前时间的接口，便于 OID4VCI 端点注入可测试的时间源。
 */
public interface TimeProvider {

    /**
     * 返回当前 Unix 时间戳（秒）。
     *
     * @return 自 1970-01-01 UTC 起的秒数
     */
    int currentTimeSeconds();

    /**
     * 返回当前 Unix 时间戳（毫秒）。
     *
     * @return 自 1970-01-01 UTC 起的毫秒数
     */
    long currentTimeMillis();

}
