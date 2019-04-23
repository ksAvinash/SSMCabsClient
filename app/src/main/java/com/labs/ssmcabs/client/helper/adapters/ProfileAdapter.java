package com.labs.ssmcabs.client.helper.adapters;

import java.util.Date;

public class ProfileAdapter {
    private String user_name, phoneno, stop_name, company_code;
    private Date updated_at;
    public ProfileAdapter(String user_name, String phoneno, String stop_name, String company_code) {
        this.user_name = user_name;
        this.phoneno = phoneno;
        this.stop_name = stop_name;
        this.company_code = company_code;
        updated_at = new Date();
    }

    public ProfileAdapter() {
    }

    public String getUser_name() {
        return user_name;
    }

    public String getPhoneno() {
        return phoneno;
    }

    public String getStop_name() {
        return stop_name;
    }

    public String getCompany_code() {
        return company_code;
    }
}
