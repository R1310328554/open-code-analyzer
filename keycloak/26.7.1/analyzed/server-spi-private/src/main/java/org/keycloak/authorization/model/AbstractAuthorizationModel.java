/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.model;

import java.util.Objects;

import org.keycloak.authorization.store.StoreFactory;

/**
 * 授权模型抽象基类，提供存储工厂引用与只读校验。
 */
public abstract class AbstractAuthorizationModel {

    protected final StoreFactory storeFactory;

    /** 构造模型，要求非空 {@link StoreFactory}。 */
    public AbstractAuthorizationModel(StoreFactory storeFactory) {
        Objects.requireNonNull(storeFactory, "storeFactory");
        this.storeFactory = storeFactory;
    }

    /** 若存储工厂处于只读模式则抛出 {@link IllegalStateException}。 */
    protected void throwExceptionIfReadonly() {
        if (storeFactory.isReadOnly()) {
            throw new IllegalStateException("Instance marked as read-only");
        }
    }
}
