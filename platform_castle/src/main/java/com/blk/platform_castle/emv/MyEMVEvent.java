package com.blk.platform_castle.emv;


import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.blk.platform.Emv.IEmv;
import com.blk.sdk.Convert;
import com.blk.sdk.UI;
import com.blk.sdk.string;
import com.blk.sdk.Utility;
import com.blk.sdk.activity.BaseActivity;
import com.blk.sdk.activity.BaseDialog;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import CTOS.CtEMV;
import CTOS.emv.EMVAppListExData;
import CTOS.emv.EMVEvent;
import CTOS.emv.EMVGetPINFuncPara;
import CTOS.emv.EMVTxnData;

class  MyEMVEvent extends EMVEvent implements CtEMV.IEventPINBypass, CtEMV.IEventShowVirtualPINEx,
        CtEMV.IEventGetPINDone, CtEMV.IEventOfflinePINVerifyResult, CtEMV.IEventTxnForcedOnline, CtEMV.IEventDebugMsg
{
    private static final String TAG = MyEMVEvent.class.getSimpleName();

    //private Converter convert = new Converter();
    private byte pinType;
    public BaseActivity activity;
    private CtEMV emv;

    public IEmv.TransactionData tranData;

    boolean pinEntryTried = false;
    int remainingCounter;
    byte txnResult;
    boolean isSignatureRequired;

    MyEMVEvent(CtEMV emv)
    {
        version = 1;
        this.emv = emv;
    }

    //Out	EMVTxnData
    @Override
    public int onTxnDataGet(EMVTxnData txnData)
    {
        final String func = new Object(){}.getClass().getEnclosingMethod().getName();
        Log.i(func, "onTxnDataGet trigger-->");

        String strDate= new SimpleDateFormat("yyMMdd").format(tranData.dt);
        String strTime =  new SimpleDateFormat("HHmmss").format(tranData.dt);

        txnData.version = 3;	//version should be set to 3
        System.arraycopy(Convert.Hex2Buffer(tranData.amount.getBytes()), 0, txnData.amount, 0, 6);				//amount is 6-byte array
        System.arraycopy(Convert.Hex2Buffer(tranData.amountCB.getBytes()), 0, txnData.amountOther, 0, 6);				//amount is 6-byte array
        txnData.posEntryMode = 0x00;
        txnData.txnType = tranData.txnType;

        System.arraycopy(strDate.getBytes(StandardCharsets.UTF_8), 0, txnData.txnDate, 0, 6);
        System.arraycopy(strTime.getBytes(StandardCharsets.UTF_8), 0, txnData.txnTime, 0, 6);

        return 0;
    }

    boolean apppListOK;
    short appSelectedIndex;
    //In	appListExData.appNum;
    //In	appListExData.appInfo[];
    //Out	appListExData.appSelectedIndex;
    @Override
    public int onAppListEx(EMVAppListExData appListExData)
    {
        Log.i(TAG, "onAppListEx trigger-->");

        String[] appList = new String[appListExData.appNum];
        for(int i = 0 ; i < appListExData.appNum ; i++)
        {
            appList[i] = new String(appListExData.appInfo[i].appLabel);
            Log.i(TAG, appList[i]);
        }

        apppListOK = false;

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Please Select One App to Execute");
        builder.setItems(appList, (dialog, pos) -> {
            Log.i(TAG, "onAppListEx onClick select : " + String.format("%d" , pos));
            apppListOK = true;
            appSelectedIndex = (short)(pos);
        });
        builder.setOnCancelListener(dialogInterface -> {
            apppListOK = true;
            appSelectedIndex = -1;
        });



        activity.runOnUiThread(() -> {

            AlertDialog ad;
            ad = builder.create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);

            //activity.dialog.hide();
            activity.baseDialog.hide();

            ad.show();
        }
        );

        do
        {
            try{Thread.sleep(100);}
            catch(Exception e)
            {
                e.printStackTrace();
                return CtEMV.d_EMVAPLIB_ERR_SELECTION_FAIL;
            }

        }while(!apppListOK);

        if (appSelectedIndex == -1)
            return CtEMV.d_EMVAPLIB_ERR_USER_ABORT;

        //Range of appListExData.appSelectedIndex value is 0 to (appListExData.appNum -1)
        appListExData.appSelectedIndex = appSelectedIndex;
        Log.i(TAG, "OnAppList Selected Index : " + appSelectedIndex + " AppLabel : " + appList[appSelectedIndex]);

        return 0;
    }

    //In	isRequiredByCard
    //In	appLabel
    //In	appLabelLen
    @Override
    public int onAppSelectedConfirm(boolean isRequiredByCard, byte[] appLabel, byte appLabelLen)
    {
        Log.i(TAG, "isRequiredByCard(" + isRequiredByCard + " appLabel(" + new string(appLabel, 0, appLabelLen).TrimStart('0'));
        return 0;
    }

    //offlinePinActivity offlineA;
    BaseDialog offlineA;
    //IN	type
    //IN	remainingCounter
    //OUT	onlinePinPara
    @Override
    public int onGetPINNotify(byte type, final int remainingCounter, EMVGetPINFuncPara getPinPara)
    {
        Log.i(TAG, "OnGetPINNotify offlinepin(" + type + ")" + "remaincounter(" + remainingCounter + ")");
        this.remainingCounter = remainingCounter;

        pinEntryTried = true;

        getPinPara.version = 1;

        if(type == 1)
        {
            //offline PIN, use internal PIN Get
            getPinPara.isInternalPINPad = 1;

//            Intent intent = new Intent(BaseActivity.GetTopActivity(), offlinePinActivity.class);
//            offlineA = (offlinePinActivity) BaseActivity.StartActivity(intent, true);;
//
//            while (!offlineA.activityVisible) Utility.EP_Wait(10);
//            Utility.EP_Wait(5000);

            AtomicBoolean waitUI = new AtomicBoolean(true);
            if (offlineA == null) {
                Log.i(TAG, "offline dialog " + activity.toString());
                activity.runOnUiThread(() -> {
                    activity.baseDialog.hide();
                    //activity.dialog.hide();
                    offlineA = new BaseDialog(activity, false);

                    // TODO: burda show yapma
                    offlineA.show();
                    while (!offlineA.isShowing()) Utility.sleep(10);

                    waitUI.set(false);
                });

                while (waitUI.get())
                    Utility.sleep(10);
                // TODO: bekleme
                Utility.sleep(500);
            }
        }
        else
        {
            //online PIN, use external PIN Get
            getPinPara.isInternalPINPad = 0;
        }


        getPinPara.timeout = 60;
        getPinPara.maxPINDigitLength = 4;
        getPinPara.minPINDigitLength = 4;

		/*
		getPinPara.onlinePINCipherKeySet = 0xC001;
		getPinPara.onlinePINCipherKeyIndex = 0x0000;
		*/

        this.pinType = type;
        return 0;
    }



    //for Virtual PIN EX (customize Virtual PIN)
    //return a fixed int[16][5] buffer as keyboard attribute
    public int[][] eventShowVirtualPINEx()
    {
        Log.d(TAG, "eventShowVirtualPINEx trigger--> " + offlineA);

        //private static final byte VKBD_0 = '0';    	// 0x30;
        //private static final byte VKBD_1 = '1';    	// 0x31;
        //private static final byte VKBD_2 = '2';    	// 0x32;
        //private static final byte VKBD_3 = '3';    	// 0x33;
        //private static final byte VKBD_4 = '4';    	// 0x34;
        //private static final byte VKBD_5 = '5';    	// 0x35;
        //private static final byte VKBD_6 = '6';    	// 0x36;
        //private static final byte VKBD_7 = '7';    	// 0x37;
        //private static final byte VKBD_8 = '8';    	// 0x38;
        //private static final byte VKBD_9 = '9';    	// 0x39;
        //private static final byte VKBD_ENTER = 'A';   // 0x41;
        //private static final byte VKBD_CLEAR = 'R';   // 0x52;
        //private static final byte VKBD_CANCEL = 'C';  // 0x43;
        //private static final byte VKBD_SPACE = 'S';   // 0x53;

        //keyboard attribute is a fixed int[16][5] buffer !!
        int[][] KBDAttribute = new int[16][5];
        TextView[] tv = new TextView[16];
        int[] XY = new int[2];
        int x;
        int y;
        int w;
        int h;


        for(int i = 0 ; i < 16 ; i++)
        {
            switch(i)
            {
                //set key value for key borad 0 ~ 9, enter, cancel, clear(backspace)
                case 0 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD0);		KBDAttribute[i][4] = '0';	break;
                case 1 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD1);		KBDAttribute[i][4] = '1';	break;
                case 2 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD2);		KBDAttribute[i][4] = '2';	break;
                case 3 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD3);		KBDAttribute[i][4] = '3';	break;
                case 4 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD4);		KBDAttribute[i][4] = '4';	break;
                case 5 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD5);		KBDAttribute[i][4] = '5';	break;
                case 6 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD6);		KBDAttribute[i][4] = '6';	break;
                case 7 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD7);		KBDAttribute[i][4] = '7';	break;
                case 8 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD8);		KBDAttribute[i][4] = '8';	break;
                case 9 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD9);		KBDAttribute[i][4] = '9';	break;
                case 10 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD_Cancel);	KBDAttribute[i][4] = 'C';	break;
                case 11 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD_Clear);	KBDAttribute[i][4] = 'R';	break;
                case 12 :	tv[i] = (TextView) offlineA.findViewById(com.blk.sdk.R.id.KBD_Enter);	KBDAttribute[i][4] = 'A';	break;

                //set the key value to 'S' for key board not in above(key borad 0 ~ 9, enter, cancel, clear)
                default :	KBDAttribute[i][4] = 'S';	break;
            }

            if(i > 12)
            {
                x = 10;
                y = 10;
                w = 1;
                h = 1;
            }
            else
            {
                tv[i].getLocationOnScreen(XY);
                x = XY[0];
                y = XY[1];
                w = tv[i].getWidth();
                h = tv[i].getHeight();
            }


            KBDAttribute[i][0] = x;
            KBDAttribute[i][1] = y;
            KBDAttribute[i][2] = x + w;
            KBDAttribute[i][3] = y + h;

//            Log.d("KeyValue = ", String.valueOf(KBDAttribute[i][4]));
//            Log.d("location x = ", String.valueOf(x));
//            Log.d("location y = ", String.valueOf(y));
//            Log.d("location x + width = ", String.valueOf(KBDAttribute[i][2]));
//            Log.d("location y + height = ", String.valueOf(KBDAttribute[i][3]));

        }

        Log.d(TAG, "eventShowVirtualPINEx END-----------------------");

        return KBDAttribute;
    }


    //IN	digitsNum
    @Override
    public void onShowPINDigit(byte digitsNum)
    {
        Log.i(TAG, "onShowPINDigit digitsNum(" + digitsNum + ")" + offlineA);

        byte[] pinMaskStr = new byte[digitsNum + 1];
        Arrays.fill(pinMaskStr, 0, digitsNum, (byte)'*');
        final String str = new String(pinMaskStr);


        activity.runOnUiThread(() -> {
            TextView textView = offlineA.findViewById(com.blk.sdk.R.id.textViewPinDigitEx);

            String text;
            if(pinType == 0)
                text = "Enter Online Pin(" + remainingCounter + ") :\n" + str + "\n";
            else {
                text = "Şifre Giriniz\n";
                if (remainingCounter == 1)
                    text += "SON DENEME\n";
                text += str;
            }
            textView.setText(text);
        });
    }



    @Override
    public int eventGetPINDone() {
        Log.i(TAG, "eventGetPINDone");

        if (offlineA != null) {
            activity.runOnUiThread(() -> {
                offlineA.dismiss();
                offlineA = null;
                Log.i(TAG, "offline dialog END");
            });
        }
        return 0;
    }


    @Override
    public void onShowPINBypass() {
        Log.i(TAG, "onShowPINBypass");
    }
    @Override
    public byte eventPINBypass() {
//        This event will be triggered when PIN Bypass key is pressed in an
//        offline pin process. The message of confirmation of PIN Bypass can be
//        shown to the cardholder for confirming if they would like to PIN Bypass.
//        The following actions can be brought back to the kernel to bypass PIN
//        or restart a process of getting offline PIN.
//        Parameters:
//        None
//        Returns:
//        0 - Action : Bypass PIN
//        1 - Action : Don't bypass PIN, back to get PIN process
//        2 - Action : Don't bypass PIN, trigger
//        eventShowVirtualPINEx() again then back to get PIN process

        if (tranData.emvEvent.onPINBypass()) {
            Log.i(TAG, "eventPINBypass OK");
            activity.runOnUiThread(() -> Toast.makeText(activity, "Pin Bypass", Toast.LENGTH_LONG).show());
            return 0;
        }
        Log.i(TAG, "eventPINBypass NO Permission");
        return 1;
    }

    @Override
    public int eventOfflinePINVerifyResult(int i) {
        String msg = "";
        if (i == CtEMV.d_PIN_RESULT_OK) {
            msg = "Şifre Doğru";
            String finalMsg = msg;
            activity.runOnUiThread(() -> Toast.makeText(activity, finalMsg, Toast.LENGTH_LONG).show());
        } else if (i == CtEMV.d_PIN_RESULT_FAIL) {
            //"!PIN Wrong!"
            msg = "Şifre Yanlış";
            UI.ShowMessage(msg);
        } else if (i == CtEMV.d_PIN_RESULT_BLOCKED || i == CtEMV.d_PIN_RESULT_FAILBLOCKED) {
            //"!PIN Blocked!"
            msg = "Pin Bloke";
            UI.ShowMessage(msg);
        }
        Log.i(TAG, "eventOfflinePINVerifyResult( " + i + " ) " + msg);
        return 0;
    }


    //In	txnResult
    //In	isSignatureRequired
    @Override
    public void onTxnResult(byte txnResult, boolean isSignatureRequired)
    {

//#define d_TXN_RESULT_APPROVAL                                           0x01
//#define d_TXN_RESULT_DECLINE                                            0x02
//#define d_TXN_RESULT_GO_ONLINE                                          0x03
//#define d_TXN_RESULT_FORCED_APPROVAL                                    0x04

        Log.i(TAG, "onTxnResult txnResult(" + txnResult + ") isSignatureRequired(" + isSignatureRequired + ")");
        this.txnResult = txnResult;
        this.isSignatureRequired = isSignatureRequired;

//        GlobalPara.isNeedSignature = isSignatureRequired;
//
//        switch(txnResult)
//        {
//            case (byte)0x01:	GlobalPara.transactionResult = 0x0002;		break;
//            case (byte)0x02:	GlobalPara.transactionResult = 0x0003;		break;
//            case (byte)0x03:	GlobalPara.transactionResult = 0x0004;		break;
//
//            default:			GlobalPara.transactionResult = 0x00FF;		break;
//        }
    }

    @Override
    public byte eventTxnForcedOnline() {
        Log.i(TAG, "eventTxnForcedOnline " + tranData.forceOnline);
        return tranData.forceOnline ? (byte)1: 0;
    }

    @Override
    public int eventDebugMsg(byte[] bytes, int i) {
        Log.i(TAG, "eventDebugMsg i(" + i + ") msg(" + new String(bytes) + ")");
        return 0;
    }
}
