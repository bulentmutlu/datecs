package com.blk.techpos;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.blk.platform.IPlatform;
import com.blk.sdk.Convert;
import com.blk.sdk.UI;
import com.blk.sdk.Utility;
import com.blk.sdk.activity.BaseActivity;
import com.blk.techpos.Bkm.Messages.Msgs;
import com.blk.techpos.tran.Tran;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public Thread myThread;
    public boolean fShowMenu = false;
    TextView textView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(" + (savedInstanceState == null) + ")");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UI.ShowMessage(0, "Lütfen Bekleyiniz");


        textView = (TextView) findViewById(R.id.textView);
        //textView.setText("Uygulama açılıyor...");

        findViewById(R.id.start_image).setOnClickListener(view -> fShowMenu = true);

        try {
            Log.i(TAG, "MainActivity Start : " + Thread.currentThread().getId());
            //Toast.makeText(this, "BLK TECHPOS", Toast.LENGTH_LONG).show();

//            if (!isInited) {
//                try {
//                    Utility.EP_Init(a);
//                    Utils.TechposInit();
//                    isInited = true;
//                    //CallWebService.createRequest();
//
////                    WebServicePrms.Read();
//                    //if(WebServicePrms.GetWSParams().isOdeal.equals("True")){
//                        //Msgs.ProcessMsg(Msgs.MessageType.M_INIT);
////                        CallWebService.OdealInit();
//                    //}
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return;
//                }
//            }


            myThread = new Thread(() -> workerThread(this));
            myThread.start();
        } catch (Exception e) {
            e.printStackTrace();

            IPlatform.get().system.beepErr();
            UI.ShowMessage(15000, e.getMessage());
        }

    }
        @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        myThread.interrupt();
        try {
            myThread.join();
        } catch (InterruptedException ignored) {
        }
        Utility.Destroy();
        super.onDestroy();

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    boolean firstStart = true;
    static int[] images = new int[16];

    public void workerThread(Activity activity) {
        Log.i(TAG, "Main Thread Start : " + Thread.currentThread().getId());

        try {
            techpos.Init();

            byte[] in = new byte[] {1, 1, 1, 1, 1, 1, 1, 1};
            byte[] key = new byte [] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            byte [] out = Msgs.DecryptMsg2(key, in, 0, 8);
            Log.i(TAG, "out : " + Convert.byteArray2HexString(out));



//                    IPlatform.get().IPrinter.typeface =
//                            Typeface.createFromAsset(a.getAssets(),
//                            // "JetBrainsMono-ExtraBold.ttf"
//                                    //"Bitstream Vera Sans Mono Bold.ttf"
//                                    //"DejaVuSansMono-Bold.ttf"
//                                    //"VeraMono.ttf"
//                                    "Code New Roman b.otf"
//                            //"VeraMono-Bold.ttf");//"DejaVuSansMono.ttf"
//                    );
            //new Typeface.Builder(a.getAssets(), "DejaVuSansMono.ttf").setWeight(1000).build();
            //IPrinter.PrintTest();
            //finish();

//                    UI.ShowQR2("QR OKUTUNUZ",
//                            Utility.QRencodeAsBitmap("idrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidrisidris",
//                                    UiUtil.ScreenSize().getWidth(), UiUtil.ScreenSize().getWidth() * 4 / 5),
//                            10000);


            //ODEALSERVİS
//                    CallWebService.createRequest();
//                    WebServicePrms.Read();

//                   if(!TcpClient.IsNetworkOnline()){
//                       UI.ShowMessage("LÜTFEN İNTERNET BAĞLANTINIZI KONTROL EDİN");
//                   }

//                    Odeal.HandleInit();

            UI.UiUtil.ShowMessageHide();
        } catch (Exception e) {
            UI.ShowMessage(60000, e.getMessage());
            e.printStackTrace();
            return;
        }

        while (!Thread.currentThread().isInterrupted()) {




            if (!fShowMenu) {
                Utility.sleep(100);
                continue;
            }
            activity.runOnUiThread(() -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));

            try {
                //if(firstStart){
//                        while (TranProc.TransMenu(true) != -6) ;
//                                    firstStart=false;
                //  }
                switch (UI.ShowList("TECHPOS", new String[]{"İŞLEMLER", "İŞYERİ","ADMİN"}, new int[]{
                        R.drawable.islem_menu, R.drawable.isyeri_menu,R.drawable.kurulum}, UI.ViewType.VIEW_GRID)) {
                    case 0:

                       Tran.StartTran(true);

                        //while (TranProc.TransMenu(true) != -6) ;
                        break;
                    case 1:
                        Apps.MerchantMenu();
                        break;
                    case 2:
                        Apps.AdminMenu();
                        break;
                    default:
                        fShowMenu = false;
                        activity.runOnUiThread(() -> activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
                        break;
                }
                UI.UiUtil.ShowMessageHide();

            } catch (Exception e) {
                e.printStackTrace();

                try {
                    if (Apps.Debug() < 0)
                        break;

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

}
