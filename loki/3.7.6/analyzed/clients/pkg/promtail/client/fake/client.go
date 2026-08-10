package fake

// 测试用假 Promtail Client：内存收集收到的 Entry，不发起真实 HTTP 推送。
// 实现 Client 接口，支持 Stop/Clear 在单测间复用同一实例。

import (
	"sync"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
)

// fake Client：channel 收 Entry，后台 goroutine 追加到 received 切片。
// Client is a fake client used for testing.
type Client struct {
	entries  chan api.Entry
	received []api.Entry
	once     sync.Once
	mtx      sync.Mutex
	wg       sync.WaitGroup
	OnStop   func()
}

// 启动消费 goroutine，Stop 时关闭 channel、等待并调用 OnStop 回调。
func New(stop func()) *Client {
	c := &Client{
		OnStop:  stop,
		entries: make(chan api.Entry),
	}
	c.wg.Add(1)
	go func() {
		defer c.wg.Done()
		for e := range c.entries {
			c.mtx.Lock()
			c.received = append(c.received, e)
			c.mtx.Unlock()
		}
	}()
	return c
}

// once 关闭 entries，wg 等待 drain 后执行 OnStop。
// Stop implements client.Client
func (c *Client) Stop() {
	c.once.Do(func() { close(c.entries) })
	c.wg.Wait()
	c.OnStop()
}

func (c *Client) Chan() chan<- api.Entry {
	return c.entries
}

// 返回已收条目的副本，避免测试读写竞态。
func (c *Client) Received() []api.Entry {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	cpy := make([]api.Entry, len(c.received))
	copy(cpy, c.received)
	return cpy
}

// 与 Stop 相同，满足 Client 接口的立即停止语义。
// StopNow implements client.Client
func (c *Client) StopNow() {
	c.Stop()
}

func (c *Client) Name() string {
	return "fake"
}

// 清空 received 缓冲，便于多样例共用同一 fake client。
// Clear is used to cleanup the buffered received entries, so the same client can be re-used between
// test cases.
func (c *Client) Clear() {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	c.received = []api.Entry{}
}
