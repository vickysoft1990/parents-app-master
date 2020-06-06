package in.codeomega.parents;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        Log.e("getToken",""+ FirebaseInstanceId.getInstance().getToken());


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    Thread.sleep(3000);

                    if(AppController.getInstance().preferences.getBoolean("login",false))
                    {
                        startActivity(new Intent(Splash.this, Home.class));

                    }else
                    {
                        startActivity(new Intent(Splash.this,Login.class));
                    }

                    finish();


                } catch (InterruptedException e) {
                    e.printStackTrace();

                }


            }
        }).start();
    }
}
