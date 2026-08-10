package ingester

// StreamRateCalculator 以分片 stripe 结构统计各租户/流的写入速率，每秒聚合一次供 distributor 做流级别限速决策。

import (
	"sync"
	"time"

	"github.com/grafana/loki/v3/pkg/logproto"
)

const (
	// defaultStripeSize is the default number of entries to allocate in the
	// stripeSeries list.
	defaultStripeSize = 1 << 10

	// The intent is for a per-second rate so this is hard coded
	updateInterval = time.Second
)

// stripeLock is taken from ruler/storage/wal/series.go
// stripeLock 带 cache line 填充的读写锁，避免多 stripe 锁争用同一缓存行。
type stripeLock struct {
	sync.RWMutex
	// Padding to avoid multiple locks being on the same cache line.
	_ [40]byte
}

type StreamRateCalculator struct {
	size     int
	samples  []map[string]map[uint64]logproto.StreamRate
	locks    []stripeLock
	stopchan chan struct{}

	rateLock sync.RWMutex
	allRates []logproto.StreamRate
}

// NewStreamRateCalculator 初始化 stripe 数组并启动后台 updateLoop 定时刷新速率。
func NewStreamRateCalculator() *StreamRateCalculator {
	calc := &StreamRateCalculator{
		size: defaultStripeSize,
		// Lookup pattern: tenant -> fingerprint -> rate
		samples:  make([]map[string]map[uint64]logproto.StreamRate, defaultStripeSize),
		locks:    make([]stripeLock, defaultStripeSize),
		stopchan: make(chan struct{}),
	}

	for i := 0; i < defaultStripeSize; i++ {
		calc.samples[i] = make(map[string]map[uint64]logproto.StreamRate)
	}

	go calc.updateLoop()

	return calc
}

func (c *StreamRateCalculator) updateLoop() {
	t := time.NewTicker(updateInterval)
	defer t.Stop()

	for {
		select {
		case <-t.C:
			c.updateRates()
		case <-c.stopchan:
			return
		}
	}
}

// updateRates 遍历各 stripe 累加字节与 push 次数，清空采样 map 后发布新快照。
func (c *StreamRateCalculator) updateRates() {
	rates := make([]logproto.StreamRate, 0, c.size)

	for i := 0; i < c.size; i++ {
		c.locks[i].Lock()

		tenantRates := c.samples[i]
		for _, tenant := range tenantRates {
			for _, streamRate := range tenant {
				rates = append(rates, logproto.StreamRate{
					Tenant:            streamRate.Tenant,
					StreamHash:        streamRate.StreamHash,
					StreamHashNoShard: streamRate.StreamHashNoShard,
					Rate:              streamRate.Rate,
					Pushes:            streamRate.Pushes,
				})
			}
		}

		c.samples[i] = make(map[string]map[uint64]logproto.StreamRate)
		c.locks[i].Unlock()
	}

	c.rateLock.Lock()
	defer c.rateLock.Unlock()

	c.allRates = rates
}

func (c *StreamRateCalculator) Rates() []logproto.StreamRate {
	c.rateLock.RLock()
	defer c.rateLock.RUnlock()

	return c.allRates
}

// Record 按 streamHash 选 stripe，累加该流本周期字节数与 push 计数。
func (c *StreamRateCalculator) Record(tenant string, streamHash, streamHashNoShard uint64, bytes int) {
	i := streamHash & uint64(c.size-1)

	c.locks[i].Lock()
	defer c.locks[i].Unlock()

	tenantMap := c.getTenant(i, tenant)
	streamRate := tenantMap[streamHash]
	streamRate.StreamHash = streamHash
	streamRate.StreamHashNoShard = streamHashNoShard
	streamRate.Tenant = tenant
	streamRate.Rate += int64(bytes)
	streamRate.Pushes++
	tenantMap[streamHash] = streamRate

	c.samples[i][tenant] = tenantMap
}

func (c *StreamRateCalculator) getTenant(idx uint64, tenant string) map[uint64]logproto.StreamRate {
	if t, ok := c.samples[idx][tenant]; ok {
		return t
	}
	return make(map[uint64]logproto.StreamRate)
}

// Stop 关闭 stopchan 终止后台更新 goroutine。
func (c *StreamRateCalculator) Stop() {
	close(c.stopchan)
}
// lookup 路径为 tenant → fingerprint → StreamRate，与 distributor 流限速策略配合。
