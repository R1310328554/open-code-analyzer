/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec;

import io.netty.util.Signal;
import io.netty.util.internal.ObjectUtil;

/**
 * 解码结果：成功、未完成或失败（携带 {@link Throwable}）。
 * 使用 {@link io.netty.util.Signal} 单例表示成功/未完成，避免多余分配。
 */
public class DecoderResult {

    protected static final Signal SIGNAL_UNFINISHED = Signal.valueOf(DecoderResult.class, "UNFINISHED");
    protected static final Signal SIGNAL_SUCCESS = Signal.valueOf(DecoderResult.class, "SUCCESS");

    public static final DecoderResult UNFINISHED = new DecoderResult(SIGNAL_UNFINISHED);
    public static final DecoderResult SUCCESS = new DecoderResult(SIGNAL_SUCCESS);

    public static DecoderResult failure(Throwable cause) {
        return new DecoderResult(ObjectUtil.checkNotNull(cause, "cause"));
    }

    private final Throwable cause;

    protected DecoderResult(Throwable cause) {
        this.cause = ObjectUtil.checkNotNull(cause, "cause");
    }

    /** 是否已结束解码（成功或失败，非 UNFINISHED）。 */
    public boolean isFinished() {
        return cause != SIGNAL_UNFINISHED;
    }

    /** 是否为成功结果。 */
    public boolean isSuccess() {
        return cause == SIGNAL_SUCCESS;
    }

    /** 是否为失败结果（含具体 cause）。 */
    public boolean isFailure() {
        return cause != SIGNAL_SUCCESS && cause != SIGNAL_UNFINISHED;
    }

    /** 失败时返回原因；成功或未完成时返回 {@code null}。 */
    public Throwable cause() {
        if (isFailure()) {
            return cause;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (isFinished()) {
            if (isSuccess()) {
                return "success";
            }

            String cause = cause().toString();
            return new StringBuilder(cause.length() + 17)
                .append("failure(")
                .append(cause)
                .append(')')
                .toString();
        } else {
            return "unfinished";
        }
    }
}
