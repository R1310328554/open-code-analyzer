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
package org.keycloak.models.cache.infinispan.authorization.entities;

/**
 * 标记缓存实体关联到特定作用域的接口。
 * <p>
 * 供流式谓词（InScopePredicate）在批量失效时按作用域 ID 匹配查询结果。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface InScope {
    /** 返回关联作用域 ID。 */
    String getScopeId();
}
