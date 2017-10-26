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

import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.dykim.com.beaver.MainActivity;
import android.dykim.com.beaver.R;
import android.dykim.com.beaver.alarm.AlarmMsg;
import android.dykim.com.beaver.alarm.ListViewAdapter;
import android.dykim.com.beaver.database.DBController;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private DBController dbconn = null;
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // 메시지 수신 함수
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getData() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getData().get("body"));

            AlarmMsg alarmMsg = new AlarmMsg();
            //1. 데이터 불러오기
           /*
            SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
            Gson gson = new Gson();
            String jsonStr = pref.getString("alarmList", "");
            int rowNum = pref.getInt("alarmRownum", 0);

            //2.메시징 클래스 초기화

            /*
            AlarmMsgList alarmMsgList = new AlarmMsgList();
            AlarmMsg[] tmpMsgList = null;
            List<AlarmMsg> getMsgList = new ArrayList<AlarmMsg>();

            int arrPos = 0;
            //기존 데이터 존재시 json 파싱처리하여 변수 할당
            if(jsonStr != ""){
                tmpMsgList = gson.fromJson(jsonStr, AlarmMsg[].class);
                for(int i=0; i < tmpMsgList.length; i++){
                    getMsgList.add(tmpMsgList[i]);
                }
            }
            */

            //알람시간 저장용
            long cutTime = System.currentTimeMillis();
            Date date = new Date( cutTime );
            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
            String dateStr = sdf.format( date );

            //메시징 할당
            alarmMsg.setTitle(remoteMessage.getData().get("title"));
            alarmMsg.setContent(remoteMessage.getData().get("body"));
            alarmMsg.setDate(dateStr);
            //alarmMsg.setRownum(rowNum++);
            //getMsgList.add(alarmMsg);

            //안드로이드 데이터 저장
            //sqlite로 변경
            dbconn = new DBController(getApplicationContext(), "AlarmList.db", null, 1);
            int rowId = dbconn.insertAlarm(alarmMsg.getTitle(), alarmMsg.getContent(), alarmMsg.getDate());
            /*
            SharedPreferences.Editor editor = pref.edit();
            String alarmJson = gson.toJson(getMsgList);
            Log.d(TAG, alarmJson);
            editor.putString("alarmList", alarmJson);
            editor.putInt("alarmRownum", rowNum);
            editor.commit();
            */

            //어댑터 추가
            ListViewAdapter adapter = new ListViewAdapter() ;
            String getContent = alarmMsg.getContent();
            getContent = getContent.replaceAll("<br/><br/>", "");
            getContent = getContent.replaceAll("<br/>", "\n");
            adapter.addItem(alarmMsg.getTitle(), getContent, alarmMsg.getDate(), rowId);

            //알림 호출
            sendNotification(alarmMsg.getTitle(), alarmMsg.getContent());

            //메인 Activity 호출
            //앱 실행시에만 호출
            if(MainActivity.AppRunnedChk) {
                Intent i = new Intent(this, MainActivity.class);
                PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);
                try {
                    p.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    private Object[] copyArray(Object[] oriArr){
        Object[] saveArr = new Object[oriArr.length + 1];
        for(int i=0; i < oriArr.length; i++){
            saveArr[i] = oriArr[i];
        }
        return saveArr;
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */

    private void scheduleJob() {
        Log.d(TAG, "scheduleJob" );
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("fcm-chk-job")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }
    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param title FCM message body received.
     * @param body FCM message body received.
     */
    private void sendNotification(String title, String body) {
        Log.d(TAG, "sendNotification" );
        Intent intent = new Intent(this, ListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        body = body.replaceAll("<br/><br/>", "");
        body = body.replaceAll("<br/>", " / ");

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 1000, 100, 1000, 100};

        //Big Message 설정
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setSummaryText("더보기..");
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(body);

        //알림 설정
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.app_icon)
                .setAutoCancel(true)
                .setStyle(bigTextStyle)
                .setSound(defaultSoundUri)
                .setVibrate(pattern)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
