package in.codeomega.parents;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Feedback extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText title;
    private EditText feedback;
    private Button send;

    ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feedback);

        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, Feedback.this);
        
        this.send = (Button) findViewById(R.id.send);
        this.feedback = (EditText) findViewById(R.id.feedback);
        this.title = (EditText) findViewById(R.id.title);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("Feedback");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Boolean flag = true;
                String msg = "";

                if (title.getText().toString().isEmpty()) {
                    msg = "Invalid Title";
                    flag = false;

                } else if (feedback.getText().toString().isEmpty()) {
                    msg = "Invalid Feedback";
                    flag = false;
                }

                if (flag) {

                    sendFeedback();
                    
                } else {
                    Toast.makeText(Feedback.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendFeedback() {

        progressDialog.show();

        Log.e("sendFeedback", "" + getResources().getString(R.string.base_url) + getResources().getString(R.string.sendFeedback));

        StringRequest sendFeedback = new StringRequest(Request.Method.POST, getResources().getString(R.string.base_url) + getResources().getString(R.string.sendFeedback), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                Log.e("response", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("status")) {

                        AppController.getInstance().show_popup_alert("Thank You for your feedback.",Feedback.this);

                        title.setText("");
                        feedback.setText("");

                    } else {
                        AppController.getInstance().show_popup_alert(jsonObject.getString("message"), Feedback.this);
                    }

                } catch (Exception e) {

                    AppController.getInstance().printError(e);
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", Feedback.this);

                }

                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub

                
                progressDialog.dismiss();
                AppController.getInstance().show_toast("Unable to connect !", Feedback.this, Toast.LENGTH_SHORT);
                Log.e("error", "" + volleyError.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", AppController.getInstance().preferences.getString("user_id", ""));
                params.put("token", AppController.getInstance().preferences.getString("token", ""));
                params.put("aca_year", AppController.getInstance().preferences.getString("academic_year", ""));
                params.put("title", title.getText().toString());
                params.put("btfeed", "1");
                params.put("message", feedback.getText().toString());

                Log.e("params", "" + params.toString());

                return params;

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;

            }

        };

        AppController.getInstance().setRetryPolicies(sendFeedback);
        Log.e("request", "sendFeedback :" + "sent");

        AppController.getInstance().requestQueue.add(sendFeedback);
        
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
}
