package cache

// stop_once 保证 Cache.Stop 只执行一次，避免多层装饰器重复关闭底层连接或 goroutine。

import "sync"

type stopOnce struct {
	once sync.Once
	Cache
}

// StopOnce 常用于 tiered、background 等多层 Cache 组合场景。
// StopOnce wraps a Cache and ensures its only stopped once.
func StopOnce(cache Cache) Cache {
	return &stopOnce{
		Cache: cache,
	}
}

func (s *stopOnce) Stop() {
	s.once.Do(func() {
		s.Cache.Stop()
	})
}
// 嵌入 Cache 接口其余方法直接委托；仅 Stop 路径受 once 保护。
