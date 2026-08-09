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
 * Pull 请求系统标志：提交 offset、挂起、订阅表达式、类过滤与 Lite Pull 等选项。
 */
public class PullSysFlag {
    /** 请求携带 commit offset。 */
    private final static int FLAG_COMMIT_OFFSET = 0x1;
    /** 长轮询挂起标志。 */
    private final static int FLAG_SUSPEND = 0x1 << 1;
    /** 携带订阅表达式。 */
    private final static int FLAG_SUBSCRIPTION = 0x1 << 2;
    /** 启用类过滤。 */
    private final static int FLAG_CLASS_FILTER = 0x1 << 3;
    /** Lite Pull 消费模式。 */
    private final static int FLAG_LITE_PULL_MESSAGE = 0x1 << 4;

    /** 组装 Pull 系统标志（不含 Lite Pull）。 */
    public static int buildSysFlag(final boolean commitOffset, final boolean suspend,
        final boolean subscription, final boolean classFilter) {
        int flag = 0;

        if (commitOffset) {
            flag |= FLAG_COMMIT_OFFSET;
        }

        if (suspend) {
            flag |= FLAG_SUSPEND;
        }

        if (subscription) {
            flag |= FLAG_SUBSCRIPTION;
        }

        if (classFilter) {
            flag |= FLAG_CLASS_FILTER;
        }

        return flag;
    }

    /** 组装 Pull 系统标志（含 Lite Pull 选项）。 */
    public static int buildSysFlag(final boolean commitOffset, final boolean suspend,
        final boolean subscription, final boolean classFilter, final boolean litePull) {
        int flag = buildSysFlag(commitOffset, suspend, subscription, classFilter);

        if (litePull) {
            flag |= FLAG_LITE_PULL_MESSAGE;
        }

        return flag;
    }

    /** 清除 commit offset 标志位。 */
    public static int clearCommitOffsetFlag(final int sysFlag) {
        return sysFlag & (~FLAG_COMMIT_OFFSET);
    }

    /** 是否设置了 commit offset 标志。 */
    public static boolean hasCommitOffsetFlag(final int sysFlag) {
        return (sysFlag & FLAG_COMMIT_OFFSET) == FLAG_COMMIT_OFFSET;
    }

    /** 是否设置了挂起标志。 */
    public static boolean hasSuspendFlag(final int sysFlag) {
        return (sysFlag & FLAG_SUSPEND) == FLAG_SUSPEND;
    }

    /** 清除挂起标志位。 */
    public static int clearSuspendFlag(final int sysFlag) {
        return sysFlag & (~FLAG_SUSPEND);
    }

    /** 是否携带订阅表达式。 */
    public static boolean hasSubscriptionFlag(final int sysFlag) {
        return (sysFlag & FLAG_SUBSCRIPTION) == FLAG_SUBSCRIPTION;
    }

    /** 在现有标志上追加订阅标志。 */
    public static int buildSysFlagWithSubscription(final int sysFlag) {
        return sysFlag | FLAG_SUBSCRIPTION;
    }

    /** 是否启用类过滤。 */
    public static boolean hasClassFilterFlag(final int sysFlag) {
        return (sysFlag & FLAG_CLASS_FILTER) == FLAG_CLASS_FILTER;
    }

    /** 是否为 Lite Pull 请求。 */
    public static boolean hasLitePullFlag(final int sysFlag) {
        return (sysFlag & FLAG_LITE_PULL_MESSAGE) == FLAG_LITE_PULL_MESSAGE;
    }
}
