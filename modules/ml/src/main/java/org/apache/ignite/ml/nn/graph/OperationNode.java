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

import java.util.LinkedList;
import java.util.List;
import org.apache.ignite.ml.math.Tensor;

/**
 * TODO: add description.
 */
public class OperationNode<T extends Tensor> implements Node<T> {
    private T apply;
    private Operator<T> operator;
    /**
     * All input nodes.
     */
    private List<Node> inputNodes;
    /**
     * Nodes that receive this operation's output as input.
     */
    private List<Node> consumers = new LinkedList<>();

    public OperationNode(Operator<T> operator, List<Node> inputNodes) {
        this.operator = operator;
        this.inputNodes = inputNodes;
    }

    public void addConsumer(Node node){
        consumers.add(node);
    }

    public void compute() {
        inputNodes.stream().filter(node -> node instanceof OperationNode).forEach(node -> ((OperationNode)node).compute());
        apply = operator.apply((Tensor[])inputNodes.stream().map(Node::output).toArray());
    }

    @Override public T output() {
        return apply;
    }
}
