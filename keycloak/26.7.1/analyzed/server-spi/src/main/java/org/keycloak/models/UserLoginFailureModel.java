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

package org.keycloak.models;

/**
 * 用户登录失败模型：跟踪暴力破解防护相关的失败次数与时间戳。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserLoginFailureModel {

   /** @return 登录失败记录 ID */
   String getId();
   /** @return 关联用户 ID */
   String getUserId();
   /** @return 允许再次尝试登录的最早时间（秒） */
   int getFailedLoginNotBefore();
   /** @param notBefore 允许再次尝试登录的最早时间（秒） */
   void setFailedLoginNotBefore(int notBefore);
   /** @return 主认证失败次数 */
   int getNumFailures();
   /** 递增主认证失败次数。 */
   void incrementFailures();
   /** @return 临时锁定次数 */
   int getNumTemporaryLockouts();
   /** 递增临时锁定次数。 */
   void incrementTemporaryLockouts();
   /** 清除全部主认证失败计数。 */
   void clearFailures();
   /** @return 最后一次失败时间戳（毫秒） */
   long getLastFailure();
   /** @param lastFailure 最后一次失败时间戳（毫秒） */
   void setLastFailure(long lastFailure);
   /** @return 最后一次失败来源 IP */
   String getLastIPFailure();
   /** @param ip 最后一次失败来源 IP */
   void setLastIPFailure(String ip);
   /** @return 第二因素认证失败次数 */
   int getNumSecondaryAuthFailures();
   /** 递增第二因素认证失败次数。 */
   void incrementSecondaryAuthFailures();
   /** 清除主认证与第二因素认证失败计数。 */
   void clearPrimaryAndSecondaryAuthFailures();
}
