package com.blk.fatura_vizyon.model;

import com.blk.sdk.file;
import com.blk.sdk.emulator.Emulator;
import com.blk.sdk.olib.olib;
import java.util.ArrayList;
import java.util.List;

public class Parameters {

    public static class Tcpip
    {
        public String destIp;
        public int destPort;
        public int recvTO;
        public int connTO;

        public Tcpip(String destIp, int destPort, int recvTO, int connTO)
        {
            this.destIp = destIp;
            this.destPort = destPort;
            this.recvTO = recvTO;
            this.connTO = connTO;
        }
    }

    private static final String  fileName = "Parameters";
    public static Parameters params = new Parameters();


    public Tcpip tcpIp = new Tcpip(Emulator.localHostPcIp, 5150, 30000, 10000);
    public String TCKN = "19484090926"; //[16];
    public String VN = "0006000625"; //[16];
    public String MerchName = "BLK BILGI";//[64];
    public String TaxpayerName = "BLK BILGI SITEMLERI";//[64];
    public String MerchAddr1 = "Avrupa Konutlari 1";//[64];
    public String MerchAddr2 = "Blok 10A, Daire:29";//[64];
    public String MerchAddr3 = "KucukCekmece / Istanbul";//[64];
    public String TaxofficeInfo = "Kucukcekmece VD. 19484090926";//[64];
    public String MerchantId = "";
    public String TerminalId = "";
    public boolean fDownloaded;

    public List<Institution> institutionList = new ArrayList<>();

    public static void Read()
    {
        try {
            String json = file.ReadAllText(fileName);
            olib.DeserializeJson(params, json);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
    public static void Write()
    {
        try {
            file.WriteAllText(fileName, olib.SerializeJson(params));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
