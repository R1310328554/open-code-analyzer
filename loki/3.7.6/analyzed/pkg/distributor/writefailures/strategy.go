package writefailures

// strategy 实现 dskit limiter 的 Burst/Limit 接口，为写入失败日志提供令牌桶参数。

type strategy struct {
	burst int
	rate  float64
}

func newStrategy(burst int, rate float64) *strategy {
	return &strategy{
		burst: burst,
		rate:  rate,
	}
}

func (s *strategy) Burst(_ string) int {
	return s.burst
}

// Limit 返回每秒补充令牌的字节速率，与 burst 同源配置。
func (s *strategy) Limit(_ string) float64 {
	return s.rate
}
// RateLimiter 以 tenantID 为键独立计费，策略本身不感知租户差异。
