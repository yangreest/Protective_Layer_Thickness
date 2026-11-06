package com.example.gtj_sd_dg240.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.view.LayoutInflater;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_sd_dg240.R;
import com.example.gtj_sd_dg240.view.CommonProgressDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.core.content.FileProvider;

@SuppressLint({"StaticFieldLeak", "InflateParams", "WakelockTimeout"})
public class DownloadAppTask extends AsyncTask<String, Integer, String> {
    private static final String DOWNLOAD_NAME = "jingzai.apk";
    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private CommonProgressDialog progressDialog;

    DownloadAppTask(Context context) {
        this.context = context;
        progressDialog = new CommonProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("正在下载");
        progressDialog.setCustomTitle(LayoutInflater.from(context).inflate(R.layout.title_dialog, null));
        progressDialog.setMessage("正在下载");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        File file = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error
            // report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP "
                        + connection.getResponseCode() + " "
                        + connection.getResponseMessage();
            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                file = new File(Environment.getExternalStorageDirectory(),
                        DOWNLOAD_NAME);

                if (!file.exists()) {
                    // 判断父文件夹是否存在
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                }

            } else {
                ToastUtils.showShort("sd卡未挂载");
            }
            input = connection.getInputStream();
            output = new FileOutputStream(file);
            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);

            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return e.toString();

        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        progressDialog.dismiss();
        if (result != null) {
            ToastUtils.showShort(result);
        } else {
            update();
        }
    }

    private void update() {
        //安装应用
        String fileName = Environment.getExternalStorageDirectory() + "/" + DOWNLOAD_NAME;//承接我的代码，filename指获取到了我的文件相应路径
        if (fileName != null) {
            if (fileName.endsWith(".apk")) {
                if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
                    File file = new File(fileName);
                    String authority = context.getPackageName() + ".provider";
                    Uri apkUri = FileProvider.getUriForFile(context, authority, file);//在AndroidManifest中的android:authorities值
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                    install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    context.startActivity(install);
                } else {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(install);
                }
            }
        }
    }
}