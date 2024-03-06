package com.blk.fatura_vizyon.model;

import java.util.ArrayList;
import java.util.List;

public class BillInfo {
    public long id;
    public int institutionCode;
    public String nameSurname;
    public double amount;
    public String lastPaymentDate;
    public String billId;
    public List<String> parameters = new ArrayList<>();
    public int statusCode;
    public boolean isSuccess;
    public String description;

    public static class BILLS {
        public List<BillInfo> Bills = new ArrayList<>();
    }
}
