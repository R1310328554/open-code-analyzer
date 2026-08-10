// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Remote write HTTP 客户端 DNS 轮询：解析主机名后随机选取 A/AAAA 记录建立连接，提升多 IP 后端的负载均衡与容错。

package remote

import (
	"context"
	"math/rand"
	"net"
	"net/http"
	"time"

	"github.com/prometheus/common/config"
)

type hostResolver interface {
	LookupHost(context.Context, string) ([]string, error)
}

// dialContextWithRoundRobinDNS 包装 DialContext 并在拨号前做 RR DNS 选择。
type dialContextWithRoundRobinDNS struct {
	dialContext config.DialContextFunc
	resolver    hostResolver
	rand        *rand.Rand
}

// newDialContextWithRoundRobinDNS 使用默认 Transport 与 net.DefaultResolver 构造实例。
// newDialContextWithRoundRobinDNS creates a new dialContextWithRoundRobinDNS.
// We discourage creating new instances of struct dialContextWithRoundRobinDNS by explicitly setting its members,
// except for testing purposes, and recommend using newDialContextWithRoundRobinDNS.
func newDialContextWithRoundRobinDNS() *dialContextWithRoundRobinDNS {
	return &dialContextWithRoundRobinDNS{
		dialContext: http.DefaultTransport.(*http.Transport).DialContext,
		resolver:    net.DefaultResolver,
		rand:        rand.New(rand.NewSource(time.Now().Unix())),
	}
}

// dialContextFn 返回带 DNS 轮询逻辑的 DialContext 闭包。
func (dc *dialContextWithRoundRobinDNS) dialContextFn() config.DialContextFunc {
	return func(ctx context.Context, network, addr string) (net.Conn, error) {
		host, port, err := net.SplitHostPort(addr)
		if err != nil {
			return dc.dialContext(ctx, network, addr)
		}

		addrs, err := dc.resolver.LookupHost(ctx, host)
		if err != nil || len(addrs) == 0 {
			return dc.dialContext(ctx, network, addr)
		}

		randomAddr := net.JoinHostPort(addrs[dc.rand.Intn(len(addrs))], port)
		return dc.dialContext(ctx, network, randomAddr)
	}
}
