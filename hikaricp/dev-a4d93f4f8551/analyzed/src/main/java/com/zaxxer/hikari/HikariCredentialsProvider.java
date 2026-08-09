/*
 * Copyright (C) 2025 Brett Wooldridge
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

package com.zaxxer.hikari;

import com.zaxxer.hikari.util.Credentials;

/**
 * 用户可实现此接口，为 HikariCP 提供数据库凭据。
 * 适用于凭据需要在运行时动态生成或获取，而非在配置中硬编码的场景。
 */
public interface HikariCredentialsProvider {
      /**
      * 调用此方法以获取 HikariCP 所需的数据库凭据。
      *
      * @return 包含用户名和密码的 {@link Credentials} 对象
      */
   Credentials getCredentials();
}
