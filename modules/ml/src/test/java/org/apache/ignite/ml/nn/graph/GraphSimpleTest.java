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

import java.util.Arrays;
import java.util.List;
import org.apache.ignite.ml.math.Matrix;
import org.apache.ignite.ml.math.Tensor;
import org.apache.ignite.ml.math.Vector;
import org.apache.ignite.ml.math.impls.matrix.DenseLocalOnHeapMatrix;
import org.apache.ignite.ml.math.impls.vector.DenseLocalOnHeapVector;
import org.apache.ignite.ml.math.util.Tracer;
import org.apache.ignite.ml.nn.graph.operation.MatrixByVectorMultiplication;
import org.apache.ignite.ml.nn.graph.operation.VectorsAddition;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: add description.
 */
public class GraphSimpleTest {
    /**
     * Simple test.
     *
     * z = A * x + b
     *
     * A - matrix, b - vector, x - input
     */
    @Test
    public void test(){
        Matrix a = new DenseLocalOnHeapMatrix(new double[][] {{1,0}, {0,-1}});

        Vector b = new DenseLocalOnHeapVector(new double[] {1,1});

        VariableNode varA = new VariableNode<>(a);
        VariableNode varB = new VariableNode<>(b);

        InputNode x = new InputNode();

        OperationNode y = new OperationNode(new MatrixByVectorMultiplication(), Arrays.asList(varA, x));
        OperationNode z = new OperationNode(new VectorsAddition(), Arrays.asList(y, varB));

        Tensor[] input = {new DenseLocalOnHeapVector(new double[] {1, 2})};

        List<VariableNode> varNodes = Arrays.asList(varA, varB);
        List<InputNode> inputNodes = Arrays.asList(x);
        List<OperationNode> operationNodes = Arrays.asList(y, z);

        ComputationGraph graph = new ComputationGraph(varNodes, inputNodes, operationNodes);

        graph.addTop(z);
        graph.setInput(input);

        graph.compute();
        Tensor output = graph.getTop().get(0).output();

        Vector outVec = (Vector)output;

        Tracer.showAscii(outVec);

        Assert.assertTrue(outVec.get(0) == 2);
        Assert.assertTrue(outVec.get(1) == -1);
    }
}
