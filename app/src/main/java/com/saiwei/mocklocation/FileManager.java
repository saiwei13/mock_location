package com.saiwei.mocklocation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;


public class FileManager extends Activity implements OnClickListener{

	private final String TAG = "chenwei.FileManager";
    
    private Context mContext;
    
    /**
     *  最小的文件大小
     */
    private final int MIN_FILE_SIZE = 5000;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.video_file_manager);
        mContext = this;
        init();
    }
    
    
    private ListView mListView;
    private MyAdapter mAdapter;
    private ArrayList<File> mListFiles = null;
    private Button mBtOk = null;
    private Button mBtShowLoc = null;
    
    /**
     * 文件夹路径
     */
    private String mFileDir;
    private CheckedTextView mCheckedTextView;
    
    /**
     * 初始化
     */
    private void init() {

/*        tvCurPath = (TextView) this.findViewById(R.id.file_manager_currentfilepath);
        tvCurPath.setText("当前路径："+Recorder_Global.mCurFilePath);*/
        
        mListView = (ListView) findViewById(R.id.list_listactivity);
        mAdapter = new MyAdapter(mContext);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	mAdapter.toggleChecked(position);
            	
//            	mCheckedTextView = (CheckedTextView) (View)parent.getItemAtPosition(position);
//            	
//            	if(mCheckedTextView.isChecked()){
//            		mCheckedTextView.setChecked(false);
//            		Toast.makeText(mContext, " 没选中  position="+position, Toast.LENGTH_SHORT).show();
//            	} else {
//            		mCheckedTextView.setChecked(true);
//            		Toast.makeText(mContext, " 选中  position="+position, Toast.LENGTH_SHORT).show();
//            	}
            	
//            	parent.getChildAt(position).setBackgroundColor(Color.BLUE);
            	
//            	mListView.getChildAt(position).setBackgroundColor(Color.BLUE);
//            	view.setSelected(true);
            	
//                File file = getFile(mFileDir, mListFiles.get(position).getName());
//                Logutil.i(TAG, "file="+file);
//                openFile(file);
            	
//                Toast.makeText(mContext, "position="+position, Toast.LENGTH_SHORT).show();
                
                
            	Log.i(TAG, "onItemClick()   position="+position+" , file.length="+mListFiles.get(position).length());
            	
////            	view.setBackgroundColor(Color.BLUE);
//                if(mListView.isItemChecked(position)){
//                	mListView.setItemChecked(position, false);
//                } else {
//                	mListView.setItemChecked(position, true);
//                }
            }
        });
        
        mBtOk = (Button) this.findViewById(R.id.bt_ok);
        mBtOk.setOnClickListener(this);
        
        mBtShowLoc = (Button) this.findViewById(R.id.bt_show_loc);
        mBtShowLoc.setOnClickListener(this);
        
        mFileDir = LocationUtils.FILE_DIR_PATH;
        
        scannerFile(mFileDir,"gps");
    }
    
    /**
     * 浏览文件
     */
    private void scannerFile(String filedir,final String endswith){

        File currentDirectory  = new File(filedir);
        
        if(!currentDirectory.exists()){
            return;
        }
        
        File[] files = currentDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(endswith);
            }
        });
    
        mListFiles = new ArrayList<File>(Arrays.asList(files));
    
//        Log.i("chenwei.FileMan", " size = "+mListFiles.size());
        
        
        ArrayList<File> mDeleteFiles = new ArrayList<File>();
        
        //自动删除0.0B大小的文件
        for(int i=0;i<mListFiles.size() ;i++){
            File file = mListFiles.get(i);
            
//            Log.i(TAG, "["+i+"]  file.length()="+file.length());
            
            if(file.length() < MIN_FILE_SIZE){
            	
            	mDeleteFiles.add(file);
            	
//                mListFiles.remove(file);
                
//                Log.i("chenwei.FileMan", " remove   file.length()= "+file.length()+" , size="+mListFiles.size());
            }
        }
        
        for(int i=0;i<mDeleteFiles.size();i++){
        	mListFiles.remove(mDeleteFiles.get(i));
        }
        
//        Log.i("chenwei.FileMan", " size = "+mListFiles.size());
        
        //按时间排序
        Collections.sort(mListFiles, new FileComparator()); 
        
        mAdapter.setList(mListFiles);
    }
    
    
    /**
     * 自定义适配器
     * @author wei.chen
     *
     */
    private class MyAdapter extends BaseAdapter{

    	
    	ArrayList mchecks  =  new ArrayList();
    	
        private LayoutInflater mInflater;
        private ArrayList<File> mList = null;
        private Context mContext;
        
        private Formatter mFormatter ;
        
        
        public MyAdapter(Context context) {
            
        	
            mContext = context;
            
            mFormatter = new Formatter();
            mInflater = LayoutInflater.from(context);
            
            mchecks.clear();
        }
        
        public void setList(ArrayList<File> list) {
            this.mList = list;
            notifyDataSetChanged();
        }
        
        public void toggleChecked(Integer position){
        	
        	if(mchecks.contains(position)){
        		mchecks.remove(position);
        	} else {
        		mchecks.add(position);
        	}
        	
        	notifyDataSetChanged();
        }
        
        @Override
        public int getCount() {
            if(mList != null){
                return mList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            
            if(mList != null){
                return mList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            ViewHolder holder;
            
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.file_manager_list_item, null);
                
                holder = new ViewHolder();
                
                holder.filename 
                        = (TextView) convertView.findViewById(R.id.tv_file_name);
                holder.filesize 
                        = (TextView) convertView.findViewById(R.id.tv_file_size);
                holder.filedate 
                        =  (TextView) convertView.findViewById(R.id.tv_file_date);
                
                holder.checktv = (CheckedTextView) convertView.findViewById(R.id.checktv_title);
                            
                
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.filename.setText(mList.get(position).getName());
            
            long lfiledate = mList.get(position).lastModified();
            String datetime = DateFormat.getDateFormat(mContext).format(new Date(lfiledate));
            
            holder.filedate.setText(datetime);
            
            String sfilesize = mFormatter.formatFileSize(mContext, mList.get(position).length());
            holder.filesize.setText(sfilesize +" , ");
            
//            Log.i(TAG, "getView()   position="+position+" , sfilesize="+sfilesize);
            
//            if(position ==3 ){
//            	holder.checktv.setChecked(true);
//            	holder.checktv.setBackgroundColor(Color.BLUE);
//            }
            
            if(mchecks.contains(position)){
            	holder.checktv.setChecked(true);
            } else {
            	holder.checktv.setChecked(false);
            }
            return convertView;
        }
        
        class ViewHolder {
            ImageView icon;
            TextView filename;
            TextView filesize;
            TextView filedate;
            CheckedTextView checktv;
        }
    }

	@Override
	public void onClick(View v) {
		if(v == mBtOk){
			Toast.makeText(mContext, "确定按钮", Toast.LENGTH_SHORT).show();
			
			
			Intent intent = new Intent(mContext, MainActivity.class);
            Bundle bundle = new Bundle();
            
            String[] strs = new String[mAdapter.mchecks.size()]  ;
            for(int i=0;i<mAdapter.mchecks.size();i++){
            	strs[i]= mListFiles.get((Integer) mAdapter.mchecks.get(i)).getName();
            }

            if(strs==null || strs.length == 0){
            	Toast.makeText(mContext, "no select item", Toast.LENGTH_SHORT).show();
            	return;
            }
            
//            bundle.putString("file_name", mListFiles.get(position).getName());
            bundle.putStringArray("file_names", strs);
            
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
			
		} else if(v == mBtShowLoc){
			scannerFile(mFileDir,"loc");
		}
	}
}
