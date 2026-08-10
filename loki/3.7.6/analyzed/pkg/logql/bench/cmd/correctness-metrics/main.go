package main

// correctness-metrics 命令行工具：解析 gotestsum 输出的 JUnit XML，
// 将 LogQL 远程存储一致性测试结果转换为 Prometheus 指标并 remote_write 推送。

import (
	"flag"
	"fmt"
	"log"
	"net/url"
	"os"
)

func main() {
	os.Exit(run())
}

func run() int {
	input := flag.String("input", "", "Path to JUnit XML file from gotestsum (required)")
	remoteWriteURL := flag.String("remote-write-url", envOrDefault("REMOTE_WRITE_URL", ""), "Prometheus remote_write endpoint")
	remoteWriteUsername := flag.String("remote-write-username", envOrDefault("REMOTE_WRITE_USERNAME", ""), "Basic auth username")
	remoteWritePassword := flag.String("remote-write-password", envOrDefault("REMOTE_WRITE_PASSWORD", ""), "Basic auth password")
	rangeType := flag.String("range-type", envOrDefault("RANGE_TYPE", ""), "Query range type: instant or range (required)")
	job := flag.String("job", "logql-correctness", "Job label for all metrics")
	dryRun := flag.Bool("dry-run", false, "Parse and log metrics without pushing")
	flag.Parse()

	if *input == "" {
		log.Println("ERROR: --input is required")
		flag.Usage()
		return 1
	}

	if *rangeType != "instant" && *rangeType != "range" {
		log.Printf("ERROR: --range-type must be 'instant' or 'range', got %q", *rangeType)
		return 1
	}

	if !*dryRun && *remoteWriteURL == "" {
		log.Println("ERROR: --remote-write-url is required (or use --dry-run)")
		flag.Usage()
		return 1
	}

	if (*remoteWriteUsername == "") != (*remoteWritePassword == "") {
		log.Println("ERROR: --remote-write-username and --remote-write-password must both be provided or both omitted")
		return 1
	}

	data, err := os.ReadFile(*input)
	if err != nil {
		log.Printf("ERROR: failed to read input file %q: %v", *input, err)
		return 1
	}

	results, err := parseJUnitXML(data)
	if err != nil {
		log.Printf("ERROR: failed to parse JUnit XML: %v", err)
		return 1
	}

	series := buildMetrics(results, *rangeType, *job)

	counts := map[testStatus]int{}
	for _, t := range results.Tests {
		counts[t.Status]++
	}
	summary := fmt.Sprintf("%d pass, %d fail, %d error, %d skip",
		counts[statusPass], counts[statusFail], counts[statusError], counts[statusSkip])

	if *dryRun {
		log.Printf("DRY RUN: would push %d time series (%s)", len(series), summary)
		for _, s := range series {
			var name string
			for _, l := range s.Labels {
				if l.Name == "__name__" {
					name = l.Value
					break
				}
			}
			log.Printf("  %s = %.4f", name, s.Samples[0].Value)
		}
		return 0
	}

	if err := pushMetrics(*remoteWriteURL, *remoteWriteUsername, *remoteWritePassword, series); err != nil {
		log.Printf("ERROR: failed to push metrics: %v", err)
		return 1
	}

	log.Printf("Pushed %d metrics (%s) to %s", len(series), summary, redactURL(*remoteWriteURL))
	return 0
}

// redactURL 去除 URL 中的用户凭证，避免日志泄露认证信息。
// redactURL strips any userinfo from a URL to prevent credential leakage in logs.
func redactURL(rawURL string) string {
	u, err := url.Parse(rawURL)
	if err != nil {
		return rawURL
	}
	u.User = nil
	return u.String()
}

// envOrDefault 优先读取环境变量，未设置时返回默认值。
func envOrDefault(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
// 支持 instant/range 两种查询模式，按 suite 聚合通过率与耗时指标。
