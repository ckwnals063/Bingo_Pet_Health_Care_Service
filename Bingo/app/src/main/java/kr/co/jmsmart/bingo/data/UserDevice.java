package kr.co.jmsmart.bingo.data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Administrator on 2019-01-17.
 */

public class UserDevice implements Serializable {
    private String petSrn;
    private String petNm;
    private String mac;
    public UserDevice(Map<String, String> map){
        this.petSrn = map.get("petSrn");
        this.petNm = map.get("petNm");
        this.mac = map.get("macAddr");
    }

    public String getPetSrn() {
        return petSrn;
    }

    public String getPetNm() {
        return petNm;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac){ this.mac = mac; }
}
