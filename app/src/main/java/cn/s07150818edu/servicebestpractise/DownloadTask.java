package cn.s07150818edu.servicebestpractise;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/8/13.
 */

public class DownloadTask extends AsyncTask<String, Integer, Integer> {
    //     返回识别的参数
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSE = 2;
    public static final int TYPE_CANCEL = 3;

    //回调接口
    private DownloadListener downloadListener;
    private boolean isPasuse = false;
    private boolean isCancel = false;

    private int lastProgress;

    public DownloadTask(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        File file = null;
        InputStream in = null;
        RandomAccessFile saveFile = null;


        long downloadLength = 0;
        String downloadUri = params[0];
        String filename = downloadUri.substring(downloadUri.lastIndexOf("/"));
        String diretory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        file = new File(diretory + filename);
        if (file.exists()) {
            downloadLength = file.length();
        }
        long contentLength = getContentLength(downloadUri);

        if (contentLength == 0) {
            return TYPE_FAILED;
        } else if (contentLength == downloadLength) {
            return TYPE_SUCCESS;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("RANGE", "Byte" + downloadLength + "-")
                .url(downloadUri)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (request != null) {
                in = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                saveFile.seek(downloadLength);
                byte[] bytes = new byte[1024];
                int total = 0;
                int len;
                while ((len = in.read(bytes)) != -1) {
                    if (isCancel) {
                        return TYPE_CANCEL;
                    } else if (isPasuse) {
                        return TYPE_PAUSE;
                    } else {
                        total += len;
                        saveFile.write(bytes, 0, len);
                        int progress = (int) ((total + downloadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }


                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (saveFile != null) {
                    saveFile.close();
                }
                if (isCancel && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0];
        if (progress > lastProgress) {
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        switch (integer) {
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                downloadListener.onFaile();
                break;
            case TYPE_CANCEL:
                downloadListener.onCancel();
                break;
            case TYPE_PAUSE:
                downloadListener.onPause();
                break;
            default:
                break;
        }
    }

    public void pauseDownload() {
        isPasuse = true;
    }

    public void cancelDownload() {
        isCancel = true;
    }

    private long getContentLength(String downloadUri) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUri)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentlength = response.body().contentLength();
                response.body().close();
                return contentlength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
