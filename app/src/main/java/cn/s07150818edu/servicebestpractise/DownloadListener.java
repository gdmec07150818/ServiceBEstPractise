package cn.s07150818edu.servicebestpractise;

/**
 * Created by Administrator on 2017/8/13.
 */

public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFaile();
    void onPause();
    void onCancel();
}
