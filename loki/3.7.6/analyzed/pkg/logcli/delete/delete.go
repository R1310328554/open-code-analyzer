package delete

// delete 包封装 logcli 日志删除子命令：创建、列出、取消删除请求。

import (
	"encoding/json"
	"fmt"
	"os"
	"strconv"
	"time"

	"github.com/grafana/loki/v3/pkg/logcli/client"
)

// Query 聚合 LogQL 查询串、时间范围、quiet 模式与 requestID 等 CLI 参数。
// Query contains all necessary fields to execute delete requests
type Query struct {
	QueryString string
	Start       time.Time
	End         time.Time
	MaxInterval string
	Quiet       bool
	RequestID   string
	Force       bool
}

// CreateQuery 将时间转为 Unix 字符串后调用 client.CreateDeleteRequest。
// CreateQuery executes a delete request creation
func (q *Query) CreateQuery(c client.Client) error {
	params := client.DeleteRequestParams{
		Query: q.QueryString,
	}

	// Convert times to Unix timestamps as strings
	if !q.Start.IsZero() {
		params.Start = strconv.FormatInt(q.Start.Unix(), 10)
	}
	if !q.End.IsZero() {
		params.End = strconv.FormatInt(q.End.Unix(), 10)
	}
	if q.MaxInterval != "" {
		params.MaxInterval = q.MaxInterval
	}

	err := c.CreateDeleteRequest(params, q.Quiet)
	if err != nil {
		return err
	}

	if !q.Quiet {
		fmt.Println("Delete request created successfully")
	}
	return nil
}

// ListQuery 拉取全部删除请求并以可读格式打印到 stdout。
// ListQuery executes a delete request listing
func (q *Query) ListQuery(client client.Client) error {
	deleteRequests, err := client.ListDeleteRequests(q.Quiet)
	if err != nil {
		return err
	}

	if !q.Quiet {
		fmt.Printf("Found %d delete requests:\n", len(deleteRequests))
	}

	// Pretty print the results
	for _, req := range deleteRequests {
		q.printDeleteRequest(req)
	}

	return nil
}

// CancelQuery 按 requestID 取消删除；force 为 true 时强制取消进行中的请求。
// CancelQuery executes a delete request cancellation
func (q *Query) CancelQuery(client client.Client, requestID string, force bool) error {
	err := client.CancelDeleteRequest(requestID, force, q.Quiet)
	if err != nil {
		return err
	}

	if !q.Quiet {
		fmt.Printf("Delete request %s cancelled successfully\n", requestID)
	}
	return nil
}

// printDeleteRequest prints a single delete request in a formatted way
func (q *Query) printDeleteRequest(req client.DeleteRequest) {
	fmt.Printf("Query: %s\n", req.Query)
	fmt.Printf("Start Time: %s\n", time.Unix(req.StartTime, 0).Format(time.RFC3339))
	fmt.Printf("End Time: %s\n", time.Unix(req.EndTime, 0).Format(time.RFC3339))
	fmt.Printf("Status: %s\n", req.Status)
	fmt.Println("---")
}

// ListQueryJSON 以缩进 JSON 格式输出删除请求列表，便于脚本解析。
// ListQueryJSON executes a delete request listing with JSON output
func (q *Query) ListQueryJSON(client client.Client) error {
	deleteRequests, err := client.ListDeleteRequests(q.Quiet)
	if err != nil {
		return err
	}

	encoder := json.NewEncoder(os.Stdout)
	encoder.SetIndent("", "  ")
	return encoder.Encode(deleteRequests)
}
// printDeleteRequest 将 start/end 时间格式化为 RFC3339 便于人工审阅。
