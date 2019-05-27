package kr.co.jmsmart.bingo.data;

import java.io.Serializable;

/**
 * Created by Administrator on 2019-01-21.
 */

public class Command implements Serializable{
    private String mac;
    private String userId;
    private String petSrn;
    private String petNm;

    public Command(String mac, String userId, String petSrn, String petNm) {
        this.mac = mac;
        this.userId = userId;
        this.petSrn = petSrn;
        this.petNm = petNm;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPetSrn() {
        return petSrn;
    }

    public void setPetSrn(String petSrn) {
        this.petSrn = petSrn;
    }

    public String getPetNm() {
        return petNm;
    }

    public void setPetNm(String petNm) {
        this.petNm = petNm;
    }
}
