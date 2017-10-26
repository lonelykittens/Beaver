package android.dykim.com.beaver.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.dykim.com.beaver.alarm.AlarmMsg;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBController extends SQLiteOpenHelper {
    final String TABLE_NAME = "ALARM_LIST";

    public DBController(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        //관리할 DB 이름과 버전 정보를 받음
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+" (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, del_yn TEXT, reg_date TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //업그레이드
    }

    public int insertAlarm(String title, String content, String reg_date) {
        SQLiteDatabase db = getWritableDatabase("test1234");
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("del_yn", "N");
        values.put("reg_date", reg_date);
        long rowId = db.insert(TABLE_NAME, null, values);
        db.close();
        return (int)rowId;
    }

    public void updateDelAlarm(int _id) {
        SQLiteDatabase db = getWritableDatabase("test1234");
        db.execSQL("UPDATE "+TABLE_NAME+" SET DEL_YN='Y' WHERE _id = '"+_id+"';");
        db.close();
    }
/*
    public void delete(String item) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE _id = '';");
        db.close();
    }
*/
    public List<AlarmMsg> getAlarmList() {
        SQLiteDatabase db = getReadableDatabase("test1234");
        String result = "";
        List<AlarmMsg> alarmList = new ArrayList<AlarmMsg>();
        AlarmMsg alarmMsg = new AlarmMsg();

        Cursor cursor = db.rawQuery("SELECT title, content, reg_date, _id FROM "+TABLE_NAME + " WHERE del_yn = 'N' ORDER BY _id desc ", null);
        while (cursor.moveToNext()) {
            alarmMsg = new AlarmMsg();
            alarmMsg.setTitle(cursor.getString(0));
            alarmMsg.setContent(cursor.getString(1));
            alarmMsg.setDate(cursor.getString(2));
            alarmMsg.setRownum(cursor.getInt(3));
            alarmList.add(alarmMsg);
        }

        return alarmList;
    }

    public AlarmMsg getAlarm(int _Id) {
        SQLiteDatabase db = getReadableDatabase("test1234");
        String result = "";
        AlarmMsg alarmMsg = new AlarmMsg();

        Cursor cursor = db.rawQuery("SELECT title, content, reg_date, _id FROM "+TABLE_NAME+" WHERE _id =" + _Id + " AND DEL_YN = 'N' ", null);
        if(cursor.moveToNext()) {
            alarmMsg.setTitle(cursor.getString(0));
            alarmMsg.setContent(cursor.getString(1));
            alarmMsg.setDate(cursor.getString(2));
            alarmMsg.setRownum(cursor.getInt(3));
        }

        return alarmMsg;
    }
}
