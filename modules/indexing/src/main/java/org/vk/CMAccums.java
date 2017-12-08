package org.vk;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryRawReader;
import org.apache.ignite.binary.BinaryRawWriter;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

class CMAccums implements Binarylizable {

    private static final AtomicLong CTR = new AtomicLong();

//    public long historykey;
//    @QuerySqlField (index=true)
//    public String ActNumber;

    @QuerySqlField(index = true)
    public int CustomerId;

    @QuerySqlField
    public String d;

    @QuerySqlField
    public long id;

    @QuerySqlField
    public String merchantNumber;

    @QuerySqlField
    public double amount;

    @QuerySqlField
    public String t;

    @QuerySqlField
    public String cmhistory_String_Variable1;

    @QuerySqlField
    public String profile_String_Variable1;

    @QuerySqlField
    public long profile_long_Variable1;

    @QuerySqlField
    public long seprof_long_Variable1;

    @QuerySqlField
    public String cmhistory_String_Variable2;

    @QuerySqlField
    public String trs_String_Variable1;

    @QuerySqlField
    public String profile_String_Variable2;

    @QuerySqlField
    public String seprof_String_Variable7;

    @QuerySqlField
    public String profile_String_Variable3;

    @QuerySqlField
    public String profile_String_Variable4;

    @QuerySqlField
    public String cmhistory_String_Variable3;

    @QuerySqlField
    public String trs_String_Variable2;

    @QuerySqlField
    public String seprof_String_Variable2;

    @QuerySqlField
    public String seprof_String_Variable3;

    @QuerySqlField
    public long seprof_long_Variable2;

    @QuerySqlField
    public long trs_long_Variable1;

    @QuerySqlField
    public long trs_long_Variable2;

    @QuerySqlField
    public String seprof_String_Variable4;

    @QuerySqlField
    public String seprof_String_Variable5;

    @QuerySqlField
    public long profile_long_Variable2;

    @QuerySqlField
    public String trs_String_Variable3;

    @QuerySqlField
    public String seprof_String_Variable6;

    @QuerySqlField
    public double trs_double_Variable1;

    @QuerySqlField
    public double trs_double_Variable2;

    @QuerySqlField
    public double trs_double_Variable3;

    @QuerySqlField
    public double trs_double_Variable4;

    @QuerySqlField
    public double trs_double_Variable5;

    @QuerySqlField
    public int trs_int_Variable1;

    @QuerySqlField
    public double seprof_double_Variable;

    @QuerySqlField
    public double seprof_double_Variable1;

    @QuerySqlField
    public String profile_String_Variable5;

    @QuerySqlField
    public String profile_String_Variable6;

    CMAccums(int CustomerId, Random rand) {
        this.CustomerId = CustomerId;

        d = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        id = CTR.incrementAndGet();
        merchantNumber = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        amount = rand.nextDouble();
        t = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);

        cmhistory_String_Variable1 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        profile_String_Variable1 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        profile_long_Variable1 = rand.nextLong();
        seprof_long_Variable1 = rand.nextLong();
        cmhistory_String_Variable2 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        trs_String_Variable1 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        profile_String_Variable2 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        seprof_String_Variable7 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        profile_String_Variable3 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        profile_String_Variable4 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        cmhistory_String_Variable3 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        trs_String_Variable2 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        seprof_String_Variable2 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        seprof_String_Variable3 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        seprof_long_Variable2 = rand.nextLong();
        trs_long_Variable1 = rand.nextLong();
        trs_long_Variable2 = rand.nextLong();
        seprof_String_Variable4 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        seprof_String_Variable5 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        profile_long_Variable2 = rand.nextLong();
        trs_String_Variable3 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        seprof_String_Variable6 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        trs_double_Variable1 = rand.nextDouble();
        trs_double_Variable2 = rand.nextDouble();
        trs_double_Variable3 = rand.nextDouble();
        trs_double_Variable4 = rand.nextDouble();
        trs_double_Variable5 = rand.nextDouble();
        trs_int_Variable1 = rand.nextInt();
        seprof_double_Variable = rand.nextDouble();
        seprof_double_Variable1 = rand.nextDouble();
        profile_String_Variable5 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
        profile_String_Variable6 = RandomStringUtils.randomAlphabetic(rand.nextInt(12) + 1);
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeInt("CustomerId", CustomerId);

        BinaryRawWriter rawWriter = writer.rawWriter();

        rawWriter.writeString(d);
        rawWriter.writeLong(id);
        rawWriter.writeString(merchantNumber);
        rawWriter.writeDouble(amount);
        rawWriter.writeString(t);

        rawWriter.writeString(cmhistory_String_Variable1);
        rawWriter.writeString(profile_String_Variable1);
        rawWriter.writeLong(profile_long_Variable1);
        rawWriter.writeLong(seprof_long_Variable1);
        rawWriter.writeString(cmhistory_String_Variable2);
        rawWriter.writeString(trs_String_Variable1);
        rawWriter.writeString(profile_String_Variable2);
        rawWriter.writeString(seprof_String_Variable7);
        rawWriter.writeString(profile_String_Variable3);
        rawWriter.writeString(profile_String_Variable4);
        rawWriter.writeString(cmhistory_String_Variable3);
        rawWriter.writeString(trs_String_Variable2);
        rawWriter.writeString(seprof_String_Variable2);
        rawWriter.writeString(seprof_String_Variable3);
        rawWriter.writeLong(seprof_long_Variable2);
        rawWriter.writeLong(trs_long_Variable1);
        rawWriter.writeLong(trs_long_Variable2);
        rawWriter.writeString(seprof_String_Variable4);
        rawWriter.writeString(seprof_String_Variable5);
        rawWriter.writeLong(profile_long_Variable2);
        rawWriter.writeString(trs_String_Variable3);
        rawWriter.writeString(seprof_String_Variable6);
        rawWriter.writeDouble(trs_double_Variable1);
        rawWriter.writeDouble(trs_double_Variable2);
        rawWriter.writeDouble(trs_double_Variable3);
        rawWriter.writeDouble(trs_double_Variable4);
        rawWriter.writeDouble(trs_double_Variable5);
        rawWriter.writeInt(trs_int_Variable1);
        rawWriter.writeDouble(seprof_double_Variable);
        rawWriter.writeDouble(seprof_double_Variable1);
        rawWriter.writeString(profile_String_Variable5);
        rawWriter.writeString(profile_String_Variable6);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        CustomerId = reader.readInt("CustomerId");

        BinaryRawReader rawReader = reader.rawReader();

        d = rawReader.readString();
        id = rawReader.readLong();
        merchantNumber = rawReader.readString();
        amount = rawReader.readDouble();
        t = rawReader.readString();

        cmhistory_String_Variable1 = rawReader.readString();
        profile_String_Variable1 = rawReader.readString();
        profile_long_Variable1 = rawReader.readLong();
        seprof_long_Variable1 = rawReader.readLong();
        cmhistory_String_Variable2 = rawReader.readString();
        trs_String_Variable1 = rawReader.readString();
        profile_String_Variable2 = rawReader.readString();
        seprof_String_Variable7 = rawReader.readString();
        profile_String_Variable3 = rawReader.readString();
        profile_String_Variable4 = rawReader.readString();
        cmhistory_String_Variable3 = rawReader.readString();
        trs_String_Variable2 = rawReader.readString();
        seprof_String_Variable2 = rawReader.readString();
        seprof_String_Variable3 = rawReader.readString();
        seprof_long_Variable2 = rawReader.readLong();
        trs_long_Variable1 = rawReader.readLong();
        trs_long_Variable2 = rawReader.readLong();
        seprof_String_Variable4 = rawReader.readString();
        seprof_String_Variable5 = rawReader.readString();
        profile_long_Variable2 = rawReader.readLong();
        trs_String_Variable3 = rawReader.readString();
        seprof_String_Variable6 = rawReader.readString();
        trs_double_Variable1 = rawReader.readDouble();
        trs_double_Variable2 = rawReader.readDouble();
        trs_double_Variable3 = rawReader.readDouble();
        trs_double_Variable4 = rawReader.readDouble();
        trs_double_Variable5 = rawReader.readDouble();
        trs_int_Variable1 = rawReader.readInt();
        seprof_double_Variable = rawReader.readDouble();
        seprof_double_Variable1 = rawReader.readDouble();
        profile_String_Variable5 = rawReader.readString();
        profile_String_Variable6 = rawReader.readString();
    }
}
