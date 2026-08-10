/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.client;

/**
 * Protection API 查询或操作的目标资源不存在时抛出的运行时异常。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourceNotFoundException extends RuntimeException {

    /** @param cause 原始异常 */
    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }

    /** @param message 可读错误信息 @param cause 原始异常 */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
