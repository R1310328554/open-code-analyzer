package storage

// util 提供索引下载辅助：sync.Pool 复用 gzip 解压器，DownloadFileFromStorage 先落临时文件再可选解压到目标路径。

import (
	"io"
	"os"
	"strings"
	"sync"
	"time"

	"github.com/dustin/go-humanize"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	gzip "github.com/klauspost/pgzip"
)

var (
	gzipReader = sync.Pool{}
)

// getGzipReader 从池中取出 pgzip.Reader 并重置源，减少高频下载时的分配开销。
// getGzipReader gets or creates a new CompressionReader and reset it to read from src
func getGzipReader(src io.Reader) (io.Reader, error) {
	if r := gzipReader.Get(); r != nil {
		reader := r.(*gzip.Reader)
		err := reader.Reset(src)
		if err != nil {
			return nil, err
		}
		return reader, nil
	}
	reader, err := gzip.NewReader(src)
	if err != nil {
		return nil, err
	}
	return reader, nil
}

// putGzipReader places back in the pool a CompressionReader
func putGzipReader(reader io.Reader) {
	gzipReader.Put(reader)
}

type GetFileFunc func() (io.ReadCloser, error)

// DownloadFileFromStorage 经 getFileFunc 拉流、写 -tmp 文件，可选 gzip 解压后落盘。
// DownloadFileFromStorage downloads a file from storage to given location.
func DownloadFileFromStorage(destination string, decompressFile bool, sync bool, logger log.Logger, getFileFunc GetFileFunc) error {
	start := time.Now()
	readCloser, err := getFileFunc()
	if err != nil {
		return err
	}

	defer func() {
		if err := readCloser.Close(); err != nil {
			level.Error(logger).Log("msg", "failed to close read closer", "err", err)
		}
	}()

	tmpName := destination + "-tmp"

	ftmp, err := os.Create(tmpName)
	if err != nil {
		return err
	}
	defer func() {
		if err := ftmp.Close(); err != nil {
			level.Warn(logger).Log("msg", "failed to close file", "file", tmpName)
		}
		if err := os.Remove(tmpName); err != nil {
			level.Warn(logger).Log("msg", "failed to delete temp file from index download", "err", err)
		}
	}()

	_, err = io.Copy(ftmp, readCloser)
	if err != nil {
		return err
	}

	dlTime := time.Since(start)
	level.Info(logger).Log("msg", "downloaded file", "total_time", dlTime)
	start = time.Now()

	tmpReader, err := os.Open(tmpName)
	if err != nil {
		return err
	}
	defer func() {
		if err := tmpReader.Close(); err != nil {
			level.Warn(logger).Log("msg", "failed to close file", "file", tmpName)
		}
	}()

	f, err := os.Create(destination)
	if err != nil {
		return err
	}

	defer func() {
		if err := f.Close(); err != nil {
			level.Warn(logger).Log("msg", "failed to close file", "file", destination)
		}
	}()
	var objectReader io.Reader = tmpReader
	if decompressFile {
		decompressedReader, err := getGzipReader(tmpReader)
		if err != nil {
			return err
		}
		defer putGzipReader(decompressedReader)

		objectReader = decompressedReader
	}

	_, err = io.Copy(f, objectReader)
	if err != nil {
		return err
	}

	fStat, err := f.Stat()
	if err != nil {
		level.Error(logger).Log("msg", "failed to get stat for downloaded file", "err", err)
	}

	if err == nil {
		logger = log.With(logger, "size", humanize.Bytes(uint64(fStat.Size())))
	}
	level.Info(logger).Log("msg", "downloaded and extracted file", "download time", dlTime, "extract time", time.Since(start))

	if sync {
		return f.Sync()
	}
	return nil
}

// IsCompressedFile 通过后缀 .gz 判断对象存储索引是否需解压。
func IsCompressedFile(filename string) bool {
	return strings.HasSuffix(filename, ".gz")
}

func LoggerWithFilename(logger log.Logger, filename string) log.Logger {
	return log.With(logger, "file-name", filename)
}
// LoggerWithFilename 为下载日志附加 file-name 字段，便于 shipper sync 排查。
