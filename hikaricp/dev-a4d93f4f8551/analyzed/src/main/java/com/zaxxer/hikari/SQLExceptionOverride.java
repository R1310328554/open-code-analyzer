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

package com.zaxxer.hikari;

import java.sql.SQLException;

/**
 * 用户可实现此接口，覆盖 HikariCP 对 {@link SQLException} 的默认处理逻辑。
 * 当 JDBC 执行方法抛出 {@link SQLException} 时，连接池会检查 SQLState 与错误码，
 * 以决定是否将该连接从池中驱逐。
 * <p>
 * 提供此接口的实现后，可自定义异常处理策略。
 * {@link #adjudicate(SQLException)} 方法会收到抛出的异常；
 * 若返回 {@link Override#CONTINUE_EVICT}，则按内置规则继续处理；
 * 若返回 {@link Override#DO_NOT_EVICT}，则跳过驱逐；
 * 若返回 {@link Override#MUST_EVICT}，则无论 SQLState 或错误码如何，一律驱逐该连接。
 */
public interface SQLExceptionOverride {
   /** 驱逐裁决结果。 */
   enum Override {
      /** 继续按内置规则判断是否驱逐。 */
      CONTINUE_EVICT,
      /** 不驱逐该连接。 */
      DO_NOT_EVICT,
      /** 强制驱逐该连接。 */
      MUST_EVICT
   }

   /**
    * 当 JDBC 方法抛出 {@link SQLException} 时调用。
    *
    * @param sqlException 抛出的 {@link SQLException}
    * @return 指示后续驱逐策略的 {@link Override} 值
    */
   default Override adjudicate(final SQLException sqlException)
   {
      return Override.CONTINUE_EVICT;
   }
}
