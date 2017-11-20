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
public class ComputationGraph {
    private List<VariableNode> vars;
    private List<InputNode> input;
    private List<OperationNode> operations;

    //
    private List<OperationNode> top = new LinkedList<>();

    public ComputationGraph(List<VariableNode> vars, List<InputNode> input,
        List<OperationNode> operations) {
        this.vars = vars;
        this.input = input;
        this.operations = operations;
    }

    public void compute(){
        for (OperationNode node: top)
            node.compute();
    }

    public void setTop(List top){
        this.top = top;
    }

    public void addTop(OperationNode node){
        top.add(node);
    }

    public List<OperationNode> getTop() {
        return top;
    }

    public void setInput(Tensor... inputVal) {
        assert input.size() == inputVal.length;

        for (int i = 0; i < input.size(); i++)
            input.get(i).setVal(inputVal[i]);
    }

}
