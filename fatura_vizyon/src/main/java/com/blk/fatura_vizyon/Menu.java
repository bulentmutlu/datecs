package com.blk.fatura_vizyon;

import static com.blk.fatura_vizyon.model.Parameters.params;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.blk.fatura_vizyon.model.BillInfo;
import com.blk.fatura_vizyon.model._BillInquiry;
import com.blk.fatura_vizyon.model.Institution;
import com.blk.fatura_vizyon.model.Response;
import com.blk.sdk.UI;
import com.blk.sdk.Utility;
import com.blk.sdk.activity.BaseActivity;

import java.net.SocketTimeoutException;

public class Menu {
    public static void MainMenu(BaseActivity activity)
    {

        try {
            //if(firstStart){
//                        while (TranProc.TransMenu(true) != -6) ;
//                                    firstStart=false;
            //  }
            Response res;
                    switch (UI.ShowList("FATURA VİZYON",
                    new String[] {
                            "Fatura Ödeme",
                            "Fatura Sorgulama",
                            "ParameterDownload",
                            "satış"
                    })) {
                        case 3:
                        {
                            final boolean[] fWait = {true};
                            final String[] tranData = new String[1];
                            Intent intent = new Intent();
                            intent.setAction("com.blk.techpos.USER_ACTION");
                            intent.putExtra("TranType", 1);
                            intent.putExtra("Amount", "1");
                            activity.sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    int code = getResultCode();
                                    String data = getResultData();
                                    Bundle b = getResultExtras(true);
                                    String baslik = b.getString("TranData");

                                    Log.i("", "Code:" + code + " Data :" + data + " Bundle: " + baslik);
                                    tranData[0] = baslik;
                                    fWait[0] = false;
                                }
                            }, null,
                                    Activity.RESULT_OK,
                                    null,
                                    null);

                            while (fWait[0])
                                Utility.sleep(10);
                            Log.i("TAG", "onReceive: " + tranData[0]);
                        }
                        break;
                case 0:
                {
                    if (!params.fDownloaded) {
                        UI.ShowMessage("Parametre Yükleyiniz");
                        break;
                    }
                    Institution institution = UI.ShowList("Kurum Şeciniz", params.institutionList, item -> item.name);
                    if (institution == null) break;

                    _BillInquiry billInquiry = new _BillInquiry();
                    billInquiry.institutionCode = institution.code;

                    for (String param : institution.fieldNames) {
                        String number = UI.GetNumber(param);
                        if (number == null) break;
                        billInquiry.parameters.add(number);
                    }
                    if (billInquiry.parameters.size() != institution.fieldCount)
                        break;

                    res = FaturaVizyon.BillInquiry(billInquiry);

                    BillInfo.BILLS bills = new BillInfo.BILLS();
                    BillInfo bill = UI.ShowList("Fatura Şeciniz", res.billList, item -> item.billId);
                    if (bill == null) break;

                    bills.Bills.add(bill);
                    res = FaturaVizyon.BillPayment(bills);

                    UI.ShowMessage("İşlem Tamamlandı.");
                }
                    break;
                case 1:
                {
                    if (!params.fDownloaded) {
                        UI.ShowMessage("Parametre Yükleyiniz");
                        break;
                    }

                    Institution institution = UI.ShowList("Kurum Şeciniz", params.institutionList, item -> item.name);
                    if (institution == null) break;

                    _BillInquiry billInquiry = new _BillInquiry();
                    billInquiry.institutionCode = institution.code;

                    for (String param : institution.fieldNames) {
                        String number = UI.GetNumber(param);
                        if (number == null) break;
                        billInquiry.parameters.add(number);
                    }
                    if (billInquiry.parameters.size() != institution.fieldCount)
                        break;

                    res = FaturaVizyon.BillInquiry(billInquiry);

                    BillInfo.BILLS bills = new BillInfo.BILLS();
                    BillInfo bill = UI.ShowList("Fatura Şeciniz", res.billList, item -> item.billId);
                    if (bill == null) break;
                    UI.ShowMessage(15000, bill.nameSurname + "\n\n" + "TUTAR" + "\n\n" + bill.amount);
                }
                    break;
                case 2:
                    FaturaVizyon.ParameterDownload();
                    break;
                default:
                    //MainActivity.activity.runOnUiThread(() -> MainActivity.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
                    break;
            }

        }
        catch (SocketTimeoutException e)
        {
            e.printStackTrace();
            UI.ShowMessage(30000, "Bağlantı hatası\n\n" + e);
        }
        catch (Exception e) {
            e.printStackTrace();

            UI.ShowMessage(30000, e.getMessage());
        }
    }
}
