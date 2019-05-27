package kr.co.jmsmart.bingo.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by ZZQYU on 2019-01-29.
 */

public class User implements Serializable {
    public String phone, sex, birth, registerDttm, name, userId;

    public User(JSONObject data, String userId) {
        this.userId = userId;
        try {
            this.phone = ((String) data.get("userPhone"));
            this.sex = ((String)data.get("userSex"));
            this.birth = ((String)data.get("userBirth"));
            this.registerDttm = ((String)data.get("userRegisterDttm"));
            this.name = ((String)data.get("userNm"));
        }
        catch (JSONException e){
            e.printStackTrace();
            this.phone ="";
            this.sex ="";
            this.birth ="";
            this.registerDttm ="";
            this.name ="";
        }
    }

}
