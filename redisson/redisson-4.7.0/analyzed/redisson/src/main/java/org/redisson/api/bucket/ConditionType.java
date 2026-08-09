/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.bucket;

/**
 * Bucket 比较并设置/删除操作使用的条件类型枚举。
 * <p>
 * 区分值相等/不等及摘要（digest）相等/不等四种模式。
 *
 * @author Nikita Koksharov
 *
 */
public enum ConditionType {

    /** 当前值等于期望值。 */
    EXPECTED,
    /** 当前值不等于指定值。 */
    UNEXPECTED,
    /** 当前值摘要等于期望摘要。 */
    EXPECTED_DIGEST,
    /** 当前值摘要不等于指定摘要。 */
    UNEXPECTED_DIGEST

}
