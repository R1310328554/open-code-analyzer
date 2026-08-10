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

package org.keycloak.cluster;

import java.util.function.Consumer;

/**
 * 集群事件：实现 {@link Consumer}{@code <ClusterListener>}，收到通知时调用 {@link ClusterListener#eventReceived(ClusterEvent)}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClusterEvent extends Consumer<ClusterListener> {

    /** 将事件分发给监听器。 */
    @Override
    default void accept(ClusterListener listener) {
        listener.eventReceived(this);
    }
}
