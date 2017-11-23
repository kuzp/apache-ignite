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

package org.apache.ignite.ml.nn.graph;

import org.apache.ignite.ml.math.Tensor;

/**
 * TODO: add description.
 */
public class ScalarTensor implements Tensor {
    private double value;

    public ScalarTensor(double v) {
        value = v;
    }

    /**
     * @return Value.
     */
    public double value() {
        return value;
    }

    /**
     * @param val New value.
     */
    public void value(double val) {
        value = val;
    }

    /** {@inheritDoc} */
    @Override public int[] shape() {
        return new int[0];
    }

    /** {@inheritDoc} */
    @Override public Tensor tensorFold(Tensor tensor) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public Tensor tensorProduct(Tensor tensor) {
        return null;
    }
}
