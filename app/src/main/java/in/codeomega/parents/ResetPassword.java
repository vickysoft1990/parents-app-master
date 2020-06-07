package in.codeomega.parents;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static in.codeomega.parents.interfaces.AppConstants.URLforUpdatePassword;

public class ResetPassword extends AppCompatActivity {

    TextView phoneNumber;
    EditText otp, password;
    Button update;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, ResetPassword.this);

         phoneNumber = (TextView) findViewById(R.id.phoneNumber);
         otp = (EditText) findViewById(R.id.otp);
         password = (EditText) findViewById(R.id.password);
         update = (Button) findViewById(R.id.update);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Reset Password");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phoneNumber.setText(getIntent().getStringExtra("ph"));

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = "";
                Boolean flag = true;

                if (otp.getText().toString().length() == 0) {

                    msg = "Invalid OTP";
                    flag = false;

                } else if (password.getText().toString().length() == 0) {

                    msg = "Invalid Password";
                    flag = false;
                }

                if (flag) {

                    updatePassword();

                } else {
                    Toast.makeText(ResetPassword.this, msg, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void updatePassword() {

        progressDialog.show();

        StringRequest updatePassword = new StringRequest(Request.Method.POST, URLforUpdatePassword, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                Log.e("response", response);

                try {

                    if(Integer.parseInt(response.replace("\"",""))>0)
                    {
                        try {

                            final AlertDialog.Builder builder = new AlertDialog.Builder(ResetPassword.this);
                            builder.setMessage("Password has been reset successfully").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                                    dialog.dismiss();
                                    finish();
                                }
                            });
                            final AlertDialog alert = builder.create();
                            alert.setTitle("Message");
                            alert.show();

                        } catch (Exception e) {
                            //_Log.e("Exception", e.toString());
                        }

                    }else
                    {
                        AppController.getInstance().show_popup_alert(response, ResetPassword.this);

                    }


                } catch (Exception e) {

                    Log.e("e", e.toString());
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", ResetPassword.this);

                }

                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub

                progressDialog.dismiss();

                Log.e("error", "" + volleyError.toString());
                AppController.getInstance().show_error_popup_alert(volleyError, ResetPassword.this);
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();

                params.put("mobile", phoneNumber.getText().toString());
                params.put("entotp", otp.getText().toString());
                params.put("newpass", password.getText().toString());

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

        Log.e("request", "updatePassword :" + "sent");

        AppController.getInstance().requestQueue.add(updatePassword);
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
