//go:build !linux || !cgo || !promtail_journal_enabled
// +build !linux !cgo !promtail_journal_enabled

package journal

// 非 Linux/无 cgo/未启用 promtail_journal 时的 journal target 桩实现：
// NewJournalTargetManager 打 WARN 并返回空 manager，Ready 恒 false。

import (
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/positions"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// build tag 不满足时此文件编译，journal 功能不可用。
// JournalTargetManager manages a series of JournalTargets.
// nolint:revive
type JournalTargetManager struct{}

// 配置 journal target 但二进制未编译 journal 支持时仅告警不报错。
// NewJournalTargetManager returns nil as JournalTargets are not supported
// on this platform.
func NewJournalTargetManager(
	_ *Metrics,
	logger log.Logger,
	_ positions.Positions,
	_ api.EntryHandler,
	_ []scrapeconfig.Config,
) (*JournalTargetManager, error) {
	level.Warn(logger).Log("msg", "WARNING!!! Journal target was configured but support for reading the systemd journal is not compiled into this build of promtail!")
	return &JournalTargetManager{}, nil
}

// 桩实现无法读取 journal，readiness 始终 false。
// Ready always returns false for JournalTargetManager on non-Linux
// platforms.
func (tm *JournalTargetManager) Ready() bool {
	return false
}

// Stop is a no-op on non-Linux platforms.
func (tm *JournalTargetManager) Stop() {}

// ActiveTargets always returns nil on non-Linux platforms.
func (tm *JournalTargetManager) ActiveTargets() map[string][]target.Target {
	return nil
}

// AllTargets always returns nil on non-Linux platforms.
func (tm *JournalTargetManager) AllTargets() map[string][]target.Target {
	return nil
}
