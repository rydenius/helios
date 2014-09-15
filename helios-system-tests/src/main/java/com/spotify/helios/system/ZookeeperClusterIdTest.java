/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.helios.system;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;

public class ZookeeperClusterIdTest extends SystemTestBase {

  // helios namespace which includes random zookeeper cluster ID
  private static final String NAMESPACE = "helios/12345";

  @Test
  public void testCreateJobWithoutNamespace() throws Exception {
    startDefaultMaster("--zk-namespace", NAMESPACE);
    final String response = cli("create", "-z", masterEndpoint(),
                                testJobName + ":" + testJobVersion, BUSYBOX, "date");
    // Verify response contains the error status and the missing cluster ID
    assertThat(response,
               both(containsString("ZOO_KEEPER_NOT_INITIALIZED")).and(containsString(NAMESPACE)));
  }

  @Test
  public void testRegisterWithoutNamespace() throws Exception {
    startDefaultMaster("--zk-namespace", NAMESPACE);
    final String response = cli("register", "-z", masterEndpoint(), "host", "id");
    // Verify the operation failed
    assertThat(response, containsString("400"));
  }

}
