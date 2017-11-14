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

package org.apache.ignite.ml.nn;

import java.util.function.BiFunction;
import org.apache.ignite.ml.Model;

/**
 * TODO: add description.
 */
public class NeuralNetworkModel<T, V> implements Model<T, V> {
    /** {@inheritDoc} */
    @Override public V predict(T val) {
        //TODO: implement.
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override public <X, W> Model<T, X> combine(Model<T, W> other, BiFunction<V, W, X> combiner) {
        //TODO: implement.
        return null;
    }
}
