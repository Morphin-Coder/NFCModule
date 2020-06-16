package kr.ac.uck.nfcmodule;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;

public interface RequestService {
    @PATCH("/QR/shuttle/")
    Call<CheckMessage> shuttleBus(@Body HashMap<String,Object> map);
}
