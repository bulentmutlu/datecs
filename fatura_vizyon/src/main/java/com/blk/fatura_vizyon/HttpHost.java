package com.blk.fatura_vizyon;

import android.util.Base64;

import com.blk.fatura_vizyon.model.Response;
import com.blk.fatura_vizyon.model.BillInfo;
import com.blk.fatura_vizyon.model._BillInquiry;
import com.blk.fatura_vizyon.model.Institution;
import com.blk.sdk.Convert;
import com.blk.sdk.com.HttpClient;
import com.blk.sdk.olib.olib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HttpHost {

    final String baseUrl = "https://sandboxservices.faturavizyon.com.tr";

    private static final String TAG = HttpHost.class.getSimpleName();

    public String token;

    public HttpHost()
    {
        try {
            token = getToken().token;
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    JSONObject get(String url) throws Exception {

        HttpClient httpClient = new HttpClient();
        httpClient.client.setBearerAuth(token);
        httpClient.get(baseUrl + url);

        if (httpClient.StatusCode == 401) {
            token = getToken().token;

            httpClient.client.setBearerAuth(token);
            httpClient.get(baseUrl + url);
        }

        JSONObject jResponse = new JSONObject(httpClient.ResponseBody);

        if (!jResponse.getBoolean("isSuccess"))
            throw  new Exception(jResponse.getString("description"));

        return jResponse;
    }

    JSONObject post(String url, String jsonRequest) throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.client.setBearerAuth(token);
        httpClient.post(baseUrl + url, jsonRequest);
        if (httpClient.StatusCode == 401) {
            token = getToken().token;

            httpClient.client.setBearerAuth(token);
            httpClient.post(baseUrl + url, jsonRequest);
        }

        JSONObject jResponse = new JSONObject(httpClient.ResponseBody);

        if (!jResponse.getBoolean("isSuccess"))
            throw  new Exception(jResponse.getString("description"));

        return jResponse;
    }

    public static String hash(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] bytes = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        return  Base64.encodeToString(Convert.Buffer2Hex(bytes).toLowerCase().getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    Response getToken() throws Exception {

        String url = baseUrl + "/api/integration/gettoken";
        url += "?dealercode=8543440018&apikey=DJD0T6BGC44A3X1FXWF63L8HPZ4KJIFY";
        String ravdata = "1234567890123456" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String secretKey = "Q66KVKBBBCCKFF0FC4FXG9JDQV9X2N8P";
        url += "&rawdata=" + ravdata;
        String h = hash(secretKey, ravdata);
        //h = hash("40HXJW1PFNQQSU0VPMHXKLHS5O4IXQQI", "123456789012345620220222153333");
        url += "&hash=" + h;

        HttpClient httpClient = new HttpClient();
        httpClient.get(url);

        JSONObject jResponse = new JSONObject(httpClient.ResponseBody);

        if (!jResponse.getBoolean("isSuccess"))
            throw  new Exception(jResponse.getString("description"));

        Response response = new Response();
        response.token = jResponse.getJSONObject("data").getString("token");
//        response.Data.Expiration = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").
//                parse(jResponse.getJSONObject("data").getString("expiration")
//                        .replace('T', '_')
//                        .substring(0, 19)
//                );
        return response;
    }

    public Response GetRemainder() throws Exception {

        JSONObject jResponse = get("/api/integration/getremainder");

        Response response = new Response();
        //response.bakiye = jResponse.getString("data");
        return response;
    }
    public Response GetInstitutionList() throws Exception {

        JSONObject jResponse = get("/api/integration/getinstitutionlist");

        Response response = new Response();
        JSONArray institutionlist = jResponse.getJSONArray("data");

        for (int i = 0; i < institutionlist.length(); ++i) {
            Institution institution = new Institution();
            olib.DeserializeJson(institution, institutionlist.getJSONObject(i));
            response.institutionlist.add(institution);
        }
        return response;
    }
    public Response BillInquiry(_BillInquiry billInfo) throws Exception {
        JSONObject jResponse = post("/api/integration/billinquiry", olib.SerializeJson(billInfo));

        Response response = new Response();
        JSONArray jBillList = jResponse.getJSONArray("data");

        for (int i = 0; i < jBillList.length(); ++i) {
            BillInfo bill = new BillInfo();
            olib.DeserializeJson(bill, jBillList.getJSONObject(i));
            response.billList.add(bill);
        }
        return response;
    }
    public Response BillPayment(BillInfo.BILLS bills) throws Exception {

        JSONObject jResponse = post("/api/integration/billpayment", olib.SerializeJson(bills));

        Response response = new Response();
        JSONArray jBillList = jResponse.getJSONArray("data");

        for (int i = 0; i < jBillList.length(); ++i) {
            BillInfo billInfo = new BillInfo();
            olib.DeserializeJson(billInfo, jBillList.getJSONObject(i));
            response.billList.add(billInfo);
        }
        return response;
    }

    public static String test2() throws Exception {
        String s = "{\"code\":6001,\"name\":\"Digiturk\",\"fieldCount\":1,\"fieldNames\":[\"Abone No\"]}";
        JSONObject jo = new JSONObject();

//        Institution institution = new Institution();
//        olib.ParseJson(institution, new JSONObject(s));
//        olib.ToJson(institution, jo);
//        s = jo.toString();


        s = "{\n" +
                " \"id\": 132900761285989371,\n" +
                " \"institutionCode\": 4002,\n" +
                " \"nameSurname\": \"OS*** CE****\",\n" +
                " \"amount\": 35.894230769230769230769230769,\n" +
                " \"lastPaymentDate\": \"2022-02-04T10:48:48.598923+03:00\",\n" +
                " \"billId\": \"DXCJ0XCVO7\",\n" +
                " \"parameters\": [\n" +
                " \"5420000000\"\n" +
                " ],\n" +
                " \"statusCode\": 0,\n" +
                " \"isSuccess\": true,\n" +
                " \"description\": null\n" +
                " }";
        BillInfo billInfo = new BillInfo();
        olib.DeserializeJson(billInfo, new JSONObject(s));
        jo = new JSONObject();
        s = olib.SerializeJson(billInfo);

        return  s;
    }
}
