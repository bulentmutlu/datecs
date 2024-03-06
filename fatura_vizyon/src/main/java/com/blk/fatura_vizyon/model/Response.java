package com.blk.fatura_vizyon.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Response {
    public String token;
    public Date expiration;
    public String bakiye;
    public List<Institution> institutionlist = new ArrayList<>();
    public List<BillInfo> billList = new ArrayList<>();
}
