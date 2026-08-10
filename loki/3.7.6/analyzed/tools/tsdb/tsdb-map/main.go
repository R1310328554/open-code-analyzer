package main

// tsdb-map 开发工具：读取 boltdb-shipper 索引文件，迭代 series 与 chunk 元数据并写入本地 TSDB builder 输出目录。

import (
	"bytes"
	"context"
	"flag"
	"log"
	"strconv"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"
	"go.etcd.io/bbolt"
	"gopkg.in/yaml.v2"

	"github.com/grafana/loki/v3/pkg/compactor/retention"
	"github.com/grafana/loki/v3/pkg/storage/config"
	boltdbcompactor "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/boltdb/compactor"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/index"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/util"
)

var (
	source = flag.String("source", "", "the source boltdb file")
	dest   = flag.String("dest", "", "the dest tsdb dir")
	// periodConfig 硬编码 schema 供 boltdb ForEachSeries 解析 chunk 时间范围。
// Hardcode a periodconfig for convenience as the boltdb iterator needs one
	// NB: must match the index file you're reading from
	periodConfig = func() config.PeriodConfig {
		input := `
from: "2022-01-01"
index:
  period: 24h
  prefix: loki_index_
object_store: gcs
schema: v13
store: boltdb-shipper
`
		var cfg config.PeriodConfig
		if err := yaml.Unmarshal([]byte(input), &cfg); err != nil {
			panic(err)
		}
		return cfg
	}()
)

func extractChecksumFromChunkID(b []byte) uint32 {
	i := bytes.LastIndexByte(b, ':')
	x, err := strconv.ParseUint(string(b[i+1:]), 16, 32)
	if err != nil {
		panic(err)
	}
	return uint32(x)
}

// main 要求 -source boltdb 与 -dest 目录，SafeOpenBoltdbFile 只读遍历 index bucket。
func main() {
	flag.Parse()

	if source == nil || *source == "" {
		panic("source is required")
	}

	if dest == nil || *dest == "" {
		panic("dest is required")
	}

	db, err := util.SafeOpenBoltdbFile(*source)
	if err != nil {
		panic(err)
	}

	indexFormat, err := periodConfig.TSDBFormat()
	if err != nil {
		panic(err)
	}

	builder := tsdb.NewBuilder(indexFormat)

	log.Println("Loading index into memory")

	// loads everything into memory.
	if err := db.View(func(t *bbolt.Tx) error {
		return boltdbcompactor.ForEachSeries(context.Background(), t.Bucket([]byte("index")), periodConfig, func(s retention.Series) error {
			chunkMetas := make([]index.ChunkMeta, 0, len(s.Chunks()))
			for _, chunk := range s.Chunks() {
				chunkMetas = append(chunkMetas, index.ChunkMeta{
					Checksum: extractChecksumFromChunkID([]byte(chunk.ChunkID)),
					MinTime:  int64(chunk.From),
					MaxTime:  int64(chunk.Through),
					KB:       ((3 << 20) / 4) / 1024, // guess: 0.75mb, 1/2 of the max size, rounded to KB
					Entries:  10000,                  // guess: 10k entries
				})
			}
			// AddSeries 使用 StableHash fingerprint；chunk KB/Entries 为估算占位值。
builder.AddSeries(s.Labels(), model.Fingerprint(labels.StableHash(s.Labels())), chunkMetas)
			return nil
		})
	}); err != nil {
		panic(err)
	}

	log.Println("writing index")
	if _, err := builder.Build(context.Background(), *dest, func(_, _ model.Time, _ uint32) tsdb.Identifier {
		panic("todo")
	}); err != nil {
		panic(err)
	}
}
// Build 阶段 identifier 回调当前 panic(todo)，工具仅用于离线索引格式转换实验。
