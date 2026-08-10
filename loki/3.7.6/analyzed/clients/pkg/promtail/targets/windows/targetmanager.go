//go:build !windows
// +build !windows

package windows

// 非 Windows 平台的 Windows target 管理器桩实现：编译未含 win_eventlog 时
// 记录警告并返回空 TargetManager，Ready 恒为 false。

import (
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// 空结构体占位，满足 targetManager 接口但不创建任何 target。
// TargetManager manages a series of windows event targets.
type TargetManager struct{}

// 打印 WARNING 说明当前构建未启用 Windows 事件日志采集支持。
// NewTargetManager creates a new Windows managers.
func NewTargetManager(
	_ prometheus.Registerer,
	logger log.Logger,
	_ api.EntryHandler,
	_ []scrapeconfig.Config,
) (*TargetManager, error) {
	level.Warn(logger).Log("msg", "WARNING!!! Windows target was configured but support for reading the windows event is not compiled into this build of promtail!")
	return &TargetManager{}, nil
}

// Ready returns true if at least one Windows target is also ready.
func (tm *TargetManager) Ready() bool { return false }

// Stop stops the Windows target manager and all of its targets.
func (tm *TargetManager) Stop() {}

// ActiveTargets returns the list of active Windows targets.
func (tm *TargetManager) ActiveTargets() map[string][]target.Target { return nil }

// AllTargets returns the list of all targets.
func (tm *TargetManager) AllTargets() map[string][]target.Target { return nil }
