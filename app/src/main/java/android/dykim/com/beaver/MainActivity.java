package android.dykim.com.beaver;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.dykim.com.beaver.alarm.AlarmMsg;
import android.dykim.com.beaver.alarm.ListViewAdapter;
import android.dykim.com.beaver.database.DBController;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private final String TAG = "MainActivity";
    private ActionMode mActionMode = null;
    private DBController dbconn = null;
    public static boolean AppRunnedChk = false;

    //기본 생성자 함수
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppRunnedChk = true;
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        //if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }else{
                loadFcmManager();
            }
       // }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    //초기화 함수
    public boolean loadFcmManager(){
        FirebaseMessaging.getInstance().subscribeToTopic("appMsg");
        String token = FirebaseInstanceId.getInstance().getToken();
        loadAlarmList();
        return true;
    }

    //알람 데이터 로드
    public void loadAlarmList(){
        if(dbconn == null){
            dbconn = new DBController(getApplicationContext(), "AlarmList.db", null, 1);
            SQLiteDatabase.loadLibs(this);
        }
        //메시징 클래스 초기화
        List<AlarmMsg> getMsgList = new ArrayList<AlarmMsg>();
        getMsgList = dbconn.getAlarmList();

        ListView listview = (ListView) findViewById(R.id.alarmList);
        ListViewAdapter adapter = new ListViewAdapter() ;
        listview.setAdapter(adapter);

        if(getMsgList.size() > 0) {
            for(int i=0; i < getMsgList.size(); i++) {
                String getContent = getMsgList.get(i).getContent();
                getContent = getContent.replaceAll("<br/><br/>", "");
                getContent = getContent.replaceAll("<br/>", "\n");
                adapter.addItem(getMsgList.get(i).getTitle(), getContent, getMsgList.get(i).getDate(), getMsgList.get(i).getRownum());
            }
        }
        registerForContextMenu(listview);
    }
    /*
    private void loadAlarmList(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonStr = pref.getString("alarmList", "");
        int rowNum = pref.getInt("alarmRownum", 0);

        Log.d(TAG, jsonStr);
        //메시징 클래스 초기화
        AlarmMsgList alarmMsgList = new AlarmMsgList();
        AlarmMsg[] getMsgList = null;
        //기존 데이터 존재시 json 파싱처리하여 변수 할당
        if(jsonStr != ""){
            getMsgList = gson.fromJson(jsonStr, AlarmMsg[].class);
        }
        Log.d(TAG, getMsgList.toString());
        ListView listview = (ListView) findViewById(R.id.alarmList);
        ListViewAdapter adapter = new ListViewAdapter() ;
        listview.setAdapter(adapter);

        if(getMsgList.length > 0) {
            for(int i=0; i < getMsgList.length; i++) {
                String getContent = getMsgList[i].getContent();
                getContent = getContent.replaceAll("<br/>", "\n");
                adapter.addItem(getMsgList[i].getTitle(), getContent, getMsgList[i].getDate(), getMsgList[i].getRownum());
            }
        }

        registerForContextMenu(listview);
    }
    */

    //알람 삭제
    private void delAlarmList(int rownum){
        dbconn.updateDelAlarm(rownum);
        loadAlarmList();
    }
    /*
    private void delAlarmList(int rownum){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonStr = pref.getString("alarmList", "");
        Log.d(TAG, jsonStr);
        //메시징 클래스 초기화
        AlarmMsgList alarmMsgList = new AlarmMsgList();
        AlarmMsg[] tmpMsgList = null;
        List<AlarmMsg> getMsgList = new ArrayList<AlarmMsg>();

        //기존 데이터 존재시 json 파싱처리하여 변수 할당
        if(jsonStr != ""){
            tmpMsgList = gson.fromJson(jsonStr, AlarmMsg[].class);
            Log.d(TAG, getMsgList.toString());
            for(int i=0; i < tmpMsgList.length; i++){
                Log.d(TAG, tmpMsgList[i].getRownum() + "-" + rownum);
                if(tmpMsgList[i].getRownum() != rownum) {
                    getMsgList.add(tmpMsgList[i]);
                }
            }
            SharedPreferences.Editor editor = pref.edit();
            String alarmJson = gson.toJson(getMsgList);
            editor.putString("alarmList", alarmJson);
            editor.commit();
            loadAlarmList();
        }
    }
    */
    //메뉴 호출
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        menu.setHeaderTitle("알람 처리방법을 선택하세요");
        inflater.inflate(R.menu.list_popup, menu);

    }

    //메뉴 선택
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        ListView listview = (ListView) findViewById(R.id.alarmList);
        AlarmMsg alarmMsg = (AlarmMsg)listview.getAdapter().getItem(index);
        int rownum = alarmMsg.getRownum();

        switch (item.getItemId()) {
            case R.id.delItem:
                delAlarmList(rownum);
                Toast.makeText(
                        MainActivity.this,
                        "삭제 되었습니다.",
                        Toast.LENGTH_SHORT
                ).show();
                return true;
            case R.id.copyItem:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(alarmMsg.getTitle(), alarmMsg.getContent());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(
                        MainActivity.this,
                        "복사 되었습니다.",
                        Toast.LENGTH_SHORT
                ).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    /*
    //옵션메뉴 생성 함수
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    //옵션메뉴 선택 함수
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    //화면 복귀 함수
    @Override
    protected void onResume() {
        super.onResume();
        AppRunnedChk = true;
        loadAlarmList();
    }

    @Override
    protected void onPause(){
        super.onPause();
        AppRunnedChk = false;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        AppRunnedChk = false;
    }

    //퍼미션 허용 결과 확인 함수
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadFcmManager();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_PHONE_STATE},
                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                }
                return;
        }
    }


}
