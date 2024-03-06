package com.blk.fatura_vizyon;

import static com.blk.fatura_vizyon.model.Parameters.params;

import android.os.Build;

import com.blk.fatura_vizyon.model.BillInfo;
import com.blk.fatura_vizyon.model._BillInquiry;
import com.blk.fatura_vizyon.model.Parameters;
import com.blk.fatura_vizyon.model.Response;
import com.blk.fatura_vizyon.model.Institution;
import com.blk.platform_castle.PlatfromCtos;
import com.blk.sdk.UI;
import com.blk.sdk.Utility;
import com.blk.sdk.com.TcpClient;
import com.blk.sdk.com.xmlswitchserver.BlkXmlMessage;
import com.blk.sdk.com.xmlswitchserver.Message;
import com.blk.sdk.com.xmlswitchserver.SwitchClient;
import com.blk.sdk.emulator.Emulator;
import com.blk.sdk.olib.olib;

import org.json.JSONArray;
import org.json.JSONObject;

public class FaturaVizyon {

    public static boolean isInited = false;
    public static void Init() throws Exception {
        if (isInited) return;

        isInited = true;

        if (Build.DEVICE.startsWith("generic"))
            new Emulator();
        else
            new PlatfromCtos();

        Utility.Init("fatura_vizyon");
        Parameters.Read();
        SwitchClient.Configure(new TcpClient.Config(Emulator.localHostPcIp, 5150, 10, 30));

        UI.UiUtil.ShowMessageHide();
    }

    public static void ParameterDownload() throws Exception {
        BlkXmlMessage blkXmlMessage = new BlkXmlMessage("ParameterDownload", params.MerchantId, params.TerminalId);

        UI.ShowMessage(0, "Lütfen Bekleyiniz");
        String sResponse = SwitchClient.SendReceive(blkXmlMessage.GetMessage());

        Message message = new Message();
        olib.DeserializeXml(message, sResponse);

        JSONObject jResponse = new JSONObject(message.body.appData);
        if (!jResponse.getBoolean("isSuccess"))
            throw  new Exception(jResponse.getString("description"));

        // {"isSuccess":true,"description":null,"data":[{"code":6001,"name":"Digiturk","fieldCount":1,"fieldNames":["Abone No"]},{"code":3012,"name":"İGDAŞ","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":1000,"name":"İski","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":1072,"name":"Ordu Su","fieldCount":1,"fieldNames":["Abone No"]},{"code":2023,"name":"Sepaş Elektrik ","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":2100,"name":"Tedaş","fieldCount":2,"fieldNames":["Tesisat No","Abone No"]},{"code":4000,"name":"Türk Telekom","fieldCount":1,"fieldNames":["Telefon No"]},{"code":5011,"name":"Türk Telekom İnternet (TTNet)","fieldCount":1,"fieldNames":["Hizmet No"]},{"code":4008,"name":"Türk Telekom Mobil (Avea)","fieldCount":1,"fieldNames":["Telefon No"]},{"code":4002,"name":"Vodafone","fieldCount":1,"fieldNames":["Telefon No"]},{"code":5013,"name":"Vodafone Net","fieldCount":1,"fieldNames":["Kullanıcı Kodu"]}]}
        Response response = new Response();
        JSONArray institutionlist = jResponse.getJSONArray("data");

        for (int i = 0; i < institutionlist.length(); ++i) {
            Institution institution = new Institution();
            olib.DeserializeJson(institution, institutionlist.getJSONObject(i));
            response.institutionlist.add(institution);
        }

        params.institutionList = response.institutionlist;
        params.fDownloaded = true;
        Parameters.Write();
        UI.ShowMessage(5000, "Parametre Yüklendi.\n\nBakiye : " + message.body.remainder);

    }

    public static Response BillInquiry(_BillInquiry billInfo) throws Exception {

        UI.ShowMessage(0, "Lütfen Bekleyiniz");

        BlkXmlMessage blkXmlMessage = new BlkXmlMessage("_BillInquiry", params.MerchantId, params.TerminalId);
        blkXmlMessage.message.body.appData = olib.SerializeJson(billInfo);

        String sResponse = SwitchClient.SendReceive(blkXmlMessage.GetMessage());

        Message message = new Message();
        olib.DeserializeXml(message, sResponse);

        JSONObject jResponse = new JSONObject(message.body.appData);
        if (!jResponse.getBoolean("isSuccess"))
            throw  new Exception(jResponse.getString("description"));

        // {"isSuccess":true,"description":null,"data":[{"code":6001,"name":"Digiturk","fieldCount":1,"fieldNames":["Abone No"]},{"code":3012,"name":"İGDAŞ","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":1000,"name":"İski","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":1072,"name":"Ordu Su","fieldCount":1,"fieldNames":["Abone No"]},{"code":2023,"name":"Sepaş Elektrik ","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":2100,"name":"Tedaş","fieldCount":2,"fieldNames":["Tesisat No","Abone No"]},{"code":4000,"name":"Türk Telekom","fieldCount":1,"fieldNames":["Telefon No"]},{"code":5011,"name":"Türk Telekom İnternet (TTNet)","fieldCount":1,"fieldNames":["Hizmet No"]},{"code":4008,"name":"Türk Telekom Mobil (Avea)","fieldCount":1,"fieldNames":["Telefon No"]},{"code":4002,"name":"Vodafone","fieldCount":1,"fieldNames":["Telefon No"]},{"code":5013,"name":"Vodafone Net","fieldCount":1,"fieldNames":["Kullanıcı Kodu"]}]}
        Response response = new Response();
        JSONArray jBillList = jResponse.getJSONArray("data");

        for (int i = 0; i < jBillList.length(); ++i) {
            BillInfo bill = new BillInfo();
            olib.DeserializeJson(bill, jBillList.getJSONObject(i));
            response.billList.add(bill);
        }

        return response;
    }

    public static Response BillPayment(BillInfo.BILLS bills) throws Exception {

        UI.ShowMessage(0, "Lütfen Bekleyiniz");

        BlkXmlMessage blkXmlMessage = new BlkXmlMessage("BillPayment", params.MerchantId, params.TerminalId);
        blkXmlMessage.message.body.appData = olib.SerializeJson(bills);

        String sResponse = SwitchClient.SendReceive(blkXmlMessage.GetMessage());

        Message message = new Message();
        olib.DeserializeXml(message, sResponse);

        JSONObject jResponse = new JSONObject(message.body.appData);
        if (!jResponse.getBoolean("isSuccess"))
            throw  new Exception(jResponse.getString("description"));

        // {"isSuccess":true,"description":null,"data":[{"code":6001,"name":"Digiturk","fieldCount":1,"fieldNames":["Abone No"]},{"code":3012,"name":"İGDAŞ","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":1000,"name":"İski","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":1072,"name":"Ordu Su","fieldCount":1,"fieldNames":["Abone No"]},{"code":2023,"name":"Sepaş Elektrik ","fieldCount":1,"fieldNames":["Sözleşme No"]},{"code":2100,"name":"Tedaş","fieldCount":2,"fieldNames":["Tesisat No","Abone No"]},{"code":4000,"name":"Türk Telekom","fieldCount":1,"fieldNames":["Telefon No"]},{"code":5011,"name":"Türk Telekom İnternet (TTNet)","fieldCount":1,"fieldNames":["Hizmet No"]},{"code":4008,"name":"Türk Telekom Mobil (Avea)","fieldCount":1,"fieldNames":["Telefon No"]},{"code":4002,"name":"Vodafone","fieldCount":1,"fieldNames":["Telefon No"]},{"code":5013,"name":"Vodafone Net","fieldCount":1,"fieldNames":["Kullanıcı Kodu"]}]}
        Response response = new Response();
        JSONArray jBillList = jResponse.getJSONArray("data");

        for (int i = 0; i < jBillList.length(); ++i) {
            BillInfo bill = new BillInfo();
            olib.DeserializeJson(bill, jBillList.getJSONObject(i));
            response.billList.add(bill);
        }

        return response;
    }
}
