package deletion

// deletion 包通用工具：LogQL 删除查询解析与租户 deletion 模式校验。

import (
	"errors"

	"github.com/grafana/loki/v3/pkg/compactor/deletionmode"
	"github.com/grafana/loki/v3/pkg/logql/syntax"
)

var (
	errInvalidQuery = errors.New("invalid query expression")
)

// parseDeletionQuery 解析 LogQL 日志选择器，失败时返回 errInvalidQuery。
// parseDeletionQuery checks if the given logQL is valid for deletions
func parseDeletionQuery(query string) (syntax.LogSelectorExpr, error) {
	logSelectorExpr, err := syntax.ParseLogSelector(query, false)
	if err != nil {
		return nil, errInvalidQuery
	}

	return logSelectorExpr, nil
}

// validDeletionLimit 判断租户 deletion 模式是否允许删除或查询时过滤。
func validDeletionLimit(l Limits, userID string) (bool, error) {
	mode, err := deleteModeFromLimits(l, userID)
	if err != nil {
		return false, err
	}

	return mode.DeleteEnabled(), nil
}

func deleteModeFromLimits(l Limits, userID string) (deletionmode.Mode, error) {
	mode := l.DeletionMode(userID)
	return deletionmode.ParseMode(mode)
}
