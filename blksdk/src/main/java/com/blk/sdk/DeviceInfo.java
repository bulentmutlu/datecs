package com.blk.sdk;

import com.blk.platform.IPlatform;

/**
 * Created by id on 2.03.2018.
 */

public class DeviceInfo {

    private static final String TAG = DeviceInfo.class.getSimpleName();

    public static final int M_NULL = 0;
    public static final int M_S900 = 1;
    public static final int M_S800 = 2;
    public static final int M_S300 = 3;


    public int			dModel;
    public String		dModelName;//[16];
    public String		dSerial;//[16];
    public String		dOSVer;//[16];
    public String		dIMEI;//[24];
    public byte		dETH;				//0x30 no, 0x31 yes
    public byte		dDhcpMode;			//0x30 no, 0x31 yes
    public String		dMAC;//[18];
    public String		RFU;//[928];

    public static DeviceInfo devInfo;
    //public static IDAL idal = null;

    private DeviceInfo()
    {
    }

    public static void Init() throws Exception {

        if (devInfo != null)
            return;
        devInfo = new DeviceInfo();

        devInfo.dSerial =  IPlatform.get().system.Serial();
        devInfo.dModelName = IPlatform.get().system.Model();
//        idal = NeptuneLiteUser.getInstance().getDal(context);
//
//
//        ISys iSys = idal.getSys();
//
//        Map<ETermInfoKey, String> termInfo = iSys.getTermInfo();
//        devInfo.dSerial = termInfo.get(ETermInfoKey.SN);
//        devInfo.dModelName = termInfo.get(ETermInfoKey.MODEL);
    }

}
