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
package org.keycloak.authorization.client.util;

/**
 * <p>HTTP 响应体字节到目标类型的转换函数式接口。
 *
 * <p>由 {@link HttpMethod#execute(HttpResponseProcessor)} 在状态码校验通过后调用。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface HttpResponseProcessor<R> {

    /** 将响应体字节解析为类型 {@code R}。 */
    R process(byte[] entity);
}
