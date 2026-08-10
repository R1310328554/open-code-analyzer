package main

// dataobj-sort 开发工具：对本地 dataobj 文件执行 CopyAndSort，重建 section 顺序并写入临时 sorted 文件，用于调试与性能对比。

import (
	"context"
	"io"
	"log"
	"os"
	"time"

	gokitlog "github.com/go-kit/log"
	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/consumer/logsobj"
	"github.com/grafana/loki/v3/pkg/scratch"
)

// main 10 分钟超时内打开 dataobj，用 logsobj.Builder CopyAndSort 后 io.Copy 落盘。
func main() {
	args := os.Args[1:]
	if len(args) < 1 {
		log.Fatal("requires at least 1 argument: dataobj")
	}

	ctx := context.Background()
	ctx, cancel := context.WithTimeout(ctx, 10*time.Minute)
	defer cancel()

	fp, err := os.Open(args[0])
	if err != nil {
		log.Fatal(err)
	}
	defer fp.Close()

	fi, err := fp.Stat()
	if err != nil {
		log.Fatal(err)
	}

	orig, err := dataobj.FromReaderAt(fp, fi.Size())
	if err != nil {
		log.Fatal(err)
	}

	cfg := logsobj.BuilderConfig{
		BuilderBaseConfig: logsobj.BuilderBaseConfig{
			TargetPageSize:          64 << 10,
			MaxPageRows:             1000,
			TargetObjectSize:        512 << 20,
			TargetSectionSize:       512 << 20,
			BufferSize:              16 << 20,
			SectionStripeMergeLimit: 8,
		},
	}
	scr, err := scratch.NewFilesystem(gokitlog.NewNopLogger(), os.TempDir())
	if err != nil {
		log.Fatal(err)
	}
	b, err := logsobj.NewBuilder(cfg, scr)
	if err != nil {
		log.Fatal(err)
	}

	start := time.Now()
	sortedObj, closer, err := b.CopyAndSort(ctx, orig)
	duration := time.Since(start)
	if err != nil {
		log.Fatal(err)
	}
	defer closer.Close()

	log.Printf("Took %s\n", duration)

	log.Println("== ORIIGNAL DATAOBJ")
	for _, s := range sortedObj.Sections() {
		log.Println(" ", s.Type.String(), s.Tenant)
	}

	log.Println("== SORTED DATAOBJ")
	for _, s := range sortedObj.Sections() {
		log.Println(" ", s.Type.String(), s.Tenant)
	}

	fw, err := os.CreateTemp("", fi.Name()+"-sorted")
	if err != nil {
		log.Fatal(err)
	}
	defer fw.Close()

	reader, err := sortedObj.Reader(ctx)
	if err != nil {
		log.Fatal(err)
	}
	defer reader.Close()

	start = time.Now()
	// Copy the sorted data from reader to the output file
	written, err := io.Copy(fw, reader)
	duration = time.Since(start)
	if err != nil {
		log.Fatal(err)
	}

	log.Printf("Written %d bytes to %s in %s\n", written, fw.Name(), duration)
}
// BuilderConfig 指定页大小、对象/section 目标尺寸与 stripe 合并上限等重建参数。
