package kr.ac.uck.nfcmodule;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CheckMessage {
    @SerializedName("message")
    @Expose
    private String MESSSAGE;
    @SerializedName("Message")
    @Expose
    private String mESSSAGE;

    public CheckMessage(String MESSSAGE) {
        this.MESSSAGE = MESSSAGE;
    }


    public String getMESSSAGE() {
        return MESSSAGE;
    }

    public void setMESSSAGE(String MESSSAGE) {
        this.MESSSAGE = MESSSAGE;
    }
}
