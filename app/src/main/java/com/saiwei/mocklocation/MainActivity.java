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

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * Interface to the SendMockLocationService that sends mock locations into Location Services.
 *
 * This Activity collects parameters from the UI, sends them to the Service, and receives back
 * status messages from the Service.
 * <p>
 * The following parameters are sent:
 * <ul>
 * <li><b>Type of test:</b> one-time cycle through the mock locations, or continuous sending</li>
 * <li><b>Pause interval:</b> Amount of time (in seconds) to wait before starting mock location
 * sending. This pause allows the tester to switch to the app under test before sending begins.
 * </li>
 * <li><b>Send interval:</b> Amount of time (in seconds) before sending a new location.
 * This time is unrelated to the update interval requested by the app under test. For example, the
 * app under test can request updates every second, and the tester can request a mock location
 * send every five seconds. In this case, the app under test will receive the same location 5
 * times before a new location becomes available.
 * </li>
 */
public class MainActivity extends FragmentActivity implements OnClickListener,OnCheckedChangeListener{

    private static final String TAG = "chenwei.MainActivity";
    
    private final int DLG_EXCEPION_ID = 1;

    private TestLocation tmp;
    
    public Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LocationUtils.MSG_SecurityException:
                    
                    showDialog(DLG_EXCEPION_ID);
                    
                    break;
                    
                case LocationUtils.MSG_START_POINT:
                    tmp = (TestLocation) msg.obj;
                    mTVStartPoint.setText("起点： "+tmp.Latitude+" , "+tmp.Longitude);
                    break;
                    
                case LocationUtils.MSG_STOP_POINT:
                    tmp = (TestLocation) msg.obj;
                    mTVStopPoint.setText("终点： "+tmp.Latitude+" , "+tmp.Longitude);
                    break;
                    
                case LocationUtils.MSG_SHOW_LOG:
                    mTVShowLog.setText("log: "+(String) msg.obj);
                    
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    // Intent to send to SendMockLocationService. Contains the type of test to run
    private Intent mRequestIntent;
    
    private Button mBtStart;
    private Button mBtStop;
    private SendMockLocationService mockLocationService;
    private TextView mTVStartPoint;
    private TextView mTVStopPoint;
    private Button mBtExit;
    private Button mBtSelectFile;
    private EditText mEdChangeSpeed;
    private Button mBtChangeSpeed;
    
    private TextView mTVFileName;
    private Button mBtIsRealSpeed;
    private TextView mTVShowLog;
    private Button mBtResetSpeed;
    
    private CheckBox mCbContinusMock;
    
    ServiceConnection conn = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected()   name="+name );
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected()   name="+name );
            mockLocationService = ((SendMockLocationService.MockLocationBinder)service).getService();
            if(mockLocationService != null){
                mockLocationService.setHandle(mHandler);
            }
        }
    };

    /*
     * Initialize global variables and set up inner components
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        Toast.makeText(this, "oncreate()", Toast.LENGTH_SHORT).show();
        
        setContentView(R.layout.activity_main);
        
        mBtStart = (Button) this.findViewById(R.id.start_button);
        mBtStart.setOnClickListener(this);
        mBtStop = (Button) this.findViewById(R.id.stop_button);
        mBtStop.setOnClickListener(this);
        mBtExit = (Button) this.findViewById(R.id.exit_button);
        mBtExit.setOnClickListener(this);
        mBtSelectFile = (Button) this.findViewById(R.id.select_file_button);
        mBtSelectFile.setOnClickListener(this);
        

        mTVFileName = (TextView) this.findViewById(R.id.file_name);
        mTVStartPoint = (TextView) this.findViewById(R.id.startpoint);
        mTVStopPoint = (TextView) this.findViewById(R.id.stoppoint);
        
        mEdChangeSpeed = (EditText) this.findViewById(R.id.speed_mul);
        mBtChangeSpeed = (Button) this.findViewById(R.id.bt_change_speed);
        mBtChangeSpeed.setOnClickListener(this);
        
        mBtIsRealSpeed = (Button) this.findViewById(R.id.bt_is_real_speed);
        mBtIsRealSpeed.setOnClickListener(this);
        mBtIsRealSpeed.setText("当前：真实轨迹速度，【点击切换成过滤小于2.5m/s的点");
        
        mBtResetSpeed = (Button) this.findViewById(R.id.bt_reset_speed);
        mBtResetSpeed.setOnClickListener(this);
        
        
        
        mTVShowLog = (TextView) this.findViewById(R.id.tv_log);
        
        mCbContinusMock = (CheckBox) this.findViewById(R.id.checkbox_continus_mock);
        mCbContinusMock.setOnCheckedChangeListener(this);
        
        
        mRequestIntent = new Intent(this, SendMockLocationService.class);
        startService(mRequestIntent);
        if(mockLocationService == null){
            bindService(mRequestIntent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        
        switch (id) {
            case DLG_EXCEPION_ID:

                        return new AlertDialog.Builder(this).setTitle("错误")
                                .setMessage("java.lang.SecurityException: Requires ACCESS_MOCK_LOCATION secure setting ," +
                                		"请勾选【设置】---【开发者选项】--【允许模拟位置】!!")
                                .create();
            default:
                break;
        }
        
        return super.onCreateDialog(id);
    }

    @Override
    public void onClick(View v) {
        if(v == mBtStart){
            Toast.makeText(this, "开始模拟Gps ", Toast.LENGTH_SHORT).show();
            
            changeSpeed();
            
            mRequestIntent = new Intent(this, SendMockLocationService.class);
            mRequestIntent.setAction(LocationUtils.ACTION_START_ONCE);
            
            bundle = new Bundle();
//            bundle.putString("file_name", mTVFileName.getText().toString());
            bundle.putStringArray("file_names", file_names);
            mRequestIntent.putExtras(bundle);
            startService(mRequestIntent);
            
//            bindService(mRequestIntent, conn, Context.BIND_AUTO_CREATE);
        } else if(v == mBtStop){
            
            mRequestIntent = new Intent(this, SendMockLocationService.class);
            mRequestIntent.setAction(LocationUtils.ACTION_STOP_TEST);
            startService(mRequestIntent);
//            bindService(mRequestIntent, conn, Context.BIND_AUTO_CREATE);
            
            mTVStartPoint.setText("");
            mTVStopPoint.setText("");
        } else if(mBtExit == v){
            Toast.makeText(this, "退出程序 ", Toast.LENGTH_SHORT).show();
            mRequestIntent = new Intent(this, SendMockLocationService.class);
            stopService(mRequestIntent);
            
            finish();
        } else if(v == mBtSelectFile){
            mRequestIntent = new Intent(this,FileManager.class);
            startActivityForResult(mRequestIntent, 11);
        } else if(v == mBtChangeSpeed){
            changeSpeed();
        } else if(v == mBtIsRealSpeed){
            
//            Toast.makeText(this, "开发中。。。", Toast.LENGTH_SHORT).show();
            if(mockLocationService.isRealSpeed()){
                mockLocationService.setRealSpeed(false);
                Toast.makeText(this, "过滤速度小于2.5m/s的点", Toast.LENGTH_SHORT).show();
                mBtIsRealSpeed.setText("当前：过滤速度小于2.5m/s的点，【点击切换成真实速度");
            } else {
                mockLocationService.setRealSpeed(true);
                Toast.makeText(this, "真实轨迹速度", Toast.LENGTH_SHORT).show();
                mBtIsRealSpeed.setText("当前：真实轨迹速度，【点击切换成过滤小于2.5m/s的点");
            }
        } else if(v == mBtResetSpeed){
            resetSpeed();
            Toast.makeText(this, "速度重置 ，倍数为1", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void changeSpeed(){
        if(mockLocationService != null){
            String str = mEdChangeSpeed.getText().toString();
            float tmp = Float.parseFloat(str);
            mockLocationService.setSpeed_mul(tmp);
        }
    }
    
    private void resetSpeed(){
        if(mockLocationService != null){
        	mEdChangeSpeed.setText(1+"");
            mockLocationService.setSpeed_mul(1);
        }
    }
    
    Bundle bundle ;
//    String file_name = "";
    String[] file_names = null;
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        Log.i(TAG, "onActivityResult() requestCode="+requestCode);
        if(resultCode == RESULT_OK){
            if(requestCode == 11){
                if(data != null){
                    bundle = data.getExtras();
//                    file_name = bundle.getString("file_name");
                    file_names = bundle.getStringArray("file_names");
                    
                    String tmp = "";
                    
                    if(file_names!=null){
                    	for(int i=0 ; i<file_names.length;i++){
                        	tmp += file_names[i]+"\n";
                        }
                    }
                    
                    mTVFileName.setText(tmp);
                    Toast.makeText(MainActivity.this, "file_name = "+tmp, Toast.LENGTH_SHORT).show();
                }
            }
        }
        Log.i(TAG, "onActivityResult()");
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			Toast.makeText(this, "打开循环模拟", Toast.LENGTH_SHORT).show();
			if(mockLocationService != null){
	            mockLocationService.setContinusMock(true);
	        }
		} else {
			Toast.makeText(this, "关闭循环模拟", Toast.LENGTH_SHORT).show();
			if(mockLocationService != null){
	            mockLocationService.setContinusMock(false);
	        }
		}
	}
}
