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
package org.redisson.client.protocol;

/**
 * Redis {@code TIME} 命令返回的时间戳，由秒与微秒两部分组成。
 * <p>
 * 与 Unix 纪元对齐，微秒部分为秒以下的精度扩展。
 *
 * @author Nikita Koksharov
 *
 */
public class Time {

    /** 自 Unix 纪元起的整秒数。 */
    private final int seconds;
    /** 当前秒内的微秒偏移（0–999999）。 */
    private final int microseconds;
    
    /** 指定秒与微秒构造时间戳。 */
    public Time(int seconds, int microseconds) {
        super();
        this.seconds = seconds;
        this.microseconds = microseconds;
    }
   
    /** 返回微秒部分。 */
    public int getMicroseconds() {
        return microseconds;
    }
    
    /** 返回秒部分。 */
    public int getSeconds() {
        return seconds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + microseconds;
        result = prime * result + seconds;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Time other = (Time) obj;
        if (microseconds != other.microseconds)
            return false;
        if (seconds != other.seconds)
            return false;
        return true;
    }
    
}
