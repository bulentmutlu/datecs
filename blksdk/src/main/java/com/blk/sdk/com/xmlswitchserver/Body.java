package com.blk.sdk.com.xmlswitchserver;



/*
<body       SoftwareVersion="1"             AppName="METRO"
            posSerial="001005817207"        posModel="ANDROID"
            terminalID="2222222222"         merchantID="8888888888"
            osVer="1"                       claVersion="1"
            managerVersion="1"              ecrVersion="1"
            SelfIp="1"                      SelfMac="1"
            SelfImei="1"                    tranDate="130509"
            tranTime="032511">
    <BaMobile       cardno=""
                    securecode=""/>
</body>
*/

import com.blk.platform.IPlatform;
import com.blk.sdk.Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Body {

    public String SoftwareVersion;
    public String AppName;
    public String posSerial;
    public String posModel;
    public String terminalID;
    public String merchantID;
    public String osVer;
    public String claVersion;
    public String managerVersion;
    public String ecrVersion;
    public String SelfIp;
    public String SelfMac;
    public String SelfImei;
    public String tranDate;
    public String tranTime;
    public String currentDate;
    public String currentTime;
    public String specialMessage;
    public String remainder;
    public String appData;

    public Body() throws Exception {
        super();
        setDefaultValues();
    }

    private void setDefaultValues() throws Exception {
        SoftwareVersion= Utility.GetAppVersion(false);// DynamicData.getVersionString();
        AppName= Utility.appName;
        posSerial= IPlatform.get().system.Serial();// DynamicData.getUniqueID();
        posModel= IPlatform.get().system.Model();// DynamicData.getPosModel();
        osVer="1";
        claVersion="1";
        managerVersion="1";
        ecrVersion="1";
        SelfIp="1";
        SelfMac="1";
        SelfImei="1";

        SimpleDateFormat df_date = new SimpleDateFormat("ddMMyy", java.util.Locale.getDefault());
        tranDate = df_date.format(Calendar.getInstance().getTime());

        SimpleDateFormat df_time = new SimpleDateFormat("HHmmss", java.util.Locale.getDefault());
        tranTime = df_time.format(Calendar.getInstance().getTime());
    }
}
