/*
 * Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

/* eslint-disable @typescript-eslint/no-unused-vars */
// clipper-lib 最小类型声明：供检测框 polygon offset（unclip）使用
declare module "clipper-lib" {
    // Clipper 整数坐标点（X/Y 大写，与库 API 一致）
  interface IntPoint {
    X: number;
    Y: number;
  }

  type Path = IntPoint[];
  type Paths = Path[];

    // 多边形偏移器：AddPath 添加路径后 Execute 按 delta 扩缩
  class ClipperOffset {
    AddPath(path: Path, joinType: number, endType: number): void;
    Execute(result: Paths, delta: number): void;
  }

    // 连接类型常量，当前仅声明 jtRound 圆角连接
  const JoinType: {
    readonly jtRound: number;
  };

  const EndType: {
    readonly etClosedPolygon: number;
  };

    // 模块默认导出对象：ClipperOffset、Paths 与 JoinType/EndType
  const ClipperLib: {
    ClipperOffset: typeof ClipperOffset;
    Paths: { new (): Paths };
    JoinType: typeof JoinType;
    EndType: typeof EndType;
  };

  export default ClipperLib;
}
