//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package utility

// ssrf.go 提供 SSRF 防护与 DNS 固定 HTTP 客户端。

import (
	"context"
	"fmt"
	"net"
	"net/http"
	"net/url"
	"slices"
	"sort"
	"strings"
	"time"
)

// AllowedURLSchemes 为 AssertURLSafe 允许的 URL scheme（http/https）。
var AllowedURLSchemes = []string{"http", "https"}

// LookupHost 为 DNS 解析函数，测试可覆盖。
var LookupHost = net.LookupHost

// AllowAnyHostForTest 为测试专用开关，生产代码必须保持 false。
var AllowAnyHostForTest = false

// allowAnyHost 读取测试专用 SSRF 绕过开关。
func allowAnyHost() bool {
	return AllowAnyHostForTest
}

// AssertURLSafe 校验 URL 并拒绝非公网 IP，返回主机名与首个公网 IP 供 DNS 固定。
func AssertURLSafe(rawURL string) (hostname, resolvedIP string, err error) {
	parsed, err := url.Parse(strings.TrimSpace(rawURL))
	if err != nil {
		return "", "", fmt.Errorf("invalid url")
	}

	scheme := strings.ToLower(parsed.Scheme)
	if !slices.Contains(AllowedURLSchemes, scheme) {
		sorted := append([]string(nil), AllowedURLSchemes...)
		sort.Strings(sorted)
		return "", "", fmt.Errorf("disallowed URL scheme: '%s'. Only %v are allowed", scheme, sorted)
	}

	hostname = parsed.Hostname()
	if hostname == "" {
		return "", "", fmt.Errorf("URL is missing a host")
	}

	allowAny := allowAnyHost()
	addresses, err := LookupHost(hostname)
	if err != nil {
		return "", "", fmt.Errorf("could not resolve hostname '%s': %v", hostname, err)
	}
	if len(addresses) == 0 {
		return "", "", fmt.Errorf("hostname '%s' resolved to no addresses", hostname)
	}

	for _, addr := range addresses {
		ip := net.ParseIP(addr)
		if ip == nil {
			return "", "", fmt.Errorf("could not parse resolved address '%s' for hostname '%s'", addr, hostname)
		}
		if !allowAny && !isGlobalIP(effectiveIP(ip)) {
			return "", "", fmt.Errorf("URL resolves to a non-public address (%s), which is not allowed", ip.String())
		}
		if resolvedIP == "" {
			resolvedIP = ip.String()
		}
	}
	return hostname, resolvedIP, nil
}

// effectiveIP 解包 IPv4-mapped IPv6，防止绕过私网检测。
func effectiveIP(ip net.IP) net.IP {
	if v4 := ip.To4(); v4 != nil {
		return v4
	}
	return ip
}

// isGlobalIP 判断是否为全球可路由公网地址，对齐 Python ipaddress.is_global。
func isGlobalIP(ip net.IP) bool {
	if ip == nil || ip.IsUnspecified() || ip.IsLoopback() || ip.IsMulticast() || ip.IsLinkLocalUnicast() || ip.IsLinkLocalMulticast() || ip.IsInterfaceLocalMulticast() || ip.IsPrivate() {
		return false
	}
	if v4 := ip.To4(); v4 != nil {
		// CGNAT 100.64.0.0/10 — 旧版 Go IsPrivate 未覆盖
		if v4[0] == 100 && v4[1]&0xC0 == 64 {
			return false
		}
		// 192.0.0.0/24 IETF 协议保留段
		if v4[0] == 192 && v4[1] == 0 && v4[2] == 0 {
			return false
		}
		// TEST-NET 文档地址段
		if v4[0] == 192 && v4[1] == 0 && v4[2] == 2 {
			return false
		}
		if v4[0] == 198 && v4[1] == 51 && v4[2] == 100 {
			return false
		}
		if v4[0] == 203 && v4[1] == 0 && v4[2] == 113 {
			return false
		}
		// 198.18.0.0/15 基准测试段
		if v4[0] == 198 && (v4[1] == 18 || v4[1] == 19) {
			return false
		}
		// 240.0.0.0/4 保留段
		if v4[0] >= 240 {
			return false
		}
	} else if v6 := ip.To16(); v6 != nil {
		// 2001:db8::/32 IPv6 文档前缀
		if v6[0] == 0x20 && v6[1] == 0x01 && v6[2] == 0x0d && v6[3] == 0xb8 {
			return false
		}
		// 100::/64 仅丢弃地址块
		if v6[0] == 0x01 && v6[1] == 0x00 && allZero(v6[2:8]) {
			return false
		}
	}
	return true
}

func allZero(b []byte) bool {
	for _, x := range b {
		if x != 0 {
			return false
		}
	}
	return true
}

// PinnedHTTPClient 返回 DNS 固定 HTTP 客户端，消除校验与连接间的 TOCTOU 窗口。
func PinnedHTTPClient(hostname, resolvedIP string, timeout time.Duration) *http.Client {
	dialer := &net.Dialer{
		Timeout:   timeout,
		KeepAlive: 30 * time.Second,
	}
	transport := &http.Transport{
		// 禁用环境代理，防止绕过 DNS 固定
		Proxy: nil,
		DialContext: func(ctx context.Context, network, addr string) (net.Conn, error) {
			host, port, splitErr := net.SplitHostPort(addr)
			if splitErr == nil && host == hostname && resolvedIP != "" {
				return dialer.DialContext(ctx, network, net.JoinHostPort(resolvedIP, port))
			}
			return dialer.DialContext(ctx, network, addr)
		},
		TLSHandshakeTimeout:   timeout,
		ResponseHeaderTimeout: timeout,
		ExpectContinueTimeout: 1 * time.Second,
		ForceAttemptHTTP2:     false,
	}
	return &http.Client{
		Transport: transport,
		Timeout:   timeout,
	}
}
// ssrf.go — SSRF 防护：URL 校验、公网 IP 过滤与 DNS 固定 HTTP 客户端。
