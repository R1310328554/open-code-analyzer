/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common.sysflag;

/**
 * Topic 系统标志：标记 Topic 是否为单元化 Topic 及是否含单元订阅。
 */
public class TopicSysFlag {

    /** 单元化 Topic 标志。 */
    private final static int FLAG_UNIT = 0x1 << 0;

    /** Topic 含单元订阅标志。 */
    private final static int FLAG_UNIT_SUB = 0x1 << 1;

    /** 根据 unit 与 hasUnitSub 构建 Topic 系统标志。 */
    public static int buildSysFlag(final boolean unit, final boolean hasUnitSub) {
        int sysFlag = 0;

        if (unit) {
            sysFlag |= FLAG_UNIT;
        }

        if (hasUnitSub) {
            sysFlag |= FLAG_UNIT_SUB;
        }

        return sysFlag;
    }

    /** 设置单元化 Topic 标志。 */
    public static int setUnitFlag(final int sysFlag) {
        return sysFlag | FLAG_UNIT;
    }

    /** 清除单元化 Topic 标志。 */
    public static int clearUnitFlag(final int sysFlag) {
        return sysFlag & (~FLAG_UNIT);
    }

    /** 是否为单元化 Topic。 */
    public static boolean hasUnitFlag(final int sysFlag) {
        return (sysFlag & FLAG_UNIT) == FLAG_UNIT;
    }

    /** 设置单元订阅标志。 */
    public static int setUnitSubFlag(final int sysFlag) {
        return sysFlag | FLAG_UNIT_SUB;
    }

    /** 清除单元订阅标志。 */
    public static int clearUnitSubFlag(final int sysFlag) {
        return sysFlag & (~FLAG_UNIT_SUB);
    }

    /** 是否含单元订阅。 */
    public static boolean hasUnitSubFlag(final int sysFlag) {
        return (sysFlag & FLAG_UNIT_SUB) == FLAG_UNIT_SUB;
    }
}
