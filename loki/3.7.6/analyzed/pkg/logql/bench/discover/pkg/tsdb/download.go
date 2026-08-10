package tsdb

// download 从对象存储按时间范围发现并下载 TSDB 索引文件到本地临时目录，供后续结构发现与范围推导使用。

import (
	"context"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/go-kit/log"
	"github.com/prometheus/common/model"

	shipperstorage "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/storage"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb"
)

// tsdbIndexTablePrefix 为默认索引表名前缀；dayInSeconds 用于按天生成表名。
const (
	tsdbIndexTablePrefix = "index_"
	dayInSeconds         = 24 * 60 * 60
)

var downloadFileFromStorage = shipperstorage.DownloadFileFromStorage

func DiscoverAndDownloadIndexes(ctx context.Context, client shipperstorage.Client, cfg DownloadConfig) (DownloadResult, error) {
	if strings.TrimSpace(cfg.Tenant) == "" {
		return DownloadResult{}, fmt.Errorf("tenant is required")
	}
	if strings.TrimSpace(cfg.TmpDir) == "" {
		return DownloadResult{}, fmt.Errorf("tmp dir is required")
	}

	from, to, err := normalizeDownloadRange(cfg.From, cfg.To)
	if err != nil {
		return DownloadResult{}, err
	}

	prefix := cfg.TablePrefix
	if prefix == "" {
		prefix = tsdbIndexTablePrefix
	}

	tables := tableNamesForRange(from, to, prefix)
	results := make([]DownloadedIndexFile, 0)

	for _, table := range tables {
		files, err := client.ListUserFiles(ctx, table, cfg.Tenant, true)
		if err != nil {
			if client.IsFileNotFoundErr(err) {
				continue
			}
			return DownloadResult{}, fmt.Errorf("list user files for table %q tenant %q: %w", table, cfg.Tenant, err)
		}

		matching, err := filterTSDBFilesByTimeOverlap(files, model.TimeFromUnixNano(from.UnixNano()), model.TimeFromUnixNano(to.UnixNano()))
		if err != nil {
			return DownloadResult{}, fmt.Errorf("filter files for table %q tenant %q: %w", table, cfg.Tenant, err)
		}

		for _, match := range matching {
			compressed := shipperstorage.IsCompressedFile(match.ObjectName)
			localName := match.ObjectName
			if compressed {
				localName = strings.TrimSuffix(localName, ".gz")
			}

			localPath := filepath.Join(cfg.TmpDir, table, cfg.Tenant, localName)
			if err := downloadIndexFile(ctx, client, table, cfg.Tenant, match.ObjectName, localPath, compressed); err != nil {
				return DownloadResult{}, err
			}

			results = append(results, DownloadedIndexFile{
				Table:      table,
				ObjectName: match.ObjectName,
				LocalPath:  localPath,
				From:       match.From,
				Through:    match.Through,
			})
		}
	}

	sort.Slice(results, func(i, j int) bool {
		if results[i].Table == results[j].Table {
			return results[i].ObjectName < results[j].ObjectName
		}
		return results[i].Table < results[j].Table
	})

	return DownloadResult{Files: results}, nil
}

// normalizeDownloadRange 补全零值边界：to 默认当前 UTC，from 默认 to 前 24 小时。
func normalizeDownloadRange(from, to time.Time) (time.Time, time.Time, error) {
	if to.IsZero() {
		to = time.Now().UTC()
	}
	if from.IsZero() {
		from = to.Add(-24 * time.Hour)
	}
	from = from.UTC()
	to = to.UTC()

	if from.After(to) {
		return time.Time{}, time.Time{}, fmt.Errorf("invalid range: from %s is after to %s", from.Format(time.RFC3339), to.Format(time.RFC3339))
	}

	return from, to, nil
}

// tableNamesForRange 按 Unix 日序号生成 index_<day> 表名列表。
func tableNamesForRange(from, to time.Time, prefix string) []string {
	startDay := from.Unix() / dayInSeconds
	endDay := to.Unix() / dayInSeconds

	tables := make([]string, 0, endDay-startDay+1)
	for day := startDay; day <= endDay; day++ {
		tables = append(tables, prefix+strconv.FormatInt(day, 10))
	}

	return tables
}

// filterTSDBFilesByTimeOverlap 解析单租户 TSDB 对象名并保留与查询窗口重叠的文件。
func filterTSDBFilesByTimeOverlap(files []shipperstorage.IndexFile, from, to model.Time) ([]DownloadedIndexFile, error) {
	filtered := make([]DownloadedIndexFile, 0, len(files))

	for _, file := range files {
		id, ok := parseSingleTenantTSDBObjectName(file.Name)
		if !ok {
			continue
		}

		if id.Through < from || id.From > to {
			continue
		}

		filtered = append(filtered, DownloadedIndexFile{
			ObjectName: file.Name,
			From:       id.From,
			Through:    id.Through,
		})
	}

	return filtered, nil
}

// parseSingleTenantTSDBObjectName 去除 .gz 后缀后解析 TSDB 路径标识符。
func parseSingleTenantTSDBObjectName(name string) (tsdb.SingleTenantTSDBIdentifier, bool) {
	trimmed := name
	if shipperstorage.IsCompressedFile(name) {
		trimmed = strings.TrimSuffix(name, ".gz")
	}

	return tsdb.ParseSingleTenantTSDBPath(trimmed)
}

// downloadIndexFile 创建本地目录并调用 shipper 下载，支持压缩文件自动解压。
func downloadIndexFile(ctx context.Context, client shipperstorage.Client, table, tenant, objectName, localPath string, compressed bool) error {
	if err := ensureDirectory(filepath.Dir(localPath)); err != nil {
		return fmt.Errorf("create local directory for %q: %w", localPath, err)
	}

	err := downloadFileFromStorage(
		localPath,
		compressed,
		true,
		shipperstorage.LoggerWithFilename(log.NewNopLogger(), objectName),
		func() (io.ReadCloser, error) {
			return client.GetUserFile(ctx, table, tenant, objectName)
		},
	)
	if err != nil {
		return fmt.Errorf("download file %q from table %q: %w", objectName, table, err)
	}

	return nil
}

var ensureDirectory = func(path string) error {
	return os.MkdirAll(path, 0o755)
}
// 下载结果按表名与对象名排序，便于确定性回归测试与 diff 对比。
