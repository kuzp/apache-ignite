package org.apache.ignite.internal.pagemem.wal.record;

public class ExchangeRecord extends TimeStampRecord {
    /** Event. */
    private String constId;

    /** Type. */
    private Type type;

    /**
     * @param constId Const id.
     * @param type Type.
     */
    public ExchangeRecord(String constId, Type type) {
        this.constId = constId;
        this.type = type;
    }

    /**
     *
     */
    public ExchangeRecord(Type type, String constId) {
        this.type = type;
        this.constId = constId;
    }

    /** {@inheritDoc} */
    @Override public RecordType type() {
        return RecordType.EXCHANGE;
    }

    /**
     *
     */
    public String getConstId() {
        return constId;
    }

    /**
     *
     */
    public Type getType() {
        return type;
    }

    public enum Type {
        /** Join. */
        JOIN,
        /** Left. */
        LEFT
    }
}
