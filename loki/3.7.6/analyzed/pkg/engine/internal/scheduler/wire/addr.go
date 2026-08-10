package wire

// addr 提供 wire 协议中 TCP 地址字符串与 net.TCPAddr 的解析辅助。StreamBind 消息在 protobuf 中以字符串传输 endpoint，解码时需还原为 Go net.Addr。

import (
	"fmt"
	"net"
	"net/netip"
)

func addrPortStrToAddr(addrPortStr string) (*net.TCPAddr, error) {
	addrPort, err := netip.ParseAddrPort(addrPortStr)
	if err != nil {
		return nil, fmt.Errorf("parse addr port from %s: %w", addrPortStr, err)
	}
	return net.TCPAddrFromAddrPort(addrPort), nil
}
// 解析失败时包装原始字符串便于排错；成功则通过 net.TCPAddrFromAddrPort 构造 TCPAddr。
