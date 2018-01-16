/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ml.dlearn;

import java.io.Serializable;

/**
 * With assumption that a d-lear partition is the type-safe wrapper on top of a partition storage (distributed or not),
 * {@code DLearnPartitionFactory} provides API for instantiation of a new d-learn partition instances based on given
 * partition storage.
 *
 * @param <P> type of d-learn partition
 */
@FunctionalInterface
public interface DLearnPartitionFactory<P> extends Serializable {
    /**
     * Creates a new d-learn partition (type-safe wrapper on top of a partition storage).
     *
     * @param storage partition storage
     * @return d-learn partition
     */
    public P createPartition(DLearnPartitionStorage storage);
}