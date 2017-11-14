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

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: add description.
 */
public class Node {
    private List<Edge> links;

    public Node(List<Edge> links) {
        this.links = links;
    }

    public List<Edge> getInputLinks(){
        return links.stream().filter(l->l.getInput().equals(this)).collect(Collectors.toList());
    }

    public List<Edge> getOutputLinks(){
        return links.stream().filter(l->l.getOutput().equals(this)).collect(Collectors.toList());
    }

    public List<Node> getInputNodes(){
        return links.stream().filter(l->l.getOutput().equals(this)).map(Edge::getInput).collect(Collectors.toList());
    }

    public List<Node> getOutputNodes(){
        return links.stream().filter(l->l.getInput().equals(this)).map(Edge::getOutput).collect(Collectors.toList());
    }
}
