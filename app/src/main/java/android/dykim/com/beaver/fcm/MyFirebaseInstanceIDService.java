
/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.dykim.com.beaver.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    //토큰 자동 새로고침
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    //토큰 서버로 보내기
    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "sendRegistrationToServer");
        //1.SharedPreferences에 토큰 저장
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token);
        editor.commit();

        //2. 핸드폰 정보 구하기
        String imei = "";
        String phoneNumber = "";
        try {
            TelephonyManager telephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyMgr.getDeviceId();
            phoneNumber = telephonyMgr.getLine1Number();
        }catch(Exception e){
            Log.e(TAG, "error get telephony service");
        }
        String modelNm = Build.MODEL;
        String manufactor = Build.MANUFACTURER;


        //2.토큰 서버에 전송
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .add("modelNm", modelNm)
                .add("manufacturer", manufactor)
                .add("IMEI", imei)
                .add("typeNm", "android")
                .build();
        Request request = new Request.Builder()
                .url("https://script.google.com/macros/s/AKfycbyyxqzmQ5RV1Vvo1GaTCt4OSXA_9N_wI1zwnZlBRShti5w41FVQ/exec")
                .post(body)
                .build();
        try {
            Response res = client.newCall(request).execute();
            ResponseBody resBody = res.body();
            resBody.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUserData(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String token = pref.getString("token", "");

        //2. 핸드폰 정보 구하기
        String imei = "";
        String phoneNumber = "";
        try {
            TelephonyManager telephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyMgr.getDeviceId();
            phoneNumber = telephonyMgr.getLine1Number();
        }catch(Exception e){
            Log.e(TAG, "error get telephony service");
        }
        String modelNm = Build.MODEL;
        String manufactor = Build.MANUFACTURER;


        //2.토큰 서버에 전송
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .add("modelNm", modelNm)
                .add("manufacturer", manufactor)
                .add("IMEI", imei)
                .add("typeNm", "android")
                .build();
        Request request = new Request.Builder()
                .url("https://script.google.com/macros/s/AKfycbyyxqzmQ5RV1Vvo1GaTCt4OSXA_9N_wI1zwnZlBRShti5w41FVQ/exec")
                .post(body)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
