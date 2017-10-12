package org.apache.ignite.examples;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * Created by Vyacheslav Koptilin on 12.10.2017.
 */
public class TestValue {
    @QuerySqlField
    public Double int_val;

    @QuerySqlField
    public String name;
}
