/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

import org.keycloak.provider.Provider;


/**
 * OAuth 2.0 设备授权用户码生成与格式化提供者 SPI。
 * <p>负责生成、展示与规范化用户输入的设备用户码。</p>
 *
 * @author <a href="mailto:h2-wada@nri.co.jp">Hiroyuki Wada</a>
 */
public interface OAuth2DeviceUserCodeProvider extends Provider {

    /**
     * 为 OAuth 2.0 设备授权流程生成新的用户码。
     *
     * @return Return a generated user code
     */
    String generate();

    /**
     * 将内部用户码格式化为人类可读形式（如插入连字符）。
     *
     * @param userCode Original user code
     * @return Return a human-readability user code
     */
    String display(String userCode);

    /**
     * 规范化用户输入的用户码（去空格、统一大小写等）。
     *
     * @param userCode Inputted user code.
     * @return
     */
    String format(String userCode);
}
