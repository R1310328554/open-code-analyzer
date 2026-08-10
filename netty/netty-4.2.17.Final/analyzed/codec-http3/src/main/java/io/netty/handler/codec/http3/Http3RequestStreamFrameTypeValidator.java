/*
 * Copyright 2021 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http3;

/**
 * Validate that the frame type is valid for a request stream.
 * <p>请求流只允许消息体相关帧；若出现 CANCEL_PUSH、GOAWAY、MAX_PUSH_ID、SETTINGS 等
 * 控制流帧类型，立即以 {@code H3_FRAME_UNEXPECTED} 拒绝。
 */
final class Http3RequestStreamFrameTypeValidator implements Http3FrameTypeValidator {

    /** 单例：帧类型校验无 per-stream 状态，可全局复用。 */
    static final Http3RequestStreamFrameTypeValidator INSTANCE = new Http3RequestStreamFrameTypeValidator();

    private Http3RequestStreamFrameTypeValidator() { }

    @Override
    public void validate(long type, boolean first) throws Http3Exception {
        switch ((int) type) {
            case Http3CodecUtils.HTTP3_CANCEL_PUSH_FRAME_TYPE:
            case Http3CodecUtils.HTTP3_GO_AWAY_FRAME_TYPE:
            case Http3CodecUtils.HTTP3_MAX_PUSH_ID_FRAME_TYPE:
            case Http3CodecUtils.HTTP3_SETTINGS_FRAME_TYPE:
                throw new Http3Exception(Http3ErrorCode.H3_FRAME_UNEXPECTED,
                        "Unexpected frame type '" + type + "' received");
            default:
                break;
        }
    }
}
