package com.blk.techpos.Bkm.Messages;
import static com.blk.techpos.PrmStruct.params;
import static com.blk.techpos.Bkm.TranStruct.currentTran;
import static com.blk.sdk.c.memcpy;



import android.util.Log;

import com.blk.sdk.Convert;
import com.blk.sdk.Iso8583;
import com.blk.sdk.c;
import com.blk.techpos.PrmStruct;
import com.blk.techpos.Bkm.TranStruct;
import com.blk.techpos.techpos;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.DESKeySpec;
/**
 * Created by id on 14.03.2018.
 */

//[00] [004] 0800
//[03] [006] 810000
//[11] [006] 000002
//[12] [006] 183445
//[13] [004] 0407
//[43] [040] CA21717001600000BLKV30010036470000000000
//[63] [011] 010008D8B6DF50A7206B7E
//----------------------------------------------------------------------------------------------------
//CRC32(: 08002038000000200002810000000002183445040743413231373137303031363030303030424C4B5633303031303033363437303030303030303030300011010008D8B6DF50A7206B7E) (FA38D094)
//ISO : 08002038000000200002810000000002183445040743413231373137303031363030303030424C4B5633303031303033363437303030303030303030300011010008D8B6DF50A7206B7E
//CommSend(105) : 00670117001243413231373137303031363000000000080081000008002038000000200002810000000002183445040743413231373137303031363030303030424C4B5633303031303033363437303030303030303030300011010008D8B6DF50A7206B7EFA38D094
//
//COMRECEIVE(146) : 0090011700124341323137313730303136302020202008108100000810203800000A2000028100000000021834450407323039373138343639323239303043413231373137303031363030303030424C4B5633303031303033363437303030303030303030300038020008000000006D1F3F20030018041539C0F1957DC64FA31883C0B120EC2364CDFDBCD06A70037B9B20
//ISO : 0810203800000A2000028100000000021834450407323039373138343639323239303043413231373137303031363030303030424C4B5633303031303033363437303030303030303030300038020008000000006D1F3F20030018041539C0F1957DC64FA31883C0B120EC2364CDFDBCD06A70037B9B20
//[00] [004] 0810
//[03] [006] 810000
//[11] [006] 000002
//[12] [006] 183445
//[13] [004] 0407
//[37] [012] 209718469229
//[39] [002] 00
//[43] [040] CA21717001600000BLKV30010036470000000000
//[63] [038] 020008000000006D1F3F20030018041539C0F1957DC64FA31883C0B120EC2364CDFDBCD06A70
//----------------------------------------------------------------------------------------------------

//[00] [004] 0810
//[03] [006] 810000
//[11] [006] 000029
//[12] [006] 185137
//[13] [004] 0418
//[37] [012] 210821558470
//[39] [002] 00
//[43] [040] CA21717001600000BLKV30010036470000000000
//[63] [038] 0200080000000034753CB2030018EC1F30339DD130E1E2F48E6C591123538F81D2A5D2AB7948
//----------------------------------------------------------------------------------------------------
//D/PrmStruct: GetTLVData T(2) L(8) V (0000000034753CB2)
//D/KeyExchange: HCN : 0000000034753CB2
//D/PrmStruct: GetTLVData T(3) L(24) V (EC1F30339DD130E1E2F48E6C591123538F81D2A5D2AB7948)
//D/KeyExchange: MSKTCN : EC1F30339DD130E1E2F48E6C591123538F81D2A5D2AB7948
//D/KeyExchange: MSK : EC1F30339DD130E1E2F48E6C59112353
//D/KeyExchange: TCN : 8F81D2A5D2AB7948
//D/KeyExchange: IMMK2 : 100
//D/KeyExchange: IMMK(MSK) : EC1F30339DD130E1E2F48E6C59112353
//D/KeyExchange: IMMK(TCN) : 56394285C46FBB29
//D/KeyExchange: HCN(MSK) : C2F193D2BB54DF0A
//D/KeyExchange: HPK(128) : E02218CE4B964BFB291680B8A104A93810785916F2815B9F61927E61018F231CE7EDC72558C0AD4B54F648F2A82666AD2AC86CAD346008EEDEAA024A0C5102117CCC0F53A83CCB3C8CEE46B54D628D002194EB460A82F8A5E92DC87589FB53E1F5E2DF456CCC609A6C7554F097F4EB4119F96255F95DB87FEB32CB44443928AB
//D/KeyExchange: HPK(ITMK) RSABlock(128) : 8B3FB231FFAC9A1CAD4025B72B0B2E06BC1ABEC6D6AD6391914E37C7B939D458A096896632AEB0BB9E8BE250D058143D5223C114465CC671555C8195378B099C6E4C4560E61413DFC08A3BAFA2040595193E29E5DD9B2A2553E6146FB18F92D06E3213CEE2003882C8CD2EA338006CEF4EACF62E6BD54B1D4DCFE7500F1FB8F1
//D/KeyExchange: RSABlock (MSK) : 694A683D1BC267E3B6A5B8125F0F35414762E77DFE60FEA15F5E3AC6181004160830AB0E48800A04099E21C5826B791BE31D49BD87C2374E9F741B8AC8222BD830AA368EED0E7C7D9DC2A0262E1162575362F1EB293F9E89992EBB676A9E43E0658AE3FD53675CCFC921D9B5558064CD40CB50C2B2C3C9638619300130BA6F99
//D/Msgs: ProcessMsg M_KEYEXCHANGE 800 810001
//D/Reversal: ***********REMOVE REVERSAL***************
//
// CA21717001600000BLKV30010036470000000000
// [00] [004] 0800
// [03] [006] 810001
// [11] [006] 000030
// [12] [006] 185137
// [13] [004] 0418
// [37] [012] 210821558470
// [43] [040] CA21717001600000BLKV30010036470000000000
// [63] [142] 040008C2F193D2BB54DF0A1A0080694A683D1BC267E3B6A5B8125F0F35414762E77DFE60FEA15F5E3AC6181004160830AB0E48800A04099E21C5826B791BE31D49BD87C2374E9F741B8AC8222BD830AA368EED0E7C7D9DC2A0262E1162575362F1EB293F9E89992EBB676A9E43E0658AE3FD53675CCFC921D9B5558064CD40CB50C2B2C3C9638619300130BA6F99
// ----------------------------------------------------------------------------------------------------
//
//D/CRC32(: 08002038000008200002810001000030185137041832313038323135353834373043413231373137303031363030303030424C4B5633303031303033363437303030303030303030300142040008C2F193D2BB54DF0A1A0080694A683D1BC267E3B6A5B8125F0F35414762E77DFE60FEA15F5E3AC6181004160830AB0E48800A04099E21C5826B791BE31D49BD87C2374E9F741B8AC8222BD830AA368EED0E7C7D9DC2A0262E1162575362F1EB293F9E89992EBB676A9E43E0658AE3FD53675CCFC921D9B5558064CD40CB50C2B2C3C9638619300130BA6F99) (8D32C609)
//D/Msgs: ISO : 08002038000008200002810001000030185137041832313038323135353834373043413231373137303031363030303030424C4B5633303031303033363437303030303030303030300142040008C2F193D2BB54DF0A1A0080694A683D1BC267E3B6A5B8125F0F35414762E77DFE60FEA15F5E3AC6181004160830AB0E48800A04099E21C5826B791BE31D49BD87C2374E9F741B8AC8222BD830AA368EED0E7C7D9DC2A0262E1162575362F1EB293F9E89992EBB676A9E43E0658AE3FD53675CCFC921D9B5558064CD40CB50C2B2C3C9638619300130BA6F99
//D/Bkm: DeviceInfo: CA21717001600000BLKV30010036470000000000
//I/ISystem.out: SHOWMSG ------ActivityList-------- LÜTFEN BEKLEYİNİZ
//I/ISystem.out: İŞLEM YAPILIYOR
//D/TcpClient: EP_CommConnect(31.145.171.94:12121)10000
//D/Comms: CommSend(248) : 00F60117001243413231373137303031363000000000080081000108002038000008200002810001000030185137041832313038323135353834373043413231373137303031363030303030424C4B5633303031303033363437303030303030303030300142040008C2F193D2BB54DF0A1A0080694A683D1BC267E3B6A5B8125F0F35414762E77DFE60FEA15F5E3AC6181004160830AB0E48800A04099E21C5826B791BE31D49BD87C2374E9F741B8AC8222BD830AA368EED0E7C7D9DC2A0262E1162575362F1EB293F9E89992EBB676A9E43E0658AE3FD53675CCFC921D9B5558064CD40CB50C2B2C3C9638619300130BA6F998D32C609
//D/Comms: CommRecv(2) : 00A9
//D/Comms: COMRECEIVE(171) : 00A901170112434132313731373030313630202020200810810001DF2F408497E4725E67764D621C60ED1574D49161DBBEE1874CF97966890B139584F1D336BC34BDD0ED2AA5F6ADAF9FA939497E2129D283C725EAF6EB91D069B4147D585E005E342D6BFA4D85EEA18C1CA556C3C0D9037FB0CD9C2BB7623AD51B505D15352222C299010A8232880F2C3525A31F5F4321F6404CDC20B36A9C71965D25AB07D4BC7394014BA2FD9855F69B
//D/Msgs: ISO : 0810203800000A2000028100010000301851370418323130383231353538343730303043413231373137303031363030303030424C4B56333030313030333634373030303030303030303000580500100089758ABEBC8774E82CD01B8A21E97B06001101ED6DB8D8A2DC61F3C846BB44EB506ED51700101DE55C0919B402D733A57CBAA03C4197
//[00] [004] 0810
//[03] [006] 810001
//[11] [006] 000030
//[12] [006] 185137
//[13] [004] 0418
//[37] [012] 210821558470
//[39] [002] 00
//[43] [040] CA21717001600000BLKV30010036470000000000
//[63] [058] 0500100089758ABEBC8774E82CD01B8A21E97B06001101ED6DB8D8A2DC61F3C846BB44EB506ED51700101DE55C0919B402D733A57CBAA03C4197
//----------------------------------------------------------------------------------------------------
//D/PrmStruct: GetTLVData T(5) L(16) V (0089758ABEBC8774E82CD01B8A21E97B)
//D/KeyExchange: TMK : 0089758ABEBC8774E82CD01B8A21E97B
//D/PrmStruct: GetTLVData T(6) L(17) V (01ED6DB8D8A2DC61F3C846BB44EB506ED5)
//D/KeyExchange: TMKInd : 1
//D/KeyExchange: TPK : ED6DB8D8A2DC61F3C846BB44EB506ED5
//D/PrmStruct: GetTLVData T(23) L(16) V (1DE55C0919B402D733A57CBAA03C4197)
//D/KeyExchange: OKK : 1DE55C0919B402D733A57CBAA03C4197
//D/KeyExchange: *******KEY EXCHANGE SUCCEEDED******
//D/PrmStruct: Save Start
//D/PrmStruct: Save End
//D/KeyExchange: ProcessKeyExchange Ended: 0
//D/ParameterDownload: ProcessDownloadPrms Started
//D/Msgs: ProcessMsg M_PRMDOWNLOAD 800 900000
//D/Reversal: ***********REMOVE REVERSAL***************
//D/PrmStruct: Save Start
//D/PrmStruct: Save End
//D/Bkm: DeviceInfo: CA21717001600000BLKV30010036470000000000
//D/VTerm: ReadVTermPrms size : 880
//D/PrmStruct: GetTLVData T(1) L(2) V (0301)
//D/PrmStruct: GetTLVData T(2) L(1) V (01)
//D/PrmStruct: GetTLVData T(1) L(103) V (0D07A000000003101007A000000003201007A000000003202007A000000003801007A000000004101007A000000004306006A0000000250106A0000000651007A000000065101007A000000152301007A000000333010107A000000672301007A0000006723020)
//D/PrmStruct: GetTLVData T(2) L(49) V (0607A000000003101007A000000003201007A000000004101007A000000004306007A000000672301007A0000006723020)
//D/PrmStruct: GetTLVData T(3) L(22) V (47FC6E736F6E7520425420536C6970206D6573616A69)
//D/PrmStruct: GetTLVData T(5) L(4) V (01393439)
//D/PrmStruct: GetTLVData T(1) L(49) V (0607A000000003101007A000000003201007A000000004101007A000000004306007A000000672301007A0000006723020)
//D/PrmStruct: GetTLVData T(2) L(49) V (0607A000000003101007A000000003201007A000000004101007A000000004306007A000000672301007A0000006723020)
//D/PrmStruct: GetTLVData T(3) L(18) V (414B54494642414E4B2047DC4E20534F4E55)
//D/PrmStruct: GetTLVData T(5) L(4) V (01393439)
//
//[00] [004] 0800
//[03] [006] 900000
//[11] [006] 000031
//[12] [006] 185138
//[13] [004] 0418
//[43] [040] CA21717001600000BLKV30010036470000000000
//[63] [136] 070037090001000000010002000010800003000000350004000000170005000000E80006000001C20008000001C2000900000140000B0000001A0D00153335303434323636373330313034363038333031380F0003020000130013030000000600020101000300010102000200001500010118001300000020202020202020202020202020202020
//----------------------------------------------------------------------------------------------------
//D/CRC32(: 08002038000000200002900000000031185138041843413231373137303031363030303030424C4B5633303031303033363437303030303030303030300136070037090001000000010002000010800003000000350004000000170005000000E80006000001C20008000001C2000900000140000B0000001A0D00153335303434323636373330313034363038333031380F0003020000130013030000000600020101000300010102000200001500010118001300000020202020202020202020202020202020) (A93E49D3)
//D/Msgs: ISO : 08002038000000200002900000000031185138041843413231373137303031363030303030424C4B5633303031303033363437303030303030303030300136070037090001000000010002000010800003000000350004000000170005000000E80006000001C20008000001C2000900000140000B0000001A0D00153335303434323636373330313034363038333031380F0003020000130013030000000600020101000300010102000200001500010118001300000020202020202020202020202020202020
//D/Bkm: DeviceInfo: CA21717001600000BLKV30010036470000000000
//I/ISystem.out: SHOWMSG ------ActivityList-------- LÜTFEN BEKLEYİNİZ
//I/ISystem.out: PARAMETRE YÜKLENİYOR
//D/TcpClient: EP_CommConnect(31.145.171.94:12121)10000
//D/Comms: CommSend(235) : 00E9011701124341323137313730303136300000000008009000003B9432A3E2DA42B6C49F77C05097C7F863A1E6DCE3B7B956C693CB2F3422C5C4499E6A50495ED5C244D9356EB30C5561CEA0A3A91C5004665D29F546A95F81D0AA13AFCA18959BE23CF7E35DFE61FDD9B2599F71DB7C04ECFBEA91584CFC90CFCACAA68E7D5C73C192C5ECB4B270A1512B0E28F27452D86E13027B324F33D6ECE7A4B4D04C635B49AAC404D1F7735B7A1D33BEC57A26B4D0180FD0D5EF2D2C69A98AB3962FD1966D55E319DC323F39C1B3CF45344F236D7C1545E6FCE7F045107CA674BF1F0AA00F83C2442BBE8BE35B
//D/Comms: CommRecv(2) : 0069
//D/Comms: COMRECEIVE(107) : 006901170112434132313731373030313630202020200810900000DF2F408497E4725EB05CD58D1EC9B9180ECC9172A98939AF10DC0E0701F83893F3AD09BC51F2A727C152C270632FE56D419E7A6150C02B46A41B5FB959A53F5407C1979B3673B2C613F84DA1F8104257
//D/Msgs: ISO : 0810203800000A2000009000000000311851380418323130383231353538343735303043413231373137303031363030303030424C4B563330303130303336343730303030303030303030
//[00] [004] 0810
//[03] [006] 900000
//[11] [006] 000031
//[12] [006] 185138
//[13] [004] 0418
//[37] [012] 210821558475
//[39] [002] 00
//[43] [040] CA21717001600000BLKV30010036470000000000
//----------------------------------------------------------------------------------------------------
//D/VTerm: ReadVTermPrms size : 880

public class KeyExchange {
    private static final String TAG = KeyExchange.class.getSimpleName();


    private static void GenerateTripleDesKey(byte[] key, int len) throws Exception {
        byte[] k1 = new byte[8], k2 = new byte[8];

        KeyGenerator keyGen = KeyGenerator.getInstance("DES");

        memcpy(k1, keyGen.generateKey().getEncoded(), 8);
        techpos.adjustDESParity(k1);

        memcpy(k2, keyGen.generateKey().getEncoded(), 8);
        techpos.adjustDESParity(k2);

        if (!DESKeySpec.isParityAdjusted(k1, 0) || !DESKeySpec.isParityAdjusted(k2, 0))
            throw new Exception("Des key exception");

        memcpy(key, k1, 8);
        memcpy(key, 8, k2, 0, 8);
    }

    public static PublicKey RSAPublicKey(byte[] modulus, byte[] exponent) throws Exception {
        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1, modulus),
                new BigInteger(exponent));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey pub = factory.generatePublic(spec);
        return pub;
    }

    public static byte[] EncryptWithHPK(byte[] HPK, byte[] in, int len) throws Exception {

        Log.i(TAG, "HPK(" + HPK.length + ") : " + Convert.Buffer2Hex(HPK));

        //byte[] b = new Comms().a(in);        return b;

//        return Openssl.EncryptWithHPK(HPK, in, len);

        //Log.i(TAG, "in(" + in.length + ") : " + Convert.Buffer2Hex(in));

        //PublicKey publicKey = DeviceImplNeptune.RSA.genPublicKey(HPK, new byte[] {1, 0, 1});

        PublicKey publicKey = RSAPublicKey(HPK, new byte[] {1, 0, 1});
        return techpos.RSA_public_encrypt(publicKey,in);


    }

    public static int ProcessKeyExchange() throws NoSuchFieldException, IllegalAccessException {
        int rv = -1, i = 0;
        byte[] ITMK = new byte[16], tmpBuff = new byte[256];

        Log.i(TAG, "ProcessKeyExchange");

        try {
            TranStruct.ClearTranData();

            currentTran.MsgTypeId = 800;
            currentTran.ProcessingCode = 810000;
            techpos.OsGetRandom(currentTran.TCN, 8);

            rv = Msgs.ProcessMsg(Msgs.MessageType.M_KEYEXCHANGE);

            if (rv == 0 && !currentTran.f39OK()) {
                rv = -1;
            }

            if(rv == 0)
            {
                //Process Keys
            /*
            EP_printf("HCN");
            EP_HexDump(currentTran.HCN, 8);

            EP_printf("IMMK(MSK)");
            EP_HexDump(currentTran.MSK, 16);
            */

//                {
//                    currentTran.HCN = Convert.Hex2Buffer("000000004577683B".getBytes());
//                    currentTran.MSK = Convert.Hex2Buffer("0AA4D9126224504E90BC5F47F87F93D4".getBytes());
//                    currentTran.TCN = Convert.Hex2Buffer("5C11D5BBDBF3122E".getBytes());
//                    ITMK = Convert.Hex2Buffer("57E52C20F45D70F45225DAE9D0FDFEE6".getBytes());
//                }

                Log.i(TAG, "IMMK2 : " + params.IMMK[0]);
                techpos.OsDES(currentTran.MSK, params.MSK, params.IMMK, 16, 0);
                techpos.OsDES(currentTran.MSK, 8, params.MSK, 8, params.IMMK, 16, 0);

                Log.i(TAG, "IMMK(MSK) : " + Convert.Buffer2Hex(currentTran.MSK));

            /*
            EP_printf("MSK");
            EP_HexDump(params.MSK, 16);

            EP_printf("IMMK(TCN)");
            EP_HexDump(currentTran.TCN, 8);
            */

                techpos.OsDES(currentTran.TCN, tmpBuff, params.MSK, 16, 0);
                Log.i(TAG, "IMMK(TCN) : " + Convert.Buffer2Hex(tmpBuff, 0, 8));

            /*
            EP_printf("TCN");
            EP_HexDump(tmpBuff, 8);
            EP_HexDump(TCN, 8);
             */

                techpos.OsDES(currentTran.HCN, tmpBuff, params.MSK, 16, 1);
                memcpy(currentTran.HCN, tmpBuff, 8);

                Log.i(TAG, "HCN(MSK) : " + Convert.Buffer2Hex(currentTran.HCN));

                GenerateTripleDesKey(ITMK, 16);

            /*
            EP_printf("ITMK");
            EP_HexDump(ITMK, 16);
            */

                byte[] b = EncryptWithHPK(params.HPK, ITMK, 16);
                memcpy(tmpBuff, b, b.length);

                Log.i(TAG, "HPK(ITMK) RSABlock(" + b.length + ") : " + Convert.Buffer2Hex(b));

            /*
            EP_printf("HPK(ITMK)");
            EP_HexDump(tmpBuff, 128);
            */

                for(i = 0; i < 128; i+= 8)
                    techpos.OsDES(tmpBuff, i, currentTran.RSAKeyBlk, i, params.MSK, 16, 1);

                Log.i(TAG, "RSABlock (MSK) : " + Convert.Buffer2Hex(currentTran.RSAKeyBlk, 0, 128));

            /*
            EP_printf("MSK(HPK(ITMK))");
            EP_HexDump(currentTran.RSAKeyBlk, 128);//***
            */

                currentTran.RSAKeyBlkLen = 128;

                currentTran.MsgTypeId = 800;
                currentTran.ProcessingCode = 810001;

                rv = Msgs.ProcessMsg(Msgs.MessageType.M_KEYEXCHANGE);
                if (rv == 0 && !currentTran.f39OK()) {
                    rv = -1;
                }
                if(rv == 0)
                {
                    //Save Keys
                /*
                EP_printf("ITMK(TMKNew)");
                EP_HexDump(currentTran.TMK, 16);
                */

                    techpos.OsDES(currentTran.TMK, params.TMK, ITMK, 16, 0);
                    techpos.OsDES(currentTran.TMK, 8, params.TMK, 8, ITMK, 16, 0);

                /*
                EP_printf("TMKNew");
                EP_HexDump(params.TMK, 16);

                EP_printf("TMK(TPKNew):%d", currentTran.TMKInd);
                EP_HexDump(currentTran.TPK, 16);
                */

                    if(currentTran.TMKInd != 0)
                    {
                        techpos.OsDES(currentTran.TPK, params.TPK, params.TMK, 16, 0);
                        techpos.OsDES(currentTran.TPK, 8, params.TPK, 8, params.TMK, 16, 0);
                    }
                    else
                    {
                        techpos.OsDES(currentTran.TPK, params.TPK, ITMK, 16, 0);
                        techpos.OsDES(currentTran.TPK, 8, params.TPK, 8, ITMK, 16, 0);
                    }

                /*
                EP_printf("TPKNew");
                EP_HexDump(params.TPK, 16);

                EP_printf("MSK(OKK)");
                EP_HexDump(currentTran.OKK, 16);
                */

                    techpos.OsDES(currentTran.OKK, params.OKK, params.MSK, 16, 0);
                    techpos.OsDES(currentTran.OKK, 8, params.OKK, 8, params.MSK, 16, 0);

                /*
                EP_printf("OKK");
                EP_HexDump(params.OKK, 16);
                */

                    Log.i(TAG,"*******KEY EXCHANGE SUCCEEDED******");
                    rv = 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            rv = -1;
        }

        PrmStruct.params.Save();
        Log.i(TAG,"ProcessKeyExchange Ended: " + rv);

        return rv;
    }

    public static int PrepareKeyExchangeMsg(Iso8583 entity) throws Exception {
        byte[] tmpBuff = new byte[1024];
        int rv = 0, idx = 0;
        short tmpLen = 0;

        if(currentTran.ProcessingCode == 810000)
        {
            idx = 0;
            memcpy(tmpBuff, idx, new byte[] {0x01, 0x00, 0x08}, 0, 3);
            idx += 3;
            memcpy(tmpBuff, idx, currentTran.TCN, 0, 8);
            idx += 8;

            entity.setFieldBin("63", Arrays.copyOf(tmpBuff, idx));
        }
        else if(currentTran.ProcessingCode == 810001)
        {
            entity.setFieldValue("37", currentTran.RRN);

            idx = 0;
            memcpy(tmpBuff, idx, new byte[] {0x04, 0x00, 0x08}, 0, 3);
            idx += 3;
            memcpy(tmpBuff, idx, currentTran.HCN, 0, 8);
            idx += 8;

            memcpy(tmpBuff, idx, new byte[] {0x1A}, 0, 1);
            idx += 1;
            tmpLen = Convert.SWAP_UINT16(currentTran.RSAKeyBlkLen);
            memcpy(tmpBuff, idx, Convert.ToArray(tmpLen), 0, 2);
            idx += 2;
            memcpy(tmpBuff, idx, currentTran.RSAKeyBlk, 0, currentTran.RSAKeyBlkLen);
            idx += currentTran.RSAKeyBlkLen;

            entity.setFieldBin("63", Arrays.copyOf(tmpBuff, idx));
        }

        return rv;
    }

    public static int ParseKeyExchangeMsg(HashMap<String, byte[]> isoMsg)
    {
        int rv = 0;
        byte[] tlvData = new byte[1024];

        if(isoMsg.containsKey("63")) {
            int len = isoMsg.get("63").length;
            byte[] tmpBuff = isoMsg.get("63");

            if(techpos.GetTLVData(tmpBuff, len, 0x02, tlvData, (short) c.sizeof(tlvData)) > 0)
            {
                memcpy(currentTran.HCN, tlvData, 8);
                Log.i(TAG, "HCN : " + Convert.Buffer2Hex(currentTran.HCN));
            }

            if(techpos.GetTLVData(tmpBuff, len, 0x03, tlvData, (short) c.sizeof(tlvData)) > 0)
            {
                memcpy(currentTran.MSK, tlvData, 16);
                memcpy(currentTran.TCN, 0, tlvData, 16, 8);

                Log.i(TAG, "MSKTCN : " + Convert.Buffer2Hex(tlvData, 0, 24));
                Log.i(TAG, "MSK : " + Convert.Buffer2Hex(currentTran.MSK));
                Log.i(TAG, "TCN : " + Convert.Buffer2Hex(currentTran.TCN));
            }

            if(techpos.GetTLVData(tmpBuff, len, 0x05, tlvData, (short) c.sizeof(tlvData)) > 0)
            {
                memcpy(currentTran.TMK, tlvData, 16);
                Log.i(TAG, "TMK : " + Convert.Buffer2Hex(currentTran.TMK));
            }

            if(techpos.GetTLVData(tmpBuff, len, 0x06, tlvData, (short) c.sizeof(tlvData)) > 0)
            {
                currentTran.TMKInd = tlvData[0];
                memcpy(currentTran.TPK, 0, tlvData, 1, 16);
                Log.i(TAG, "TMKInd : " + currentTran.TMKInd);
                Log.i(TAG, "TPK : " + Convert.Buffer2Hex(currentTran.TPK));
            }

            if(techpos.GetTLVData(tmpBuff, len, 0x17, tlvData, (short) c.sizeof(tlvData)) > 0)
            {
                memcpy(currentTran.OKK, tlvData, 16);
                Log.i(TAG, "OKK : " + Convert.Buffer2Hex(currentTran.OKK));
            }
        }

        return rv;
    }

}
