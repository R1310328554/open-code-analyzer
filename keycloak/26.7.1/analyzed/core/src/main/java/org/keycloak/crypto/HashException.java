/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.crypto;

/**
 * 哈希计算失败时抛出的运行时异常。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class HashException extends RuntimeException {

    /**
     * @param message 错误描述
     */
    public HashException(String message) {
        super(message);
    }

    /**
     * @param message 错误描述
     * @param cause 原始异常
     */
    public HashException(String message, Throwable cause) {
        super(message, cause);
    }
}
