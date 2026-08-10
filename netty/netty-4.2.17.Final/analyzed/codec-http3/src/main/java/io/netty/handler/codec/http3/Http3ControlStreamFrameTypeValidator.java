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
 * Validate that the frame type is valid for a control stream.
 * <p>控制流只允许 SETTINGS、GOAWAY、MAX_PUSH_ID、CANCEL_PUSH 及保留类型；
 * 请求流专用的 HEADERS/DATA/PUSH_PROMISE 若出现在控制流上即协议违规。
 */
final class Http3ControlStreamFrameTypeValidator implements Http3FrameTypeValidator {

    static final Http3ControlStreamFrameTypeValidator INSTANCE = new Http3ControlStreamFrameTypeValidator();

    private Http3ControlStreamFrameTypeValidator() { }

    @Override
    public void validate(long type, boolean first) throws Http3Exception {
        switch ((int) type) {
            // 下列帧类型属于请求/推送流，绝不应出现在控制流
            case Http3CodecUtils.HTTP3_PUSH_PROMISE_FRAME_TYPE:
            case Http3CodecUtils.HTTP3_HEADERS_FRAME_TYPE:
            case Http3CodecUtils.HTTP3_DATA_FRAME_TYPE:
                if (first) {
                    // 首帧不是 SETTINGS，说明对端跳过了必须的 SETTINGS 握手
                    throw new Http3Exception(Http3ErrorCode.H3_MISSING_SETTINGS,
                            "Missing settings frame.");
                }
                throw new Http3Exception(Http3ErrorCode.H3_FRAME_UNEXPECTED,
                        "Unexpected frame type '" + type + "' received");
            default:
               break;
        }
    }
}
