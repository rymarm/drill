/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.rpc;

import org.apache.drill.common.exceptions.DrillRuntimeException;
import org.apache.drill.shaded.guava.com.google.common.base.Stopwatch;

import java.util.EventListener;
import java.util.concurrent.TimeUnit;

/**
 * PongListener realizes the listener pattern. It is used to verify, whether {@link org.apache.drill.exec.rpc.RpcBus.InboundHandler}
 * received {@link InboundRpcMessage} with {@link org.apache.drill.exec.proto.GeneralRPCProtos.RpcMode#PONG PONG} mode.
 */
public class PongListener implements EventListener {
  private boolean hasReceivedPong = false;

  /**
   * Verify whether received {@link org.apache.drill.exec.proto.GeneralRPCProtos.RpcMode#PONG PONG} mode.
   *
   * @param timeout time in seconds to wait message receiving. Should be higher than 0
   * @return true if message is received until timeout, false otherwise
   * @throws DrillRuntimeException if the value supplied for timeout is less than 0
   */
  public boolean pongIsReceived(int timeout) throws DrillRuntimeException {
    if (timeout < 0) {
      throw new DrillRuntimeException(String.format("Invalid timeout (%d<0).", timeout));
    }

    Stopwatch stopwatch = Stopwatch.createStarted();
    while (!hasReceivedPong) {
      if (stopwatch.elapsed(TimeUnit.SECONDS) > timeout) {
        return false;
      }
    }
    return true;
  }

  /**
   * Notify PongListener about receiving {@link InboundRpcMessage} with
   * {@link org.apache.drill.exec.proto.GeneralRPCProtos.RpcMode#PONG PONG} mode
   */
  public void update() {
    hasReceivedPong = true;
  }
}
