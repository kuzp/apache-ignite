﻿/*
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

namespace Apache.Ignite.Core.Impl.Plugin
{
    using Apache.Ignite.Core.Plugin;

    /// <summary>
    /// Plugin context.
    /// </summary>
    internal class PluginContext<T> : IPluginContext<T> where T : IPluginConfiguration
    {
        /** */
        private readonly IIgnite _ignite;

        /** */
        private readonly IgniteConfiguration _igniteConfiguration;
        
        /** */
        private readonly T _pluginConfiguration;

        /// <summary>
        /// Initializes a new instance of the <see cref="PluginContext{T}"/> class.
        /// </summary>
        public PluginContext(IIgnite ignite, IgniteConfiguration igniteConfiguration, T pluginConfiguration)
        {
            _ignite = ignite;
            _igniteConfiguration = igniteConfiguration;
            _pluginConfiguration = pluginConfiguration;
        }

        /** <inheritdoc /> */
        public IIgnite Ignite
        {
            get { return _ignite; }
        }

        /** <inheritdoc /> */
        public IgniteConfiguration IgniteConfiguration
        {
            get { return _igniteConfiguration; }
        }

        /** <inheritdoc /> */
        public T PluginConfiguration
        {
            get { return _pluginConfiguration; }
        }
    }
}
