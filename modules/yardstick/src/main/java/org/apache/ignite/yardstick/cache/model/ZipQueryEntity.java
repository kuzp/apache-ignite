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

package org.apache.ignite.yardstick.cache.model;

import org.jsr166.ThreadLocalRandom8;

import static org.apache.ignite.yardstick.cache.model.ZipEntity.randomString;

/**
 *
 */
public class ZipQueryEntity {
    public Double TOTALVALUE;
    public String SYS_AUDIT_TRACE;
    public String BUSINESSDATE;
    public String BOOKSOURCESYSTEMCODE;

    public static ZipQueryEntity generateHard() {
        ZipQueryEntity entity = new ZipQueryEntity();

        ThreadLocalRandom8 rnd = ThreadLocalRandom8.current();

        entity.BUSINESSDATE = "2017-06-30";
        entity.SYS_AUDIT_TRACE = randomString(rnd, 200);
        entity.TOTALVALUE = rnd.nextDouble();
        entity.BOOKSOURCESYSTEMCODE = randomString(rnd, 10);

        return entity;

    }
}
