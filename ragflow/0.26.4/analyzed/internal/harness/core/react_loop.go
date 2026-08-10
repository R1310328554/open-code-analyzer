package core

// react_loop.go — ReAct 经典 for 循环执行路径：迭代调用模型、ToolsNode 执行 tool calls，含 Resume 历史修改。


import (
	"context"
	"fmt"

	"ragflow/internal/harness/core/schema"
)

// ---- ReAct Run 函数（for 循环）----

func (a *ReActAgent[M]) buildReActRunFunc() typedRunFunc[M] {
	return func(ctx context.Context, p *typedRunParams[M]) {
		maxIter := a.config.MaxIterations
		if maxIter <= 0 {
			maxIter = 10
		}

		var state *TypedReActAgentState[M]
		if p.interruptState != nil {
			state = p.interruptState
		} else {
			state = NewReActAgentState(p.input.Messages, a.exeCtx.toolInfos, maxIter)
		}

		// 深拷贝输入消息，防止中间件副作用污染 state
		if len(state.Messages) > 0 {
			copied := make([]M, len(state.Messages))
			for i, m := range state.Messages {
				copied[i] = copyMessage(m)
			}
			state.Messages = copied
		}

		// Resume 时应用 historyModifier 改写历史
		if p.historyModifier != nil && len(state.Messages) > 0 {
			switch any(state.Messages[0]).(type) {
			case *schema.Message:
				// 仅收集类型断言成功的消息，避免零值空洞
				var msgs []Message
				for _, m := range state.Messages {
					if msg, ok := any(m).(Message); ok {
						msgs = append(msgs, msg)
					}
				}
				modified := p.historyModifier(ctx, msgs)
				var stateMsgs []M
				for _, m := range modified {
					if msg, ok := any(m).(M); ok {
						stateMsgs = append(stateMsgs, msg)
					}
				}
				state.Messages = stateMsgs
			}
		}

		// 合并 config.Tools、ToolsConfig.Tools 与 contribTools
		tcTools := 0
		if a.config.ToolsConfig != nil {
			tcTools = len(a.config.ToolsConfig.Tools)
		}
		allTools := make([]Tool, 0, len(a.config.Tools)+tcTools+len(a.exeCtx.contribTools))
		allTools = append(allTools, a.config.Tools...)
		if a.config.ToolsConfig != nil {
			allTools = append(allTools, a.config.ToolsConfig.Tools...)
		}
		allTools = append(allTools, a.exeCtx.contribTools...)

		// 合并 ReturnDirectly 映射
		allRD := make(map[string]bool, len(a.exeCtx.returnDirectly)+len(a.exeCtx.contribReturnDirectly))
		for k, v := range a.exeCtx.returnDirectly {
			allRD[k] = v
		}
		for k, v := range a.exeCtx.contribReturnDirectly {
			allRD[k] = v
		}

		// BeforeAgent 中间件链
		rc := &ReActAgentContext{Instruction: a.exeCtx.instruction, Tools: allTools, ReturnDirectly: allRD, ToolSearchTool: a.exeCtx.toolSearchTool}
		if err := a.runBeforeAgent(&ctx, rc, p.generator); err != nil {
			return
		}
		// 捕获 BeforeAgent 对 Tools/ReturnDirectly 的运行时修改
		allTools = rc.Tools
		allRD = rc.ReturnDirectly

		model := BuildModelWrapperChain(a.config.Model, nil, a.config, a.exeCtx.toolInfos)

		// 从合并工具构建 ToolsNode，保留 ToolInvokeMiddlewares
		var tn *ToolsNode[M]
		if len(allTools) > 0 {
			tnCfg := &ToolsNodeConfig{
				Tools:            allTools,
				ReturnDirectly:   allRD,
				LoopGuard:        nil,
				ArgumentsAliases: nil,
			}
			if a.config.ToolsConfig != nil {
				tnCfg.ToolInvokeMiddlewares = a.config.ToolsConfig.ToolInvokeMiddlewares
				tnCfg.Registry = a.config.ToolsConfig.Registry
				tnCfg.EmitInternalEvents = a.config.ToolsConfig.EmitInternalEvents
				tnCfg.LoopGuard = a.config.ToolsConfig.LoopGuard
				tnCfg.UnknownToolHandler = a.config.ToolsConfig.UnknownToolHandler
				tnCfg.ArgumentsAliases = a.config.ToolsConfig.ArgumentsAliases
			}
			tn = NewToolsNode[M](tnCfg)
		}

		for state.RemainingIterations > 0 {
			state.RemainingIterations--

			mc := &TypedModelContext[M]{Tools: state.ToolInfos, DeferredToolInfos: state.DeferredToolInfos, ModelRetryConfig: a.config.RetryConfig, ModelFailoverConfig: a.config.FailoverConfig}
			if err := a.runBeforeModelRewrite(&ctx, &state, mc, p.generator); err != nil {
				return
			}

			var modelMsgs []M
			if a.config.StateModifier != nil {
				var err error
				state, err = a.config.StateModifier(ctx, state)
				if err != nil {
					p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("StateModifier: %w", err)})
					return
				}
			}

			if a.config.GenModelInput != nil {
				var err error
				modelMsgs, err = a.config.GenModelInput(ctx, rc.Instruction, &TypedAgentInput[M]{Messages: state.Messages})
				if err != nil {
					p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("GenModelInput: %w", err)})
					return
				}
			} else {
				modelMsgs = buildModelInputFromState(state.Messages, rc.Instruction)
			}

			resp, err := model.Generate(ctx, modelMsgs)
			if err != nil {
				p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("model: %w", err)})
				return
			}
			p.generator.Send(typedModelOutputEvent(resp, nil))
			state.Messages = append(state.Messages, resp)

			if err := a.runAfterModelRewrite(&ctx, &state, mc, p.generator); err != nil {
				return
			}

			toolCalls := extractToolCalls(resp)
			if len(toolCalls) == 0 || tn == nil {
				break
			}

			var action *AgentAction
			results, act, err := tn.Execute(ctx, resp, state, nil)
			if err != nil {
				p.generator.Send(&TypedAgentEvent[M]{Err: err})
				return
			}
			for _, tr := range results {
				state.Messages = append(state.Messages, tr)
			}
			action = act
			if action != nil && action.Exit {
				break
			}
		}

		if state.RemainingIterations <= 0 {
			p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("exceeded max iterations (%d)", a.config.MaxIterations)})
		}
		if a.config.OutputKey != "" && len(state.Messages) > 0 {
			if last := state.Messages[len(state.Messages)-1]; !isNilMessage(last) {
				setOutputToSession(ctx, last, a.config.OutputKey)
			}
		}
		if p.afterToolCallsHook != nil {
			if err := p.afterToolCallsHook(ctx); err != nil {
				p.generator.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("after_tool_calls_hook: %w", err)})
			}
		}
		a.runAfterAgent(&ctx, state, p.generator)
	}
}

// 迭代耗尽时发送 exceeded max iterations 错误；OutputKey 与 AfterAgent 在循环结束后处理。
