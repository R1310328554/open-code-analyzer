/*
 * Copyright 2023 The Netty Project
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
package io.netty.handler.codec.quic;

import java.net.InetSocketAddress;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * 与特定网络路径（本地/远端地址对）相关的 {@link QuicEvent} 基类。
 */
public abstract class QuicPathEvent implements QuicEvent {

    private final InetSocketAddress local;
    private final InetSocketAddress remote;

    QuicPathEvent(InetSocketAddress local, InetSocketAddress remote) {
        this.local = requireNonNull(local, "local");
        this.remote = requireNonNull(remote, "remote");
    }

    /**
     * 该网络路径的本地地址。
     *
     * @return  local
     */
    public InetSocketAddress local() {
        return local;
    }

    /**
     * 该网络路径的远端地址。
     *
     * @return  local
     */
    public InetSocketAddress remote() {
        return remote;
    }

    @Override
    public String toString() {
        return "QuicPathEvent{" +
                "local=" + local +
                ", remote=" + remote +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QuicPathEvent that = (QuicPathEvent) o;
        if (!Objects.equals(local, that.local)) {
            return false;
        }
        return Objects.equals(remote, that.remote);
    }

    @Override
    public int hashCode() {
        int result = local != null ? local.hashCode() : 0;
        result = 31 * result + (remote != null ? remote.hashCode() : 0);
        return result;
    }

    public static final class New extends QuicPathEvent {
        /**
         * 收到报文时观测到新的网络路径（本地/远端地址对）。
         * 仅服务端触发（新路径由客户端发起）；应用可按需探测该路径。
         *
         * @param local     local address.
         * @param remote    remote address.
         */
        public New(InetSocketAddress local, InetSocketAddress remote) {
            super(local, remote);
        }

        @Override
        public String toString() {
            return "QuicPathEvent.New{" +
                    "local=" + local() +
                    ", remote=" + remote() +
                    '}';
        }
    }

    public static final class Validated extends QuicPathEvent {
        /**
         * 本地与远端之间的该网络路径已通过验证。
         *
         * @param local     local address.
         * @param remote    remote address.
         */
        public Validated(InetSocketAddress local, InetSocketAddress remote) {
            super(local, remote);
        }

        @Override
        public String toString() {
            return "QuicPathEvent.Validated{" +
                    "local=" + local() +
                    ", remote=" + remote() +
                    '}';
        }
    }

    public static final class FailedValidation extends QuicPathEvent {
        /**
         * 该网络路径验证失败，除非应用再次请求探测，否则不再使用。
         *
         * @param local     local address.
         * @param remote    remote address.
         */
        public FailedValidation(InetSocketAddress local, InetSocketAddress remote) {
            super(local, remote);
        }

        @Override
        public String toString() {
            return "QuicPathEvent.FailedValidation{" +
                    "local=" + local() +
                    ", remote=" + remote() +
                    '}';
        }
    }

    public static final class Closed extends QuicPathEvent {

        /**
         * 该网络路径已关闭，在此连接上不可再用。
         *
         * @param local     local address.
         * @param remote    remote address.
         */
        public Closed(InetSocketAddress local, InetSocketAddress remote) {
            super(local, remote);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode();
        }

        @Override
        public String toString() {
            return "QuicPathEvent.Closed{" +
                    "local=" + local() +
                    ", remote=" + remote() +
                    '}';
        }
    }

    public static final class ReusedSourceConnectionId extends QuicPathEvent {
        private final long seq;
        private final InetSocketAddress oldLocal;
        private final InetSocketAddress oldRemote;

        /**
         * 观测到对等体在第二组地址上复用了原先在第一组地址上使用的源连接 ID（给定序列号）。
         *
         * @param seq           sequence number
         * @param oldLocal      old local address.
         * @param oldRemote     old remote address.
         * @param local         local address.
         * @param remote        remote address.
         */
        public ReusedSourceConnectionId(long seq, InetSocketAddress oldLocal, InetSocketAddress oldRemote,
                                       InetSocketAddress local, InetSocketAddress remote) {
            super(local, remote);
            this.seq = seq;
            this.oldLocal = requireNonNull(oldLocal, "oldLocal");
            this.oldRemote = requireNonNull(oldRemote, "oldRemote");
        }

        /**
         * 源连接 ID 序列号。
         *
         * @return  sequence number
         */
        public long seq() {
            return seq;
        }

        /**
         * 原先网络路径的本地地址。
         *
         * @return  local
         */
        public InetSocketAddress oldLocal() {
            return oldLocal;
        }

        /**
         * 原先网络路径的远端地址。
         *
     * @return  local
         */
        public InetSocketAddress oldRemote() {
            return oldRemote;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            ReusedSourceConnectionId that = (ReusedSourceConnectionId) o;

            if (seq != that.seq) {
                return false;
            }
            if (!Objects.equals(oldLocal, that.oldLocal)) {
                return false;
            }
            return Objects.equals(oldRemote, that.oldRemote);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) (seq ^ (seq >>> 32));
            result = 31 * result + (oldLocal != null ? oldLocal.hashCode() : 0);
            result = 31 * result + (oldRemote != null ? oldRemote.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "QuicPathEvent.ReusedSourceConnectionId{" +
                    "seq=" + seq +
                    ", oldLocal=" + oldLocal +
                    ", oldRemote=" + oldRemote +
                    ", local=" + local() +
                    ", remote=" + remote() +
                    '}';
        }
    }

    public static final class PeerMigrated extends QuicPathEvent {

        /**
         * 连接观测到远端已迁移至该地址对表示的路径（收到非探测报文）。
         * 仅服务端、且路径已验证时触发。
         *
         * @param local     local address.
         * @param remote    remote address.
         */
        public PeerMigrated(InetSocketAddress local, InetSocketAddress remote) {
            super(local, remote);
        }

        @Override
        public String toString() {
            return "QuicPathEvent.PeerMigrated{" +
                    "local=" + local() +
                    ", remote=" + remote() +
                    '}';
        }
    }
}
