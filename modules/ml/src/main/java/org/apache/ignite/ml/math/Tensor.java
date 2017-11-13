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

package org.apache.ignite.ml.math;

/**
 * TODO: add description.
 */
public interface Tensor {
    /**
     * * Return tensor shape. It`s a tensor dimension.
     *
     * For scalars shape result is int[0], for vectors int[1], for matrices[2], etc...
     *
     * @return tensor shape.
     */
    int[] shape();

    /**
     * TODO: add description.
     *
     * @param tensor
     * @return
     */
    Tensor tensorFold(Tensor tensor);

    /**
     * TODO: add description.
     *
     * @param tensor
     * @return
     */
    Tensor tensorProduct(Tensor tensor);
}
