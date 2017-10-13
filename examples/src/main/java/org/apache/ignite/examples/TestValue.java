package org.apache.ignite.examples;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * Created by Vyacheslav Koptilin on 12.10.2017.
 */
public class TestValue {
    /** Value for posnId. */
    @QuerySqlField(index = true)
    public String posnId;

    /** Value for posnVerN. */
    @QuerySqlField
    public Double posnVerN;

    /** Value for accId. */
    @QuerySqlField
    public String accId;

    /** Value for accN. */
    @QuerySqlField
    public String accN;

    public TestValue(){}

    public TestValue(String posnId, Double posnVerN, String accId, String accN) {
        this.posnId = posnId;
        this.posnVerN = posnVerN;
        this.accId = accId;
        this.accN = accN;
    }
}
