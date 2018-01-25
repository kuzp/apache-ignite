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

package org.apache.ignite.ml.dlc.dataset;

import org.apache.ignite.ml.dlc.DLC;
import org.apache.ignite.ml.dlc.dataset.part.recoverable.DLCLabeledDatasetPartitionRecoverable;
import org.apache.ignite.ml.dlc.dataset.part.replicated.DLCLabeledDatasetPartitionReplicated;

/**
 * Dataset provides API to work with labeled dataset.
 *
 * @param <K> type of an upstream value key
 * @param <V> type of an upstream value
 */
public class DLCLabeledDataset<K, V>
    extends DLCWrapper<K, V, DLCLabeledDatasetPartitionReplicated, DLCLabeledDatasetPartitionRecoverable> {
    /**
     * Constructs a new instance of Distributed Learning Context wrapper
     *
     * @param delegate delegate which actually performs base functions
     */
    public DLCLabeledDataset(
        DLC<K, V, DLCLabeledDatasetPartitionReplicated, DLCLabeledDatasetPartitionRecoverable> delegate) {
        super(delegate);
    }
}
