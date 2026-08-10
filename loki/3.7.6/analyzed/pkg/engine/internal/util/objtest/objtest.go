// Package objtest 为测试构建本地 dataobj 日志与索引对象存储目录。
// Package objtest provides support for creating a data object storage directory
// for testing purposes.
package objtest

import (
	"context"
	"errors"
	"flag"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/go-kit/log"
	"github.com/stretchr/testify/require"
	"github.com/thanos-io/objstore"
	"github.com/thanos-io/objstore/providers/filesystem"

	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/consumer/logsobj"
	"github.com/grafana/loki/v3/pkg/dataobj/index"
	"github.com/grafana/loki/v3/pkg/dataobj/index/indexobj"
	"github.com/grafana/loki/v3/pkg/dataobj/metastore"
	"github.com/grafana/loki/v3/pkg/dataobj/uploader"
	"github.com/grafana/loki/v3/pkg/logproto"
)

// Tenant 为 Builder 写入日志时使用的固定租户标识 objtest。
// Tenant is the tenant used for storing logs with a [Builder].
const Tenant = "objtest"

// Builder 在临时目录累积 logproto.Stream，flush 后上传并更新 metastore TOC。
// Builder is a directory holding logs data objects and index data objects.
// Logs can be appended to the builder using [Builder.Append]. After all logs
// have been appended, the builder must be closed using the [Builder.Close].
type Builder struct {
	t      *testing.T // Test associated with the store
	dir    string     // Actual directory holding data
	logger log.Logger

	dirty bool // Whether there's any pending data to flush.

	uploader                            *uploader.Uploader
	bucket, indexBucket                 objstore.Bucket
	logsBuilder                         *logsobj.Builder
	logsMetastoreToc, indexMetastoreToc *metastore.TableOfContentsWriter
}

// NewBuilder 初始化 filesystem bucket、logs builder 与双 metastore TOC writer。
// NewBuilder creates a builder that can be used for accumulating logs.
func NewBuilder(t *testing.T) *Builder {
	logger := log.NewNopLogger()

	dir := t.TempDir()

	require.NoError(t, os.MkdirAll(filepath.Join(dir, "tocs"), 0700), "could not create object toc directory")
	require.NoError(t, os.MkdirAll(filepath.Join(dir, "index/v0/tocs"), 0700), "could not create index toc directory")

	bucket, err := filesystem.NewBucket(dir)
	require.NoError(t, err, "expected to be able to create bucket")

	var builderConfig logsobj.BuilderConfig
	builderConfig.RegisterFlagsWithPrefix("", flag.NewFlagSet("", flag.PanicOnError)) // Acquire defaults
	logsBuilder, err := logsobj.NewBuilder(builderConfig, nil)
	require.NoError(t, err, "expected to be able to create logs builder")

	indexWriterBucket := objstore.NewPrefixedBucket(bucket, "index/v0")
	logsMetastoreToc := metastore.NewTableOfContentsWriter(bucket, logger)
	indexMetastoreToc := metastore.NewTableOfContentsWriter(indexWriterBucket, logger)

	return &Builder{
		t:      t,
		dir:    dir,
		logger: logger,

		uploader:          uploader.New(uploader.Config{SHAPrefixSize: 2}, bucket, logger),
		bucket:            bucket,
		indexBucket:       indexWriterBucket,
		logsBuilder:       logsBuilder,
		logsMetastoreToc:  logsMetastoreToc,
		indexMetastoreToc: indexMetastoreToc,
	}
}

// Append 在 builder 满时自动 flush 后重试追加，保证测试数据不丢失。
// Append appends the given streams to the builder.
func (b *Builder) Append(ctx context.Context, streams ...logproto.Stream) {
	for _, stream := range streams {
		if err := b.logsBuilder.Append(Tenant, stream); err != nil && errors.Is(err, logsobj.ErrBuilderFull) {
			require.NoError(b.t, b.flush(ctx), "failed to flush logs builder")

			// Try appending again after the flush.
			require.NoError(b.t, b.logsBuilder.Append(Tenant, stream), "failed to append stream after flush")
		} else if err != nil {
			require.NoError(b.t, err, "failed to append stream")
		}

		b.dirty = true
	}
}

// flush flushes any pending data in the logs builder and writes a logs
// metastore entry.
// flush 将 dirty 日志对象上传并写入 logs metastore，随后 Reset builder。
func (b *Builder) flush(ctx context.Context) error {
	if !b.dirty {
		// Nothing to do.
		return nil
	}

	timeRanges := b.logsBuilder.TimeRanges()

	obj, closer, err := b.logsBuilder.Flush()
	if err != nil {
		return fmt.Errorf("flushing builder: %w", err)
	}
	defer closer.Close()

	// Upload the logs object.
	path, err := b.uploader.Upload(ctx, obj)
	if err != nil {
		return fmt.Errorf("uploading logs object: %w", err)
	}

	if err := b.logsMetastoreToc.WriteEntry(ctx, path, timeRanges); err != nil {
		return fmt.Errorf("updating metastore: %w", err)
	}

	b.logsBuilder.Reset()
	b.dirty = false
	return nil
}

// Close 先 flush 剩余日志再 buildIndex，完成后数据可通过 Location 读取。
// Close flushes all remaining data and closes the builder.
func (b *Builder) Close() {
	require.NoError(b.t, b.flush(b.t.Context()), "must be able to flush logs builder")
	require.NoError(b.t, b.buildIndex(b.t.Context()), "must be able to close logs builder")
}

// buildIndex 遍历 objects 路径，每 16 个对象批量生成并上传索引对象。
func (b *Builder) buildIndex(ctx context.Context) error {
	var builderConfig logsobj.BuilderConfig
	builderConfig.RegisterFlagsWithPrefix("", flag.NewFlagSet("", flag.PanicOnError)) // Acquire defaults
	indexBuilder, err := indexobj.NewBuilder(builderConfig.BuilderBaseConfig, nil)
	if err != nil {
		return fmt.Errorf("creating logs builder: %w", err)
	}

	calculator := index.NewCalculator(indexBuilder)

	var (
		count           int
		objectsPerIndex = 16
	)
	err = b.bucket.Iter(ctx, "", func(name string) error {
		if !strings.Contains(name, "objects") {
			return nil
		}

		reader, err := dataobj.FromBucket(ctx, b.bucket, name, 0)
		if err != nil {
			return fmt.Errorf("reading object: %w", err)
		}

		if err := calculator.Calculate(ctx, b.logger, reader, name); err != nil {
			return fmt.Errorf("calculating index: %w", err)
		}

		count++
		if count%objectsPerIndex != 0 {
			// Stop early if we haven't accumulated enough objects yet.
			return nil
		}

		if err := b.flushAndUpload(ctx, calculator); err != nil {
			return fmt.Errorf("flushing and uploading index: %w", err)
		}
		return nil
	}, objstore.WithRecursiveIter())
	if err != nil {
		return fmt.Errorf("iterating over objects: %w", err)
	}

	if count%objectsPerIndex != 0 {
		if err := b.flushAndUpload(ctx, calculator); err != nil {
			return fmt.Errorf("failed to flush and upload index: %w", err)
		}
	}
	return nil
}

func (b *Builder) flushAndUpload(ctx context.Context, calculator *index.Calculator) error {
	timeRanges := calculator.TimeRanges()

	obj, closer, err := calculator.Flush()
	if err != nil {
		return fmt.Errorf("failed to flush index: %w", err)
	}
	defer closer.Close()

	key, err := index.ObjectKey(ctx, obj)
	if err != nil {
		return fmt.Errorf("failed to create object key: %w", err)
	}

	reader, err := obj.Reader(ctx)
	if err != nil {
		return fmt.Errorf("failed to create reader for index object: %w", err)
	}
	defer reader.Close()

	if err := b.indexBucket.Upload(ctx, key, reader); err != nil {
		return fmt.Errorf("failed to upload index: %w", err)
	} else if err := b.indexMetastoreToc.WriteEntry(ctx, key, timeRanges); err != nil {
		return fmt.Errorf("failed to update metastore: %w", err)
	}

	calculator.Reset()
	return nil
}

// Location 暴露 bucket 与 index 前缀，供测试客户端直接读取对象。
// Location holds information about where objects for a [Builder] are stored.
// Location can be used to read data from a builder.
type Location struct {
	Bucket      objstore.Bucket // Bucket where all data is stored.
	IndexPrefix string          // Prefix for index objects in the Bucket.
}

// Location returns the location of index for b so they can be read. Data is not
// guaranteed to exist in the location until calling [Builder.Close].
func (b *Builder) Location() Location {
	return Location{
		Bucket:      b.bucket,
		IndexPrefix: "index/v0",
	}
}
// flushAndUpload 计算索引 key、上传至 index/v0 并更新索引 TOC。
