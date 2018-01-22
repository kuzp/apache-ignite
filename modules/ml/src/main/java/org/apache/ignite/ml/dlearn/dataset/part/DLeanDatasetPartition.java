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

package org.apache.ignite.ml.dlearn.dataset.part;

import org.apache.ignite.ml.dlearn.DLearnPartitionStorage;

/**
 * Interface which provides simple dataset API which allows to get or set an underlying feature matrix in flat format.
 */
public class DLeanDatasetPartition implements AutoCloseable {
    /** Key of feature values. */
    private static final String FEATURES_KEY = "features";

    /** Key of row values. */
    private static final String ROWS_KEY = "rows";

    /** Learning partition storage. */
    private final DLearnPartitionStorage storage;

    /**
     * Creates a new instance of learning dataset partition.
     *
     * @param storage learning partition storage
     */
    public DLeanDatasetPartition(DLearnPartitionStorage storage) {
        this.storage = storage;
    }

    /**
     * Sets matrix of features in flat format.
     *
     * @param features matrix of features in flat format
     */
    public void setFeatures(double[] features) {
        storage.put(FEATURES_KEY, features);
    }

    /**
     * Retrieves matrix of features in flat format.
     *
     * @return matrix of features in flat format
     */
    public double[] getFeatures() {
        return storage.get(FEATURES_KEY);
    }

    /**
     * Sets number of rows.
     *
     * @param rows number of rows
     */
    public void setRows(int rows) {
        storage.put(ROWS_KEY, rows);
    }

    /**
     * Retrieves number of rows.
     *
     * @return number of rows
     */
    public int getRows() {
        return storage.get(ROWS_KEY);
    }

    /**
     * Removes all data associated with the partition.
     */
    @Override public void close() {
        storage.remove(FEATURES_KEY);
        storage.remove(ROWS_KEY);
    }
}
