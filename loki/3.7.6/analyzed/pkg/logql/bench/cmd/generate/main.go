package main

// generate 命令：在指定目录生成指定字节大小的 Loki 基准测试数据集，
// 同时写入 chunk store 与 dataobj store。

import (
	"context"
	"flag"
	"fmt"
	"os"

	"github.com/grafana/loki/v3/pkg/logql/bench"
)

func main() {
	var (
		size        = flag.Int64("size", 2147483648, "Size in bytes to generate")
		dir         = flag.String("dir", "data", "Output directory")
		tenantID    = flag.String("tenant", "test-tenant", "Tenant ID")
		clearFolder = flag.Bool("clear", true, "Clear output directory before generating data")
	)
	flag.Parse()

// 若指定 --clear，生成前先删除输出目录以保证干净环境。
	// Clean the output directory if requested
	if *clearFolder {
		if err := os.RemoveAll(*dir); err != nil {
			fmt.Fprintf(os.Stderr, "Failed to clear output directory: %v\n", err)
			os.Exit(1)
		}
	}

// 创建 ChunkStore 与 DataObjStore，分别承载 chunk 与 data object 布局。
	// Create stores
	chunkStore, err := bench.NewChunkStore(*dir, *tenantID)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to create chunk store: %v\n", err)
		os.Exit(1)
	}
	dataObjStore, err := bench.NewDataObjStore(*dir, *tenantID)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to create dataobj store: %v\n", err)
		os.Exit(1)
	}

	// Create builder with default options and the store
	builder := bench.NewBuilder(*dir, bench.DefaultOpt(), chunkStore, dataObjStore)

	// Generate the data
	ctx := context.Background()
	if err := builder.Generate(ctx, *size); err != nil {
		fmt.Fprintf(os.Stderr, "Failed to generate dataset: %v\n", err)
		os.Exit(1)
	}
}
// 默认生成约 2GiB 数据，租户 ID 可通过 --tenant 指定。
