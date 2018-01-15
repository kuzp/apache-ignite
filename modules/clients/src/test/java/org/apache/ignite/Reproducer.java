package org.apache.ignite;

import org.apache.ignite.cache.affinity.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.util.tostring.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.junit.*;

import java.net.*;
import java.sql.*;
import java.util.*;

import static org.junit.Assert.*;

/** A temporary class to demonstrate a bug. Move the test to appropriate place when fixing the bug. */
public class Reproducer {
    /** Default cache name. */
    private static final String DEFAULT_CACHE_NAME = "default";

    /** Cache name. */
    private static final String CACHE_NAME = "PERSON";

    /**
     * 1. Create a cache with BINARY affinity key using JDBC thin client.
     * 2. Add an entry using Java API.
     * 3. Select the entry using JDBC thin client.
     */
    @Test
    public void javaPutIntoSqlCacheWithBinaryAffinityKey() throws SQLException {
        try (Ignite srv = Ignition.start(getServerConfig());
             Ignite cln = Ignition.start(getClientConfig());
             Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/")
        ) {
            conn.prepareStatement(
                "CREATE TABLE " + CACHE_NAME + "(" +
                    "ssn BINARY(16), orgId BINARY(16), name BINARY(16), PRIMARY KEY(ssn, orgId)" +
                    ") WITH \"affinitykey=orgId,value_type=org.apache.ignite.Reproducer$Person\""
            ).execute();

            IgniteCache<AffinityKey<byte[]>, Person> cache = cln.cache("SQL_PUBLIC_" + CACHE_NAME);

            AffinityKey<byte[]> key = new AffinityKey<>(new byte[] {1, 2}, new byte[] {3, 4});

            cache.put(key, new Person(key.key(), key.affinityKey(), new byte[] {5, 6}));

            List<Person> entries = convert(conn.prepareStatement("SELECT * from " + CACHE_NAME).executeQuery());

            assertEquals("1 person must be in the cache", 1, entries.size());

            assertArrayEquals("Person SSN must be same as affinity key's key", key.key(), entries.get(0).getSsn());
        }
    }

    /** */
    private List<Person> convert(ResultSet resSet) throws SQLException {
        List<Person> res = new ArrayList<>();

        while (resSet.next())
            res.add(new Person(resSet.getBytes(1), resSet.getBytes(2), resSet.getBytes(3)));

        return res;
    }

    /** */
    private static IgniteConfiguration getServerConfig() {
        IgniteConfiguration igniteCfg = new IgniteConfiguration();

        igniteCfg.setIgniteInstanceName("server");

        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

        TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder();

        ipFinder.registerAddresses(
            Collections.singleton(new InetSocketAddress(InetAddress.getLoopbackAddress(), 47500)));

        discoverySpi.setIpFinder(ipFinder);

        igniteCfg.setDiscoverySpi(discoverySpi);

        return igniteCfg;
    }

    /** */
    private static IgniteConfiguration getClientConfig() {
        IgniteConfiguration igniteCfg = getServerConfig();

        igniteCfg.setIgniteInstanceName("client");

        igniteCfg.setClientMode(true);

        CacheConfiguration cacheCfg = new CacheConfiguration<>(DEFAULT_CACHE_NAME).setSqlSchema("PUBLIC");

        igniteCfg.setCacheConfiguration(cacheCfg);

        return igniteCfg;
    }

    /** */
    public static class Person {
        /** Ssn. */
        @GridToStringInclude
        private final byte[] ssn;

        /** Org id. */
        @GridToStringInclude
        private final byte[] orgId;

        /** Name. */
        @GridToStringInclude
        private final byte[] name;

        /** Constructor. */
        public Person(byte[] ssn, byte[] orgId, byte[] name) {
            this.ssn = ssn;
            this.name = name;
            this.orgId = orgId;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return S.toString(Person.class, this);
        }

        /** */
        public byte[] getSsn() {
            return ssn;
        }
    }
}