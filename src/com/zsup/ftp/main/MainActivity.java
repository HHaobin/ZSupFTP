package com.zsup.ftp.main;

import java.io.IOException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zsup.ftp.R;
import com.zsup.ftp.dbhelper.FtpDAO;
import com.zsup.ftp.method.FTPInfo;
import com.zsup.ftp.upDown.FTP;
import com.zsup.ftp.upDown.UploadService;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initFtpConfig();

	}

	private FTP ftp = null;

	/**
	 * 打开ftp连接
	 */
	private void openConnection(FTPInfo ftpInfo) {
		try {
			if (ftp != null) {
				// 关闭FTP服务
				ftp.closeConnect();
			}
			// 初始化FTP
			ftp = new FTP(ftpInfo);
			// 打开FTP服务
			ftp.openConnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void goTo(View v) {
		 Intent intent = new Intent(MainActivity.this, LocalActivity.class);
		 intent.putExtra("remotePath", "/");
		 intent.putExtra("ftpInfo", ftpDAO.getFTPInfo("Forsafe"));
		 MainActivity.this.startActivity(intent);
	}

	private FTPInfo ftpInfo = null;
	private FtpDAO ftpDAO = null;

	public void initFtpConfig() {
		ftpDAO = new FtpDAO(this);
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"ftp_share", MODE_PRIVATE);
		boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
		if (isFirstRun) {
			if (ftpInfo == null) {
				ftpInfo = new FTPInfo();
			}
			ftpInfo.setFtpName("Forsafe");
			ftpInfo.setHostName("www.forsafe100.cn");
			ftpInfo.setPort(21);
			ftpInfo.setUserName("app");
			ftpInfo.setPassword("fire123ftp");
			ftpDAO.insertFTP(ftpInfo);

			Editor editor = sharedPreferences.edit();
			editor.putBoolean("isFirstRun", false);
			editor.commit();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				FTPInfo ftpInfo = ftpDAO.getFTPInfo("Forsafe");
				openConnection(ftpInfo);
			}
		}).start();
	}	
	
	public void startUpload(View v) {
		Intent intent = new Intent(this, UploadService.class);
        intent.setAction(UploadService.ACTION_START);
        startService(intent);
	}
}
