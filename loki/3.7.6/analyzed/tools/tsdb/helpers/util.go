package helpers

// tsdb helpers util：表名解析、period 匹配与 object store 租户枚举，供 index-analyzer 等离线工具定位 TSDB 索引表与租户列表。

import (
	"context"
	"fmt"
	"math"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
	"time"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/types"
)

const (
	daySeconds = int64(24 * time.Hour / time.Second)
)

// extractTableNumberRegex 匹配表名末尾数字作为日级 bucket 编号。
// regexp for finding the trailing index bucket number at the end of table name
var extractTableNumberRegex = regexp.MustCompile(`[0-9]+$`)

// copied from indexshipper/downloads
// extractTableNumberFromName extract the table number from a given tableName.
// if the tableName doesn't match the regex, it would return -1 as table number.
func extractTableNumberFromName(tableName string) (int64, error) {
	match := extractTableNumberRegex.Find([]byte(tableName))
	if match == nil {
		return -1, nil
	}

	tableNumber, err := strconv.ParseInt(string(match), 10, 64)
	if err != nil {
		return -1, err
	}

	return tableNumber, nil
}
func getActiveTableNumber() int64 {
	return getTableNumberForTime(model.Now())
}

func getTableNumberForTime(t model.Time) int64 {
	return t.Unix() / daySeconds
}

// GetPeriodConfigForTableNumber 在 schema configs 中查找 TSDB period 与完整表名。
func GetPeriodConfigForTableNumber(table string, periodicConfigs []config.PeriodConfig) (config.PeriodConfig, config.TableRange, string, error) {
	tableNo, err := extractTableNumberFromName(table)
	if err != nil {
		return config.PeriodConfig{}, config.TableRange{}, "", fmt.Errorf("extracting table number: %w", err)
	}

	for i, periodCfg := range periodicConfigs {
		if periodCfg.IndexType != types.TSDBType {
			continue
		}

		periodEndTime := config.DayTime{Time: math.MaxInt64}
		if i < len(periodicConfigs)-1 {
			periodEndTime = config.DayTime{Time: periodicConfigs[i+1].From.Add(-time.Millisecond)}
		}

		tableName := fmt.Sprintf("%s%s", periodCfg.IndexTables.Prefix, strconv.Itoa(int(tableNo)))
		tableRange := periodCfg.GetIndexTableNumberRange(periodEndTime)

		if ok, _ := tableRange.TableInRange(tableName); ok {
			return periodCfg, tableRange, tableName, nil
		}
	}

	return config.PeriodConfig{}, config.TableRange{}, "", fmt.Errorf("table does not belong to any period")
}

// ResolveTenants 列出 object store 前缀下 object key 首段作为租户 ID。
func ResolveTenants(objectClient client.ObjectClient, pathPrefix, tableName string) ([]string, error) {
	prefix := filepath.Join(pathPrefix, tableName)
	indices, _, err := objectClient.List(context.Background(), prefix, "")
	if err != nil {
		return nil, fmt.Errorf("error listing tenants: %w", err)
	}

	tenants := make(map[string]struct{})
	for _, index := range indices {
		s := strings.TrimPrefix(index.Key, prefix+"/")
		tenant := strings.Split(s, "/")[0]
		tenants[tenant] = struct{}{}
	}

	var result []string
	for tenant := range tenants {
		result = append(result, tenant)
	}

	return result, nil
}
// getTableNumberForTime 将 Unix 秒除以 daySeconds 得到与 Loki 一致的日表编号。
