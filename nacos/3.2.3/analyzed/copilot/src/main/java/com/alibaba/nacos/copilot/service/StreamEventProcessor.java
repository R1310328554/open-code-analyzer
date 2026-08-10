/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.copilot.service;

import com.alibaba.nacos.copilot.adapter.StreamResponseCallback;
import com.alibaba.nacos.copilot.model.StreamResponseType;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.ThinkingBlock;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 统一流式事件处理器：解析 AgentScope 流式事件，提取思考/工具/正文内容并驱动回调。
 * Unified stream event processor for handling AgentScope stream events.
 * Provides common logic for processing events and extracting content.
 *
 * @author nacos
 */
public class StreamEventProcessor {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamEventProcessor.class);
    
    /**
     * 从 {@link Msg} 提取纯文本内容，优先 textContent 再回退 content 字符串。
     *
     * @param msg 待提取的消息
     * @return 文本内容，不可用时返回 null
     */
    public static String getTextContent(Msg msg) {
        if (msg == null) {
            return null;
        }
        
        String textContent = msg.getTextContent();
        if (textContent != null && !textContent.isEmpty()) {
            return textContent;
        }
        
        Object content = msg.getContent();
        if (content instanceof String) {
            return (String) content;
        }
        
        return null;
    }
    
    /**
     * 判断消息是否仅包含单个 {@link ThinkingBlock}。
     *
     * @param msg 待检查消息
     * @return 仅含一个 think block 时返回 true
     */
    public static boolean hasOnlyThinkBlock(Msg msg) {
        if (msg == null) {
            return false;
        }
        
        try {
            Object content = msg.getContent();
            if (content instanceof List) {
                List<?> contentList = (List<?>) content;
                return contentList.size() == 1 && contentList.get(0) instanceof ThinkingBlock;
            }
            return false;
        } catch (Exception e) {
            LOGGER.debug("Failed to check thinkblock in msg", e);
            return false;
        }
    }
    
    /**
     * 从 think block 提取模型思考文本。
     *
     * @param msg 含 think block 的消息
     * @return 思考内容，不可用时返回 null
     */
    public static String getThinkingContent(Msg msg) {
        if (msg == null) {
            return null;
        }
        
        try {
            // 从消息 content 列表读取 think block
            Object content = msg.getContent();
            if (content instanceof List) {
                List<?> contentList = (List<?>) content;
                if (contentList.size() == 1) {
                    Object element = contentList.get(0);
                    if (element instanceof ThinkingBlock) {
                        ThinkingBlock thinkBlock = (ThinkingBlock) element;
                        return thinkBlock.getThinking();
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            LOGGER.debug("Failed to extract thinking content from msg", e);
            return null;
        }
    }
    
    /**
     * 处理单个流式事件，映射为 {@link StreamResponseType} 与内容片段。
     *
     * @param event AgentScope 事件
     * @return 处理结果；应跳过的 event 返回 null
     */
    public static EventProcessResult processEvent(io.agentscope.core.agent.Event event) {
        // 末帧含完整内容，跳过分片推送以避免重复
        if (event.isLast()) {
            return null;
        }
        
        Msg msg = event.getMessage();
        if (msg == null) {
            return null;
        }
        
        // 根据事件类型与消息结构确定响应类型
        StreamResponseType type = StreamResponseType.CONTENT;
        String content = null;
        
        if (event.getType() == EventType.TOOL_RESULT) {
            // 工具调用：从 textContent 取内容
            type = StreamResponseType.TOOL_CALL;
            content = getTextContent(msg);
        } else if (event.getType() == EventType.REASONING && hasOnlyThinkBlock(msg)) {
            // 思考过程：从 think block 取内容
            type = StreamResponseType.THINKING;
            content = getThinkingContent(msg);
        } else {
            // 正文或其他：从 textContent 取内容
            type = StreamResponseType.CONTENT;
            content = getTextContent(msg);
        }
        
        // 内容为空则跳过
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        return new EventProcessResult(type, content);
    }
    
    /**
     * 响应构建器：将类型、内容与完成标志转换为具体响应 DTO。
     *
     * @param <T> 响应类型
     */
    public interface ResponseBuilder<T> {
        
        /**
         * 构建单帧响应对象。
         *
         * @param type 响应类型
         * @param content 内容片段（DONE 时为 null）
         * @param done 是否已完成
         * @return 响应实例；返回 null 表示过滤该帧
         */
        T build(StreamResponseType type, String content, boolean done);
    }
    
    /**
     * 创建 Reactor {@link Subscriber}，统一处理 onNext/onError/onComplete 流式生命周期。
     *
     * @param responseBuilder 响应构建器
     * @param callback 流式回调
     * @param <T> 响应类型
     * @return Subscriber 实例
     */
    public static <T> Subscriber<io.agentscope.core.agent.Event> createSubscriber(
        ResponseBuilder<T> responseBuilder,
        StreamResponseCallback<T> callback) {
        
        return new Subscriber<io.agentscope.core.agent.Event>() {
            
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }
            
            @Override
            public void onNext(io.agentscope.core.agent.Event event) {
                try {
                    EventProcessResult result = processEvent(event);
                    if (result != null) {
                        T response =
                            responseBuilder.build(result.getType(), result.getContent(), false);
                        // builder 返回 null 时跳过（如 THINKING 过滤）
                        if (response != null) {
                            callback.onNext(response);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to process stream event", e);
                }
            }
            
            @Override
            public void onError(Throwable t) {
                LOGGER.error("Error in AgentScope stream response", t);
                callback.onError(t);
            }
            
            @Override
            public void onComplete() {
                // 前端自行解析累积内容，此处仅发送 DONE 信号
                T finalResponse = responseBuilder.build(StreamResponseType.DONE, null, true);
                callback.onNext(finalResponse);
                callback.onComplete();
            }
        };
    }
    
    /**
     * 单事件处理结果，携带类型与内容。
     */
    public static class EventProcessResult {
        
        private final StreamResponseType type;
        private final String content;
        
        /** 构造事件处理结果。 */
        public EventProcessResult(StreamResponseType type, String content) {
            this.type = type;
            this.content = content;
        }
        
        /** 获取响应类型。 */
        public StreamResponseType getType() {
            return type;
        }
        
        /** 获取内容片段。 */
        public String getContent() {
            return content;
        }
    }
}
