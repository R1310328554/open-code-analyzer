package net

// net 子包提供本机环回网卡名解析，供 gRPC/HTTP 绑定 loopback 时避免硬编码 lo/eth0。

import (
	"fmt"
	"net"
)

// LoopbackInterfaceName 扫描 net.Interfaces，返回首个 FlagLoopback 为真的接口名。
// LoopbackInterfaceName search for the name of a loopback interface in the list
// of the system's network interfaces and returns the first one found.
func LoopbackInterfaceName() (string, error) {
	is, err := net.Interfaces()
	if err != nil {
		return "", fmt.Errorf("can't retrieve loopback interface name: %s", err)
	}

	for _, i := range is {
		if i.Flags&net.FlagLoopback != 0 {
			return i.Name, nil
		}
	}

	return "", fmt.Errorf("can't retrieve loopback interface name")
}
// 未找到环回接口时返回包装错误，调用方应 fallback 到 127.0.0.1 或显式配置。
