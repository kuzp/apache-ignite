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

package org.apache.ignite.yardstick.cache;

import java.util.Map;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.yardstick.cache.model.Position;
import org.apache.ignite.yardstick.cache.model.SampleValue;
import org.yardstickframework.BenchmarkConfiguration;

import static org.yardstickframework.BenchmarkUtils.println;

public class IgnitePutGetUpdateBenchmark extends IgniteCacheAbstractBenchmark<Integer, Object> {
    /** {@inheritDoc} */
    @Override public void setUp(BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);

        if (args.preloadAmount() > args.range())
            throw new IllegalArgumentException("Preloading amount (\"-pa\", \"--preloadAmount\") " +
                "must by less then the range (\"-r\", \"--range\").");

        String cacheName = cache().getName();

        println(cfg, "Loading data for cache: " + cacheName);

        long start = System.nanoTime();

        try (IgniteDataStreamer<Object, Object> dataLdr = ignite().dataStreamer(cacheName)) {
            for (int i = 0; i < args.preloadAmount(); i++) {
                dataLdr.addData(i, createPosition(i));

                if (i % 100000 == 0) {
                    if (Thread.currentThread().isInterrupted())
                        break;

                    println("Loaded entries: " + i);
                }
            }
        }

        println(cfg, "Finished populating query data in " + ((System.nanoTime() - start) / 1_000_000) + " ms.");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        int key = nextRandom(args.range());

        cache.put(key, createPosition(key));

        return true;
    }

    /** {@inheritDoc} */
    @Override protected IgniteCache<Integer, Object> cache() {
        return ignite().cache("test-fd-atomic");
    }

    private Position createPosition(int key) {
        Position p = new Position(
            "posnId",    //String posnId,
            1L,          //Long posnVerN,
            "accId",     //String accId,
            "accN",      //String accN,
            "csdnC",     //String csdnC,
            "accTyC",    //String accTyC,
            "issIdTyC",  //String issIdTyC,
            "issId",     //String issId,
            "issSrcC",   //String issSrcC,
            1,           //Integer issEffD,
            "srcN",      //String srcN,
            "srcNm",     //String srcNm,
            "plnN",      //String plnN,
            "currC",     //String currC,
            1d,          //Double clsPrcA,
            1d,          //Double tdMkvlA,
            1d,          //Double sdMkvlA,
            "tdLngShtC", //String tdLngShtC,
            "sdLngShtC", //String sdLngShtC,
            1d,          //Double tdQtyA,
            1d,          //Double sdQtyA,
            1d,          //Double sfkShrQ,
            1d,          //Double trnfShrQ,
            1d,          //Double lglTrnfShrQ,
            1d,          //Double nonNegShrQ,
            1d,          //Double pldgShrQ,
            "mkvlC",     //String mkvlC,
            1,           //Integer loclMktPrcD,
            1d,          //Double loclPrcA,
            1d,          //Double loclTdMkvlA,
            1d,          //Double cnvPrcA,
            "mulrDivC",  //String mulrDivC,
            1d,          //Double loclSdMkvlA,
            "ctryC",     //String ctryC,
            "asetClC",   //String asetClC,
            1d,          //Double clsQA,
            1d,          //Double cbasA,
            1d,          //Double cbasGnlsA,
            1d,          //Double mkvlAcriA,
            1d,          //Double portP,
            1d,          //Double avgCostA,
            1d,          //Double ttCostA,
            1d,          //Double acruIntA,
            1d,          //Double ltShrA,
            1d,          //Double stShrA,
            1,           //Integer asofD,
            "cdpPrmFirmC",//String cdpPrmFirmC,
            "regTyC",    //String regTyC,
            "accClsfC",  //String accClsfC,
            "prdtLvC",   //String prdtLvC,
            "prdtLvSbtyC", //String prdtLvSbtyC,
            "lineOfBusC", //String lineOfBusC,
            "rconPptgC", //String rconPptgC,
            "pmaPptgC", //String pmaPptgC,
            "secTyC", //String secTyC,
            "prdtC", //String prdtC,
            "symbC", //String symbC,
            1d, //Double prcMulrA,
            "coreFundI", //String coreFundI,
            "coreFundC", //String coreFundC,
            1d, //Double trdDShtQ,
            1d, //Double setlDShtQ,
            1, //Integer adjD,
            "adjAcnC", //String adjAcnC,
            "adjRsnC", //String adjRsnC,
            1, //Integer fileRunD,
            "fileAccSrcC", //String fileAccSrcC,
            1l, //Long insrTmst,
            "runId", //String runId,
            1d, //Double origTdMkvlA,
            1d, //Double origTdQtyA,
            "accSrcC", //String accSrcC,
            1 //Integer runD
        );
        p.setPosnId(Integer.toString(key));

        return p;
    }
}
