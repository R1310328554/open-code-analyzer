package loki

// profiling_config 定义 Go runtime 采样剖析开关：block、CPU 与 mutex profile 速率，供运维在需要时启用 pprof 诊断。

import "flag"

type ProfilingConfig struct {
	BlockProfileRate     int `yaml:"block_profile_rate"`
	CPUProfileRate       int `yaml:"cpu_profile_rate"`
	MutexProfileFraction int `yaml:"mutex_profile_fraction"`
}

// RegisterFlags 以 profiling. 前缀注册三个剖析相关命令行参数。
// RegisterFlags registers flag.
func (c *ProfilingConfig) RegisterFlags(f *flag.FlagSet) {
	c.RegisterFlagsWithPrefix("profiling.", f)
}

// RegisterFlagsWithPrefix registers flag with a common prefix.
// RegisterFlagsWithPrefix 允许嵌套配置段复用同一结构注册带前缀的标志。
func (c *ProfilingConfig) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.IntVar(&c.BlockProfileRate, prefix+"block-profile-rate", 0, "Sets the value for runtime.SetBlockProfilingRate")
	f.IntVar(&c.CPUProfileRate, prefix+"cpu-profile-rate", 0, "Sets the value for runtime.SetCPUProfileRate")
	f.IntVar(&c.MutexProfileFraction, prefix+"mutex-profile-fraction", 0, "Sets the value for runtime.SetMutexProfileFraction")
}
// 默认均为 0 表示关闭额外采样；生产环境按需开启以避免性能开销。
