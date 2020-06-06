package in.codeomega.parents;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.util.Log.e;
import static android.widget.Toast.LENGTH_SHORT;
import static com.android.volley.Request.Method.POST;

public class Login extends AppCompatActivity {

    private ImageView imageView;
    private EditText name;
    private EditText password;
    private Button login;
    private android.widget.TextView forgotPassword;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, Login.this);

        this.login = (Button) findViewById(R.id.login);
        this.password = (EditText) findViewById(R.id.password);
        this.name = (EditText) findViewById(R.id.name);
        this.imageView = (ImageView) findViewById(R.id.imageView);

        this.forgotPassword = (TextView) findViewById(R.id.forgotPassword);


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle("Reset Password");

                View phoneNumberView = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
                final EditText phoneNumber = (EditText) phoneNumberView.findViewById(R.id.phoneNumber);
                builder.setView(phoneNumberView);

                // Set up the buttons
                builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String ph = phoneNumber.getText().toString();

                        if (ph.length() != 10) {

                            Toast.makeText(Login.this, "Invalid Phone Number", LENGTH_SHORT).show();

                        } else {

                            dialog.dismiss();
                            getOTP(ph);

                        }

                        Log.e("input", phoneNumber.getText().toString());

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Boolean flag = true;
                String msg = "";

                if(name.getText().toString().length()==0)
                {
                    msg = "Invalid User Name";
                    flag = false;

                }else if(password.getText().toString().length()==0)
                {
                    msg = "Invalid Password";
                    flag = false;
                }


                if(flag)
                {
                    loginUser();

                }else
                {
                    Toast.makeText(Login.this,msg,Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void getOTP(final String ph) {

        progressDialog.show();

        e("getOTP", getResources().getString(R.string.getotp));

        StringRequest getOTP = new StringRequest(POST, getResources().getString(R.string.getotp), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                e("response", response);

                try {

                    if(response.contains(ph))
                    {
                        startActivity(new Intent(Login.this,ResetPassword.class).putExtra("ph",ph));

                    }else
                    {
                        AppController.getInstance().show_popup_alert("Please enter a registered phone number", Login.this);
                    }

                } catch (Exception e) {

                    e("e", e.toString());
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", Login.this);

                }

                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub

                progressDialog.dismiss();

                e("error", "" + volleyError.toString());
                AppController.getInstance().show_error_popup_alert(volleyError, Login.this);
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();

                params.put("mobile", ph);

                e("params", "" + params.toString());

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

        e("request", "getOTP :" + "sent");

        AppController.getInstance().requestQueue.add(getOTP);
    }


    private void loginUser() {

        progressDialog.show();

        e("loginUser", "" + getResources().getString(R.string.base_url) + getResources().getString(R.string.loginUser));

        StringRequest loginUser = new StringRequest(POST, getResources().getString(R.string.base_url) + getResources().getString(R.string.loginUser), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                e("response", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("status")) {

                        JSONObject profileObject = jsonObject.getJSONArray("response").getJSONObject(0);

                        AppController.getInstance().editor.putBoolean("login",true);
                        AppController.getInstance().editor.putString("academic_year",profileObject.getString("academic_year"));
                        AppController.getInstance().editor.putString("token",profileObject.getString("refid"));
                        AppController.getInstance().editor.putString("user_id",profileObject.getString("refid"));
                        AppController.getInstance().editor.putString("name",profileObject.getString("name"));
                        AppController.getInstance().editor.putString("class",profileObject.getString("class"));
                        AppController.getInstance().editor.putString("section",profileObject.getString("section"));
                        AppController.getInstance().editor.putString("van_refid",profileObject.getString("van"));
                        AppController.getInstance().editor.putString("username",name.getText().toString());
                        AppController.getInstance().editor.commit();

                        startActivity(new Intent(Login.this, Home.class));
                        finish();

                    } else {

                        AppController.getInstance().show_popup_alert(jsonObject.getString("message"), Login.this);
                    }

                } catch (Exception e) {

                    e("e", e.toString());
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", Login.this);

                }

                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub

                progressDialog.dismiss();
                AppController.getInstance().show_toast("Unable to connect !", Login.this, LENGTH_SHORT);
                e("error", "" + volleyError.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();

                params.put("username", name.getText().toString());
                params.put("password", password.getText().toString());
//                params.put("type", "T");
                params.put("btnlogin", "1");
                e("params", "" + params.toString());

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

        loginUser.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return AppController.getInstance().current_timeout;
            }

            @Override
            public int getCurrentRetryCount() {
                return AppController.getInstance().retry_count;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                //_Log.e("error", error.toString());
            }
        });

        e("request", "loginUser :" + "sent");

        AppController.getInstance().requestQueue.add(loginUser);

    }

}
