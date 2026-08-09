/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2018 The HighwayHash Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.misc;

/**
 * Google HighwayHash 算法的 Java 实现。
 * <p>
 * 支持增量更新与 64/128/256 位输出；
 * 详见 <a href="https://github.com/google/highwayhash">HighwayHash on GitHub</a>。
 */
@SuppressWarnings({"OperatorWrap", "BooleanExpressionComplexity", "UnnecessaryParentheses", "WhitespaceAfter", "ParameterName", "LocalVariableName"})
public final class HighwayHash {
  private final long[] v0 = new long[4];
  private final long[] v1 = new long[4];
  private final long[] mul0 = new long[4];
  private final long[] mul1 = new long[4];
  /** 是否已 finalize，每个实例只能计算一次。 */
  private boolean done = false;

  /**
   * 用四个 64 位字初始化哈希状态。
   * @param key0 密钥前 8 字节
   * @param key1 密钥第 2 个 8 字节
   * @param key2 密钥第 3 个 8 字节
   * @param key3 密钥最后 8 字节
   */
  public HighwayHash(long key0, long key1, long key2, long key3) {
    reset(key0, key1, key2, key3);
  }

  /**
   * 用长度为 4 的 long 数组作为 256 位密钥初始化。
   * @param key 长度为 4 的密钥数组
   */
  public HighwayHash(long[] key) {
    if (key.length != 4) {
      throw new IllegalArgumentException(String.format("Key length (%s) must be 4", key.length));
    }
    reset(key[0], key[1], key[2], key[3]);
  }

  /**
   * 用 32 字节数据块更新哈希状态。
   * <p>
   * 若可直接读取 4 个 long，优先调用 {@link #update(long, long, long, long)} 更快。
   * @param packet 至少包含 pos + 32 字节的数据数组
   * @param pos 起始读取位置
   */
  public void updatePacket(byte[] packet, int pos) {
    if (pos < 0) {
      throw new IllegalArgumentException(String.format("Pos (%s) must be positive", pos));
    }
    if (pos + 32 > packet.length) {
      throw new IllegalArgumentException("packet must have at least 32 bytes after pos");
    }
    long a0 = read64(packet, pos + 0);
    long a1 = read64(packet, pos + 8);
    long a2 = read64(packet, pos + 16);
    long a3 = read64(packet, pos + 24);
    update(a0, a1, a2, a3);
  }

  /**
   * 用 4 个 little-endian long 表示的 32 字节更新哈希（高效路径）。
   * @param a0 第 1 个 8 字节
   * @param a1 第 2 个 8 字节
   * @param a2 第 3 个 8 字节
   * @param a3 第 4 个 8 字节
   */
  public void update(long a0, long a1, long a2, long a3) {
    if (done) {
      throw new IllegalStateException("Can compute a hash only once per instance");
    }
    v1[0] += mul0[0] + a0;
    v1[1] += mul0[1] + a1;
    v1[2] += mul0[2] + a2;
    v1[3] += mul0[3] + a3;
    for (int i = 0; i < 4; ++i) {
      mul0[i] ^= (v1[i] & 0xffffffffL) * (v0[i] >>> 32);
      v0[i] += mul1[i];
      mul1[i] ^= (v0[i] & 0xffffffffL) * (v1[i] >>> 32);
    }
    v0[0] += zipperMerge0(v1[1], v1[0]);
    v0[1] += zipperMerge1(v1[1], v1[0]);
    v0[2] += zipperMerge0(v1[3], v1[2]);
    v0[3] += zipperMerge1(v1[3], v1[2]);
    v1[0] += zipperMerge0(v0[1], v0[0]);
    v1[1] += zipperMerge1(v0[1], v0[0]);
    v1[2] += zipperMerge0(v0[3], v0[2]);
    v1[3] += zipperMerge1(v0[3], v0[2]);
  }


  /**
   * 处理最后 1～31 字节的尾部数据。
   * <p>
   * 须先对每个完整 32 字节块调用 {@link #updatePacket}，
   * 再用本方法处理余数。
   * @param bytes 至少包含 pos + size_mod32 字节的数据数组
   * @param pos 起始位置
   * @param size_mod32 待读字节数（1～31）
   */
  public void updateRemainder(byte[] bytes, int pos, int size_mod32) {
    if (pos < 0) {
      throw new IllegalArgumentException(String.format("Pos (%s) must be positive", pos));
    }
    if (size_mod32 < 0 || size_mod32 >= 32) {
      throw new IllegalArgumentException(
          String.format("size_mod32 (%s) must be between 0 and 31", size_mod32));
    }
    if (pos + size_mod32 > bytes.length) {
      throw new IllegalArgumentException("bytes must have at least size_mod32 bytes after pos");
    }
    int size_mod4 = size_mod32 & 3;
    int remainder = size_mod32 & ~3;
    byte[] packet = new byte[32];
    for (int i = 0; i < 4; ++i) {
      v0[i] += ((long)size_mod32 << 32) + size_mod32;
    }
    rotate32By(size_mod32, v1);
    for (int i = 0; i < remainder; i++) {
      packet[i] = bytes[pos + i];
    }
    if ((size_mod32 & 16) != 0) {
      for (int i = 0; i < 4; i++) {
        packet[28 + i] = bytes[pos + remainder + i + size_mod4 - 4];
      }
    } else {
      if (size_mod4 != 0) {
        packet[16 + 0] = bytes[pos + remainder + 0];
        packet[16 + 1] = bytes[pos + remainder + (size_mod4 >>> 1)];
        packet[16 + 2] = bytes[pos + remainder + (size_mod4 - 1)];
      }
    }
    updatePacket(packet, 0);
  }

  /**
   * 完成全部输入后计算 64 位哈希并失效内部状态。
   *
   * NOTE: 64 位 HighwayHash 算法已冻结，不再变更。
   *
   * @return 64 位哈希值
   */
  public long finalize64() {
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    done = true;
    return v0[0] + v1[0] + mul0[0] + mul1[0];
  }

  /**
   * 完成全部输入后计算 128 位哈希并失效内部状态。
   *
   * NOTE: 128 位 HighwayHash 算法尚未冻结，可能变更。
   *
   * @return 长度为 2 的 long 数组，表示 128 位哈希
   */
  public long[] finalize128() {
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    done = true;
    long[] hash = new long[2];
    hash[0] = v0[0] + mul0[0] + v1[2] + mul1[2];
    hash[1] = v0[1] + mul0[1] + v1[3] + mul1[3];
    return hash;
  }

  /**
   * Computes the hash value after all bytes were processed. Invalidates the
   * state.
   *
   * NOTE: The 256-bit HighwayHash algorithm is not yet frozen and subject to change.
   *
   * @return array of size 4 containing 256-bit hash
   */
  public long[] finalize256() {
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    permuteAndUpdate();
    done = true;
    long[] hash = new long[4];
    modularReduction(v1[1] + mul1[1], v1[0] + mul1[0],
                     v0[1] + mul0[1], v0[0] + mul0[0],
                     hash, 0);
    modularReduction(v1[3] + mul1[3], v1[2] + mul1[2],
                     v0[3] + mul0[3], v0[2] + mul0[2],
                     hash, 2);
    return hash;
  }
  private void reset(long key0, long key1, long key2, long key3) {
    mul0[0] = 0xdbe6d5d5fe4cce2fL;
    mul0[1] = 0xa4093822299f31d0L;
    mul0[2] = 0x13198a2e03707344L;
    mul0[3] = 0x243f6a8885a308d3L;
    mul1[0] = 0x3bd39e10cb0ef593L;
    mul1[1] = 0xc0acf169b5f18a8cL;
    mul1[2] = 0xbe5466cf34e90c6cL;
    mul1[3] = 0x452821e638d01377L;
    v0[0] = mul0[0] ^ key0;
    v0[1] = mul0[1] ^ key1;
    v0[2] = mul0[2] ^ key2;
    v0[3] = mul0[3] ^ key3;
    v1[0] = mul1[0] ^ ((key0 >>> 32) | (key0 << 32));
    v1[1] = mul1[1] ^ ((key1 >>> 32) | (key1 << 32));
    v1[2] = mul1[2] ^ ((key2 >>> 32) | (key2 << 32));
    v1[3] = mul1[3] ^ ((key3 >>> 32) | (key3 << 32));
  }

  private long zipperMerge0(long v1, long v0) {
    return (((v0 & 0xff000000L) | (v1 & 0xff00000000L)) >>> 24) |
             (((v0 & 0xff0000000000L) | (v1 & 0xff000000000000L)) >>> 16) |
             (v0 & 0xff0000L) | ((v0 & 0xff00L) << 32) |
             ((v1 & 0xff00000000000000L) >>> 8) | (v0 << 56);
  }

  private long zipperMerge1(long v1, long v0) {
    return (((v1 & 0xff000000L) | (v0 & 0xff00000000L)) >>> 24) |
             (v1 & 0xff0000L) | ((v1 & 0xff0000000000L) >>> 16) |
             ((v1 & 0xff00L) << 24) | ((v0 & 0xff000000000000L) >>> 8) |
             ((v1 & 0xffL) << 48) | (v0 & 0xff00000000000000L);
  }

  /** 从字节数组 little-endian 读取 64 位值。 */
  private long read64(byte[] src, int pos) {
    // 用 0xffL 掩码将 signed byte 转为 0..255 的无符号 long
    return (src[pos + 0] & 0xffL) | ((src[pos + 1] & 0xffL) << 8) |
        ((src[pos + 2] & 0xffL) << 16) | ((src[pos + 3] & 0xffL) << 24) |
        ((src[pos + 4] & 0xffL) << 32) | ((src[pos + 5] & 0xffL) << 40) |
        ((src[pos + 6] & 0xffL) << 48) | ((src[pos + 7] & 0xffL) << 56);
  }

  private void rotate32By(long count, long[] lanes) {
    for (int i = 0; i < 4; ++i) {
      long half0 = (lanes[i] & 0xffffffffL);
      long half1 = (lanes[i] >>> 32) & 0xffffffffL;
      lanes[i] = ((half0 << count)  & 0xffffffffL) | (half0 >>> (32 - count));
      lanes[i] |= ((long)(((half1 << count) & 0xffffffffL) |
          (half1 >>> (32 - count)))) << 32;
    }
  }

  private void permuteAndUpdate() {
    update((v0[2] >>> 32) | (v0[2] << 32),
        (v0[3] >>> 32) | (v0[3] << 32),
        (v0[0] >>> 32) | (v0[0] << 32),
        (v0[1] >>> 32) | (v0[1] << 32));
  }

  private void modularReduction(long a3_unmasked, long a2, long a1,
                                long a0, long[] hash, int pos) {
    long a3 = a3_unmasked & 0x3FFFFFFFFFFFFFFFL;
    hash[pos + 1] = a1 ^ ((a3 << 1) | (a2 >>> 63)) ^ ((a3 << 2) | (a2 >>> 62));
    hash[pos + 0] = a0 ^ (a2 << 1) ^ (a2 << 2);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * NOTE: The 64-bit HighwayHash algorithm is declared stable and no longer subject to change.
   *
   * @param data array with data bytes
   * @param offset position of first byte of data to read from
   * @param length number of bytes from data to read
   * @param key array of size 4 with the key to initialize the hash with
   * @return 64-bit hash for the given data
   */
  public static long hash64(byte[] data, int offset, int length, long[] key) {
    HighwayHash h = new HighwayHash(key);
    h.processAll(data, offset, length);
    return h.finalize64();
  }

  /**
   * NOTE: The 128-bit HighwayHash algorithm is not yet frozen and subject to change.
   *
   * @param data array with data bytes
   * @param offset position of first byte of data to read from
   * @param length number of bytes from data to read
   * @param key array of size 4 with the key to initialize the hash with
   * @return array of size 2 containing 128-bit hash for the given data
   */
  public static long[] hash128(
      byte[] data, int offset, int length, long[] key) {
    HighwayHash h = new HighwayHash(key);
    h.processAll(data, offset, length);
    return h.finalize128();
  }

  /**
   * NOTE: The 256-bit HighwayHash algorithm is not yet frozen and subject to change.
   *
   * @param data array with data bytes
   * @param offset position of first byte of data to read from
   * @param length number of bytes from data to read
   * @param key array of size 4 with the key to initialize the hash with
   * @return array of size 4 containing 256-bit hash for the given data
   */
  public static long[] hash256(
      byte[] data, int offset, int length, long[] key) {
    HighwayHash h = new HighwayHash(key);
    h.processAll(data, offset, length);
    return h.finalize256();
  }

  /** 对整段字节数组按 32 字节块 + 余数完成哈希更新。 */
  private void processAll(byte[] data, int offset, int length) {
    int i;
    for (i = 0; i + 32 <= length; i += 32) {
      updatePacket(data, offset + i);
    }
    if ((length & 31) != 0) {
      updateRemainder(data, offset + i, length & 31);
    }
  }
}