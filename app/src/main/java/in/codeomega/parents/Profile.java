package in.codeomega.parents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Profile extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView profilepic;
    private TextView name;
    private LinearLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        this.parentLayout = (LinearLayout) findViewById(R.id.parentLayout);
        this.name = (TextView) findViewById(R.id.name);
        this.profilepic = (ImageView) findViewById(R.id.profile_pic);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);

        name.setText(AppController.getInstance().preferences.getString("name",""));
        Glide.with(Profile.this).load(AppController.getInstance().preferences.getString("image","")).placeholder(R.drawable.circular_user_place_holder).into(profilepic);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void logout(View view)
    {
        String academic_year,classId,section;

        academic_year = AppController.getInstance().preferences.getString("academic_year","");
        classId = AppController.getInstance().preferences.getString("class","");
        section = AppController.getInstance().preferences.getString("section","");


        FirebaseMessaging.getInstance().unsubscribeFromTopic("general_notification");
        FirebaseMessaging.getInstance().unsubscribeFromTopic(academic_year);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(academic_year+"_"+classId);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(academic_year+"_"+classId+"_"+section);

        AppController.getInstance().editor.clear();
        AppController.getInstance().editor.commit();

        Intent intent = new Intent(Profile.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
