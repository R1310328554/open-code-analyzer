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

package org.keycloak.broker.provider;

/**
 * 身份联邦上下文数据的序列化/反序列化 SPI，用于在 broker 流程间持久化复杂对象。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface IdentityProviderDataMarshaller {

    /** 将对象序列化为可存储的字符串。 */
    String serialize(Object obj);
    /** 从存储字符串反序列化为指定类型。 */
    <T> T deserialize(String serialized, Class<T> clazz);

}
