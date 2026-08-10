// logprob 转换：将 llm 层 Token 对数概率映射为 API 响应格式。
package server

import (
	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/llm"
)

// toAPILogprobs 将 llm.Logprob 切片转为 api.Logprob（含 TopLogprobs）。
// toAPILogprobs converts llm.Logprobs to api.Logprobs
func toAPILogprobs(logprobs []llm.Logprob) []api.Logprob {
	result := make([]api.Logprob, len(logprobs))
	for i, lp := range logprobs {
		result[i] = api.Logprob{
			TokenLogprob: api.TokenLogprob{
				Token:   lp.Token,
				Bytes:   stringToByteInts(lp.Token),
				Logprob: lp.Logprob,
			},
		}
		if len(lp.TopLogprobs) > 0 {
			result[i].TopLogprobs = make([]api.TokenLogprob, len(lp.TopLogprobs))
			for j, tlp := range lp.TopLogprobs {
				result[i].TopLogprobs[j] = api.TokenLogprob{
					Token:   tlp.Token,
					Bytes:   stringToByteInts(tlp.Token),
					Logprob: tlp.Logprob,
				}
			}
		}
	}
	return result
}

// stringToByteInts 将 token 字节序列化为 OpenAI 风格的 bytes 整数数组。
func stringToByteInts(s string) []int {
	if s == "" {
		return nil
	}

	raw := []byte(s)
	ints := make([]int, len(raw))
	for i, b := range raw {
		ints[i] = int(b)
	}
	return ints
}
