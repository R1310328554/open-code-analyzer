package com.taobao.arthas.core.env.convert;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** 字符串到 {@link InetAddress} 的转换器，支持主机名或 IP 字面量。 */
public class StringToInetAddressConverter implements Converter<String, InetAddress> {

    /** 解析主机名或 IP 地址；失败时包装为 {@link IllegalArgumentException} */
    @Override
    public InetAddress convert(String source, Class<InetAddress> targetType) {
        try {
            return InetAddress.getByName(source);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid InetAddress value '" + source + "'", e);
        }
    }

}
