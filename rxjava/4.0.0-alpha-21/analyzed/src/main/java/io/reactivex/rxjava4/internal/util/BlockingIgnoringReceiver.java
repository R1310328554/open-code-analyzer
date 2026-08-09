/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.internal.util;

import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava4.functions.*;

/**
 * 保存收到的 Throwable（若有）并 countDown 自身。
 */
public final class BlockingIgnoringReceiver
extends CountDownLatch
implements Consumer<Throwable>, Action {
    /** 收到的错误，若无则为 null。 */
    public Throwable error;

    /** 构造 count 为 1 的接收器。 */
    public BlockingIgnoringReceiver() {
        super(1);
    }

    /** 保存异常并 countDown。 */
    @Override
    public void accept(Throwable e) {
        error = e;
        countDown();
    }

    /** 正常完成时 countDown。 */
    @Override
    public void run() {
        countDown();
    }
}
