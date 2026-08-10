/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.sctp;

import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.PeerAddressChangeNotification;
import com.sun.nio.sctp.SendFailedNotification;
import com.sun.nio.sctp.ShutdownNotification;
import io.netty.channel.ChannelPipeline;
import io.netty.util.internal.ObjectUtil;


/**
 * {@link AbstractNotificationHandler} implementation which will handle all {@link Notification}s by trigger a
 * {@link Notification} user event in the {@link ChannelPipeline} of a {@link SctpChannel}.
 * <p>将 JDK SCTP 通知转为 Netty {@code userEventTriggered}； {@link ShutdownNotification} 时关闭通道并返回 {@link HandlerResult#RETURN}。</p>
 */
public final class SctpNotificationHandler extends AbstractNotificationHandler<Object> {

    /** 接收通知并触发事件的 Netty SCTP 通道 */
    private final SctpChannel sctpChannel;

    /** @param sctpChannel 关联的 {@link SctpChannel}，不可为 null */
    public SctpNotificationHandler(SctpChannel sctpChannel) {
        this.sctpChannel = ObjectUtil.checkNotNull(sctpChannel, "sctpChannel");
    }

    @Override
    /** 关联变更：触发 user event 并继续 receive 循环 */
    public HandlerResult handleNotification(AssociationChangeNotification notification, Object o) {
        fireEvent(notification);
        return HandlerResult.CONTINUE;
    }

    @Override
    /** 对端地址可达性变更通知 */
    public HandlerResult handleNotification(PeerAddressChangeNotification notification, Object o) {
        fireEvent(notification);
        return HandlerResult.CONTINUE;
    }

    @Override
    /** 发送失败通知 */
    public HandlerResult handleNotification(SendFailedNotification notification, Object o) {
        fireEvent(notification);
        return HandlerResult.CONTINUE;
    }

    @Override
    /** 关联关闭：触发事件、关闭通道并终止通知处理 */
    public HandlerResult handleNotification(ShutdownNotification notification, Object o) {
        fireEvent(notification);
        sctpChannel.close();
        return HandlerResult.RETURN;
    }

    /** 向 pipeline 投递 {@code userEventTriggered} */
    private void fireEvent(Notification notification) {
        sctpChannel.pipeline().fireUserEventTriggered(notification);
    }
}

