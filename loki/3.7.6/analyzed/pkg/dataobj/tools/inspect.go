package tools

// inspect 提供 data object 命令行检查工具，打印 streams/logs 区段列统计摘要。

import (
	"context"
	"fmt"
	"io"
	"log"

	"github.com/dustin/go-humanize"

	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/logs"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/streams"
)

// Inspect 打开 data object 并遍历各区段，识别 streams/logs 后打印列级信息。
func Inspect(r io.ReaderAt, size int64) {
	obj, err := dataobj.FromReaderAt(r, size)
	if err != nil {
		log.Printf("failed to open object: %v", err)
		return
	}

	for _, section := range obj.Sections() {
		switch {
		case streams.CheckSection(section):
			streamsSection, err := streams.Open(context.Background(), section)
			if err != nil {
				log.Printf("failed to open streams section: %v", err)
				continue
			}
			printStreamInfo(streamsSection)

		case logs.CheckSection(section):
			logsSection, err := logs.Open(context.Background(), section)
			if err != nil {
				log.Printf("failed to open streams section: %v", err)
				continue
			}
			printLogsInfo(logsSection)
		}
	}
}

// printStreamInfo 调用 ReadStats 并以人类可读格式输出 streams 列压缩统计。
func printStreamInfo(sec *streams.Section) {
	fmt.Println("---- Streams Section ----")
	stats, err := streams.ReadStats(context.Background(), sec)
	if err != nil {
		log.Printf("failed to read columns for streams section: %v", err)
		return
	}
	for _, col := range stats.Columns {
		fmt.Printf("%v[%v]; %d populated rows; %v compressed (%v); %v uncompressed\n", col.Type[12:], col.Name, col.ValuesCount, humanize.Bytes(col.CompressedSize), col.Compression[17:], humanize.Bytes(col.UncompressedSize))
	}
	fmt.Println("")
	fmt.Printf("Streams Section Summary: %d columns; compressed size: %v; uncompressed size %v\n", len(stats.Columns), humanize.Bytes(stats.CompressedSize), humanize.Bytes(stats.UncompressedSize))
	fmt.Println("")
}

// printLogsInfo 调用 logs.ReadStats 并输出 logs 区段列压缩统计摘要。
func printLogsInfo(sec *logs.Section) {
	fmt.Println("---- Logs Section ----")
	stats, err := logs.ReadStats(context.Background(), sec)
	if err != nil {
		log.Printf("failed to read columns for streams section: %v", err)
		return
	}
	for _, col := range stats.Columns {
		fmt.Printf("%v[%v]; %d populated rows; %v compressed (%v); %v uncompressed\n", col.Type[12:], col.Name, col.ValuesCount, humanize.Bytes(col.CompressedSize), col.Compression[17:], humanize.Bytes(col.UncompressedSize))
	}
	fmt.Println("")
	fmt.Printf("Logs Section Summary: %d columns; compressed size: %v; uncompressed size %v\n", len(stats.Columns), humanize.Bytes(stats.CompressedSize), humanize.Bytes(stats.UncompressedSize))
	fmt.Println("")
}
// Inspect 面向运维调试，通过 humanize 格式化字节数与压缩算法名称。
