package kr.ac.uck.nfcmodule;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import kr.ac.uck.nfcmodule.databinding.ActivityNfcBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NFCActivity extends AppCompatActivity {

    ActivityNfcBinding binding;
    int j;
    private String baseurl = "https://hub.hsu.ac.kr";
    private String[] NFCdata;
    private String sid = "20141683";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_nfc);

        Intent passedIntent = getIntent();
        if(passedIntent != null){
            onNewIntent(passedIntent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        String s = "";
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(data!=null){
            try{
                for(int i=0; i<data.length; i++){
                    NdefRecord[] recs = ((NdefMessage)data[i]).getRecords();
                    for(j=0; j<recs.length; j++){
                        if(recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN&& Arrays.equals(recs[j].getType(),NdefRecord.RTD_TEXT)){
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0]&0200) ==0)?"UTF-8":"UTF-16";
                            int langCodeLen = payload[0]&0077;
                            s+=(""+new String(payload,langCodeLen+1,payload.length-langCodeLen-1,textEncoding));
                        }
                    }
                }
            }catch(Exception e){
                Log.e("TagDispatch",e.toString());
            }
        }
        binding.text.setText(s);
        NFCdata=s.split("\n");
        NFCShuttleService(NFCdata);
//        for(int i=0; i< NFCdata.length; i++){
//            Log.d("SplitString",(i+1)+" : " + NFCdata[i]+"\n");
//        }
    }
    private void NFCShuttleService(String[] ReadData){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseurl)
                .client(SSLUtil.getUnsafeOkHttpClient().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        String url = letsEncrypt(sid);

        RequestService service = retrofit.create(RequestService.class);
        HashMap<String, Object> input = new HashMap<>();
        input.put("url", url);/*url*/
        input.put("state", ReadData[2]);
        input.put("shuttle_stop_name", ReadData[1]);

        service.shuttleBus(input).enqueue(new Callback<CheckMessage>() {
            @Override
            public void onResponse(Call<CheckMessage> call, Response<CheckMessage> response) {
                if (response.isSuccessful() && response.body().getMESSSAGE().equals("정상 탑승 되었습니다.")) { //==null
                    Log.d("TAGGING", response.toString() + " / " + response.body().getMESSSAGE());
                    Toast.makeText(getApplicationContext(), response.body().getMESSSAGE(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), response.body().getMESSSAGE(), Toast.LENGTH_SHORT).show();
                    Log.d("TAGGING", response.toString() + " / " + response.body().getMESSSAGE());
                }
            }

            @Override
            public void onFailure(Call<CheckMessage> call, Throwable t) {
                t.getStackTrace();
            }
        });
    }

    private String letsEncrypt(String studentId) {

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

        String nowDate = format.format(currentTime);
        String result = "";

        try {
            result = AES256Util.AES_Encode(studentId + nowDate);
            //Log.d("Encrypted Source: ", "20141683" + nowDate);
            //Log.d("Encrypted Value: ", result);

        } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }
}
