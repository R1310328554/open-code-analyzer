//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package service

// think_tag.go 解析流式输出中的 redacted_thinking 推理标签。

import (
	"context"
	"ragflow/internal/tokenizer"
	"strings"
)

const thinkOpen = "<think>"
const thinkClose = "</think>"

var stripThinkReplacer = strings.NewReplacer("<think>", "", "</think>", "")

// ThinkStreamState 跨 chunk 累积状态，用于拆分推理段与可见答案。
// so that <think>...</think> tags can be surfaced as structured markers
type ThinkStreamState struct {
	// fullText accumulates all text received so far.
	fullText string
	// lastModelFull is the previous model-full snapshot for diffing
	lastModelFull string
	// inThink is true when we are currently inside a <think> block.
	inThink bool
	// closePending defers emission of </think> when no visible text follows the tag
	closePending bool
	// pendingAfterClose collects text received after a deferred </think>
	pendingAfterClose string
	// thinkBuffer is the think-buffer
	thinkBuffer string
	// answerBuffer accumulates answer-side text before token-batch flushing
	answerBuffer string
	// carry holds text at the end of a chunk that may be a partial <think> or </think> prefix.
	carry string
	// inReasoning tracks whether the model is currently streaming reasoning chunks
	// (via a dedicated reason field).  Kept separate from inThink, which tracks
	// <think>/</think> tag boundaries parsed from the text stream itself.
	inReasoning bool
}

// EnterReasoning 标记模型级推理流开始（与 XML 标签解析独立）。
// Returns true when this is a new transition (reasoning was not already active).
func (s *ThinkStreamState) EnterReasoning() bool {
	if s.inReasoning {
		return false
	}
	s.inReasoning = true
	return true
}

// ExitReasoning marks the end of a reasoning block (model-level, not tag-based).
// Returns true when this is a new transition (reasoning was active).
func (s *ThinkStreamState) ExitReasoning() bool {
	if !s.inReasoning {
		return false
	}
	s.inReasoning = false
	return true
}

// ThinkDeltaKind 区分文本增量与开闭标签边界事件。
type ThinkDeltaKind int

const (
	ThinkDeltaText   ThinkDeltaKind = iota // think-side or answer-side text
	ThinkDeltaMarker                       // <think> or </think> tag boundary
)

// ThinkDelta is a single event produced by NextThinkDelta.
type ThinkDelta struct {
	Kind  ThinkDeltaKind
	Value string
}

// emitText returns the batched text and its kind.
func emitText(state *ThinkStreamState, section string, text string, minTokens int) (string, ThinkDeltaKind) {
	if text == "" {
		return "", 0
	}
	if section == "think" {
		state.thinkBuffer += text
		if tokenizer.NumTokensFromString(state.thinkBuffer) >= minTokens {
			out := state.thinkBuffer
			state.thinkBuffer = ""
			return out, ThinkDeltaText
		}
		return "", 0
	}
	state.answerBuffer += text
	if tokenizer.NumTokensFromString(state.answerBuffer) >= minTokens {
		out := state.answerBuffer
		state.answerBuffer = ""
		return out, ThinkDeltaText
	}
	return "", 0
}

func flushThinkBufferInternal(state *ThinkStreamState) ThinkDelta {
	if state.thinkBuffer == "" {
		return ThinkDelta{}
	}
	out := state.thinkBuffer
	state.thinkBuffer = ""
	return ThinkDelta{Kind: ThinkDeltaText, Value: out}
}

func flushAnswerBufferInternal(state *ThinkStreamState) ThinkDelta {
	if state.answerBuffer == "" {
		return ThinkDelta{}
	}
	out := state.answerBuffer
	state.answerBuffer = ""
	return ThinkDelta{Kind: ThinkDeltaText, Value: out}
}

func stripThinkTags(s string) string {
	if s == "" {
		return ""
	}
	return stripThinkReplacer.Replace(s)
}

// tagPrefixLen returns the length of the longest suffix of s that could be a
// PARTIAL start of "<think>" or "</think>". Returns 0 if the suffix is a complete
// tag or no prefix match exists.
func tagPrefixLen(s string) int {
	for i := 0; i < len(s); i++ {
		sub := s[i:]
		if sub == thinkOpen || sub == thinkClose {
			return 0 // complete tag, not a partial prefix
		}
		if strings.HasPrefix(thinkOpen, sub) || strings.HasPrefix(thinkClose, sub) {
			return len(sub)
		}
	}
	return 0
}

// NextThinkDelta 增量解析 chunk，输出可见文本或 think 开闭标记。
// visible text or tag boundary markers that should be emitted.
func NextThinkDelta(state *ThinkStreamState, chunk string, minTokens int) []ThinkDelta {
	if state == nil {
		return nil
	}

	var newPart string
	if strings.HasPrefix(chunk, state.lastModelFull) {
		newPart = chunk[len(state.lastModelFull):]
		state.lastModelFull = chunk
	} else {
		newPart = chunk
		state.lastModelFull += chunk
	}
	if newPart == "" {
		return nil
	}
	state.fullText += newPart

	// Prepend carry from previous chunk that may complete a partial tag.
	pending := state.carry + newPart
	state.carry = ""

	// Check if pending ends with a partial <think> or </think> prefix.
	// Save it as carry so it isn't emitted as visible text.
	if n := tagPrefixLen(pending); n > 0 {
		state.carry = pending[len(pending)-n:]
		pending = pending[:len(pending)-n]
	}

	var deltas []ThinkDelta

	// Phase 1: handle deferred </think> from a previous chunk.
	if state.closePending {
		state.closePending = false
		if piece := flushThinkBufferInternal(state); piece.Value != "" {
			deltas = append(deltas, piece)
		}
		state.inThink = false
		deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaMarker, Value: thinkClose})
		if state.pendingAfterClose != "" {
			pending = state.pendingAfterClose + pending
			state.pendingAfterClose = ""
		}
	}

	// Phase 2: process pending text for think tags.
	for pending != "" {
		openIdx := strings.Index(pending, thinkOpen)
		closeIdx := strings.Index(pending, thinkClose)

		// No tags remaining — emit to the appropriate section.
		if openIdx == -1 && closeIdx == -1 {
			if piece := stripThinkTags(pending); piece != "" {
				section := "answer"
				if state.inThink {
					section = "think"
				}
				if out, kind := emitText(state, section, piece, minTokens); out != "" {
					deltas = append(deltas, ThinkDelta{Kind: kind, Value: out})
				}
			}
			break
		}

		// <think> appears first (or no </think> found).
		if openIdx != -1 && (closeIdx == -1 || openIdx < closeIdx) {
			before := pending[:openIdx]
			if before != "" {
				piece := stripThinkTags(before)
				section := "answer"
				if state.inThink {
					section = "think"
				}
				if out, kind := emitText(state, section, piece, minTokens); out != "" {
					deltas = append(deltas, ThinkDelta{Kind: kind, Value: out})
				}
			}
			pending = pending[openIdx+len(thinkOpen):]
			if !state.inThink {
				if answerPiece := flushAnswerBufferInternal(state); answerPiece.Value != "" {
					deltas = append(deltas, answerPiece)
				}
				if thinkPiece := flushThinkBufferInternal(state); thinkPiece.Value != "" {
					deltas = append(deltas, thinkPiece)
				}
				state.inThink = true
				deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaMarker, Value: thinkOpen})
			}
			continue
		}

		// </think> appears first.
		before := pending[:closeIdx]
		after := pending[closeIdx+len(thinkClose):]
		if before != "" {
			piece := stripThinkTags(before)
			section := "answer"
			if state.inThink {
				section = "think"
			}
			if out, kind := emitText(state, section, piece, minTokens); out != "" {
				deltas = append(deltas, ThinkDelta{Kind: kind, Value: out})
			}
		}
		if strings.TrimSpace(after) != "" {
			if thinkPiece := flushThinkBufferInternal(state); thinkPiece.Value != "" {
				deltas = append(deltas, thinkPiece)
			}
			state.inThink = false
			deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaMarker, Value: thinkClose})
			pending = after
			continue
		}
		// No visible text after close — defer the marker.
		state.closePending = true
		if after != "" {
			state.pendingAfterClose += after
		}
		pending = ""
		break
	}

	return deltas
}

// FlushRemaining 流结束后刷出缓冲区与延迟的闭合标签。
// markers. Call this after all LLM chunks have been processed.
func FlushRemaining(state *ThinkStreamState) []ThinkDelta {
	if state == nil {
		return nil
	}
	var deltas []ThinkDelta
	if state.thinkBuffer != "" {
		deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaText, Value: state.thinkBuffer})
		state.thinkBuffer = ""
	}
	if state.closePending {
		state.inThink = false
		state.closePending = false
		deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaMarker, Value: thinkClose})
	}
	if state.answerBuffer != "" {
		deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaText, Value: state.answerBuffer})
		state.answerBuffer = ""
	}
	if state.pendingAfterClose != "" {
		deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaText, Value: state.pendingAfterClose})
		state.pendingAfterClose = ""
	}
	if state.carry != "" {
		deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaText, Value: state.carry})
		state.carry = ""
	}
	return deltas
}

// StreamThinkTagDelta — channel-based pipeline.
// ---------------------------------------------------------------------------

// StreamThinkTagDelta goroutine 将原始 chunk 通道转为 ThinkDelta 通道。
// channel of structured deltas.  When ctx is cancelled (e.g. client
// disconnect), the goroutine drains the input channel silently and exits.
func StreamThinkTagDelta(ctx context.Context, chunks <-chan string, minTokens int) <-chan ThinkDelta {
	out := make(chan ThinkDelta, 32)
	go func() {
		defer close(out)
		state := &ThinkStreamState{}
		for {
			select {
			case <-ctx.Done():
				go func() {
					for range chunks {
					}
				}()
				return
			case chunk, ok := <-chunks:
				if !ok {
					for _, d := range FlushRemaining(state) {
						select {
						case out <- d:
						case <-ctx.Done():
							return
						}
					}
					return
				}
				deltas := NextThinkDelta(state, chunk, minTokens)
				for _, d := range deltas {
					select {
					case out <- d:
					case <-ctx.Done():
						go func() {
							for range chunks {
							}
						}()
						return
					}
				}
			}
		}
	}()
	return out
}

// ExtractVisibleAnswer 取最后一个闭合标签之后的可见答案并剥离残留标签。
// Stray <think>/</think> tags are stripped. If there is no </think>, all tags are stripped.
func ExtractVisibleAnswer(raw string) string {
	if raw == "" {
		return ""
	}
	if !strings.Contains(raw, thinkClose) {
		return stripThinkTags(raw)
	}

	lastClose := strings.LastIndex(raw, thinkClose)
	answer := raw[lastClose+len(thinkClose):]
	return stripThinkTags(answer)
}

// BufferAnswerDelta 在 closePending 等状态下将答案增量写入 answer 缓冲。
// When closePending is true, it first flushes the deferred think buffer and </think>
// marker, then processes pendingAfterClose + the new answer text.
func BufferAnswerDelta(state *ThinkStreamState, text string, minTokens int) []ThinkDelta {
	if state == nil || text == "" {
		return nil
	}
	state.fullText += text

	var deltas []ThinkDelta
	if state.closePending {
		state.closePending = false
		if piece := flushThinkBufferInternal(state); piece.Value != "" {
			deltas = append(deltas, piece)
		}
		state.inThink = false
		deltas = append(deltas, ThinkDelta{Kind: ThinkDeltaMarker, Value: thinkClose})
		if state.pendingAfterClose != "" {
			text = state.pendingAfterClose + text
			state.pendingAfterClose = ""
		}
	}

	if out, kind := emitText(state, "answer", text, minTokens); out != "" {
		deltas = append(deltas, ThinkDelta{Kind: kind, Value: out})
	}
	return deltas
}
// think_tag.go — 流式解析 redacted_thinking 标签，拆分推理与可见答案文本。
