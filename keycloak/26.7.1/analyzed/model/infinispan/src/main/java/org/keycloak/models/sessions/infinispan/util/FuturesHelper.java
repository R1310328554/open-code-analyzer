/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jboss.logging.Logger;

/**
 * 批量收集 {@link Future} 并顺序等待完成的辅助类。
 * <p>
 * 非线程安全，假定任务由单线程添加。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FuturesHelper {

    private static final Logger log = Logger.getLogger(FuturesHelper.class);

    /** 待等待的 Future 队列。 */
    private final Queue<Future> futures = new LinkedList<>();


    /** 将异步任务加入等待队列。 */
    public void addTask(Future future) {
        this.futures.add(future);
    }


    /** 阻塞等待队列中全部 Future 完成；异常仅记录日志不向外抛出。 */
    public void waitForAllToFinish() {
        for (Future future : futures) {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException ee) {
                log.error("Exception when waiting for future", ee); // TODO Possibly some good mechanism to avoid swamp log with many same exceptions?
            }
        }
    }


    /** 返回当前队列中的任务数量。 */
    public int size() {
        return futures.size();
    }

}
