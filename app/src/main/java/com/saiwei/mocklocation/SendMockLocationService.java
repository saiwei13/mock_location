/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.saiwei.mocklocation;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


/**
 * A Service that injects test Location objects into the Location Services back-end. All other
 * apps that are connected to Location Services will see the test location values instead of
 * real values, until the test is over.
 *
 * To use this service, define the mock location values you want to use in the class
 * MockLocationConstants.java, then call this Service with startService().
 */
public class SendMockLocationService extends Service {
    
    private static final String TAG = "chenwei.SendMockLocationService";

    // A background thread for the work tasks
    HandlerThread mWorkThread;

    // Stores an instance of the object that dispatches work requests to the worker thread
    private Looper mUpdateLooper;

    // An array of test location data
    private TestLocation[] mLocationArray;
    
 // The Handler instance that does the actual work
    private UpdateHandler mUpdateHandler;
    
    /**
     * 速度的倍数
     */
    private float speed_mul = 1;
    
    private final  float MIN_SPEED = 2.5f;
    
    /**
     * 是否采用真实的行驶速度
     */
    private boolean isRealSpeed = true ;

    /*
     * At startup, load the static mock location data from MockLocationConstants.java, then
     * create a HandlerThread to inject the locations and start it.
     */
    @Override
      public void onCreate() {
        
        Log.i(TAG, "onCreate()");
        
        /*
         * Load the mock location data from MockLocationConstants.java
         */
/*        mLocationArray = buildTestLocationArray(LocationUtils.WAYPOINTS_LAT,
                LocationUtils.WAYPOINTS_LNG, LocationUtils.WAYPOINTS_ACCURACY);*/
        
        
        
        
        /*
         * Create a new background thread with an associated Looper that processes Message objects
         * from a MessageQueue. The Looper allows test Activities to send repeated requests to
         * inject mock locations from this Service.
         */
        mWorkThread = new HandlerThread("UpdateThread", Process.THREAD_PRIORITY_BACKGROUND);

        /*
         * Start the thread. Nothing actually runs until the Looper for this thread dispatches a
         * Message to the Handler.
         */
        mWorkThread.start();

        // Get the Looper for the thread
        mUpdateLooper = mWorkThread.getLooper();

        /*
         * Create a Handler object and pass in the Looper for the thread.
         * The Looper can now dispatch Message objects to the Handler's handleMessage() method.
         */
        mUpdateHandler = new UpdateHandler(mUpdateLooper);
        
      }

    /*
     * Since onBind is a static method, any subclass of Service must override it.
     * However, since this Service is not designed to be a bound Service, it returns null.
     */
    @Override
    public IBinder onBind(Intent inputIntent) {
        return new MockLocationBinder();
    }
    
    public class MockLocationBinder extends Binder{
        
        public SendMockLocationService getService(){
            return SendMockLocationService.this;
        }
    }
    
    private Handler mMainHandler ; 
    
    public void setHandle(Handler handler){
        Log.i(TAG, "setHandle()");
        mMainHandler = handler;
    }

    Bundle bundle;
    
    /**
     *  是否循环模拟
     */
    private boolean isContinusMock = false ;
    
    /*
     * Respond to an Intent sent by startService. onCreate() is called before this method,
     * to take care of initialization.
     *
     * This method responds to requests from the main activity to start testing.
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {

    	if(startIntent == null) return Service.START_STICKY;
    	
        String action  = startIntent.getAction();
        
        Log.i(TAG, "onStartCommand()  action ="+action);
        
        if(!TextUtils.isEmpty(action)){
            if(action.equals(LocationUtils.ACTION_START_ONCE)){
            	
                isStop =false;
                
                bundle = startIntent.getExtras();
//                String filename = bundle.getString("file_name");
                String[] filenames = bundle.getStringArray("file_names");
                
                mUpdateLooper = mWorkThread.getLooper();
                mUpdateHandler = new UpdateHandler(mUpdateLooper);
                // Get a message object from the global pool
                Message msg = mUpdateHandler.obtainMessage();
                msg.obj = filenames;
                // Fire off the injection loop
                mUpdateHandler.sendMessage(msg);
                
//                mockGpsLocation(filename);
                
            } else if(action.equals(LocationUtils.ACTION_STOP_TEST)){

                Toast.makeText(this, "停止模拟Gps", Toast.LENGTH_SHORT).show();
                
//                if(lm != null){
//                    lm.removeTestProvider(providerName);
//                }
                
                removeNewNotification();
                isStop = true;
//                stopSelf();
            }
        }
        return Service.START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    
//        removeNewNotification();
    }
    
    private LocationManager lm;
    private String providerName = LocationManager.GPS_PROVIDER;//LocationManager.NETWORK_PROVIDER;

    private int count = 0;
    private Message msg_tmp;
    
    private void mockGpsLocation(String filename){
    
//        Log.i(TAG, "mockLocation()  filename="+filename);
        
        lm  = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

              try {
                lm.addTestProvider(providerName, false, false, false, false, false, 
                    true, true, 0, 5);
            } catch (SecurityException e1) {
                Toast.makeText(this, ""+e1.toString(), Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
                
                if(mMainHandler != null){
                    mMainHandler.obtainMessage(LocationUtils.MSG_SecurityException).sendToTarget();
                }
                return;
            }
              
              lm.setTestProviderEnabled(providerName, true);

              /**
               * 读取gps源文件
               */
              mLocationArray = buildTestLocationArray(filename);
              
              if(mLocationArray != null){
                  if(mLocationArray.length > 2){
                      //起点
                      Message msg = mMainHandler.obtainMessage();
                      msg.what = LocationUtils.MSG_START_POINT;
                      msg.obj = mLocationArray[0];
                      mMainHandler.sendMessage(msg);
                      
                      //终点
                      msg = mMainHandler.obtainMessage();
                      msg.what = LocationUtils.MSG_STOP_POINT;
                      msg.obj = mLocationArray[mLocationArray.length-1];
                      mMainHandler.sendMessage(msg);
                  }
              } else {
                  return ;
              }
              
              showNewNotification();
              
              Location mockLocation = new Location(providerName);
              
/*              mockLocation.setTime(System.currentTimeMillis());
              mockLocation.setLatitude(24.43557285);
              mockLocation.setLongitude(118.0974833); 
              mockLocation.setAltitude(0); 
              mockLocation.setAccuracy(Criteria.ACCURACY_MEDIUM);
              mockLocation.setTime(System.currentTimeMillis()); 
              mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
              lm.setTestProviderLocation(providerName, mockLocation);*/
              
//        try {
//            Thread.sleep((long) (3 * 1000));
//        } catch (InterruptedException e) {
//            return;
//        }
            
            float timediff = 1;
            BigDecimal mult_1000 = new BigDecimal(1000);

            
            count = 0;
            
            for (int index = 0; index < mLocationArray.length; index++) {
//                
                count++;
                
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                    mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }

                if(!isRealSpeed){
                    if(mLocationArray[index].speed < MIN_SPEED){
                        Log.i(TAG, "--continue--");
                        continue;
                    }
                }
                
//                mockLocation.setTime(System.currentTimeMillis());
                mockLocation.setTime(mLocationArray[index].time.multiply(mult_1000).longValue());

                // Set the location accuracy, latitude, and longitude
                mockLocation.setAccuracy(mLocationArray[index].Accuracy);
                mockLocation.setLatitude(mLocationArray[index].Latitude);
                mockLocation.setLongitude(mLocationArray[index].Longitude);
                mockLocation.setBearing(mLocationArray[index].bearing);
                mockLocation.setSpeed(mLocationArray[index].speed);
                
                lm.setTestProviderLocation(providerName, mockLocation);

                if(speed_mul<0){
                    speed_mul= 1;
                }
                
                try {
                    timediff = mLocationArray[index+1].time.subtract(mLocationArray[index].time).floatValue();
                    timediff = timediff/speed_mul;
                } catch (Exception e) {
                    
                    Log.i(TAG, "Exception  e="+e.toString());
                    timediff = 1;
                }
                
                Log.i(TAG, "lat="+mockLocation.getLatitude()+" , lon="+mockLocation.getLongitude()+", bearing="+mockLocation.getBearing()+" , speed="+mockLocation.getSpeed()+" , timediff="+timediff);
                
                // 跟ui界面交互
//                msg_tmp = mMainHandler.obtainMessage();
//                msg_tmp.what = LocationUtils.MSG_SHOW_LOG;
//                msg_tmp.obj = "lat="+mockLocation.getLatitude()+" , lon="+mockLocation.getLongitude()+", bearing="+mockLocation.getBearing()+" , speed="+mockLocation.getSpeed()+" , timediff="+timediff;
//                mMainHandler.sendMessage(msg_tmp);
                
                
//                /**
//                 * wei.chen
//                 * 2014.8.20
//                 * 刷新太快的话，低端机会反映不过来，会卡住
//                 */
//                timediff = 1.5f;
                
                // Wait for the requested update interval, by putting the thread to sleep
                try {
                    Thread.sleep((long) (timediff * 1000));
                } catch (InterruptedException e) {
                    Log.i(TAG, "mockLocation()   InterruptedException  "+e.toString());
                    return ;
                }
                
                updateNewNotification(count+" / "+mLocationArray.length+" , 时间="+getStandardFormatTime(mLocationArray[index].time.multiply(mult_1000).longValue())+"\n"
                        +"lat="+mockLocation.getLatitude()+" , lon="+mockLocation.getLongitude()+", bearing="+mockLocation.getBearing()+" , speed="+mockLocation.getSpeed()+" , timediff="+timediff);
                
                if(isStop){
                    
                    Log.i(TAG, "isStop="+isStop);
                    
                    break;
                }
            }
            
            Log.i(TAG, "mockLocation()  end");
    }
    
    
    private static String format = "yyyy-MM-dd HH:mm:ss";
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
     * 更新 notification 录像时间
     * @param str
     */
    private void updateNewNotification(String str){

        if(mNotificationManager != null && mBuilder != null){
            if(mRemoteViews != null){
                mRemoteViews.setTextViewText(R.id.notification_update_time,str );
            }
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }
    
    private boolean isStop = false;
    
    private TestLocation[] buildTestLocationArray(String filename) {

            ArrayList<TestLocation> locations = new ArrayList<TestLocation>();
//            File from_file = new File("/sdcard/20140624_151140.gps"); //TODO
//            File from_file = new File("/sdcard/autonavidata70/trace/gps_record/20140813_143704.gps"); 
//            File from_file = new File("/sdcard/autonavidata70/trace/gps_record/20140812_205738.gps"); 
//            File from_file = new File("/sdcard/external_sd/autonavidata70/trace/gps_record/20140812_202238.gps"); 
            
            File from_file = new File(LocationUtils.FILE_DIR_PATH+filename);
            
            if(!from_file.exists()){
                Toast.makeText(this, "文件不存在",Toast.LENGTH_SHORT).show();
                return null;
            }
            
            BufferedReader br = null;
            
            try {
                br = new BufferedReader(new FileReader(from_file));
                
                String line = null;
                String[] strs ;
                TestLocation mCurGpsInfo ;
                
                int count = 0;
                while ((line = br.readLine()) != null) {

                    if(line.length()>20){
                           strs = line.split(",");
                           
                           mCurGpsInfo = new TestLocation(
                                   count+"",
                                   Double.valueOf(strs[1]),
                                   Double.valueOf(strs[0]),
                                   Float.valueOf(strs[2]),
                                   Float.valueOf(strs[3]),
                                   Float.valueOf(strs[4]),
                                   BigDecimal.valueOf((Double.valueOf(strs[5]))));
                           
                           count++;
                           locations.add(mCurGpsInfo);
                    }  
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Toast.makeText(this, "locations.size() = "+locations.size(), Toast.LENGTH_SHORT).show();
            Log.i(TAG, " buildTestLocationArray()  locations.size() = "+locations.size());
            //将集合转成数组
            TestLocation[] location_array  = null;
            if(locations != null){
                location_array = locations.toArray(new TestLocation[locations.size()]);
            }
            // Return the temporary array
            return location_array;
        }
    
    public class UpdateHandler extends Handler {

        public UpdateHandler(Looper inputLooper) {
            super(inputLooper);
        }

        @Override
        public void handleMessage(Message msg) {
        	
        	String[] str =     (String[]) msg.obj;    	
        	do{
        		
        		for(int i=0;i<str.length;i++){
        			mockGpsLocation(str[i]);
        		}
        		
        		
        	}while(isContinusMock);
        }
    }
    
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteViews;
    private static final int NOTIFICATION_ID = 1234;
    /**
     * 点击，停止后台录喜
     */
    private PendingIntent mPendingIntentStopRecord;
    
    /**
     * 点击，跳转到录像界面
     */
    private PendingIntent mPendingIntentDisplayRecorder;
    
    /**
     * 当前是否有显示 notification 信息 
     */
    private boolean isShowNotification = false;
    
    /**
     * 显示新版的notification
     */
    private void showNewNotification(){
        
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent intent_1 = new Intent(this,SendMockLocationService.class);
        intent_1.setAction(LocationUtils.ACTION_STOP_TEST);
        mPendingIntentStopRecord = PendingIntent.getService(
                this,
                0, 
//                new Intent(Recorder_Global.PLUGIN_ACTION_NAVIGATOR_RECORD_STOPSELF), 
                intent_1,
                0);

        
        Intent intent_2 = new Intent("com.saiwei.app.mockgps_main");
//        intent_2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        mPendingIntentDisplayRecorder =PendingIntent.getActivity(
                this, 
                0, 
                intent_2, 
                0);
        
        mRemoteViews = new RemoteViews(getPackageName(),
                R.layout.notification_view);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_stop, mPendingIntentStopRecord);
        
        mRemoteViews.setOnClickPendingIntent(R.id.notification_message_layout, mPendingIntentDisplayRecorder);
        
        mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Background Video Recorder")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_launcher)
                .setOnlyAlertOnce(true)
                .setContent(mRemoteViews)
                .setOngoing(true)
                ;
        
//      startForeground(NOTIFICATION_ID, mBuilder.build());
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        
        isShowNotification = true;
//      mBuilder =
//              new NotificationCompat.Builder(this)
//              .setSmallIcon(R.drawable.notification_icon)
//              .setContentTitle("My notification")
//              .setContentText("Hello World!");
//      
//      mNotificationManager =
//              (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//      // mId allows you to update the notification later on.
//      mNotificationManager.notify(1333, mBuilder.build());
    }
    
    /**
     * 移除 notification
     */
    private void removeNewNotification(){
        if(mNotificationManager!=null){
//          mBuilder.setOngoing(false);
            mNotificationManager.cancel(NOTIFICATION_ID);
            isShowNotification = false;
        }
    }

    public void setSpeed_mul(float tmp) {
        speed_mul = tmp;
    }

    public boolean isRealSpeed() {
        return isRealSpeed;
    }

    public void setRealSpeed(boolean isRealSpeed) {
        this.isRealSpeed = isRealSpeed;
    }

	public boolean isContinusMock() {
		return isContinusMock;
	}

	public void setContinusMock(boolean isContinusMock) {
		this.isContinusMock = isContinusMock;
	}
}
