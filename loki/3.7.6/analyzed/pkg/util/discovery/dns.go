package discovery

// discovery 包提供基于 dskit DNS Provider 的周期性服务发现：解析逗号分隔主机名并暴露当前可用地址列表。

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/dns"
	"github.com/prometheus/client_golang/prometheus"
)

type DNS struct {
	logger        log.Logger
	cleanupPeriod time.Duration
	addresses     []string
	stop          chan struct{}
	done          sync.WaitGroup
	once          sync.Once
	dnsProvider   *dns.Provider
}

// NewDNS 启动后台 discoveryLoop，立即 Add(1) 等待首次解析完成。
func NewDNS(logger log.Logger, cleanupPeriod time.Duration, address string, reg prometheus.Registerer) *DNS {
	dnsProvider := dns.NewProvider(logger, reg, dns.GolangResolverType)
	d := &DNS{
		logger:        logger,
		cleanupPeriod: cleanupPeriod,
		addresses:     strings.Split(address, ","),
		stop:          make(chan struct{}),
		done:          sync.WaitGroup{},
		dnsProvider:   dnsProvider,
	}
	go d.discoveryLoop()
	d.done.Add(1)
	return d
}

func (d *DNS) RunOnce() {
	d.runDiscovery()
}

func (d *DNS) Addresses() []string {
	return d.dnsProvider.Addresses()
}

// Stop 通过 sync.Once 仅关闭 stop 一次，避免集成测试重复调用 panic。
func (d *DNS) Stop() {
	// Integration tests were calling Stop() multiple times, so we need to make sure
	// that we only close the stop channel once.
	d.once.Do(func() { close(d.stop) })
	d.done.Wait()
}

func (d *DNS) discoveryLoop() {
	ticker := time.NewTicker(d.cleanupPeriod)
	defer func() {
		ticker.Stop()
		d.done.Done()
	}()

	for {
		select {
		case <-ticker.C:
			d.runDiscovery()
		case <-d.stop:
			return
		}
	}
}

// runDiscovery 以 5 秒超时调用 Resolve，失败时记录 error 级别日志。
func (d *DNS) runDiscovery() {
	ctx, cancel := context.WithTimeoutCause(context.Background(), 5*time.Second, fmt.Errorf("DNS lookup timeout: %v", d.addresses))
	defer cancel()
	err := d.dnsProvider.Resolve(ctx, d.addresses)
	if err != nil {
		level.Error(d.logger).Log("msg", "failed to resolve server addresses", "err", err)
	}
}
// Addresses 返回 Provider 缓存的最新解析结果，供 gRPC 客户端轮询连接目标。
