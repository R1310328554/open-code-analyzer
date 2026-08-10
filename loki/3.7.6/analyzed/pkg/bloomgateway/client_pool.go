package bloomgateway

// JumpHash 客户端连接池：基于 DNS 地址列表维护 gRPC 连接，
// 按块键哈希选择 Bloom Gateway 实例并缓存 per-address 客户端。

import (
	"context"
	"flag"
	"sync"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/ring/client"
	"github.com/grafana/dskit/services"

	"github.com/grafana/loki/v3/pkg/util/jumphash"
)

// PoolConfig 控制 DNS 地址列表刷新间隔。
// PoolConfig is config for creating a Pool.
type PoolConfig struct {
	CheckInterval time.Duration `yaml:"check_interval"`
}

// RegisterFlags adds the flags required to config this to the given FlagSet.
func (cfg *PoolConfig) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.DurationVar(&cfg.CheckInterval, prefix+"check-interval", 15*time.Second, "How frequently to update the list of servers.")
}

func (cfg *PoolConfig) Validate() error {
	return nil
}

// compiler check
var _ clientPool = &JumpHashClientPool{}

type ClientFactory func(addr string) (client.PoolClient, error)

func (f ClientFactory) New(addr string) (client.PoolClient, error) {
	return f(addr)
}

// JumpHashClientPool 结合 jumphash.Selector 与 per-address gRPC 客户端缓存。
type JumpHashClientPool struct {
	services.Service
	*jumphash.Selector
	sync.RWMutex

	provider AddressProvider
	logger   log.Logger

	clients       map[string]client.PoolClient
	clientFactory ClientFactory
}

// AddressProvider 提供当前可用的 Bloom Gateway 实例地址列表。
type AddressProvider interface {
	Addresses() []string
}

func NewJumpHashClientPool(clientFactory ClientFactory, dnsProvider AddressProvider, updateInterval time.Duration, logger log.Logger) (*JumpHashClientPool, error) {
	selector := jumphash.DefaultSelector("bloomgateway")
	err := selector.SetServers(dnsProvider.Addresses()...)
	if err != nil {
		level.Warn(logger).Log("msg", "error updating servers", "err", err)
	}

	p := &JumpHashClientPool{
		Selector:      selector,
		clientFactory: clientFactory,
		provider:      dnsProvider,
		logger:        logger,
		clients:       make(map[string]client.PoolClient, len(dnsProvider.Addresses())),
	}

	p.Service = services.NewTimerService(updateInterval, nil, p.updateLoop, nil)
	return p, services.StartAndAwaitRunning(context.Background(), p.Service)
}

func (p *JumpHashClientPool) Stop() {
	_ = services.StopAndAwaitTerminated(context.Background(), p.Service)
}

// Addr 对给定块键执行 JumpHash 选路，返回目标实例地址字符串。
func (p *JumpHashClientPool) Addr(key string) (string, error) {
	addr, err := p.FromString(key)
	if err != nil {
		return "", err
	}
	return addr.String(), nil
}

func (p *JumpHashClientPool) updateLoop(_ context.Context) error {
	err := p.SetServers(p.provider.Addresses()...)
	if err != nil {
		level.Warn(p.logger).Log("msg", "error updating servers", "err", err)
	}
	return nil
}

// GetClientFor 返回指定地址的 gRPC 客户端，无缓存则懒创建并写入 map。
// GetClientFor implements clientPool.
func (p *JumpHashClientPool) GetClientFor(addr string) (client.PoolClient, error) {
	client, ok := p.fromCache(addr)
	if ok {
		return client, nil
	}

	// No client in cache so create one
	p.Lock()
	defer p.Unlock()

	// Check if a client has been created just after checking the cache and before acquiring the lock.
	client, ok = p.clients[addr]
	if ok {
		return client, nil
	}

	client, err := p.clientFactory.New(addr)
	if err != nil {
		return nil, err
	}
	p.clients[addr] = client
	return client, nil
}

// fromCache 读锁下查询已缓存的 per-address 连接客户端。
func (p *JumpHashClientPool) fromCache(addr string) (client.PoolClient, bool) {
	p.RLock()
	defer p.RUnlock()
	client, ok := p.clients[addr]
	return client, ok
}
