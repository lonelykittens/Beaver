package android.dykim.com.beaver.alarm;

/**
 * Created by C-PC on 2017-10-12.
 */

public class AlarmMsg {
    private String title;
    private String content;
    private String date;
    private int rownum;

    public void setRownum(int rownum){
        this.rownum = rownum;
    }
    public int getRownum(){
        return this.rownum;
    }

    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return this.title;
    }
    public void setContent(String content){
        this.content = content;
    }
    public String getContent(){
        return this.content;
    }
    public void setDate(String date){
        this.date = date;
    }
    public String getDate(){
        return this.date;
    }
}
