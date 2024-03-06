package com.blk.platform_castle;

import android.util.Log;
import com.blk.platform_castle.emv.EmvCtos;
import com.blk.platform_castle.emv.Security;
import com.blk.platform_castle.emvcl.EmvclCtos;
import com.blk.platform.IPlatform;
import com.blk.sdk.olib.olib;
import CTOS.CtCL;
import CTOS.CtEMV;
import CTOS.CtEMVCL;
import CTOS.CtEMVMSR;
import CTOS.CtMSR;
import CTOS.CtPrint;
import CTOS.CtSC;
import CTOS.CtSettings;
import CTOS.CtSystem;

public class PlatfromCtos extends IPlatform {
    public static CtEMVMSR _emvMsr = new CtEMVMSR();
    public static CtSC _sc = new CtSC();
    public static CtMSR _msr = new CtMSR();
    public static CtCL _mifare = new CtCL();
    public static CtEMVCL _emvcl = new CtEMVCL();

    public static CtEMV _emv = new CtEMV();
    public static CTOS_API _api = new CTOS_API();
    public static CtPrint _printer = new CtPrint();
    public static CtSystem _system = new CtSystem();
    public static CtSettings _settings = new CtSettings();

    private static final String TAG = PlatfromCtos.class.getSimpleName();

    public PlatfromCtos()
    {
        system = new SystemCtos();
        printer = new PrinterCtos();
        card = new CardCtos();
        emv = new EmvCtos();
        emvcl = new EmvclCtos();
        icc = new IccCtos();
        mifare = new MifareCtos();

        _emvMsr.initialize();
        _emvMsr.setTracksEncryptInfo(Security.GetEMVSecureDataInfo());

        LogInfo();
    }

    public static void LogInfo()
    {
        try {
            //((SystemCtos) ctos.system).PrintVersions();
            Log.i(TAG, "ISystem Info -----------------------------------");
            Log.i(TAG, "Model : " + _system.getModelName());
            Log.i(TAG, "DebugMode : " + _settings.getDebugModeState());

//            if (!_settings.getDebugModeState()) {
//                Log.i(TAG, "set debug mode finish activity.");
//                String str = _settings.setDebugModeState(true);
//                UiUtil.GetMainActivity().finish();
//            }
            Log.i(TAG, "" + SystemCtos.EModuleId.ID_LIBCTOSAPI_SO.name() + " : " + PlatfromCtos._system.getModuleVersion(SystemCtos.EModuleId.ID_LIBCTOSAPI_SO.getValue()));
            Object o = olib.GetStaticField("CTOS.CTOS_CtEMV_VERSION", "CTOS_CtEMV_JAR_DATE_STR");
            Log.i(TAG, "" + "CtEmv package : " + o.toString() + " system : " + PlatfromCtos._system.getModuleVersion(SystemCtos.EModuleId.ID_EMV.getValue()));
            o = olib.GetStaticField("CTOS.CTOS_CtEMVCL_VERSION", "CTOS_CtEMVCL_JAR_DATE_STR");
            Log.i(TAG, "" + "CtEMVCL package : " + o.toString() + " system : " + PlatfromCtos._system.getModuleVersion(SystemCtos.EModuleId.ID_EMVCL.getValue()));

            Log.i(TAG, "-----------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean DebugMode(Boolean fOpen)
    {
        try {
            if (fOpen == null)
                return PlatfromCtos._settings.getDebugModeState();

            String str = PlatfromCtos._settings.setDebugModeState(fOpen);

            Log.i(TAG, "debug mode : " + str);

            return PlatfromCtos._settings.getDebugModeState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  false;
    }

    @Override
    public String Name() {
        return "CASTLE";
    }

}
