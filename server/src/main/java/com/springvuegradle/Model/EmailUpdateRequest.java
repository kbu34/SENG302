package com.springvuegradle.Model;

import java.util.ArrayList;

/**
 * Model Class for incoming email/password requests used for logging in
 */
public class EmailUpdateRequest {

    private ArrayList<String> additional_email;
    private String primary_email;
    private int userId;

    public EmailUpdateRequest(ArrayList<String> additional_email, String primary_email, int userId){
        this.additional_email = additional_email;
        this.primary_email = primary_email;
        this.userId = userId;
    }

    public ArrayList<String> getAdditional_email() {
        return additional_email;
    }

    public void setAdditional_email(ArrayList<String> additional_email) {
        this.additional_email = additional_email;
    }

    public String getPrimary_email() {
        return primary_email;
    }

    public void setPrimary_email(String primary_email) {
        this.primary_email = primary_email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}
