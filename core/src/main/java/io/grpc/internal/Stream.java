/*
 * Copyright 2014, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.grpc.internal;

import io.grpc.Compressor;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;

import java.io.InputStream;

import javax.annotation.Nullable;

/**
 * A single stream of communication between two end-points within a transport.
 *
 * <p>An implementation doesn't need to be thread-safe. All methods are expected to execute quickly.
 */
public interface Stream {
  /**
   * Requests up to the given number of messages from the call to be delivered to
   * {@link StreamListener#messageRead(java.io.InputStream)}. No additional messages will be
   * delivered.  If the stream has a {@code start()} method, it must be called before requesting
   * messages.
   *
   * @param numMessages the requested number of messages to be delivered to the listener.
   */
  void request(int numMessages);

  /**
   * Writes a message payload to the remote end-point. The bytes from the stream are immediately
   * read by the Transport. Where possible callers should use streams that are
   * {@link io.grpc.KnownLength} to improve efficiency. This method will always return immediately
   * and will not wait for the write to complete.  If the stream has a {@code start()} method, it
   * must be called before writing any messages.
   *
   * <p>It is recommended that the caller consult {@link #isReady()} before calling this method to
   * avoid excessive buffering in the transport.
   *
   * @param message stream containing the serialized message to be sent
   */
  void writeMessage(InputStream message);

  /**
   * Flushes any internally buffered messages to the remote end-point.
   */
  void flush();

  /**
   * If {@code true}, indicates that the transport is capable of sending additional messages without
   * requiring excessive buffering internally. Otherwise, {@link StreamListener#onReady()} will be
   * called when it turns {@code true}.
   *
   * <p>This is just a suggestion and the application is free to ignore it, however doing so may
   * result in excessive buffering within the transport.
   */
  boolean isReady();

  /**
   * Picks a compressor for for this stream.  If no message encodings are acceptable, compression is
   * not used.  It is undefined if this this method is invoked multiple times.  If the stream has
   * a {@code start()} method, pickCompressor must be called prior to start.
   *
   *
   * @param messageEncodings a group of message encoding names that the remote endpoint is known
   *     to support.
   * @return The compressor chosen for the stream, or null if none selected.
   */
  @Nullable
  Compressor pickCompressor(Iterable<String> messageEncodings);

  /**
   * Enables per-message compression, if an encoding type has been negotiated.  If no message
   * encoding has been negotiated, this is a no-op.
   */
  void setMessageCompression(boolean enable);

  /**
   * Sets the decompressor registry to use when resolving {@code #setDecompressor(String)}.  If
   * unset, the default DecompressorRegistry will be used.  If the stream has a {@code start()}
   * method, setDecompressionRegistry must be called prior to start.
   *
   * @see DecompressorRegistry#getDefaultInstance()
   *
   * @param registry the decompressors to use.
   */
  void setDecompressionRegistry(DecompressorRegistry registry);

  /**
   * Sets the compressor registry to use when resolving {@link #pickCompressor}.  If unset, the
   * default CompressorRegistry will be used.  If the stream has a {@code start()} method,
   * setCompressionRegistry must be called prior to start.
   *
   * @see CompressorRegistry#getDefaultInstance()
   *
   * @param registry the compressors to use.
   */
  void setCompressionRegistry(CompressorRegistry registry);
}
