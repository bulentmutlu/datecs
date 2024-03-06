package com.blk.sdk.Emv;

import android.util.Xml;

import com.blk.platform.Emv.CAPublicKey;
import com.blk.platform.Emv.EmvApp;
import com.blk.platform.Emv.IEmv;
import com.blk.platform.IPlatform;
import com.blk.sdk.Convert;
import com.blk.sdk.file;
import com.blk.sdk.string;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

public class Config {


//EMV Tag Definition
//#define d_TAG_AID                                                       0x004F
//#define d_TAG_TRACK2_EQUIVALENT_DATA                                    0x0057
//#define d_TAG_PAN                                                       0x005A
//#define d_TAG_AMOUNT_AUTHORIZED_B                                       0x0081
//#define d_TAG_ARC                                                       0x008A
//#define d_TAG_TVR                                                       0x0095
//#define d_TAG_TRAN_DATE                                                 0x009A
//#define d_TAG_TX_TYPE                                                   0x009C
//#define d_TAG_TERM_CURRENCY_CODE                                        0x5F2A
//#define d_TAG_TERM_CURRENCY_EXP                                         0x5F36
//#define d_TAG_AMOUNT_AUTHORIZED                                         0x9F02
//#define d_TAG_AMOUNT_OTHER                                              0x9F03
//#define d_TAG_APP_VERSION                                               0x9F09
//#define d_TAG_TERM_COUNTRY_CODE                                         0x9F1A
//#define d_TAG_FLOOR_LIMIT                                               0x9F1B
//#define d_TAG_IFD_SN                                                    0x9F1E
//#define d_TAG_TRAN_TIME                                                 0x9F21
//#define d_TAG_TERM_CAP                                                  0x9F33
//#define d_TAG_TERM_TYPE                                                 0x9F35
//#define d_TAG_UNPREDICATE_NUMBER                                        0x9F37
//#define d_TAG_POS_ENTRY_MODE                                            0x9F39
//#define d_TAG_ADD_TERM_CAP                                              0x9F40
//#define d_TAG_TERM_SEQ_COUNTER                                          0x9F41
//
////Propriegary Tag Definition
//#define d_TAG_ISSUER_SCRIPT_RESULT                                      0x9F5B
//#define d_TAG_DEFAULT_TDOL                                              0xDFC0
//#define d_TAG_DEFAULT_DDOL                                              0xDFC1
//#define d_TAG_TARGET_PERCENT                                            0xDFC2
//#define d_TAG_MAX_TARGET_PERCENT                                        0xDFC3
//#define d_TAG_THRESHHOLD_VALUE                                          0xDFC4
//#define d_TAG_TAC_DENIAL                                                0xDFC6
//#define d_TAG_TAC_ONLINE                                                0xDFC7
//#define d_TAG_TAC_DEFAULT                                               0xDFC8
//#define d_TAG_CASTLES_ISSUER_SCRIPT_RESULT                              0xDF2D	//Another tag for issuer script result, value is the same to tag 9F5B
//#define d_TAG_CASTLES_CHECK_PAN_VS_PAN_FROM_TRACK2                      0xDF2E
//#define d_TAG_CASTLES_INTERAC_ASF_MODE                                  0xDF2F
//#define d_TAG_CASTLES_BIN                                               0xDF39
//#define d_TAG_CASTLES_BIN_REQUIRED_DIGITS                               0xDF40


//#define tag_VCAK_KEY_RECORD			0xBF8B01
//#define tag_VEMVCON_AID_CONFIG			0xBF8B02
//
//#define tag_VCAK_KEY_RID			0xDF8B01
//#define tag_VCAK_KEY_INDEX			0xDF8B02
//#define tag_VCAK_KEY_HASH_ALGO_IND		0xDF8B03
//#define tag_VCAK_KEY_PK_ALGO_IND		0xDF8B04
//#define tag_VCAK_KEY_MODULUS			0xDF8B05
//#define tag_VCAK_KEY_EXPONENT			0xDF8B06
//#define tag_VCAK_KEY_CHECKSUM			0xDF8B07
//
//#define tag_VEMVCON_THRESHOLD_VALUE		0xDF8B11
//#define tag_VEMVCON_DEFAULT_DDOL		0xDF8B12
//#define tag_VEMVCON_DEFAULT_TDDOL		0xDF8B13
//#define tag_VEMVCON_MAX_TARGET_PERCENT          0xDF8B14
//#define tag_VEMVCON_TARGET_PERCENT		0xDF8B15
//
//#define tag_VEMVCON_TAC_DEFAULT			0xDF8120
//#define tag_VEMVCON_TAC_DENIAL			0xDF8121
//#define tag_VEMVCON_TAC_ONLINE			0xDF8122
//
//#define tag_VEMVCON_ASI				0xDF8B16
//
//#define tag_EP_KERNEL				0xDF8B01
//#define tag_EP_AID_OPTIONS			0xDF8B02
//#define tag_EP_TRN_LIMIT			0xDF8B03
//#define tag_EP_CVM_REQ_LIMIT			0xDF8B04
//#define tag_EP_RDR_FLOOR_LIMIT			0xDF8B05
//
//#define tag_CARD_DATA_INPUT_C			0xDF8117
//#define tag_CVM_CAP_CVM_REQ			0xDF8118
//#define tag_CVM_CAP_NOCVM_REQ			0xDF8119
//#define tag_MAX_LIFETIME_TORN_TRN		0xDF811C
//#define tag_MAX_NUMBER_TORN_TRN			0xDF811D
//#define tag_MSG_HOLD_TIME			0xDF812D
//#define tag_RDR_FLOOR_LIMIT			0xDF8123
//#define tag_RDR_TRN_LIMIT_NOONDEVCVM            0xDF8124
//#define tag_RDR_TRN_LIMIT_ONDEVCVM		0xDF8125
//#define tag_RDR_CVM_REQ__LIMIT			0xDF8126
//#define tag_SECURITY_CAP			0xDF811F
//#define tag_TAC_DEFAULT				0xDF8120
//#define tag_TAC_DENIAL				0xDF8121
//#define tag_TAC_ONLINE				0xDF8122
//#define tag_TIMEOUT_VALUE			0xDF8127

    public static final String emv_config_file = "bkm_emv_config.xml";
    public static final String emvcl_config_file = "bkm_emvcl_config.xml";

    public static final int KerneltoUse = 0xDF8B01;
    public static final int AIDOptions = 0xDF8B02;
    public static final int ContactlessTransactionLimit = 0xDF8B03;
    public static final int CVMRequiredLimit = 0xDF8B04;
    public static final int  ReaderContactlesFloorLimit = 0xDF8B05;

    public static final int kernelVISA = 3;
    public static final int kernelMC = 2;

    static HashMap<Integer, Integer> tagMap = new HashMap<>();
    static {
        tagMap.put(0xDF8B11, 0xDFC4);
        tagMap.put(0xDF8B12, 0xDFC1);
        tagMap.put(0xDF8B13, 0xDFC0);
        tagMap.put(0xDF8B14, 0xDFC3);
        tagMap.put(0xDF8B15, 0xDFC2);
        tagMap.put(0xDF8120, 0xDFC8);
        tagMap.put(0xDF8121, 0xDFC6);
        tagMap.put(0xDF8122, 0xDFC7);

        // ctls visa
        tagMap.put(ContactlessTransactionLimit, 0xDF00);
        tagMap.put(CVMRequiredLimit, 0xDF01);
        tagMap.put(ReaderContactlesFloorLimit, 0xDF02);
    }

    public static String CreateEmvConfigFile(HashMap<String, List<CAPublicKey>> keys, EmvApp[] appList) throws Exception {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        xmlSerializer.setOutput(writer);

        xmlSerializer.startDocument("UTF-8", true);

        xmlSerializer.startTag(null, "configurationDescriptor");
        xmlSerializer.attribute(null, "version", "01");
        xmlSerializer.startTag(null, "Config");
        xmlSerializer.attribute(null, "index", "01");
        xmlSerializer.attribute(null, "active", "true");

        // CAPK (Certification Authority Public Key) is used for Offline Card Authentication
        xmlSerializer.startTag(null, "CAPKConfig");
        for ( String rId :  keys.keySet()) {
            xmlSerializer.startTag(null, "Group");
            //byte [] out = new byte[rId.length * 2]; Convert.EP_BfAscii(out, rId, 0, rId.length);
            xmlSerializer.attribute(null, "RID", rId);

            List<CAPublicKey> rKeys = keys.get(rId);
            for (CAPublicKey key : rKeys) {
                xmlSerializer.startTag(null, "Item");
                xmlSerializer.attribute(null, "index", Convert.Buffer2Hex(new byte[] {key.index}));

                xmlSerializer.startTag(null, "modules");
                xmlSerializer.text(Convert.Buffer2Hex(key.modulus, 0, key.modulusLen));
                xmlSerializer.endTag(null, "modules");

                xmlSerializer.startTag(null, "exponent");
                xmlSerializer.text(Convert.Buffer2Hex(key.exponent, 0, key.exponentLen));
                xmlSerializer.endTag(null, "exponent");

                xmlSerializer.startTag(null, "expirydata");
                xmlSerializer.text("");
                xmlSerializer.endTag(null, "expirydata");

                xmlSerializer.startTag(null, "hash");
                xmlSerializer.text(Convert.Buffer2Hex(key.hash));
                xmlSerializer.endTag(null, "hash");

                xmlSerializer.endTag(null, "Item");
            }
            xmlSerializer.endTag(null, "Group");
        }
        xmlSerializer.endTag(null, "CAPKConfig");

        // Application List is used to list all the card applications supported by the terminal.
        xmlSerializer.startTag(null, "AppList");
        byte index = 1;
        for (EmvApp app : appList) {
            xmlSerializer.startTag(null, "Item");
            xmlSerializer.attribute(null, "index", Convert.Buffer2Hex(new byte[] {index}));

            xmlSerializer.startTag(null, "Name");
            xmlSerializer.text(IEmv.GetNameFromAID(Convert.Buffer2Hex(app.aid, 0, app.aidLen)));
            xmlSerializer.endTag(null, "Name");

            xmlSerializer.startTag(null, "AID");
            xmlSerializer.text(Convert.Buffer2Hex(app.aid, 0, app.aidLen));
            xmlSerializer.endTag(null, "AID");

            xmlSerializer.startTag(null, "ASI");
            xmlSerializer.text("00");
            xmlSerializer.endTag(null, "ASI");

            xmlSerializer.endTag(null, "Item");
            ++index;
        }
        xmlSerializer.endTag(null, "AppList");

        // Application Specific Terminal Data is a set of terminal data depending on each application.
        xmlSerializer.startTag(null, "AppConfig");
        for (EmvApp app : appList) {
            xmlSerializer.startTag(null, "Group");
            xmlSerializer.attribute(null, "name", IEmv.GetNameFromAID(Convert.Buffer2Hex(app.aid, 0, app.aidLen)));
            xmlSerializer.attribute(null, "AID", Convert.Buffer2Hex(app.aid, 0, app.aidLen));
            xmlSerializer.attribute(null, "ASI", "00");


            for (IEmv.TlvData tlv : app.tags) {
                int tag = tlv.Tag;
                if (tagMap.containsKey(tlv.Tag)) {
                    tag = tagMap.get(tlv.Tag);
                }
                xmlSerializer.startTag(null, "Item");
                String tagHex = new string(Convert.Buffer2Hex(Convert.ToArray(Convert.SWAP_UINT32(tag)))).TrimStart('0').toString();
                xmlSerializer.attribute(null, "tag", tagHex);
                xmlSerializer.attribute(null, "attribute", "hex");
                if (tlv.Tag == 0x9F1A || tlv.Tag == 0x9F2A) {
                    xmlSerializer.text(new String(Convert.bcd2Str(tlv.Val, tlv.Len)));
                }
                else {
                    xmlSerializer.text(Convert.Buffer2Hex(tlv.Val, 0, tlv.Len));
                }
                xmlSerializer.endTag(null, "Item");
            }

            xmlSerializer.endTag(null, "Group");
        }
        xmlSerializer.endTag(null, "AppConfig");

        // Default Terminal Data is a set of terminal data that is loaded into EMV kernel during EMV Initialization.
        // These terminal data are as default data and used for all the applications listed in AppList
        xmlSerializer.startTag(null, "TerminalConfig");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "9F33");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("E0F8C8");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "9F40");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("F000B0A001");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "9F1E");
        xmlSerializer.attribute(null, "attribute", "asc");
        String sn = IPlatform.get().system.Serial();
        xmlSerializer.text(sn.substring(sn.length() - 9, sn.length() - 1));
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "5F2A");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("0949");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "9F1A");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("0792");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "5F36");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("02");
        xmlSerializer.endTag(null, "Item");

//        tagMap.put(0xDF8B11, 0xDFC4);
//        tagMap.put(0xDF8B12, 0xDFC1);
//        tagMap.put(0xDF8B13, 0xDFC0);
//        tagMap.put(0xDF8B14, 0xDFC3);
//        tagMap.put(0xDF8B15, 0xDFC2);
//        tagMap.put(0xDF8120, 0xDFC8);
//        tagMap.put(0xDF8121, 0xDFC6);
//        tagMap.put(0xDF8122, 0xDFC7);

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "DFC4");//"DF8B11");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("00000000");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "DFC1");//"DF8B12");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("9F37049F47018F019F3201");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "DFC0");//"DF8B13");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("9F0802");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "DFC3");//"DF8B14");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("00");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "DFC2");//"DF8B15");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("00");
        xmlSerializer.endTag(null, "Item");

//        xmlSerializer.startTag(null, "Item");
//        xmlSerializer.attribute(null, "tag", "DF8B16");
//        xmlSerializer.attribute(null, "attribute", "hex");
//        xmlSerializer.text("01");
//        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "DFC8");//"DF8120");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("0000000000");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "DFC6");//"DF8121");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("0000000000");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "DFC7");// "DF8122");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("0000000000");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "9F09");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("0002");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.startTag(null, "Item");
        xmlSerializer.attribute(null, "tag", "9F1B");
        xmlSerializer.attribute(null, "attribute", "hex");
        xmlSerializer.text("00000000");
        xmlSerializer.endTag(null, "Item");

        xmlSerializer.endTag(null, "TerminalConfig");

        xmlSerializer.endTag(null, "Config");
        xmlSerializer.endTag(null, "configurationDescriptor");

        xmlSerializer.endDocument();
        xmlSerializer.flush();
        String dataWrite = writer.toString();
        //Log.i("capk", dataWrite );
        file.WriteAllText(emv_config_file, dataWrite);

        return dataWrite;
    }

    static IEmv.TlvData getTag(List<IEmv.TlvData> tags, int tag)
    {
        for (IEmv.TlvData tlv : tags) {
            if (tag == tlv.Tag)
                return tlv;
        }
        return null;
    }



    public static String CreateEmvCLConfigFile(HashMap<String, List<CAPublicKey>> keys, EmvApp[] appList) throws Exception {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        xmlSerializer.setOutput(writer);

        xmlSerializer.startDocument("UTF-8", true);

        xmlSerializer.startTag(null, "configurationDescriptor");
        xmlSerializer.attribute(null, "version", "01");
        xmlSerializer.startTag(null, "CLConfig");
        xmlSerializer.attribute(null, "index", "01");
        xmlSerializer.attribute(null, "active", "true");

        // CAPK (Certification Authority Public Key) is used for Offline Card Authentication
        xmlSerializer.startTag(null, "CAPKConfig");
        for ( String rId :  keys.keySet()) {
            xmlSerializer.startTag(null, "Group");
            //byte [] out = new byte[rId.length * 2]; Convert.EP_BfAscii(out, rId, 0, rId.length);
            xmlSerializer.attribute(null, "RID", rId);

            List<CAPublicKey> rKeys = keys.get(rId);
            for (CAPublicKey key : rKeys) {
                xmlSerializer.startTag(null, "Item");
                xmlSerializer.attribute(null, "index", Convert.Buffer2Hex(new byte[] {key.index}));

                xmlSerializer.startTag(null, "modules");
                xmlSerializer.text(Convert.Buffer2Hex(key.modulus, 0, key.modulusLen));
                xmlSerializer.endTag(null, "modules");

                xmlSerializer.startTag(null, "exponent");
                xmlSerializer.text(Convert.Buffer2Hex(key.exponent, 0, key.exponentLen));
                xmlSerializer.endTag(null, "exponent");

                xmlSerializer.startTag(null, "expirydata");
                xmlSerializer.text("");
                xmlSerializer.endTag(null, "expirydata");

                xmlSerializer.startTag(null, "hash");
                xmlSerializer.text(Convert.Buffer2Hex(key.hash));
                xmlSerializer.endTag(null, "hash");

                xmlSerializer.endTag(null, "Item");
            }
            xmlSerializer.endTag(null, "Group");
        }
        xmlSerializer.endTag(null, "CAPKConfig");

        // Application Specific Terminal Data is a set of terminal data depending on each application.
        xmlSerializer.startTag(null, "TagCombination");
        for (EmvApp app : appList) {
            IEmv.TlvData kernelTlv = getTag(app.tags, KerneltoUse);
            assert kernelTlv != null;
            byte kerneltoUse = (byte) Convert.SWAP_UINT16(Convert.ToShort(kernelTlv.Val, 0));
            //if (app.kernel != kernelId)                throw new Exception("kernel mismatch");
            kerneltoUse = app.kernel;

            string aidHex = new string(Convert.Buffer2Hex(app.aid, 0, app.aidLen));


            xmlSerializer.startTag(null, "Group");
            xmlSerializer.attribute(null, "AID", Convert.Buffer2Hex(app.aid, 0, app.aidLen));
            xmlSerializer.attribute(null, "KernelID", new string("" + kerneltoUse).PadLeft(2, '0').toString());
            xmlSerializer.attribute(null, "TxnType", "00");

            StringBuilder tagCombination = new StringBuilder();


            // Troy Temassız için aşağıdaki taglar tabloya eklenmemiştir. Bu değerlere ihtiyacı olan kernel
            //örneklerinde aşağıdaki ön tanımlı değerler, uygulama seviyesinde ilgili temassız kernel modülüne iletilebilir.
            if (aidHex.StartsWith("A000000672")) // troy
            {
                if (getTag(app.tags, 0X9F09) == null) // Application Version Number
                    tagCombination.append("9F09020001");
                if (getTag(app.tags, 0X9F33) == null) // Terminal capabilities
                    tagCombination.append("9F3303E00008");
            }
            else if (aidHex.StartsWith("A000000004")) // mastercard
            {
                if (getTag(app.tags, 0X5F57) == null) // Account type
                    tagCombination.append("5F570100");
                if (getTag(app.tags, 0X9F01) == null) // Acquirer Identifier
                    tagCombination.append("9F0106123456");
            }

            if (getTag(app.tags, 0XDF05) == null && app.kernel == kernelVISA) // Add display offline funds indicator - Enable
                tagCombination.append("DF050101");
            if (getTag(app.tags, 0X9F1A) == null) // Add country code
                tagCombination.append("9F1A020792");
            if (getTag(app.tags, 0X5F2A) == null) // Add currency code
                tagCombination.append("5F2A020949");

            for (IEmv.TlvData tlv : app.tags) {

                int tag = tlv.Tag;
                if (tagMap.containsKey(tlv.Tag)) {
                    tag = tagMap.get(tlv.Tag);
                }


                String tagHex = new string(Convert.Buffer2Hex(Convert.ToArray(Convert.SWAP_UINT32(tag)))).TrimStart('0').toString();
                String len = String.format("%02X", tlv.Len);
                String value = Convert.Buffer2Hex(tlv.Val, 0, tlv.Len);

                tagCombination.append(tagHex).append(len).append(value);

//                if (tlv.Tag == 0x9F1A || tlv.Tag == 0x9F2A) {
//                    xmlSerializer.text(new String(Convert.bcd2Str(tlv.Val, tlv.Len)));
//                }
//                else {
//                    xmlSerializer.text(Convert.Buffer2Hex(tlv.Val, tlv.Len));
//                }
            }

            xmlSerializer.startTag(null, "Item");
            xmlSerializer.attribute(null, "attribute", "tlv");
            xmlSerializer.text(tagCombination.toString());
            xmlSerializer.endTag(null, "Item");
            xmlSerializer.endTag(null, "Group");
        }
        xmlSerializer.endTag(null, "TagCombination");

        xmlSerializer.startTag(null, "ParametersConfig");
        xmlSerializer.endTag(null, "ParametersConfig");
        xmlSerializer.startTag(null, "Revocations");
        xmlSerializer.endTag(null, "Revocations");
        xmlSerializer.startTag(null, "ExceptionFiles");
        xmlSerializer.endTag(null, "ExceptionFiles");


        xmlSerializer.endTag(null, "CLConfig");
        xmlSerializer.endTag(null, "configurationDescriptor");

        xmlSerializer.endDocument();
        xmlSerializer.flush();
        String dataWrite = writer.toString();
        //Log.i("capk", dataWrite );
        file.WriteAllText(emvcl_config_file, dataWrite);

        return dataWrite;
    }
}
