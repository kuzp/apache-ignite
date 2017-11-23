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

package org.apache.ignite.ml.nn.graph.operation;

import java.util.List;
import org.apache.ignite.ml.math.Tensor;
import org.apache.ignite.ml.math.Vector;
import org.apache.ignite.ml.nn.graph.Operator;
import org.apache.ignite.ml.nn.graph.ScalarTensor;

/**
 * TODO: add description.
 */
public class VectorDotProduct implements Operator<ScalarTensor > {
    /**
     * Build operator.
     */
    public static VectorDotProduct getInstance(){
        return new VectorDotProduct();
    }

    /** {@inheritDoc} */
    @Override public ScalarTensor apply(List<Tensor> input) {
        assert input.size() == 2;

        Vector left = (Vector)input.get(0);
        Vector right = (Vector)input.get(1);

        return new ScalarTensor(left.dot(right));
    }
}
