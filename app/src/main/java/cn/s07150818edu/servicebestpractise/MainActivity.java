package cn.s07150818edu.servicebestpractise;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private  DownlaodService.DownlaodBinder downlaodBinder;
    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downlaodBinder= (DownlaodService.DownlaodBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private Button star,pause,cancel;
    Intent intentService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        star= (Button) findViewById(R.id.start_b);
        pause= (Button) findViewById(R.id.pause_b);
        cancel= (Button) findViewById(R.id.cancel_b);
        star.setOnClickListener(this);
        pause.setOnClickListener(this);
        cancel.setOnClickListener(this);
        intentService=new Intent(this,DownlaodService.class);
        startService(intentService);
        bindService(intentService,serviceConnection,BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"拒绝权限将无法云心程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (downlaodBinder==null){
            return;
        }
        switch (view.getId()){
            case R.id.start_b:
                    String url="http://wap.sogou.com/app/redir.jsp?appdown=1&u=b-5xxbPWjTJoeTuIzmwjONmoKoIYNgdlpFxfQAAD8oQnyAN0Ad91Ya8aT5S7A-vIsVi-YNhF7FIJcxqzdYewK3njD1ytUFisDzHj2FLV9O08i1OMwoduM3PPnO0BhoQUQufnsm1eGRCKxT4hYVC9oeub4LjqsfRd&docid=5504078102252627613&sourceid=-4792509026194171546&w=1906&stamp=20170819";
                    downlaodBinder.starDownload(url);

                break;
            case R.id.pause_b:
                    downlaodBinder.pauseDownload();
                break;
            case R.id.cancel_b:
downlaodBinder.cancelDownload();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        stopService(intentService);
    }
}
