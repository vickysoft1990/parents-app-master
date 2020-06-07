package in.codeomega.parents;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import in.codeomega.parents.adapters.FeedbackAdapter;
import in.codeomega.parents.model.Feedback;

import static in.codeomega.parents.interfaces.AppConstants.URLforGetFeedback;

public class FeedbackHistory extends AppCompatActivity {

    ProgressDialog progressDialog = null;
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private androidx.appcompat.widget.Toolbar toolbar;
    private com.google.android.material.appbar.AppBarLayout appbarlayout;

    FeedbackAdapter feedbackAdapter;
    private TextView newFeedback;
    ArrayList<Feedback> feedbacks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_history);

        feedbackAdapter = new FeedbackAdapter(FeedbackHistory.this,feedbacks);

        this.appbarlayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);

        this.newFeedback = (TextView) findViewById(R.id.newFeedback);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Previous Feedback");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, FeedbackHistory.this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setRefreshing(true);
                getFeedback();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(FeedbackHistory.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(feedbackAdapter);

        newFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(FeedbackHistory.this, in.codeomega.parents.Feedback.class));
            }
        });

    }

    private void getFeedback() {

        progressDialog.show();

        StringRequest getFeedback = new StringRequest(Request.Method.POST, URLforGetFeedback, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                Log.e("response", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray responseJsonArray = jsonObject.getJSONArray("response");

                    feedbacks.clear();

                    if (jsonObject.getBoolean("status")) {

                        for (int i = 0; i < responseJsonArray.length(); i++) {

                            JSONObject feedback = responseJsonArray.getJSONObject(i);

                            feedbacks.add(new Feedback(feedback.get("title").toString(),feedback.get("message").toString(),feedback.get("updated").toString()));
                        }

                    } else {
                        AppController.getInstance().show_popup_alert(jsonObject.getString("message"), FeedbackHistory.this);
                    }


                } catch (Exception e) {

                    AppController.getInstance().printError(e);
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", FeedbackHistory.this);

                }

                feedbackAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub

                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();
                AppController.getInstance().show_toast("Unable to connect !", FeedbackHistory.this, Toast.LENGTH_SHORT);
                Log.e("error", "" + volleyError.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", AppController.getInstance().preferences.getString("user_id", ""));
                params.put("academic_year", AppController.getInstance().preferences.getString("academic_year", ""));
                params.put("btngetmsg", "1");
                params.put("token", AppController.getInstance().preferences.getString("token", ""));
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

        AppController.getInstance().setRetryPolicies(getFeedback);
        Log.e("request", "getFeedback :" + "sent");

        AppController.getInstance().requestQueue.add(getFeedback);

    }

    @Override
    protected void onResume() {
        super.onResume();

        getFeedback();

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
