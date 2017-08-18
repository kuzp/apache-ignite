package org.apache.ignite.examples.loader;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

/**
 * Created by admin on 8/17/2017.
 */
public class DepoHist implements Binarylizable {
    @OraName(value = "id")
    @InitOrder(value = "1")
    @DataType(value = TransformType.LONG)
    @IdField(value="true")
    public long id;

    @OraName(value = "partitionid")
    @InitOrder(value = "2")
    @DataType(value = TransformType.LONG)
    @PartField(value="true")
    public long partitionъid;

    @OraName(value = "clientid")
    @InitOrder(value = "3")
    @DataType(value = TransformType.LONG)
    @RootField(value="true")
    public long rootParticleъid;

    @DataType(value = TransformType.ROOT)
    @Default(value = "0")
    public long affinityParentъid;

//    @OraName(value = "templateId")
//    @DataType(value = TransformType.LONG)
//    public long templateId;

    @OraName(value = "operationid")
    @InitOrder(value = "4")
    @DataType(value = TransformType.LONG)
    public long operationRunъid;

    @DataType(value = TransformType.PARTITION)
    @PartForField(value = "operationRunъid")
    @Default(value = "0")
    public long operationRunъpartId;

    @DataType(value = TransformType.ROOT)
    @PartForField(value = "operationRunъid")
    @Default(value = "0")
    public long operationRunъrootId;

    @OraName(value = "C_ATMID")
    @InitOrder(value = "5")
    @DataType(value = TransformType.STRING)
    public String cardhistAtmid;

    @OraName(value = "C_AUTHCODE")
    @InitOrder(value = "6")
    @DataType(value = TransformType.STRING)
    public String cardhistAuthcode;

    @OraName(value = "C_AUTHKIND")
    @InitOrder(value = "7")
    @DataType(value = TransformType.LONG)
    public Long cardhistAuthkind;

    @OraName(value = "C_CARDNO")
    @InitOrder(value = "8")
    @DataType(value = TransformType.STRING)
    public String cardhistCardno;

    @OraName(value = "C_MERCHANTNO")
    @InitOrder(value = "9")
    @DataType(value = TransformType.STRING)
    public String cardhistMerchantno;

    @OraName(value = "C_TXCASH")
    @InitOrder(value = "10")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal cardhistTxcash;

    @OraName(value = "C_TXCURRENCY")
    @InitOrder(value = "11")
    @DataType(value = TransformType.LONG)
    public Long cardhistTxcurrency;

    @OraName(value = "C_TXID")
    @InitOrder(value = "12")
    @DataType(value = TransformType.LONG)
    public Long cardhistTxid;

    @OraName(value = "C_TXTIME")
    @InitOrder(value = "13")
    @DataType(value = TransformType.DATE_TIME)
    public Date cardhistTxtime;

    @OraName(value = "D_ASSIGNDAY")
    @InitOrder(value = "14")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistAssignday;

    @OraName(value = "D_ASSMINBALANCE")
    @InitOrder(value = "15")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistAssminbalance;

    @OraName(value = "D_BALANCE")
    @InitOrder(value = "16")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistBalance;

    @OraName(value = "D_BALANCEF")
    @InitOrder(value = "17")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistBalanceOwn;

    @OraName(value = "D_BALANCEF")
    @InitOrder(value = "18")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistSubsidy;

    @OraName(value = "D_BRANCHNO")
    @InitOrder(value = "19")
    @DataType(value = TransformType.LONG)
    public Long depohist_branchno;

    @OraName(value = "D_CARDRPTDAY")
    @InitOrder(value = "20")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistCardrptday;

    @OraName(value = "D_CASHSOURCE")
    @InitOrder(value = "21")
    @DataType(value = TransformType.LONG)
    public Long depohistCashsource;

    @OraName(value = "D_CHEQUECNT")
    @InitOrder(value = "22")
    @DataType(value = TransformType.LONG)
    public Long depohistChequecnt;

    @OraName(value = "D_CLERK")
    @InitOrder(value = "23")
    @DataType(value = TransformType.LONG)
    public Long depohistClerk;

    @OraName(value = "D_CONVER_CURRENCY")
    @InitOrder(value = "24")
    @DataType(value = TransformType.LONG)
    public Long depohistConverCurrency;

    @OraName(value = "D_CONVER_OPCASH")
    @InitOrder(value = "25")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistConverOpcash;

    @OraName(value = "D_CREDITACCOUNT")
    @InitOrder(value = "26")
    @DataType(value = TransformType.STRING)
    public String depohistCreditaccount;

    @OraName(value = "D_DEBITACCOUNT")
    @InitOrder(value = "27")
    @DataType(value = TransformType.STRING)
    public String depohistDebitaccount;

    @OraName(value = "D_DEPOSITRATE")
    @InitOrder(value = "28")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistDepositrate;

    @OraName(value = "D_EXPIRATIONDAY")
    @InitOrder(value = "29")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistExpirationday;

    @OraName(value = "D_EXPMINBALANCE")
    @InitOrder(value = "30")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistExpminbalance;

    @OraName(value = "D_EXPROVERDRAFT")
    @InitOrder(value = "31")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistExproverdraft;

    @OraName(value = "D_EXPROVERDRAFTINT")
    @InitOrder(value = "32")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistExproverdraftint;

    @OraName(value = "D_EXTERNALKIND")
    @InitOrder(value = "33")
    @DataType(value = TransformType.LONG)
    public Long depohistExternalkind;

    @OraName(value = "D_FLAG_CASH")
    @InitOrder(value = "34")
    @DataType(value = TransformType.LONG)
    public Long depohistFlagCash;

    @OraName(value = "D_GRANT_OPER")
    @InitOrder(value = "35")
    @DataType(value = TransformType.LONG)
    public Long depohistGrantOper;

    @OraName(value = "D_HEIRNO")
    @InitOrder(value = "36")
    @DataType(value = TransformType.LONG)
    public Long depohistHeirno;

    @OraName(value = "D_ID_MEGA")
    @InitOrder(value = "37")
    @DataType(value = TransformType.LONG)
    public Long depohist_tb;

    @OraName(value = "D_INSERTTIME")
    @InitOrder(value = "38")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistInserttime;

    @OraName(value = "D_INTEREST")
    @InitOrder(value = "39")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistInterest;

    @OraName(value = "D_INTERESTF")
    @InitOrder(value = "40")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistInterestf;

    @OraName(value = "D_ISCASHDISPENSER")
    @InitOrder(value = "41")
    @DataType(value = TransformType.LONG)
    public Long depohistIscashdispenser;

    @OraName(value = "D_ISMANUAL")
    @InitOrder(value = "42")
    @DataType(value = TransformType.LONG)
    public Long depohistIsmanual;

    @OraName(value = "D_ISMOFFICE")
    @InitOrder(value = "43")
    @DataType(value = TransformType.LONG)
    public Long depohistIsmoffice;

    @OraName(value = "D_JRNNO")
    @InitOrder(value = "44")
    @DataType(value = TransformType.LONG)
    public Long depohistJrnno;

    @OraName(value = "D_MAXAMOUNT")
    @InitOrder(value = "45")
    @DataType(value = TransformType.LONG)
    public Long depohistMaxamount;

    @OraName(value = "D_MAXAMOUNTRATE")
    @InitOrder(value = "46")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistMaxamountrate;

    @OraName(value = "D_N_DPRICE")
    @InitOrder(value = "47")
    @DataType(value = TransformType.LONG)
    public Long depohistNDprice;

    @OraName(value = "D_OFFCASHBALANCE")
    @InitOrder(value = "48")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistOffcashbalance;

    @OraName(value = "D_OFFCASHBALANCEB")
    @InitOrder(value = "49")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistOffcashbalanceb;

    @OraName(value = "D_OFFICE")
    @InitOrder(value = "50")
    @DataType(value = TransformType.LONG)
    public Long depohistOffice;

    @OraName(value = "D_OFFICETRANSDAY")
    @InitOrder(value = "51")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistOfficetransday;

    @OraName(value = "D_OPCASH")
    @InitOrder(value = "52")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistOpcash;

    @OraName(value = "D_OPCASHCOST")
    @InitOrder(value = "53")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistOpcashcost;

    @OraName(value = "D_OPCODE")
    @InitOrder(value = "54")
    @DataType(value = TransformType.LONG)
    public Long depohistOpcode;

    @OraName(value = "D_OPDAY")
    @InitOrder(value = "55")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistOpday;

    @OraName(value = "D_OPENCASH")
    @InitOrder(value = "56")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistOpencash;

    @OraName(value = "D_OPKIND")
    @InitOrder(value = "57")
    @DataType(value = TransformType.LONG)
    public Long depohistOpkind;

    @OraName(value = "D_OPNO")
    @InitOrder(value = "58")
    @DataType(value = TransformType.LONG)
    public Long depohistOpno;

    @OraName(value = "D_OPTRANSDAY")
    @InitOrder(value = "59")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistOptransday;

    @OraName(value = "D_ORDERNO")
    @InitOrder(value = "60")
    @DataType(value = TransformType.STRING)
    public String depohistOrderno;

    @OraName(value = "D_OVERDRAFT")
    @InitOrder(value = "61")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistMinBalanceMonth;

    @OraName(value = "D_OVERDRAFT")
    @InitOrder(value = "62")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistBalanceOwnProlong;

    @OraName(value = "D_OVERDRAFT")
    @InitOrder(value = "63")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistOverdraft;

    @OraName(value = "D_OVERDRAFTINT")
    @InitOrder(value = "64")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistOverdraftint;

    @OraName(value = "D_PAIRACCOUNT")
    @InitOrder(value = "65")
    @DataType(value = TransformType.STRING)
    public String depohistPairAccount;

    @OraName(value = "D_PARTRATE")
    @InitOrder(value = "66")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistPartrate;

    @OraName(value = "D_PAYROLLDAY")
    @InitOrder(value = "67")
    @DataType(value = TransformType.DATE_TIME)
    public Date depositPayrollday;

    @OraName(value = "D_PAYROLLDAY")
    @InitOrder(value = "68")
    @DataType(value = TransformType.DATE_TIME)
    public Date depositPayAddFirst;

    @OraName(value = "D_PERCENTSRATE")
    @InitOrder(value = "69")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistPercentsrate;

    @OraName(value = "D_PINACCEPTFLAG")
    @InitOrder(value = "70")
    @DataType(value = TransformType.LONG)
    public Long depohistPinacceptflag;

    @OraName(value = "D_PROLONGDAY")
    @InitOrder(value = "71")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistProlongday;

    @OraName(value = "D_RATESOURCE")
    @InitOrder(value = "72")
    @DataType(value = TransformType.LONG)
    public Long depohistRatesource;

    @OraName(value = "D_REASON")
    @InitOrder(value = "73")
    @DataType(value = TransformType.LONG)
    public Long depohistReason;

    @OraName(value = "D_SBOOKENDDAY")
    @InitOrder(value = "74")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistSbookendday;

    @OraName(value = "D_SOURCEDOCDAY")
    @InitOrder(value = "75")
    @DataType(value = TransformType.DATE_TIME)
    public Date depohistSourcedocday;

    @OraName(value = "D_SOURCEDOCNO")
    @InitOrder(value = "76")
    @DataType(value = TransformType.STRING)
    public String depohistSourcedocno;

    @OraName(value = "D_SOURCEDOCORIGINATOR")
    @InitOrder(value = "77")
    @DataType(value = TransformType.STRING)
    public String depohistSourcedocoriginator;

    @OraName(value = "D_STATE")
    @InitOrder(value = "78")
    @DataType(value = TransformType.LONG)
    public Long depohistState;

    @OraName(value = "D_SUBSYS")
    @InitOrder(value = "79")
    @DataType(value = TransformType.LONG)
    public Long depohistSubsys;

    @OraName(value = "D_SUMOBNALOPERATION")
    @InitOrder(value = "80")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistSumobnaloperation;

    @OraName(value = "D_TAXABLEPROFIT")
    @InitOrder(value = "81")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistTaxableprofit;

    @OraName(value = "D_TAXABLEPROFITCOST")
    @InitOrder(value = "82")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistTaxableprofitcost;

    @OraName(value = "D_TERMSOK")
    @InitOrder(value = "83")
    @DataType(value = TransformType.LONG)
    public Long depohistTermsok;

    @OraName(value = "D_TURNCODE")
    @InitOrder(value = "84")
    @DataType(value = TransformType.LONG)
    public Long depohistTurncode;

    @OraName(value = "D_USEDTAXEXEMPTIONS")
    @InitOrder(value = "85")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal depohistUsedtaxexemptions;

    @OraName(value = "D_V_HZAP2")
    @InitOrder(value = "86")
    @DataType(value = TransformType.LONG)
    public Long depohistVHzap2;

    @OraName(value = "D_WARRANTKIND")
    @InitOrder(value = "87")
    @DataType(value = TransformType.LONG)
    public Long depohistWarrantkind;

    @OraName(value = "D_WARRANTNO")
    @InitOrder(value = "88")
    @DataType(value = TransformType.STRING)
    public String depohistWarrantno;

    @OraName(value = "O_INFO")
    @InitOrder(value = "89")
    @DataType(value = TransformType.STRING)
    public String offcashcompInfo;

    @OraName(value = "O_OPDAY")
    @InitOrder(value = "90")
    @DataType(value = TransformType.DATE_TIME)
    public Date offcashcompOpday;

    @OraName(value = "O_OPNO")
    @InitOrder(value = "91")
    @DataType(value = TransformType.LONG)
    public Long offcashcompOpno;

    @OraName(value = "O_ORDNO")
    @InitOrder(value = "92")
    @DataType(value = TransformType.STRING)
    public String offcashcompOrdno;

    @OraName(value = "O_SSAGENCY")
    @InitOrder(value = "93")
    @DataType(value = TransformType.STRING)
    public String offcashcompSsagency;

    @OraName(value = "O_SSN")
    @InitOrder(value = "94")
    @DataType(value = TransformType.STRING)
    public String offcashcompSsn;

    @OraName(value = "O_V_DNUM1")
    @InitOrder(value = "95")
    @DataType(value = TransformType.LONG)
    public Long offcashcompVDnum1;

    @OraName(value = "O_V_DSUM1")
    @InitOrder(value = "96")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal offcashcompVDsum1;

    @OraName(value = "O_V_DSUM2")
    @InitOrder(value = "97")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal offcashcompVDsum2;

    @OraName(value = "O_V_DSUM3")
    @InitOrder(value = "98")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal offcashcompVDsum3;

    @OraName(value = "O_V_DSUM4")
    @InitOrder(value = "99")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal offcashcompVDsum4;

    @OraName(value = "O_V_DSUM5")
    @InitOrder(value = "100")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal offcashcompVDsum5;

    @OraName(value = "O_V_DSUM6")
    @InitOrder(value = "101")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal offcashcompVDsum6;

    @OraName(value = "O_V_DSUM7")
    @InitOrder(value = "102")
    @DataType(value = TransformType.BIGDECIMAL)
    public BigDecimal offcashcompVDsum7;

    @OraName(value = "category")
    @InitOrder(value = "103")
    @DataType(value = TransformType.STRING)
    public String category;

    public Long depohistTerm;

    @OraName(value = "code")
    @DataType(value = TransformType.STRING)
    public String code;

    @OraName(value = "name")
    @DataType(value = TransformType.STRING)
    public String name;

    @OraName(value = "sourceid")
    @DataType(value = TransformType.STRING)
    public String sourceId;

    @OraName(value = "sourcesystemid")
    @DataType(value = TransformType.STRING)
    public String sourceSystemId;

    public Long templObjId;

    public Long parentObjId;

    @OraName(value = "objectid")
    @InitOrder(value = "1")
    @DataType(value = TransformType.LONG)
    public long objectId;

    public boolean isDeleted=false;

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DepoHist hist = (DepoHist)o;

        if (id != hist.id)
            return false;
        if (partitionъid != hist.partitionъid)
            return false;
        if (rootParticleъid != hist.rootParticleъid)
            return false;
        if (affinityParentъid != hist.affinityParentъid)
            return false;
        if (operationRunъid != hist.operationRunъid)
            return false;
        if (operationRunъpartId != hist.operationRunъpartId)
            return false;
        if (operationRunъrootId != hist.operationRunъrootId)
            return false;
        if (objectId != hist.objectId)
            return false;
        if (isDeleted != hist.isDeleted)
            return false;
        if (cardhistAtmid != null ? !cardhistAtmid.equals(hist.cardhistAtmid) : hist.cardhistAtmid != null)
            return false;
        if (cardhistAuthcode != null ? !cardhistAuthcode.equals(hist.cardhistAuthcode) : hist.cardhistAuthcode != null)
            return false;
        if (cardhistAuthkind != null ? !cardhistAuthkind.equals(hist.cardhistAuthkind) : hist.cardhistAuthkind != null)
            return false;
        if (cardhistCardno != null ? !cardhistCardno.equals(hist.cardhistCardno) : hist.cardhistCardno != null)
            return false;
        if (cardhistMerchantno != null ? !cardhistMerchantno.equals(hist.cardhistMerchantno) : hist.cardhistMerchantno != null)
            return false;
        if (cardhistTxcash != null ? !cardhistTxcash.equals(hist.cardhistTxcash) : hist.cardhistTxcash != null)
            return false;
        if (cardhistTxcurrency != null ? !cardhistTxcurrency.equals(hist.cardhistTxcurrency) : hist.cardhistTxcurrency != null)
            return false;
        if (cardhistTxid != null ? !cardhistTxid.equals(hist.cardhistTxid) : hist.cardhistTxid != null)
            return false;
        if (cardhistTxtime != null ? !cardhistTxtime.equals(hist.cardhistTxtime) : hist.cardhistTxtime != null)
            return false;
        if (depohistAssignday != null ? !depohistAssignday.equals(hist.depohistAssignday) : hist.depohistAssignday != null)
            return false;
        if (depohistAssminbalance != null ? !depohistAssminbalance.equals(hist.depohistAssminbalance) : hist.depohistAssminbalance != null)
            return false;
        if (depohistBalance != null ? !depohistBalance.equals(hist.depohistBalance) : hist.depohistBalance != null)
            return false;
        if (depohistBalanceOwn != null ? !depohistBalanceOwn.equals(hist.depohistBalanceOwn) : hist.depohistBalanceOwn != null)
            return false;
        if (depohistSubsidy != null ? !depohistSubsidy.equals(hist.depohistSubsidy) : hist.depohistSubsidy != null)
            return false;
        if (depohist_branchno != null ? !depohist_branchno.equals(hist.depohist_branchno) : hist.depohist_branchno != null)
            return false;
        if (depohistCardrptday != null ? !depohistCardrptday.equals(hist.depohistCardrptday) : hist.depohistCardrptday != null)
            return false;
        if (depohistCashsource != null ? !depohistCashsource.equals(hist.depohistCashsource) : hist.depohistCashsource != null)
            return false;
        if (depohistChequecnt != null ? !depohistChequecnt.equals(hist.depohistChequecnt) : hist.depohistChequecnt != null)
            return false;
        if (depohistClerk != null ? !depohistClerk.equals(hist.depohistClerk) : hist.depohistClerk != null)
            return false;
        if (depohistConverCurrency != null ? !depohistConverCurrency.equals(hist.depohistConverCurrency) : hist.depohistConverCurrency != null)
            return false;
        if (depohistConverOpcash != null ? !depohistConverOpcash.equals(hist.depohistConverOpcash) : hist.depohistConverOpcash != null)
            return false;
        if (depohistCreditaccount != null ? !depohistCreditaccount.equals(hist.depohistCreditaccount) : hist.depohistCreditaccount != null)
            return false;
        if (depohistDebitaccount != null ? !depohistDebitaccount.equals(hist.depohistDebitaccount) : hist.depohistDebitaccount != null)
            return false;
        if (depohistDepositrate != null ? !depohistDepositrate.equals(hist.depohistDepositrate) : hist.depohistDepositrate != null)
            return false;
        if (depohistExpirationday != null ? !depohistExpirationday.equals(hist.depohistExpirationday) : hist.depohistExpirationday != null)
            return false;
        if (depohistExpminbalance != null ? !depohistExpminbalance.equals(hist.depohistExpminbalance) : hist.depohistExpminbalance != null)
            return false;
        if (depohistExproverdraft != null ? !depohistExproverdraft.equals(hist.depohistExproverdraft) : hist.depohistExproverdraft != null)
            return false;
        if (depohistExproverdraftint != null ? !depohistExproverdraftint.equals(hist.depohistExproverdraftint) : hist.depohistExproverdraftint != null)
            return false;
        if (depohistExternalkind != null ? !depohistExternalkind.equals(hist.depohistExternalkind) : hist.depohistExternalkind != null)
            return false;
        if (depohistFlagCash != null ? !depohistFlagCash.equals(hist.depohistFlagCash) : hist.depohistFlagCash != null)
            return false;
        if (depohistGrantOper != null ? !depohistGrantOper.equals(hist.depohistGrantOper) : hist.depohistGrantOper != null)
            return false;
        if (depohistHeirno != null ? !depohistHeirno.equals(hist.depohistHeirno) : hist.depohistHeirno != null)
            return false;
        if (depohist_tb != null ? !depohist_tb.equals(hist.depohist_tb) : hist.depohist_tb != null)
            return false;
        if (depohistInserttime != null ? !depohistInserttime.equals(hist.depohistInserttime) : hist.depohistInserttime != null)
            return false;
        if (depohistInterest != null ? !depohistInterest.equals(hist.depohistInterest) : hist.depohistInterest != null)
            return false;
        if (depohistInterestf != null ? !depohistInterestf.equals(hist.depohistInterestf) : hist.depohistInterestf != null)
            return false;
        if (depohistIscashdispenser != null ? !depohistIscashdispenser.equals(hist.depohistIscashdispenser) : hist.depohistIscashdispenser != null)
            return false;
        if (depohistIsmanual != null ? !depohistIsmanual.equals(hist.depohistIsmanual) : hist.depohistIsmanual != null)
            return false;
        if (depohistIsmoffice != null ? !depohistIsmoffice.equals(hist.depohistIsmoffice) : hist.depohistIsmoffice != null)
            return false;
        if (depohistJrnno != null ? !depohistJrnno.equals(hist.depohistJrnno) : hist.depohistJrnno != null)
            return false;
        if (depohistMaxamount != null ? !depohistMaxamount.equals(hist.depohistMaxamount) : hist.depohistMaxamount != null)
            return false;
        if (depohistMaxamountrate != null ? !depohistMaxamountrate.equals(hist.depohistMaxamountrate) : hist.depohistMaxamountrate != null)
            return false;
        if (depohistNDprice != null ? !depohistNDprice.equals(hist.depohistNDprice) : hist.depohistNDprice != null)
            return false;
        if (depohistOffcashbalance != null ? !depohistOffcashbalance.equals(hist.depohistOffcashbalance) : hist.depohistOffcashbalance != null)
            return false;
        if (depohistOffcashbalanceb != null ? !depohistOffcashbalanceb.equals(hist.depohistOffcashbalanceb) : hist.depohistOffcashbalanceb != null)
            return false;
        if (depohistOffice != null ? !depohistOffice.equals(hist.depohistOffice) : hist.depohistOffice != null)
            return false;
        if (depohistOfficetransday != null ? !depohistOfficetransday.equals(hist.depohistOfficetransday) : hist.depohistOfficetransday != null)
            return false;
        if (depohistOpcash != null ? !depohistOpcash.equals(hist.depohistOpcash) : hist.depohistOpcash != null)
            return false;
        if (depohistOpcashcost != null ? !depohistOpcashcost.equals(hist.depohistOpcashcost) : hist.depohistOpcashcost != null)
            return false;
        if (depohistOpcode != null ? !depohistOpcode.equals(hist.depohistOpcode) : hist.depohistOpcode != null)
            return false;
        if (depohistOpday != null ? !depohistOpday.equals(hist.depohistOpday) : hist.depohistOpday != null)
            return false;
        if (depohistOpencash != null ? !depohistOpencash.equals(hist.depohistOpencash) : hist.depohistOpencash != null)
            return false;
        if (depohistOpkind != null ? !depohistOpkind.equals(hist.depohistOpkind) : hist.depohistOpkind != null)
            return false;
        if (depohistOpno != null ? !depohistOpno.equals(hist.depohistOpno) : hist.depohistOpno != null)
            return false;
        if (depohistOptransday != null ? !depohistOptransday.equals(hist.depohistOptransday) : hist.depohistOptransday != null)
            return false;
        if (depohistOrderno != null ? !depohistOrderno.equals(hist.depohistOrderno) : hist.depohistOrderno != null)
            return false;
        if (depohistMinBalanceMonth != null ? !depohistMinBalanceMonth.equals(hist.depohistMinBalanceMonth) : hist.depohistMinBalanceMonth != null)
            return false;
        if (depohistBalanceOwnProlong != null ? !depohistBalanceOwnProlong.equals(hist.depohistBalanceOwnProlong) : hist.depohistBalanceOwnProlong != null)
            return false;
        if (depohistOverdraft != null ? !depohistOverdraft.equals(hist.depohistOverdraft) : hist.depohistOverdraft != null)
            return false;
        if (depohistOverdraftint != null ? !depohistOverdraftint.equals(hist.depohistOverdraftint) : hist.depohistOverdraftint != null)
            return false;
        if (depohistPairAccount != null ? !depohistPairAccount.equals(hist.depohistPairAccount) : hist.depohistPairAccount != null)
            return false;
        if (depohistPartrate != null ? !depohistPartrate.equals(hist.depohistPartrate) : hist.depohistPartrate != null)
            return false;
        if (depositPayrollday != null ? !depositPayrollday.equals(hist.depositPayrollday) : hist.depositPayrollday != null)
            return false;
        if (depositPayAddFirst != null ? !depositPayAddFirst.equals(hist.depositPayAddFirst) : hist.depositPayAddFirst != null)
            return false;
        if (depohistPercentsrate != null ? !depohistPercentsrate.equals(hist.depohistPercentsrate) : hist.depohistPercentsrate != null)
            return false;
        if (depohistPinacceptflag != null ? !depohistPinacceptflag.equals(hist.depohistPinacceptflag) : hist.depohistPinacceptflag != null)
            return false;
        if (depohistProlongday != null ? !depohistProlongday.equals(hist.depohistProlongday) : hist.depohistProlongday != null)
            return false;
        if (depohistRatesource != null ? !depohistRatesource.equals(hist.depohistRatesource) : hist.depohistRatesource != null)
            return false;
        if (depohistReason != null ? !depohistReason.equals(hist.depohistReason) : hist.depohistReason != null)
            return false;
        if (depohistSbookendday != null ? !depohistSbookendday.equals(hist.depohistSbookendday) : hist.depohistSbookendday != null)
            return false;
        if (depohistSourcedocday != null ? !depohistSourcedocday.equals(hist.depohistSourcedocday) : hist.depohistSourcedocday != null)
            return false;
        if (depohistSourcedocno != null ? !depohistSourcedocno.equals(hist.depohistSourcedocno) : hist.depohistSourcedocno != null)
            return false;
        if (depohistSourcedocoriginator != null ? !depohistSourcedocoriginator.equals(hist.depohistSourcedocoriginator) : hist.depohistSourcedocoriginator != null)
            return false;
        if (depohistState != null ? !depohistState.equals(hist.depohistState) : hist.depohistState != null)
            return false;
        if (depohistSubsys != null ? !depohistSubsys.equals(hist.depohistSubsys) : hist.depohistSubsys != null)
            return false;
        if (depohistSumobnaloperation != null ? !depohistSumobnaloperation.equals(hist.depohistSumobnaloperation) : hist.depohistSumobnaloperation != null)
            return false;
        if (depohistTaxableprofit != null ? !depohistTaxableprofit.equals(hist.depohistTaxableprofit) : hist.depohistTaxableprofit != null)
            return false;
        if (depohistTaxableprofitcost != null ? !depohistTaxableprofitcost.equals(hist.depohistTaxableprofitcost) : hist.depohistTaxableprofitcost != null)
            return false;
        if (depohistTermsok != null ? !depohistTermsok.equals(hist.depohistTermsok) : hist.depohistTermsok != null)
            return false;
        if (depohistTurncode != null ? !depohistTurncode.equals(hist.depohistTurncode) : hist.depohistTurncode != null)
            return false;
        if (depohistUsedtaxexemptions != null ? !depohistUsedtaxexemptions.equals(hist.depohistUsedtaxexemptions) : hist.depohistUsedtaxexemptions != null)
            return false;
        if (depohistVHzap2 != null ? !depohistVHzap2.equals(hist.depohistVHzap2) : hist.depohistVHzap2 != null)
            return false;
        if (depohistWarrantkind != null ? !depohistWarrantkind.equals(hist.depohistWarrantkind) : hist.depohistWarrantkind != null)
            return false;
        if (depohistWarrantno != null ? !depohistWarrantno.equals(hist.depohistWarrantno) : hist.depohistWarrantno != null)
            return false;
        if (offcashcompInfo != null ? !offcashcompInfo.equals(hist.offcashcompInfo) : hist.offcashcompInfo != null)
            return false;
        if (offcashcompOpday != null ? !offcashcompOpday.equals(hist.offcashcompOpday) : hist.offcashcompOpday != null)
            return false;
        if (offcashcompOpno != null ? !offcashcompOpno.equals(hist.offcashcompOpno) : hist.offcashcompOpno != null)
            return false;
        if (offcashcompOrdno != null ? !offcashcompOrdno.equals(hist.offcashcompOrdno) : hist.offcashcompOrdno != null)
            return false;
        if (offcashcompSsagency != null ? !offcashcompSsagency.equals(hist.offcashcompSsagency) : hist.offcashcompSsagency != null)
            return false;
        if (offcashcompSsn != null ? !offcashcompSsn.equals(hist.offcashcompSsn) : hist.offcashcompSsn != null)
            return false;
        if (offcashcompVDnum1 != null ? !offcashcompVDnum1.equals(hist.offcashcompVDnum1) : hist.offcashcompVDnum1 != null)
            return false;
        if (offcashcompVDsum1 != null ? !offcashcompVDsum1.equals(hist.offcashcompVDsum1) : hist.offcashcompVDsum1 != null)
            return false;
        if (offcashcompVDsum2 != null ? !offcashcompVDsum2.equals(hist.offcashcompVDsum2) : hist.offcashcompVDsum2 != null)
            return false;
        if (offcashcompVDsum3 != null ? !offcashcompVDsum3.equals(hist.offcashcompVDsum3) : hist.offcashcompVDsum3 != null)
            return false;
        if (offcashcompVDsum4 != null ? !offcashcompVDsum4.equals(hist.offcashcompVDsum4) : hist.offcashcompVDsum4 != null)
            return false;
        if (offcashcompVDsum5 != null ? !offcashcompVDsum5.equals(hist.offcashcompVDsum5) : hist.offcashcompVDsum5 != null)
            return false;
        if (offcashcompVDsum6 != null ? !offcashcompVDsum6.equals(hist.offcashcompVDsum6) : hist.offcashcompVDsum6 != null)
            return false;
        if (offcashcompVDsum7 != null ? !offcashcompVDsum7.equals(hist.offcashcompVDsum7) : hist.offcashcompVDsum7 != null)
            return false;
        if (category != null ? !category.equals(hist.category) : hist.category != null)
            return false;
        if (depohistTerm != null ? !depohistTerm.equals(hist.depohistTerm) : hist.depohistTerm != null)
            return false;
        if (code != null ? !code.equals(hist.code) : hist.code != null)
            return false;
        if (name != null ? !name.equals(hist.name) : hist.name != null)
            return false;
        if (sourceId != null ? !sourceId.equals(hist.sourceId) : hist.sourceId != null)
            return false;
        if (sourceSystemId != null ? !sourceSystemId.equals(hist.sourceSystemId) : hist.sourceSystemId != null)
            return false;
        if (templObjId != null ? !templObjId.equals(hist.templObjId) : hist.templObjId != null)
            return false;
        return !(parentObjId != null ? !parentObjId.equals(hist.parentObjId) : hist.parentObjId != null);
    }

    @Override public int hashCode() {
        int result = (int)(id ^ (id >>> 32));
        result = 31 * result + (int)(partitionъid ^ (partitionъid >>> 32));
        result = 31 * result + (int)(rootParticleъid ^ (rootParticleъid >>> 32));
        result = 31 * result + (int)(affinityParentъid ^ (affinityParentъid >>> 32));
        result = 31 * result + (int)(operationRunъid ^ (operationRunъid >>> 32));
        result = 31 * result + (int)(operationRunъpartId ^ (operationRunъpartId >>> 32));
        result = 31 * result + (int)(operationRunъrootId ^ (operationRunъrootId >>> 32));
        result = 31 * result + (cardhistAtmid != null ? cardhistAtmid.hashCode() : 0);
        result = 31 * result + (cardhistAuthcode != null ? cardhistAuthcode.hashCode() : 0);
        result = 31 * result + (cardhistAuthkind != null ? cardhistAuthkind.hashCode() : 0);
        result = 31 * result + (cardhistCardno != null ? cardhistCardno.hashCode() : 0);
        result = 31 * result + (cardhistMerchantno != null ? cardhistMerchantno.hashCode() : 0);
        result = 31 * result + (cardhistTxcash != null ? cardhistTxcash.hashCode() : 0);
        result = 31 * result + (cardhistTxcurrency != null ? cardhistTxcurrency.hashCode() : 0);
        result = 31 * result + (cardhistTxid != null ? cardhistTxid.hashCode() : 0);
        result = 31 * result + (cardhistTxtime != null ? cardhistTxtime.hashCode() : 0);
        result = 31 * result + (depohistAssignday != null ? depohistAssignday.hashCode() : 0);
        result = 31 * result + (depohistAssminbalance != null ? depohistAssminbalance.hashCode() : 0);
        result = 31 * result + (depohistBalance != null ? depohistBalance.hashCode() : 0);
        result = 31 * result + (depohistBalanceOwn != null ? depohistBalanceOwn.hashCode() : 0);
        result = 31 * result + (depohistSubsidy != null ? depohistSubsidy.hashCode() : 0);
        result = 31 * result + (depohist_branchno != null ? depohist_branchno.hashCode() : 0);
        result = 31 * result + (depohistCardrptday != null ? depohistCardrptday.hashCode() : 0);
        result = 31 * result + (depohistCashsource != null ? depohistCashsource.hashCode() : 0);
        result = 31 * result + (depohistChequecnt != null ? depohistChequecnt.hashCode() : 0);
        result = 31 * result + (depohistClerk != null ? depohistClerk.hashCode() : 0);
        result = 31 * result + (depohistConverCurrency != null ? depohistConverCurrency.hashCode() : 0);
        result = 31 * result + (depohistConverOpcash != null ? depohistConverOpcash.hashCode() : 0);
        result = 31 * result + (depohistCreditaccount != null ? depohistCreditaccount.hashCode() : 0);
        result = 31 * result + (depohistDebitaccount != null ? depohistDebitaccount.hashCode() : 0);
        result = 31 * result + (depohistDepositrate != null ? depohistDepositrate.hashCode() : 0);
        result = 31 * result + (depohistExpirationday != null ? depohistExpirationday.hashCode() : 0);
        result = 31 * result + (depohistExpminbalance != null ? depohistExpminbalance.hashCode() : 0);
        result = 31 * result + (depohistExproverdraft != null ? depohistExproverdraft.hashCode() : 0);
        result = 31 * result + (depohistExproverdraftint != null ? depohistExproverdraftint.hashCode() : 0);
        result = 31 * result + (depohistExternalkind != null ? depohistExternalkind.hashCode() : 0);
        result = 31 * result + (depohistFlagCash != null ? depohistFlagCash.hashCode() : 0);
        result = 31 * result + (depohistGrantOper != null ? depohistGrantOper.hashCode() : 0);
        result = 31 * result + (depohistHeirno != null ? depohistHeirno.hashCode() : 0);
        result = 31 * result + (depohist_tb != null ? depohist_tb.hashCode() : 0);
        result = 31 * result + (depohistInserttime != null ? depohistInserttime.hashCode() : 0);
        result = 31 * result + (depohistInterest != null ? depohistInterest.hashCode() : 0);
        result = 31 * result + (depohistInterestf != null ? depohistInterestf.hashCode() : 0);
        result = 31 * result + (depohistIscashdispenser != null ? depohistIscashdispenser.hashCode() : 0);
        result = 31 * result + (depohistIsmanual != null ? depohistIsmanual.hashCode() : 0);
        result = 31 * result + (depohistIsmoffice != null ? depohistIsmoffice.hashCode() : 0);
        result = 31 * result + (depohistJrnno != null ? depohistJrnno.hashCode() : 0);
        result = 31 * result + (depohistMaxamount != null ? depohistMaxamount.hashCode() : 0);
        result = 31 * result + (depohistMaxamountrate != null ? depohistMaxamountrate.hashCode() : 0);
        result = 31 * result + (depohistNDprice != null ? depohistNDprice.hashCode() : 0);
        result = 31 * result + (depohistOffcashbalance != null ? depohistOffcashbalance.hashCode() : 0);
        result = 31 * result + (depohistOffcashbalanceb != null ? depohistOffcashbalanceb.hashCode() : 0);
        result = 31 * result + (depohistOffice != null ? depohistOffice.hashCode() : 0);
        result = 31 * result + (depohistOfficetransday != null ? depohistOfficetransday.hashCode() : 0);
        result = 31 * result + (depohistOpcash != null ? depohistOpcash.hashCode() : 0);
        result = 31 * result + (depohistOpcashcost != null ? depohistOpcashcost.hashCode() : 0);
        result = 31 * result + (depohistOpcode != null ? depohistOpcode.hashCode() : 0);
        result = 31 * result + (depohistOpday != null ? depohistOpday.hashCode() : 0);
        result = 31 * result + (depohistOpencash != null ? depohistOpencash.hashCode() : 0);
        result = 31 * result + (depohistOpkind != null ? depohistOpkind.hashCode() : 0);
        result = 31 * result + (depohistOpno != null ? depohistOpno.hashCode() : 0);
        result = 31 * result + (depohistOptransday != null ? depohistOptransday.hashCode() : 0);
        result = 31 * result + (depohistOrderno != null ? depohistOrderno.hashCode() : 0);
        result = 31 * result + (depohistMinBalanceMonth != null ? depohistMinBalanceMonth.hashCode() : 0);
        result = 31 * result + (depohistBalanceOwnProlong != null ? depohistBalanceOwnProlong.hashCode() : 0);
        result = 31 * result + (depohistOverdraft != null ? depohistOverdraft.hashCode() : 0);
        result = 31 * result + (depohistOverdraftint != null ? depohistOverdraftint.hashCode() : 0);
        result = 31 * result + (depohistPairAccount != null ? depohistPairAccount.hashCode() : 0);
        result = 31 * result + (depohistPartrate != null ? depohistPartrate.hashCode() : 0);
        result = 31 * result + (depositPayrollday != null ? depositPayrollday.hashCode() : 0);
        result = 31 * result + (depositPayAddFirst != null ? depositPayAddFirst.hashCode() : 0);
        result = 31 * result + (depohistPercentsrate != null ? depohistPercentsrate.hashCode() : 0);
        result = 31 * result + (depohistPinacceptflag != null ? depohistPinacceptflag.hashCode() : 0);
        result = 31 * result + (depohistProlongday != null ? depohistProlongday.hashCode() : 0);
        result = 31 * result + (depohistRatesource != null ? depohistRatesource.hashCode() : 0);
        result = 31 * result + (depohistReason != null ? depohistReason.hashCode() : 0);
        result = 31 * result + (depohistSbookendday != null ? depohistSbookendday.hashCode() : 0);
        result = 31 * result + (depohistSourcedocday != null ? depohistSourcedocday.hashCode() : 0);
        result = 31 * result + (depohistSourcedocno != null ? depohistSourcedocno.hashCode() : 0);
        result = 31 * result + (depohistSourcedocoriginator != null ? depohistSourcedocoriginator.hashCode() : 0);
        result = 31 * result + (depohistState != null ? depohistState.hashCode() : 0);
        result = 31 * result + (depohistSubsys != null ? depohistSubsys.hashCode() : 0);
        result = 31 * result + (depohistSumobnaloperation != null ? depohistSumobnaloperation.hashCode() : 0);
        result = 31 * result + (depohistTaxableprofit != null ? depohistTaxableprofit.hashCode() : 0);
        result = 31 * result + (depohistTaxableprofitcost != null ? depohistTaxableprofitcost.hashCode() : 0);
        result = 31 * result + (depohistTermsok != null ? depohistTermsok.hashCode() : 0);
        result = 31 * result + (depohistTurncode != null ? depohistTurncode.hashCode() : 0);
        result = 31 * result + (depohistUsedtaxexemptions != null ? depohistUsedtaxexemptions.hashCode() : 0);
        result = 31 * result + (depohistVHzap2 != null ? depohistVHzap2.hashCode() : 0);
        result = 31 * result + (depohistWarrantkind != null ? depohistWarrantkind.hashCode() : 0);
        result = 31 * result + (depohistWarrantno != null ? depohistWarrantno.hashCode() : 0);
        result = 31 * result + (offcashcompInfo != null ? offcashcompInfo.hashCode() : 0);
        result = 31 * result + (offcashcompOpday != null ? offcashcompOpday.hashCode() : 0);
        result = 31 * result + (offcashcompOpno != null ? offcashcompOpno.hashCode() : 0);
        result = 31 * result + (offcashcompOrdno != null ? offcashcompOrdno.hashCode() : 0);
        result = 31 * result + (offcashcompSsagency != null ? offcashcompSsagency.hashCode() : 0);
        result = 31 * result + (offcashcompSsn != null ? offcashcompSsn.hashCode() : 0);
        result = 31 * result + (offcashcompVDnum1 != null ? offcashcompVDnum1.hashCode() : 0);
        result = 31 * result + (offcashcompVDsum1 != null ? offcashcompVDsum1.hashCode() : 0);
        result = 31 * result + (offcashcompVDsum2 != null ? offcashcompVDsum2.hashCode() : 0);
        result = 31 * result + (offcashcompVDsum3 != null ? offcashcompVDsum3.hashCode() : 0);
        result = 31 * result + (offcashcompVDsum4 != null ? offcashcompVDsum4.hashCode() : 0);
        result = 31 * result + (offcashcompVDsum5 != null ? offcashcompVDsum5.hashCode() : 0);
        result = 31 * result + (offcashcompVDsum6 != null ? offcashcompVDsum6.hashCode() : 0);
        result = 31 * result + (offcashcompVDsum7 != null ? offcashcompVDsum7.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (depohistTerm != null ? depohistTerm.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
        result = 31 * result + (sourceSystemId != null ? sourceSystemId.hashCode() : 0);
        result = 31 * result + (templObjId != null ? templObjId.hashCode() : 0);
        result = 31 * result + (parentObjId != null ? parentObjId.hashCode() : 0);
        result = 31 * result + (int)(objectId ^ (objectId >>> 32));
        result = 31 * result + (isDeleted ? 1 : 0);
        return result;
    }

    @Override public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        TupleOutput to = new TupleOutput();

        // long id;
        to.writeLong(id);
        // long partitionъid;
        to.writeLong(partitionъid);
        //long rootParticleъid;
        to.writeLong(rootParticleъid);
        //long affinityParentъid;
        to.writeLong(affinityParentъid);
        // long operationRunъid;
        to.writeLong(operationRunъid);
        // long operationRunъpartId;
        to.writeLong(operationRunъpartId);
        // long operationRunъrootId;
        to.writeLong(operationRunъrootId);
        // String cardhistAtmid;
        to.writeString(cardhistAtmid);
        // String cardhistAuthcode;
        to.writeString(cardhistAuthcode);
        // Integer cardhistAuthkind;
        writeNullableLong(to, cardhistAuthkind);
        // String cardhistCardno;
        to.writeString(cardhistCardno);
        // String cardhistMerchantno;
        to.writeString(cardhistMerchantno);
        // BigDecimal cardhistTxcash;
        writeNullableDecimal(to, cardhistTxcash);
        // Integer cardhistTxcurrency;
        writeNullableLong(to, cardhistTxcurrency);
        // Integer cardhistTxid;
        writeNullableLong(to, cardhistTxid);
        // Date cardhistTxtime;
        writeDate(to, cardhistTxtime);
        // Date depohistAssignday;
        writeDate(to, depohistAssignday);
        // BigDecimal depohistAssminbalance;
        writeNullableDecimal(to, depohistAssminbalance);
        // BigDecimal depohistBalance;
        writeNullableDecimal(to, depohistBalance);
        // BigDecimal depohistBalanceOwn;
        writeNullableDecimal(to, depohistBalanceOwn);
        // BigDecimal depohistSubsidy;
        writeNullableDecimal(to, depohistSubsidy);
        // Integer depohist_branchno;
        writeNullableLong(to, depohist_branchno);
        // Date depohistCardrptday;
        writeDate(to, depohistCardrptday);
        // Integer depohistCashsource;
        writeNullableLong(to, depohistCashsource);
        // Integer depohistChequecnt;
        writeNullableLong(to, depohistChequecnt);
        // Integer depohistClerk;
        writeNullableLong(to, depohistClerk);
        // Integer depohistConverCurrency;
        writeNullableLong(to, depohistConverCurrency);
        // BigDecimal depohistConverOpcash;
        writeNullableDecimal(to, depohistConverOpcash);
        // String depohistCreditaccount;
        to.writeString(depohistCreditaccount);
        // String depohistDebitaccount;
        to.writeString(depohistDebitaccount);
        // BigDecimal depohistDepositrate;
        writeNullableDecimal(to, depohistDepositrate);
        // Date depohistExpirationday;
        writeDate(to, depohistExpirationday);
        // BigDecimal depohistExpminbalance;
        writeNullableDecimal(to, depohistExpminbalance);
        // BigDecimal depohistExproverdraft;
        writeNullableDecimal(to, depohistExproverdraft);
        // BigDecimal depohistExproverdraftint;
        writeNullableDecimal(to, depohistExproverdraftint);
        // Integer depohistExternalkind;
        writeNullableLong(to, depohistExternalkind);
        // Integer depohistFlagCash;
        writeNullableLong(to, depohistFlagCash);
        // Integer depohistGrantOper;
        writeNullableLong(to, depohistGrantOper);
        // Integer depohistHeirno;
        writeNullableLong(to, depohistHeirno);
        // Integer depohist_tb;
        writeNullableLong(to, depohist_tb);
        // Date depohistInserttime;
        writeDate(to, depohistInserttime);
        // BigDecimal depohistInterest;
        writeNullableDecimal(to, depohistInterest);
        // BigDecimal depohistInterestf;
        writeNullableDecimal(to, depohistInterestf);
        // Integer depohistIscashdispenser;
        writeNullableLong(to, depohistIscashdispenser);
        // Integer depohistIsmanual;
        writeNullableLong(to, depohistIsmanual);
        // Integer depohistIsmoffice;
        writeNullableLong(to, depohistIsmoffice);
        // Integer depohistJrnno;
        writeNullableLong(to, depohistJrnno);
        // Integer depohistMaxamount;
        writeNullableLong(to, depohistMaxamount);
        // BigDecimal depohistMaxamountrate;
        writeNullableDecimal(to, depohistMaxamountrate);
        // Integer depohistNDprice;
        writeNullableLong(to, depohistNDprice);
        // BigDecimal depohistOffcashbalance;
        writeNullableDecimal(to, depohistOffcashbalance);
        // BigDecimal depohistOffcashbalanceb;
        writeNullableDecimal(to, depohistOffcashbalanceb);
        // Integer depohistOffice;
        writeNullableLong(to, depohistOffice);
        // Date depohistOfficetransday;
        writeDate(to, depohistOfficetransday);
        // BigDecimal depohistOpcash;
        writeNullableDecimal(to, depohistOpcash);
        // BigDecimal depohistOpcashcost;
        writeNullableDecimal(to, depohistOpcashcost);
        // Integer depohistOpcode;
        writeNullableLong(to, depohistOpcode);
        // Date depohistOpday;
        writeDate(to, depohistOpday);
        // BigDecimal depohistOpencash;
        writeNullableDecimal(to, depohistOpencash);
        // Integer depohistOpkind;
        writeNullableLong(to, depohistOpkind);
        // Integer depohistOpno;
        writeNullableLong(to, depohistOpno);
        // Date depohistOptransday;
        writeDate(to, depohistOptransday);
        // String depohistOrderno;
        to.writeString(depohistOrderno);
        //BigDecimal depohistMinBalanceMonth;
        writeNullableDecimal(to, depohistMinBalanceMonth);
        // BigDecimal depohistBalanceOwnProlong;
        writeNullableDecimal(to, depohistBalanceOwnProlong);
        // BigDecimal depohistOverdraft;
        writeNullableDecimal(to, depohistOverdraft);
        // BigDecimal depohistOverdraftint;
        writeNullableDecimal(to, depohistOverdraftint);
        // String depohistPairAccount;
        to.writeString(depohistPairAccount);
        // BigDecimal depohistPartrate;
        writeNullableDecimal(to, depohistPartrate);
        // Date depositPayrollday;
        writeDate(to, depositPayrollday);
        // Date depositPayAddFirst;
        writeDate(to, depositPayAddFirst);
        // BigDecimal depohistPercentsrate;
        writeNullableDecimal(to, depohistPercentsrate);
        // Integer depohistPinacceptflag;
        writeNullableLong(to, depohistPinacceptflag);
        // Date depohistProlongday;
        writeDate(to, depohistProlongday);
        // Integer depohistRatesource;
        writeNullableLong(to, depohistRatesource);
        // Integer depohistReason;
        writeNullableLong(to, depohistReason);
        // Date depohistSbookendday;
        writeDate(to, depohistSbookendday);
        // Date depohistSourcedocday;
        writeDate(to, depohistSourcedocday);
        // String depohistSourcedocno;
        to.writeString(depohistSourcedocno);
        // String depohistSourcedocoriginator;
        to.writeString(depohistSourcedocoriginator);
        // Integer depohistState;
        writeNullableLong(to, depohistState);
        // Integer depohistSubsys;
        writeNullableLong(to, depohistSubsys);
        // BigDecimal depohistSumobnaloperation;
        writeNullableDecimal(to, depohistSumobnaloperation);
        // BigDecimal depohistTaxableprofit;
        writeNullableDecimal(to, depohistTaxableprofit);
        // BigDecimal depohistTaxableprofitcost;
        writeNullableDecimal(to, depohistTaxableprofitcost);
        // Integer depohistTermsok;
        writeNullableLong(to, depohistTermsok);
        // Integer depohistTurncode;
        writeNullableLong(to, depohistTurncode);
        // BigDecimal depohistUsedtaxexemptions;
        writeNullableDecimal(to, depohistUsedtaxexemptions);
        // Integer depohistVHzap2;
        writeNullableLong(to, depohistVHzap2);
        // Integer depohistWarrantkind;
        writeNullableLong(to, depohistWarrantkind);
        // String depohistWarrantno;
        to.writeString(depohistWarrantno);
        // String offcashcompInfo;
        to.writeString(offcashcompInfo);
        // Date offcashcompOpday;
        writeDate(to, offcashcompOpday);
        // Integer offcashcompOpno;
        writeNullableLong(to, offcashcompOpno);
        // String offcashcompOrdno;
        to.writeString(offcashcompOrdno);
        // String offcashcompSsagency;
        to.writeString(offcashcompSsagency);
        // String offcashcompSsn;
        to.writeString(offcashcompSsn);
        // Long offcashcompVDnum1;
        writeNullableLong(to, offcashcompVDnum1);
        // BigDecimal offcashcompVDsum1;
        writeNullableDecimal(to, offcashcompVDsum1);
        // BigDecimal offcashcompVDsum2;
        writeNullableDecimal(to, offcashcompVDsum2);
        // BigDecimal offcashcompVDsum3;
        writeNullableDecimal(to, offcashcompVDsum3);
        // BigDecimal offcashcompVDsum4;
        writeNullableDecimal(to, offcashcompVDsum4);
        // BigDecimal offcashcompVDsum5;
        writeNullableDecimal(to, offcashcompVDsum5);
        // BigDecimal offcashcompVDsum6;
        writeNullableDecimal(to, offcashcompVDsum6);
        // BigDecimal offcashcompVDsum7;
        writeNullableDecimal(to, offcashcompVDsum7);
        // String category;
        to.writeString(category);
        // Long depohistTerm;
        writeNullableLong(to, depohistTerm);
        // String code;
        to.writeString(code);
        // String name;
        to.writeString(name);
        // String sourceId;
        to.writeString(sourceId);
        // String sourceSystemId;
        to.writeString(sourceSystemId);
        // Long templObjId;
        writeNullableLong(to, templObjId);
        // Long parentObjId;
        writeNullableLong(to, parentObjId);
        // long objectId;
        to.writeLong(objectId);
        // boolean isDeleted=false;
        to.writeBoolean(isDeleted);

        writer.rawWriter().writeByteArray(to.toByteArray());
    }

    private void writeDate(TupleOutput to, Date date) {
        to.writeByte(date == null ? 0 : 1);
        if (date != null)
            to.writeLong(date.getTime());
    }

    private void writeNullableLong(TupleOutput to, Long num) {
        to.writeByte(num == null ? 0 : 1);
        if (num != null)
            to.writePackedLong(num);
    }

    private void writeNullableDecimal(TupleOutput to, BigDecimal num) {
        to.writeByte(num == null ? 0 : 1);
        if (num != null)
            to.writeBigDecimal(num);
    }

    @Override public void readBinary(BinaryReader reader) throws BinaryObjectException {
        byte[] bytes = reader.rawReader().readByteArray();

        TupleInput to = new TupleInput(bytes);

        // long id;
        id = to.readLong();
        // long partitionъid;
        partitionъid = to.readLong();
        //long rootParticleъid;
        rootParticleъid = to.readLong();
        //long affinityParentъid;
        affinityParentъid = to.readLong();
        // long operationRunъid;
        operationRunъid = to.readLong();
        // long operationRunъpartId;
        operationRunъpartId = to.readLong();
        // long operationRunъrootId;
        operationRunъrootId = to.readLong();
        // String cardhistAtmid;
        cardhistAtmid = to.readString();
        // String cardhistAuthcode;
        cardhistAuthcode = to.readString();
        // Integer cardhistAuthkind;
        cardhistAuthkind = readNullableLong(to);
        // String cardhistCardno;
        cardhistCardno = to.readString();
        // String cardhistMerchantno;
        cardhistMerchantno = to.readString();
        // BigDecimal cardhistTxcash;
        cardhistTxcash = readNullableDecimal(to);
        // Integer cardhistTxcurrency;
        cardhistTxcurrency = readNullableLong(to);
        // Integer cardhistTxid;
        cardhistTxid = readNullableLong(to);
        // Date cardhistTxtime;
        cardhistTxtime = readDate(to);
        // Date depohistAssignday;
        depohistAssignday = readDate(to);
        // BigDecimal depohistAssminbalance;
        depohistAssminbalance = readNullableDecimal(to);
        // BigDecimal depohistBalance;
        depohistBalance = readNullableDecimal(to);
        // BigDecimal depohistBalanceOwn;
        depohistBalanceOwn = readNullableDecimal(to);
        // BigDecimal depohistSubsidy;
        depohistSubsidy = readNullableDecimal(to);
        // Integer depohist_branchno;
        depohist_branchno = readNullableLong(to);
        // Date depohistCardrptday;
        depohistCardrptday = readDate(to);
        // Integer depohistCashsource;
        depohistCashsource = readNullableLong(to);
        // Integer depohistChequecnt;
        depohistChequecnt = readNullableLong(to);
        // Integer depohistClerk;
        depohistClerk = readNullableLong(to);
        // Integer depohistConverCurrency;
        depohistConverCurrency = readNullableLong(to);
        // BigDecimal depohistConverOpcash;
        depohistConverOpcash = readNullableDecimal(to);
        // String depohistCreditaccount;
        depohistCreditaccount = to.readString();
        // String depohistDebitaccount;
        depohistDebitaccount = to.readString();
        // BigDecimal depohistDepositrate;
        depohistDepositrate = readNullableDecimal(to);
        // Date depohistExpirationday;
        depohistExpirationday = readDate(to);
        // BigDecimal depohistExpminbalance;
        depohistExpminbalance = readNullableDecimal(to);
        // BigDecimal depohistExproverdraft;
        depohistExproverdraft = readNullableDecimal(to);
        // BigDecimal depohistExproverdraftint;
        depohistExproverdraftint = readNullableDecimal(to);
        // Integer depohistExternalkind;
        depohistExternalkind = readNullableLong(to);
        // Integer depohistFlagCash;
        depohistFlagCash = readNullableLong(to);
        // Integer depohistGrantOper;
        depohistGrantOper = readNullableLong(to);
        // Integer depohistHeirno;
        depohistHeirno = readNullableLong(to);
        // Integer depohist_tb;
        depohist_tb = readNullableLong(to);
        // Date depohistInserttime;
        depohistInserttime = readDate(to);
        // BigDecimal depohistInterest;
        depohistInterest = readNullableDecimal(to);
        // BigDecimal depohistInterestf;
        depohistInterestf = readNullableDecimal(to);
        // Integer depohistIscashdispenser;
        depohistIscashdispenser = readNullableLong(to);
        // Integer depohistIsmanual;
        depohistIsmanual = readNullableLong(to);
        // Integer depohistIsmoffice;
        depohistIsmoffice = readNullableLong(to);
        // Integer depohistJrnno;
        depohistJrnno = readNullableLong(to);
        // Integer depohistMaxamount;
        depohistMaxamount = readNullableLong(to);
        // BigDecimal depohistMaxamountrate;
        depohistMaxamountrate = readNullableDecimal(to);
        // Integer depohistNDprice;
        depohistNDprice = readNullableLong(to);
        // BigDecimal depohistOffcashbalance;
        depohistOffcashbalance = readNullableDecimal(to);
        // BigDecimal depohistOffcashbalanceb;
        depohistOffcashbalanceb = readNullableDecimal(to);
        // Integer depohistOffice;
        depohistOffice = readNullableLong(to);
        // Date depohistOfficetransday;
        depohistOfficetransday = readDate(to);
        // BigDecimal depohistOpcash;
        depohistOpcash = readNullableDecimal(to);
        // BigDecimal depohistOpcashcost;
        depohistOpcashcost = readNullableDecimal(to);
        // Integer depohistOpcode;
        depohistOpcode = readNullableLong(to);
        // Date depohistOpday;
        depohistOpday = readDate(to);
        // BigDecimal depohistOpencash;
        depohistOpencash = readNullableDecimal(to);
        // Integer depohistOpkind;
        depohistOpkind = readNullableLong(to);
        // Integer depohistOpno;
        depohistOpno = readNullableLong(to);
        // Date depohistOptransday;
        depohistOptransday = readDate(to);
        // String depohistOrderno;
        depohistOrderno = to.readString();
        //BigDecimal depohistMinBalanceMonth;
        depohistMinBalanceMonth = readNullableDecimal(to);
        // BigDecimal depohistBalanceOwnProlong;
        depohistBalanceOwnProlong = readNullableDecimal(to);
        // BigDecimal depohistOverdraft;
        depohistOverdraft = readNullableDecimal(to);
        // BigDecimal depohistOverdraftint;
        depohistOverdraftint = readNullableDecimal(to);
        // String depohistPairAccount;
        depohistPairAccount = to.readString();
        // BigDecimal depohistPartrate;
        depohistPartrate = readNullableDecimal(to);
        // Date depositPayrollday;
        depositPayrollday = readDate(to);
        // Date depositPayAddFirst;
        depositPayAddFirst = readDate(to);
        // BigDecimal depohistPercentsrate;
        depohistPercentsrate = readNullableDecimal(to);
        // Integer depohistPinacceptflag;
        depohistPinacceptflag =readNullableLong(to);
        // Date depohistProlongday;
        depohistProlongday = readDate(to);
        // Integer depohistRatesource;
        depohistRatesource = readNullableLong(to);
        // Integer depohistReason;
        depohistReason = readNullableLong(to);
        // Date depohistSbookendday;
        depohistSbookendday = readDate(to);
        // Date depohistSourcedocday;
        depohistSourcedocday = readDate(to);
        // String depohistSourcedocno;
        depohistSourcedocno = to.readString();
        // String depohistSourcedocoriginator;
        depohistSourcedocoriginator = to.readString();
        // Integer depohistState;
        depohistState = readNullableLong(to);
        // Integer depohistSubsys;
        depohistSubsys = readNullableLong(to);
        // BigDecimal depohistSumobnaloperation;
        depohistSumobnaloperation = readNullableDecimal(to);
        // BigDecimal depohistTaxableprofit;
        depohistTaxableprofit = readNullableDecimal(to);
        // BigDecimal depohistTaxableprofitcost;
        depohistTaxableprofitcost = readNullableDecimal(to);
        // Integer depohistTermsok;
        depohistTermsok = readNullableLong(to);
        // Integer depohistTurncode;
        depohistTurncode = readNullableLong(to);
        // BigDecimal depohistUsedtaxexemptions;
        depohistUsedtaxexemptions = readNullableDecimal(to);
        // Integer depohistVHzap2;
        depohistVHzap2 = readNullableLong(to);
        // Integer depohistWarrantkind;
        depohistWarrantkind = readNullableLong(to);
        // String depohistWarrantno;
        depohistWarrantno = to.readString();
        // String offcashcompInfo;
        offcashcompInfo = to.readString();
        // Date offcashcompOpday;
        offcashcompOpday = readDate(to);
        // Integer offcashcompOpno;
        offcashcompOpno = readNullableLong(to);
        // String offcashcompOrdno;
        offcashcompOrdno = to.readString();
        // String offcashcompSsagency;
        offcashcompSsagency = to.readString();
        // String offcashcompSsn;
        offcashcompSsn = to.readString();
        // Long offcashcompVDnum1;
        offcashcompVDnum1 = readNullableLong(to);
        // BigDecimal offcashcompVDsum1;
        offcashcompVDsum1 = readNullableDecimal(to);
        // BigDecimal offcashcompVDsum2;
        offcashcompVDsum2 = readNullableDecimal(to);
        // BigDecimal offcashcompVDsum3;
        offcashcompVDsum3 = readNullableDecimal(to);
        // BigDecimal offcashcompVDsum4;
        offcashcompVDsum4 = readNullableDecimal(to);
        // BigDecimal offcashcompVDsum5;
        offcashcompVDsum5 = readNullableDecimal(to);
        // BigDecimal offcashcompVDsum6;
        offcashcompVDsum6 = readNullableDecimal(to);
        // BigDecimal offcashcompVDsum7;
        offcashcompVDsum7 = readNullableDecimal(to);
        // String category;
        category = to.readString();
        // Long depohistTerm;
        depohistTerm = readNullableLong(to);
        // String code;
        code = to.readString();
        // String name;
        name = to.readString();
        // String sourceId;
        sourceId = to.readString();
        // String sourceSystemId;
        sourceSystemId = to.readString();
        // Long templObjId;
        templObjId = readNullableLong(to);
        // Long parentObjId;
        parentObjId = readNullableLong(to);
        // long objectId;
        objectId = to.readLong();
        // boolean isDeleted=false;
        isDeleted = to.readBoolean();
    }

    private Date readDate(TupleInput to) {
        byte b = to.readByte();

        if (b == 0)
            return null;

        return new Date(to.readLong());
    }

    private BigDecimal readNullableDecimal(TupleInput to) {
        byte b = to.readByte();

        if (b == 0)
            return null;

        return to.readBigDecimal();
    }

    private Long readNullableLong(TupleInput to) {
        byte b = to.readByte();

        if (b == 0)
            return null;

        return to.readPackedLong();
    }
}