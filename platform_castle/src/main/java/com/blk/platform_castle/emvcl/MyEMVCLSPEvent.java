package com.blk.platform_castle.emvcl;

import android.util.Log;

import com.blk.ctos_api.R;
import com.blk.platform.Emv.IEmv;
import com.blk.sdk.UI;
import com.blk.sdk.activity.BaseActivity;

import CTOS.CtEMVCL;
import CTOS.emvcl.EMVCLAppListV4;
import CTOS.emvcl.EMVCLUIReqData;

class MyEMVCLSPEvent implements CtEMVCL.IEventEMVCLLEDPicShow, CtEMVCL.IEventEMVCLAudioIndication, CtEMVCL.IEventEMVCLShowMessage, CtEMVCL.IEventEMVCLAppListV4
{
    private static final String TAG = MyEMVCLSPEvent.class.getSimpleName();

    public IEmv.TransactionData tranData;;

    public ClsAudioInidcator audio;
    public MyEMVCLSPEvent()
    {
        if (audio == null) {
            audio = new ClsAudioInidcator(R.raw.ok_tone, R.raw.alert_tone, R.raw.cancel_key_tone,
                    UI.UiUtil.getApplicationContext());
                    //BaseActivity.GetMainActivity());
        }
    }

    public int eventEMVCLLEDPicShow(byte bIndex, byte bOnOff)
    {
        //GlobalPara.clLED.eventEMVCLLEDPicShow(bIndex,bOnOff);

        Log.i(TAG,"eventEMVCLLEDPicShow bIndex("+bIndex+") bOnOff("+bOnOff+")");
        return 0;
    }


    public int eventEMVCLAudioIndication(short tone)
    {
        if(tone == 0x01)
        {
            //PlatfromCtos._api.CTOS_Sound(2700, 20);
            audio.playOKSound();
        }
        else
        {
//            PlatfromCtos._api.CTOS_Sound(750, 20);
//            Utility.EP_Wait(200);
//            PlatfromCtos._api.CTOS_Sound(750, 20);
            audio.playAlertSound();
        }

        Log.i(TAG,"eventEMVCLAudioIndication:="+String.valueOf(tone));
        return 0;
    }

    void ui_ShowMsg(String msg)
    {
        Log.i(TAG, msg);
    }

    public int eventEMVCLShowMessage(byte[] KernelId, byte KernelIdLen, final EMVCLUIReqData UIReqData)
    {
        Thread th;

        th = new Thread(() -> {
            switch(UIReqData.messageIdentifier)
            {
                case (byte) 0x03:
                    ui_ShowMsg("Transaction Approved");
                    break;

                case (byte) 0x07:
                    ui_ShowMsg("Transaction Declined");
                    break;

                case (byte) 0x09:
                    ui_ShowMsg("Please Enter PIN:");
                    break;

                case (byte) 0x0F:
                    ui_ShowMsg("Processing Error");
                    break;

                case (byte) 0x10:
                    ui_ShowMsg("Please Remove Card");
                    break;

                case (byte) 0x14:
                    ui_ShowMsg("Welcome");
                    break;

                case (byte) 0x15:
                    ui_ShowMsg("Please Present card");
                    break;

                case (byte) 0x16:
                    ui_ShowMsg("Processing ...");
                    break;

                case (byte) 0x17:
                    ui_ShowMsg("Card read OK");
                    break;

                case (byte) 0x18:
                    ui_ShowMsg("Please Insert or swipe card");
                    break;

                case (byte) 0x19:
                    ui_ShowMsg("Please Present One card only");
                    break;

                case (byte) 0x1A:
                    ui_ShowMsg("Transaction Approved, please sign");
                    break;

                case (byte) 0x1B:
                    ui_ShowMsg("Authorising, please wait");
                    break;

                case (byte) 0x1C:
                    ui_ShowMsg("Error, Please try other card");
                    break;

                case (byte) 0x1D:
                    ui_ShowMsg("Please Insert Card");
                    break;

                case (byte) 0x1E:
                    //message clear command
                    ui_ShowMsg("                                        ");
                    break;

                case (byte) 0x20:
                    ui_ShowMsg("Please refer to device for Instruction");
                    break;

                case (byte) 0x21:
                    ui_ShowMsg("Please Try again");
                    break;

                case (byte) 0xA0:
                    ui_ShowMsg("No card present");
                    break;

                case (byte) 0xA1:
                    ui_ShowMsg("Card read Failure");
                    break;

                case (byte) 0xA2:
                    ui_ShowMsg("Application Not Supported");
                    break;

                default:
                    ui_ShowMsg("None define Message " + UIReqData.messageIdentifier);
                    break;
            }
        });

        th.start();

        if(UIReqData.messageIdentifier == (byte) 0x15 || UIReqData.messageIdentifier == (byte) 0x16)	//"present card", "processing" do not delay
        {

        }
        else if(UIReqData.messageIdentifier == (byte) 0x17)		//"Read Card OK"
        {
//            try{Thread.sleep(800);}
//            catch(Exception e){e.printStackTrace();}
        }
        else
        {
//            try{Thread.sleep(700);}
//            catch(Exception e){e.printStackTrace();}
        }

        //Log.d(TAG,"eventEMVCLShowMessage AP");
        return 0;
    }

    @Override
    public int eventEMVCLAppListV4(EMVCLAppListV4 emvclAppListV4) {
        Log.d(TAG,"eventEMVCLAppListV4");
        return 0;
    }
}
