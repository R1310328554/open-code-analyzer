/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.haproxy;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol.AddressFamily;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 已解码 HAProxy PROXY 协议参数的报文容器。
 * <p>
 * 承载协议版本、命令、代理协议类型、源/目的地址与端口，以及可选 TLV 扩展列表。
 */
public final class HAProxyMessage extends AbstractReferenceCounted {

    // 嵌套 TLV 的最大递归深度上限
    private static final int MAX_NESTING_LEVEL = 128;
    private static final ResourceLeakDetector<HAProxyMessage> leakDetector =
            ResourceLeakDetectorFactory.instance().newResourceLeakDetector(HAProxyMessage.class);

    private final ResourceLeakTracker<HAProxyMessage> leak;
    private final HAProxyProtocolVersion protocolVersion;
    private final HAProxyCommand command;
    private final HAProxyProxiedProtocol proxiedProtocol;
    private final String sourceAddress;
    private final String destinationAddress;
    private final int sourcePort;
    private final int destinationPort;
    private final List<HAProxyTLV> tlvs;

    /** 私有构造，由字符串端口参数创建实例。 */
    private HAProxyMessage(
            HAProxyProtocolVersion protocolVersion, HAProxyCommand command, HAProxyProxiedProtocol proxiedProtocol,
            String sourceAddress, String destinationAddress, String sourcePort, String destinationPort) {
        this(
                protocolVersion, command, proxiedProtocol,
                sourceAddress, destinationAddress, portStringToInt(sourcePort), portStringToInt(destinationPort));
    }

    /**
     * 创建不含 TLV 的 {@link HAProxyMessage} 实例。
     * @param protocolVersion the protocol version.
     * @param command the command.
     * @param proxiedProtocol the protocol containing the address family and transport protocol.
     * @param sourceAddress the source address.
     * @param destinationAddress the destination address.
     * @param sourcePort the source port. This value must be 0 for unix, unspec addresses.
     * @param destinationPort the destination port. This value must be 0 for unix, unspec addresses.
     */
    public HAProxyMessage(
            HAProxyProtocolVersion protocolVersion, HAProxyCommand command, HAProxyProxiedProtocol proxiedProtocol,
            String sourceAddress, String destinationAddress, int sourcePort, int destinationPort) {

        this(protocolVersion, command, proxiedProtocol,
             sourceAddress, destinationAddress, sourcePort, destinationPort, Collections.<HAProxyTLV>emptyList());
    }

    /**
     * 创建含 TLV 列表的完整 {@link HAProxyMessage} 实例，并校验地址与端口。
     * @param protocolVersion the protocol version.
     * @param command the command.
     * @param proxiedProtocol the protocol containing the address family and transport protocol.
     * @param sourceAddress the source address.
     * @param destinationAddress the destination address.
     * @param sourcePort the source port. This value must be 0 for unix, unspec addresses.
     * @param destinationPort the destination port. This value must be 0 for unix, unspec addresses.
     * @param tlvs the list of tlvs.
     */
    public HAProxyMessage(
            HAProxyProtocolVersion protocolVersion, HAProxyCommand command, HAProxyProxiedProtocol proxiedProtocol,
            String sourceAddress, String destinationAddress, int sourcePort, int destinationPort,
            List<? extends HAProxyTLV> tlvs) {

        ObjectUtil.checkNotNull(protocolVersion, "protocolVersion");
        ObjectUtil.checkNotNull(proxiedProtocol, "proxiedProtocol");
        ObjectUtil.checkNotNull(tlvs, "tlvs");
        AddressFamily addrFamily = proxiedProtocol.addressFamily();

        checkAddress(sourceAddress, addrFamily, protocolVersion);
        checkAddress(destinationAddress, addrFamily, protocolVersion);
        checkPort(sourcePort, addrFamily);
        checkPort(destinationPort, addrFamily);

        this.protocolVersion = protocolVersion;
        this.command = command;
        this.proxiedProtocol = proxiedProtocol;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.tlvs = Collections.unmodifiableList(tlvs);

        leak = leakDetector.track(this);
    }

    /**
     * 解码 v2 二进制 PROXY 协议头部。
     *
     * @param header                     a version 2 proxy protocol header
     * @return                           {@link HAProxyMessage} instance
     * @throws HAProxyProtocolException  if any portion of the header is invalid
     */
    static HAProxyMessage decodeHeader(ByteBuf header) {
        ObjectUtil.checkNotNull(header, "header");

        if (header.readableBytes() < 16) {
            throw new HAProxyProtocolException(
                    "incomplete header: " + header.readableBytes() + " bytes (expected: 16+ bytes)");
        }

        // 规范：第 13 字节为协议版本与命令
        header.skipBytes(12);
        final byte verCmdByte = header.readByte();

        HAProxyProtocolVersion ver;
        try {
            ver = HAProxyProtocolVersion.valueOf(verCmdByte);
        } catch (IllegalArgumentException e) {
            throw new HAProxyProtocolException(e);
        }

        if (ver != HAProxyProtocolVersion.V2) {
            throw new HAProxyProtocolException("version 1 unsupported: 0x" + Integer.toHexString(verCmdByte));
        }

        HAProxyCommand cmd;
        try {
            cmd = HAProxyCommand.valueOf(verCmdByte);
        } catch (IllegalArgumentException e) {
            throw new HAProxyProtocolException(e);
        }

        if (cmd == HAProxyCommand.LOCAL) {
            return unknownMsg(HAProxyProtocolVersion.V2, HAProxyCommand.LOCAL);
        }

        // 规范：第 14 字节为传输协议与地址族
        HAProxyProxiedProtocol protAndFam;
        try {
            protAndFam = HAProxyProxiedProtocol.valueOf(header.readByte());
        } catch (IllegalArgumentException e) {
            throw new HAProxyProtocolException(e);
        }

        if (protAndFam == HAProxyProxiedProtocol.UNKNOWN) {
            return unknownMsg(HAProxyProtocolVersion.V2, HAProxyCommand.PROXY);
        }

        int addressInfoLen = header.readUnsignedShort();

        String srcAddress;
        String dstAddress;
        int addressLen;
        int srcPort = 0;
        int dstPort = 0;

        AddressFamily addressFamily = protAndFam.addressFamily();

        if (addressFamily == AddressFamily.AF_UNIX) {
            // UNIX 域套接字地址信息固定 216 字节
            if (addressInfoLen < 216 || header.readableBytes() < 216) {
                throw new HAProxyProtocolException(
                    "incomplete UNIX socket address information: " +
                            Math.min(addressInfoLen, header.readableBytes()) + " bytes (expected: 216+ bytes)");
            }
            int startIdx = header.readerIndex();
            int addressEnd = header.indexOf(startIdx, startIdx + 108, (byte) 0); // FIND_NUL
            if (addressEnd == -1) {
                addressLen = 108;
            } else {
                addressLen = addressEnd - startIdx;
            }
            srcAddress = header.toString(startIdx, addressLen, CharsetUtil.US_ASCII);

            startIdx += 108;

            addressEnd = header.indexOf(startIdx, startIdx + 108, (byte) 0); // FIND_NUL
            if (addressEnd == -1) {
                addressLen = 108;
            } else {
                addressLen = addressEnd - startIdx;
            }
            dstAddress = header.toString(startIdx, addressLen, CharsetUtil.US_ASCII);
            // AF_UNIX 每端地址固定 108 字节；前面解析未推进 readerIndex，此处统一跳过
            header.readerIndex(startIdx + 108);
        } else {
            if (addressFamily == AddressFamily.AF_IPv4) {
                // IPv4 地址信息 12 字节（4+4 地址 + 2+2 端口）
                if (addressInfoLen < 12 || header.readableBytes() < 12) {
                    throw new HAProxyProtocolException(
                        "incomplete IPv4 address information: " +
                                Math.min(addressInfoLen, header.readableBytes()) + " bytes (expected: 12+ bytes)");
                }
                addressLen = 4;
            } else if (addressFamily == AddressFamily.AF_IPv6) {
                // IPv6 地址信息 36 字节（16+16 地址 + 2+2 端口）
                if (addressInfoLen < 36 || header.readableBytes() < 36) {
                    throw new HAProxyProtocolException(
                        "incomplete IPv6 address information: " +
                                Math.min(addressInfoLen, header.readableBytes()) + " bytes (expected: 36+ bytes)");
                }
                addressLen = 16;
            } else {
                throw new HAProxyProtocolException(
                    "unable to parse address information (unknown address family: " + addressFamily + ')');
            }

            // 规范：源地址从第 17 字节起
            srcAddress = ipBytesToString(header, addressLen);
            dstAddress = ipBytesToString(header, addressLen);
            srcPort = header.readUnsignedShort();
            dstPort = header.readUnsignedShort();
        }

        final List<HAProxyTLV> tlvs = readTlvs(header);

        return new HAProxyMessage(ver, cmd, protAndFam, srcAddress, dstAddress, srcPort, dstPort, tlvs);
    }

    private static List<HAProxyTLV> readTlvs(final ByteBuf header) {
        HAProxyTLV haProxyTLV = readNextTLV(header, 0);
        if (haProxyTLV == null) {
            return Collections.emptyList();
        }
        // 多数场景 TLV 数量少于 4 个
        List<HAProxyTLV> haProxyTLVs = new ArrayList<HAProxyTLV>(4);

        try {
            do {
                haProxyTLVs.add(haProxyTLV);
                if (haProxyTLV instanceof HAProxySSLTLV) {
                    haProxyTLVs.addAll(((HAProxySSLTLV) haProxyTLV).encapsulatedTLVs());
                }
            } while ((haProxyTLV = readNextTLV(header, 0)) != null);
        } catch (Throwable t) {
            // 异常前释放已读 TLV，避免泄漏
            releaseTlvs(haProxyTLVs);
            throw t;
        }
        return haProxyTLVs;
    }

    private static void releaseDeep(List<HAProxyTLV> children) {
        for (HAProxyTLV child : children) {
            child.release();
            if (child instanceof HAProxySSLTLV) {
                releaseDeep(((HAProxySSLTLV) child).encapsulatedTLVs());
            }
        }
    }

    private static void releaseTlvs(List<HAProxyTLV> tlvs) {
        int skip = 0;
        for (HAProxyTLV tlv : tlvs) {
            if (skip > 0) {
                skip--;
                // 扁平化的 depth-1 子 TLV；若含更深层嵌套须递归释放
                if (tlv instanceof HAProxySSLTLV) {
                    releaseDeep(((HAProxySSLTLV) tlv).encapsulatedTLVs());
                }
            } else if (tlv instanceof HAProxySSLTLV) {
                // 顶层 SSL TLV 的子项已扁平化到列表，外层循环须跳过
                skip = ((HAProxySSLTLV) tlv).encapsulatedTLVs().size();
            }
            tlv.release();
        }
    }

    private static HAProxyTLV readNextTLV(final ByteBuf header, int nestingLevel) {
        if (nestingLevel > MAX_NESTING_LEVEL) {
            throw new HAProxyProtocolException(
                    "Maximum TLV nesting level reached: " + nestingLevel + " (expected: < " + MAX_NESTING_LEVEL + ')');
        }
        // TLV 头部至少 4 字节（type 1 + length 2 + 内容起始）
        if (header.readableBytes() < 4) {
            return null;
        }

        final byte typeAsByte = header.readByte();
        final HAProxyTLV.Type type = HAProxyTLV.Type.typeForByteValue(typeAsByte);

        final int length = header.readUnsignedShort();
        switch (type) {
        case PP2_TYPE_SSL:
            if (length < 5) {
                throw new HAProxyProtocolException("TLV length must be at least 5 but was: " + length);
            }
            if (length > header.readableBytes()) {
                throw new HAProxyProtocolException("TLV length must be smaller or equal the readable bytes (" +
                        header.readableBytes() + ") but was: " + length);
            }
            // 先 slice 原始内容，无异常时才 retain，避免泄漏
            final ByteBuf rawContent = header.slice(header.readerIndex(), length);
            final ByteBuf byteBuf = header.readSlice(length);
            final byte client = byteBuf.readByte();
            final int verify = byteBuf.readInt();

            if (byteBuf.readableBytes() >= 4) {

                final List<HAProxyTLV> encapsulatedTlvs = new ArrayList<HAProxyTLV>(4);
                try {
                    do {
                        final HAProxyTLV haProxyTLV = readNextTLV(byteBuf, nestingLevel + 1);
                        if (haProxyTLV == null) {
                            break;
                        }
                        encapsulatedTlvs.add(haProxyTLV);
                    } while (byteBuf.readableBytes() >= 4);
                }  catch (Throwable t) {
                    // Release all previously read TLVs before rethrowing as otherwise we would leak.
                    releaseTlvs(encapsulatedTlvs);
                    throw t;
                }

                return new HAProxySSLTLV(verify, client, encapsulatedTlvs, rawContent.retain());
            }
            return new HAProxySSLTLV(verify, client, Collections.<HAProxyTLV>emptyList(), rawContent.retain());
        // 非 SSL 类型 TLV 统一按 type+length+content 读取
        case PP2_TYPE_ALPN:
        case PP2_TYPE_AUTHORITY:
        case PP2_TYPE_SSL_VERSION:
        case PP2_TYPE_SSL_CN:
        case PP2_TYPE_NETNS:
        case OTHER:
            return new HAProxyTLV(type, typeAsByte, header.readRetainedSlice(length));
        default:
            return null;
        }
    }

    /**
     * 解码 v1 文本格式 PROXY 协议头部（空格分隔）。
     *
     * @param header                     a version 1 proxy protocol header
     * @return                           {@link HAProxyMessage} instance
     * @throws HAProxyProtocolException  if any portion of the header is invalid
     */
    static HAProxyMessage decodeHeader(String header) {
        if (header == null) {
            throw new HAProxyProtocolException("header");
        }

        String[] parts = header.split(" ");
        int numParts = parts.length;

        if (numParts < 2) {
            throw new HAProxyProtocolException(
                    "invalid header: " + header + " (expected: 'PROXY' and proxied protocol values)");
        }

        if (!"PROXY".equals(parts[0])) {
            throw new HAProxyProtocolException("unknown identifier: " + parts[0]);
        }

        HAProxyProxiedProtocol protAndFam;
        try {
            protAndFam = HAProxyProxiedProtocol.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            throw new HAProxyProtocolException(e);
        }

        if (protAndFam != HAProxyProxiedProtocol.TCP4 &&
                protAndFam != HAProxyProxiedProtocol.TCP6 &&
                protAndFam != HAProxyProxiedProtocol.UNKNOWN) {
            throw new HAProxyProtocolException("unsupported v1 proxied protocol: " + parts[1]);
        }

        if (protAndFam == HAProxyProxiedProtocol.UNKNOWN) {
            return unknownMsg(HAProxyProtocolVersion.V1, HAProxyCommand.PROXY);
        }

        if (numParts != 6) {
            throw new HAProxyProtocolException("invalid TCP4/6 header: " + header + " (expected: 6 parts)");
        }

        try {
            return new HAProxyMessage(
                    HAProxyProtocolVersion.V1, HAProxyCommand.PROXY,
                    protAndFam, parts[2], parts[3], parts[4], parts[5]);
        } catch (RuntimeException e) {
            throw new HAProxyProtocolException("invalid HAProxy message", e);
        }
    }

    /**
     * 代理协议为 UNKNOWN 时的占位消息；规范要求丢弃其余头部字段。
     */
    private static HAProxyMessage unknownMsg(HAProxyProtocolVersion version, HAProxyCommand command) {
        return new HAProxyMessage(version, command, HAProxyProxiedProtocol.UNKNOWN, null, null, 0, 0);
    }

    /**
     * 将缓冲中的 IP 地址字节转为可读字符串。
     *
     * @param header     buffer containing ip address bytes
     * @param addressLen number of bytes to read (4 bytes for IPv4, 16 bytes for IPv6)
     * @return           string representation of the ip address
     */
    private static String ipBytesToString(ByteBuf header, int addressLen) {
        StringBuilder sb = new StringBuilder();
        final int ipv4Len = 4;
        final int ipv6Len = 8;
        if (addressLen == ipv4Len) {
            for (int i = 0; i < ipv4Len; i++) {
                sb.append(header.readByte() & 0xff);
                sb.append('.');
            }
        } else {
            for (int i = 0; i < ipv6Len; i++) {
                sb.append(Integer.toHexString(header.readUnsignedShort()));
                sb.append(':');
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 将端口字符串解析为整数（1~65535）。
     *
     * @param value                      the port
     * @return                           port as an integer
     * @throws IllegalArgumentException  if port is not a valid integer
     */
    private static int portStringToInt(String value) {
        int port;
        try {
            port = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid port: " + value, e);
        }

        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("invalid port: " + value + " (expected: 1 ~ 65535)");
        }

        return port;
    }

    /**
     * 校验地址格式（IPv4、IPv6 或 UNIX 域套接字）。
     *
     * @param address    human-readable address
     * @param addrFamily the {@link AddressFamily} to check the address against
     * @param version    the protocol version
     * @throws IllegalArgumentException if the address is invalid
     */
    private static void checkAddress(String address, AddressFamily addrFamily, HAProxyProtocolVersion version) {
        ObjectUtil.checkNotNull(addrFamily, "addrFamily");

        switch (addrFamily) {
            case AF_UNSPEC:
                if (address != null) {
                    throw new IllegalArgumentException("unable to validate an AF_UNSPEC address: " + address);
                }
                return;
            case AF_UNIX:
                ObjectUtil.checkNotNull(address, "address");
                if (address.getBytes(CharsetUtil.US_ASCII).length > 108) {
                    throw new IllegalArgumentException("invalid AF_UNIX address: " + address);
                }
                if (version == HAProxyProtocolVersion.V1) {
                    // V1 为文本格式，CR/LF 为行分隔、空格为字段分隔
                    for (int i = 0, len = address.length(); i < len; i++) {
                        char c = address.charAt(i);
                        if (c == '\r' || c == '\n' || c == ' ') {
                            throw new IllegalArgumentException("invalid AF_UNIX address: " + address);
                        }
                    }
                }
                return;
        }

        ObjectUtil.checkNotNull(address, "address");

        switch (addrFamily) {
            case AF_IPv4:
                if (!NetUtil.isValidIpV4Address(address)) {
                    throw new IllegalArgumentException("invalid IPv4 address: " + address);
                }
                break;
            case AF_IPv6:
                if (!NetUtil.isValidIpV6Address(address)) {
                    throw new IllegalArgumentException("invalid IPv6 address: " + address);
                }
                break;
            default:
                throw new IllegalArgumentException("unexpected addrFamily: " + addrFamily);
        }
    }

    /**
     * 按地址族校验端口：IP 族允许 0~65535，UNIX/UNSPEC 必须为 0。
     *
     * @param port                       the UDP/TCP port
     * @throws IllegalArgumentException  if the port is out of range (0-65535 inclusive)
     */
    private static void checkPort(int port, AddressFamily addrFamily) {
        switch (addrFamily) {
        case AF_IPv6:
        case AF_IPv4:
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("invalid port: " + port + " (expected: 0 ~ 65535)");
            }
            break;
        case AF_UNIX:
        case AF_UNSPEC:
            if (port != 0) {
                throw new IllegalArgumentException("port cannot be specified with addrFamily: " + addrFamily);
            }
            break;
        default:
            throw new IllegalArgumentException("unexpected addrFamily: " + addrFamily);
        }
    }

    /** 返回本消息的 {@link HAProxyProtocolVersion}。 */
    public HAProxyProtocolVersion protocolVersion() {
        return protocolVersion;
    }

    /** 返回本消息的 {@link HAProxyCommand}（LOCAL 或 PROXY）。 */
    public HAProxyCommand command() {
        return command;
    }

    /** 返回本消息的 {@link HAProxyProxiedProtocol}。 */
    public HAProxyProxiedProtocol proxiedProtocol() {
        return proxiedProtocol;
    }

    /** 返回可读源地址；HAProxy 健康检查（send-proxy-v2）时可能为 {@code null}。 */
    public String sourceAddress() {
        return sourceAddress;
    }

    /** 返回可读目的地址。 */
    public String destinationAddress() {
        return destinationAddress;
    }

    /** 返回 UDP/TCP 源端口。 */
    public int sourcePort() {
        return sourcePort;
    }

    /** 返回 UDP/TCP 目的端口。 */
    public int destinationPort() {
        return destinationPort;
    }

    /**
     * 返回 {@link HAProxyTLV} 列表；无 TLV 时返回空列表。
     * <p>
     * TLV 仅存在于 PROXY 协议 v2。
     */
    public List<HAProxyTLV> tlvs() {
        return tlvs;
    }

    int tlvNumBytes() {
        int tlvNumBytes = 0;
        for (int i = 0; i < tlvs.size(); i++) {
            tlvNumBytes += tlvs.get(i).totalNumBytes();
        }
        return tlvNumBytes;
    }

    @Override
    public HAProxyMessage touch() {
        tryRecord();
        return (HAProxyMessage) super.touch();
    }

    @Override
    public HAProxyMessage touch(Object hint) {
        if (leak != null) {
            leak.record(hint);
        }
        return this;
    }

    @Override
    public HAProxyMessage retain() {
        tryRecord();
        return (HAProxyMessage) super.retain();
    }

    @Override
    public HAProxyMessage retain(int increment) {
        tryRecord();
        return (HAProxyMessage) super.retain(increment);
    }

    @Override
    public boolean release() {
        tryRecord();
        return super.release();
    }

    @Override
    public boolean release(int decrement) {
        tryRecord();
        return super.release(decrement);
    }

    private void tryRecord() {
        if (leak != null) {
            leak.record();
        }
    }

    @Override
    protected void deallocate() {
        try {
            releaseTlvs(tlvs);
        } finally {
            final ResourceLeakTracker<HAProxyMessage> leak = this.leak;
            if (leak != null) {
                boolean closed = leak.close(this);
                assert closed;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256)
                .append(StringUtil.simpleClassName(this))
                .append("(protocolVersion: ").append(protocolVersion)
                .append(", command: ").append(command)
                .append(", proxiedProtocol: ").append(proxiedProtocol)
                .append(", sourceAddress: ").append(sourceAddress)
                .append(", destinationAddress: ").append(destinationAddress)
                .append(", sourcePort: ").append(sourcePort)
                .append(", destinationPort: ").append(destinationPort)
                .append(", tlvs: [");
        if (!tlvs.isEmpty()) {
            for (HAProxyTLV tlv: tlvs) {
                sb.append(tlv).append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        sb.append("])");
        return sb.toString();
    }
}
