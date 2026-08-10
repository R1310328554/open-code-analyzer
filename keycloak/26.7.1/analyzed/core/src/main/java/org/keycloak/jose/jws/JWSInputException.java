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

package org.keycloak.jose.jws;

/**
 * {@link JWSInput} 解析 Compact JWS 时抛出的异常。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JWSInputException extends Exception {

    /**
     * @param s 异常消息
     */
    public JWSInputException(String s) {
        super(s);
    }

    /** 无参构造。 */
    public JWSInputException() {
    }

    /**
     * @param throwable 根本原因
     */
    public JWSInputException(Throwable throwable) {
        super(throwable);
    }
}
