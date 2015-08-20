package com.saiwei.mocklocation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 工具类
 * @author chenwei
 *
 */
public class Tool {
	
	private final static String TAG = "chenwei.Tool";
	
	private static String format = "yyyyMMddHHmmss";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
    
    /**
     * 将系统时间转化成标准格式的时间
     * ex: 1402381622815------->20140610142702
     * @param time
     * @return
     */
    public static String getStandardFormatTime(long _time){
        String time = simpleDateFormat.format(new Date(_time));
        return time   ;
    }
    
    /**
     * 将标准格式的时间转化成系统时间
     * ex: 20140610142702------>1402381622815
     * @param time
     * @return
     */
    public static long getSystemFormatTime(String time){
    	
//    	Log.i(TAG,"getSystemFormatTime()  time="+time);
    	
        long tmp_long_time = 0 ;
        try {
            tmp_long_time = simpleDateFormat.parse(time).getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tmp_long_time;
    }
}
