package com.zsup.ftp.upDown;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zsup.ftp.dbhelper.FtpDAO;
import com.zsup.ftp.dbhelper.ThreadDAO;
import com.zsup.ftp.method.FTPInfo;
import com.zsup.ftp.method.ThreadInfo;

/**
 * Created by Leaves on 2016/6/9.
 */
public class UploadService extends Service {

	public static final String ACTION_START = "ACTION_START"; // 开始上载
	public static final String ACTION_STOP = "ACTION_STOP"; // 暂停上载
	public static final String ACTION_CANCEL = "ACTION_CANCEL"; // 暂停上载
	public static final String ACTION_UPDATE = "ACTION_UPDATE"; // 更新UI
	public static final String ACTION_FINISHED = "ACTION_FINISHED"; // 上载结束

	private FtpDAO ftpDAO = null;
	private ThreadDAO threadDAO = null;

	private FTP ftp = null;
	private FTPInfo ftpInfo = null;
	private FTPClient ftpClient = null;

	private ThreadInfo threadInfo = null;
	private File localFile = null;
	private String remotePath = "/";

	private Map<Integer, UploadTask> tasks = new LinkedHashMap<Integer, UploadTask>(); // 下载任务集合

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
//			UploadTask uploadTask = new UploadTask(UploadService.this,
//					threadInfo, ftpClient);
//			uploadTask.start();
//			tasks.put(threadInfo.getThread_id(), uploadTask);
			
		}
	};

	/**
	 * 上载列表初始化线程
	 */
	private void initDataByThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 获取待上载线程信息
				threadDAO = new ThreadDAO(UploadService.this);
				List<ThreadInfo> threadList = threadDAO.getThreadList(2); // 2
																			// 未上载完
																			// 3
																	// 已上载完
				System.out.println("上传~threadList.size():"+threadList.size());
				if (threadList.size() > 0) {		
					Log.i("ftp ---->", "startUpload");
					for (ThreadInfo mThreadInfo : threadList) {
						
//						InitThread initThread = new InitThread();
//						initThread.start();
//						threadInfo = threadList.get(i);
						Log.i("ftp ---->", "openConnect");
						FTPClient mFtpClient=null;
						try {
							ftpDAO = new FtpDAO(UploadService.this);
							ftpInfo = ftpDAO.getFTPInfo("Forsafe");
							ftp = new FTP(ftpInfo);
							ftp.openConnect();
							mFtpClient = ftp.getFtpClient();
						} catch (IOException e) {
							e.printStackTrace();
						}
						String fileName = mThreadInfo.getLocal_url().substring(
								mThreadInfo.getLocal_url().lastIndexOf("/") + 1);
						System.out.println("threadInfo:" + fileName);
						UploadTask uploadTask = new UploadTask(UploadService.this,
								mThreadInfo, mFtpClient);
						uploadTask.start();
						tasks.put(mThreadInfo.getThread_id(), uploadTask);
						Log.i("ACTION_START ---->", fileName + " Start");
					}
				}
			}
		}).start();
	}

	/**
	 * 启动Service自动调用onStartCommand
	 * 
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction().equals(ACTION_START)) {
			initDataByThread();

		} else if (intent.getAction().equals(ACTION_STOP)) {
			threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
			String fileName = threadInfo.getLocal_url().substring(
					threadInfo.getLocal_url().lastIndexOf("/") + 1);
			// 取出下载任务
			UploadTask uploadTask = tasks.get(threadInfo.getThread_id());
			if (uploadTask != null) {
				uploadTask.isPause = true;
			}
			Log.i("ACTION_STOP ---->", fileName + " Stop");

		} else if (intent.getAction().equals(ACTION_CANCEL)) {
			threadInfo = (ThreadInfo) intent.getSerializableExtra("threadInfo");
			threadDAO = new ThreadDAO(this);
			threadDAO.deleteThread(threadInfo);
			// 广播通知下载任务取消
			Intent intent1 = new Intent();
			intent1.setAction(ACTION_CANCEL);
			intent1.putExtra("threadInfo", threadInfo);
			this.sendBroadcast(intent1);
			Log.i("ACTION_CANCEL ---->", "Cancel");
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 初始化 ftp 连接子线程
	 */
	class InitThread extends Thread {
		@Override
		public void run() {
			super.run();
			handler.sendEmptyMessage(0);
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
