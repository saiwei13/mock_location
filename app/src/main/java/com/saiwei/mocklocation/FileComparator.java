package com.saiwei.mocklocation;

import java.io.File;
import java.util.Comparator;

/**
 * 比较视频文件最后修改的时间
 * @author wei.chen
 *
 */
public class FileComparator implements Comparator<File> {

	@Override
	public int compare(File o1, File o2) {
/*		long time1 = o1.lastModified();
		long time2 = o2.lastModified();	*/
		
		long time1 = getFileLastModifyTime(o1);
		long time2 = getFileLastModifyTime(o2);	
		
		return (time1 > time2 ? -1 : (time1 == time1 ? 0 : 1));
	}

	
	/**
	 * 从文件名解析出时间
	 */
	private long getFileLastModifyTime(File file){
		
		String str = file.getName();
		str = str.replace("_", "");
		str = str.replace(".gps", "");
		return Tool.getSystemFormatTime(str);
	}
	
	
	
	  
	
}
