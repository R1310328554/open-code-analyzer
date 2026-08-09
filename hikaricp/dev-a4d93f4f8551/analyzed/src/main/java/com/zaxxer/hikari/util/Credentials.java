/*
 * Copyright (C) 2013, 2014 Brett Wooldridge
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

package com.zaxxer.hikari.util;

import javax.management.ConstructorParameters;

/**
 * 不可变的连接凭据（用户名/密码）值对象。
 */
public final class Credentials
{

   private final String username;
   private final String password;

   /**
    * 使用给定用户名与密码创建不可变 {@link Credentials}。
    *
    * @param username 用户名
    * @param password 密码
    * @return 新的 {@link Credentials} 实例
    */
   public static Credentials of(final String username, final String password) {
      return new Credentials(username, password);
   }

   /**
    * 使用给定用户名与密码构造不可变凭据。
    *
    * @param username 用户名
    * @param password 密码
    */
   @ConstructorParameters({ "username", "password" })
   public Credentials(final String username, final String password)
   {
      this.username = username;
      this.password = password;
   }

   /**
    * 获取用户名。
    *
    * @return 用户名
    */
   public String getUsername()
   {
      return username;
   }

   /**
    * 获取密码。
    *
    * @return 密码
    */
   public String getPassword()
   {
      return password;
   }
}
