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

package org.apache.ignite.visor.commands.cache

import java.util.UUID

import org.apache.ignite.internal.visor.cache._
import org.apache.ignite.internal.visor.util.VisorTaskUtils._
import org.apache.ignite.visor.commands.cache.VisorModifyCommand._
import org.apache.ignite.visor.visor._

import scala.language.{implicitConversions, reflectiveCalls}

/**
 * ==Overview==
 * Visor 'modify' command implementation.
 *
 * ==Help==
 * {{{
 * +-----------------------------------------------------------------------------------------+
 * | modify -put    | Put custom value into cache.                                           |
 * +-----------------------------------------------------------------------------------------+
 * | modify -get    | Get value with specified key from cache.                               |
 * +-----------------------------------------------------------------------------------------+
 * | modify -remove | Remove value with specified key form cache.                            |
 * +-----------------------------------------------------------------------------------------+
 *
 * }}}
 *
 * ====Specification====
 * {{{
 *     modify -put -c=<cache-name> {-kt=<key-type> -kv=<key-value>} {-vt=<value-type> -vv=<value-value>}
 *     modify -get -c=<cache-name> {-kt=<key-type> -kv=<key-value>}
 *     modify -remove -c=<cache-name> {-kt=<key-type> -kv=<key-value>}
 * }}}
 *
 * ====Arguments====
 * {{{
 *     -c=<cache-name>
 *         Name of the cache.
 *     -kt=<key-type>
 *         Type of key
 *     -kv=<key-value>
 *         Value of key
 *     -vt=<value-type>
 *         Type of value
 *     -vv=<value-type>
 *         Value of value
 * }}}
 *
 * ====Examples====
 * {{{
 *     modify -put -c=@c0
 *         Put value into cache in interactive mode.
 *     modify -get -c=@c0
 *         Get value from cache in interactive mode.
 *     modify -remove -c=@c0
 *         Remove value form cache in interactive mode.
 *     modify -put -c=cache -kt=java.lang.String -kv=key1 -vt=lava.lang.String -vv=value1
 *         Put value into cache with name cache with key of String type equal to key1
 *         and value of String type equal to value1
 *     modify -get -c=cache -kt=java.lang.String -kv=key1
 *         Get value from cache with name cache with key of String type equal to key1
 *     modify -remove -c=cache -kt=java.lang.String -kv=key1
 *         Remove value from cache with name cache with key of String type equal to key1.
 *
 * }}}
 */
class VisorModifyCommand {
    /**
     * Prints error message and advise.
     *
     * @param errMsgs Error messages.
     */
    private def scold(errMsgs: Any*) {
        assert(errMsgs != null)

        warn(errMsgs: _*)
        warn("Type 'help cache' to see how to use this command.")
    }

    /**
     * ===Command===
     * Modify cache value in specified cache.
     *
     * ===Examples===
     * <ex>modify -put</ex>
     *     Put value into cache in interactive mode.
     * <br>
     * <ex>modify -get</ex>
     *     Get value from cache in interactive mode.
     * <br>
     * <ex>modify -remove</ex>
     *     Remove value form cache in interactive mode.
     * <br>
     * <ex>modify -put -c=cache -kt=java.lang.String -kv=key1 -vt=lava.lang.String -vv=value1</ex>
     *     Put value into cache with name cache with key of String type equal to key1
     *     and value of String type equal to value1
     * <br>
     * <ex>modify -get -c=cache -kt=java.lang.String -kv=key1</ex>
     *     Get value from cache with name cache with key of String type equal to key1
     * <br>
     * <ex>modify -remove -c=cache -kt=java.lang.String -kv=key1</ex>
     *     Remove value from cache with name cache with key of String type equal to key1.
     *
     * @param args Command arguments.
     */
    def modify(args: String) {
        if (!isConnected)
            adviseToConnect()
        else {
            var argLst = parseArgs(args)

            val put = hasArgFlag("put", argLst)
            val get = hasArgFlag("get", argLst)
            val remove = hasArgFlag("remove", argLst)

            if (!put && !get && !remove) {
                warn("Put, get, or remove operation should be specified")

                return
            }

            if (put && get || get && remove || get && remove) {
                warn("Only one operation put, get or remove allowed in one command invocation")

                return
            }

            if (!hasArgName("c", argLst)) {
                warn("Cache name should be specified")

                return
            }

            var cacheName = argValue("c", argLst) match {
                case Some(dfltName) if dfltName == DFLT_CACHE_KEY || dfltName == DFLT_CACHE_NAME =>
                    argLst = argLst.filter(_._1 != "c") ++ Seq("c" -> null)

                    Some(null)

                case cn => cn
            }

            if (cacheName.isEmpty) {
                warn("Cache with specified name is not found")

                return
            }

            val keyTypeStr = argValue("kt", argLst)
            val keyValueStr = argValue("kv", argLst)
            var key: Object = null

            if (keyTypeStr.isEmpty != keyValueStr.isEmpty) {
                warn("Both key type and key value should be specified")

                return
            }

            val valueTypeStr = argValue("vt", argLst)
            val valueValueStr = argValue("vv", argLst)
            var value: Object = null

            if (valueTypeStr.isEmpty != valueValueStr.isEmpty) {
                warn("Both value type and value value should be specified")

                return
            }

            keyTypeStr match {
                case Some(clsStr) =>
                    val cls = Class.forName(clsStr)

                    INPUT_TYPES.find(_._3 == cls) match {
                        case Some(t) => key = t._2(keyValueStr.get)
                        case None =>
                            warn("Specified type is not allowed")

                            return
                    }

                case None =>
                    askTypedValue("key") match {
                        case Some(k) if k.toString.nonEmpty => key = k
                        case _ =>
                            warn("Key can not be empty.")

                            return
                    }
            }

            if (put) {
                valueTypeStr match {
                    case Some(clsStr) =>
                        val cls = Class.forName(clsStr)

                        INPUT_TYPES.find(_._3 == cls) match {
                            case Some(t) => value = t._2(valueValueStr.get)
                            case None => warn("Specified type is not allowed")

                                return
                        }

                    case None =>
                        askTypedValue("value") match {
                            case Some(v) if v.toString.nonEmpty => value = v
                            case _ =>
                                warn("Value can not be empty.")

                                return
                        }
                }
            }

            if ((get || remove) && valueTypeStr.nonEmpty)
                warn("Specified value is not used by selected operation and will be ignored")

            val arg = new VisorCacheModifyTaskArg(cacheName.get,
                if (put) VisorModifyCacheMode.PUT else if (get) VisorModifyCacheMode.GET else VisorModifyCacheMode.REMOVE,
                key, value
            )

            val taskResult = executeRandom(classOf[VisorCacheModifyTask], arg)
            val resultObj = taskResult.getResult
            val affinityNode = taskResult.getAffinityNode

            if (put) {
                println("Put operation success" + "; Affinity node: " + nid8(affinityNode))

                if (resultObj != null)
                    println("Previous value is: " + resultObj)
            }

            if (get) {
                if (resultObj != null)
                    println("Value with specified key: " + resultObj + "; Affinity node: " + nid8(affinityNode))
                else
                    println("Value with specified key not found")
            }

            if (remove) {
                if (resultObj != null)
                    println("Removed value: " + resultObj + "; Affinity node: " + nid8(affinityNode))
                else
                    println("Value with specified key not found")
            }
        }
    }

    /**
     * ===Command===
     * Modify cache data by execution of put/get/remove command.
     *
     * ===Examples===
     * <ex>modify -put -c=@c0</ex>
     * Put entity in cache with alias @c0 in interactive mode
     */
    def modify() {
        this.modify("")
    }
}

/**
 * Companion object that does initialization of the command.
 */
object VisorModifyCommand {
    /** Singleton command */
    private val cmd = new VisorModifyCommand

    /** Default cache name to show on screen. */
    private final val DFLT_CACHE_NAME = escapeName(null)

    /** Default cache key. */
    protected val DFLT_CACHE_KEY: String = DFLT_CACHE_NAME + "-" + UUID.randomUUID().toString

    addHelp(
        name = "modify",
        shortInfo = "Modify cache by put/get/remove value.",
        longInfo = Seq(
            "Execute modification of cache data:",
            " ",
            "Put new value into cache.",
            " ",
            "Get value from cache.",
            " ",
            "Remove value from cache."
        ),
        spec = Seq(
            "modify -put -c=<cache-name> {-kt=<key-type> -kv=<key-value>} {-vt=<value-type> -vv=<value-value>}",
            "modify -get -c=<cache-name> {-kt=<key-type> -kv=<key-value>}",
            "modify -remove -c=<cache-name> {-kt=<key-type> -kv=<key-value>}"
    ),
        args = Seq(
            "-c=<cache-name>" ->
                "Name of the cache",
            "-kt=<key-type>" ->
                "Type of key",
            "-kv=<key-value>" ->
                "Value of key",
            "-vt=<value-type>" ->
                "Type of value",
            "-vv=<value-type>" ->
                "Value of value"
        ),
        examples = Seq(
            "modify -put -c=@c0" ->
                "Put value into cache in interactive mode.",
            "modify -get -c=@c0" ->
                "Get value from cache in interactive mode.",
            "modify -remove -c=@c0" ->
                "Remove value form cache in interactive mode.",
            "modify -put -c=@c0 -kt=java.lang.String -kv=key1 -vt=lava.lang.String -vv=value1" -> Seq(
                "Put value into cache with name @c0 with key of String type equal to key1",
                "and value of String type equal to value1"
            ),
            "modify -get -c=@c0 -kt=java.lang.String -kv=key1" ->
                "Get value from cache with name @c0 with key of String type equal to key1",
            "modify -remove -c=@c0 -kt=java.lang.String -kv=key1" ->
                "Remove value from cache with name @c0 with key of String type equal to key1."
        ),
        emptyArgs = cmd.modify,
        withArgs = cmd.modify
    )

    /**
     * Singleton.
     */
    def apply() = cmd
}
