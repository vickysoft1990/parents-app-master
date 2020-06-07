package in.codeomega.parents;

import in.codeomega.parents.fragments.Fees;
import in.codeomega.parents.fragments.Marks;
import in.codeomega.parents.fragments.Notification;
import in.codeomega.parents.fragments.Track;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import in.codeomega.parents.fragments.ZoomMeetingID;
import in.codeomega.parents.interfaces.ZoomConstants;
import in.codeomega.parents.interfaces.DialogListener;
import in.codeomega.parents.model.AvailMeetingDetailsBO;
import us.zoom.sdk.InviteOptions;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKAuthenticationListener;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static in.codeomega.parents.interfaces.AppConstants.URLforAvailMeetings;


public class Home extends AppCompatActivity implements Notification.OnFragmentInteractionListener, Fees.OnFragmentInteractionListener, Marks.OnFragmentInteractionListener, Track.OnFragmentInteractionListener,
        ZoomConstants, DialogListener, ZoomSDKInitializeListener, MeetingServiceListener, ZoomSDKAuthenticationListener {

    private TextView mTextMessage;
    int current_id = 0;
    private ImageView feedback, profile, images, IVZoomLive;
    Dialog audioPlayerDialog;
    ImageView dialogPlay, dialogPause, dialogClose;
    SeekBar dialogSeekbar;
    TextView toolbar_title;
    private DisplayMetrics displaymetrics;
    private ArrayList<AvailMeetingDetailsBO> mAvailMeetingList;
    ProgressDialog progressDialog;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Log.e("MenuItem", item.getTitle().toString() + " " + item.getItemId());
            Fragment selectedFragment = null;
            int id = item.getItemId();
            switch (id) {
                case R.id.notifications:
                    if (current_id != id) {
                        selectedFragment = new Notification();
                        current_id = id;
                        change_fragment(selectedFragment, "Notification");
                    }
                    return true;

                case R.id.track:
                    if (current_id != id) {
                        selectedFragment = new Track();
                        current_id = id;
                        change_fragment(selectedFragment, "Track");
                    }
                    return true;

                case R.id.fees:

                    current_id = id;
                    ask_permission();
                    return true;

                case R.id.marks:

                    current_id = id;
                    ask_permission();
                    return true;
            }
            return false;
        }
    };

    private void change_fragment(Fragment selectedFragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, selectedFragment, tag);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppController.getInstance().preferences.getString("van_refid", "").equals("0")) {
            setContentView(R.layout.activity_home_no_van);

        } else {
            setContentView(R.layout.activity_home);
        }
        displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, Home.this);

        audioPlayerDialog = new Dialog(Home.this);
        audioPlayerDialog.setContentView(R.layout.audio_player_popup);
        audioPlayerDialog.setTitle("Audio Player");
        audioPlayerDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        dialogClose = (ImageView) audioPlayerDialog.findViewById(R.id.close);
        dialogPlay = (ImageView) audioPlayerDialog.findViewById(R.id.play);
        dialogPause = (ImageView) audioPlayerDialog.findViewById(R.id.pause);
        dialogSeekbar = (SeekBar) audioPlayerDialog.findViewById(R.id.seekbar);

        AppController.getInstance().setMinimizedToolbarDesign(Home.this);

        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        mTextMessage = (TextView) findViewById(R.id.message);
        feedback = (ImageView) findViewById(R.id.feedback);
        profile = (ImageView) findViewById(R.id.profile);
        images = (ImageView) findViewById(R.id.images);
        IVZoomLive = (ImageView) findViewById(R.id.ivZoomMemberNo);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        disableShiftMode(navigation);

        toolbar_title.setText(AppController.getInstance().preferences.getString("name", ""));
        current_id = R.id.notifications;
        change_fragment(new Notification(), "notification");
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAvailMeetingList.clear();
                startActivity(new Intent(Home.this, FeedbackHistory.class));
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAvailMeetingList.clear();
                startActivity(new Intent(Home.this, Profile.class));
            }
        });

        images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAvailMeetingList.clear();
                startActivity(new Intent(Home.this, UploadedImages.class));
            }
        });

        IVZoomLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAvailMeetingList.size() == 0)
                    fetchDataForAvailableMeetings();
                else
                    getZoomMemberIDDialog();
            }
        });
        mAvailMeetingList = new ArrayList<>();

        ZoomSDK zoomSDK = ZoomSDK.getInstance();
        if (savedInstanceState == null) {
            ZoomSDKInitParams initParams = new ZoomSDKInitParams();
            initParams.appKey = APP_KEY;
            initParams.appSecret = APP_SECRET;
            initParams.domain = WEB_DOMAIN;
            zoomSDK.initialize(this, this, initParams);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void disableShiftMode(BottomNavigationView view) {

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        menuView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        menuView.buildMenuView();
    }


    public void playAudio(String audioPath) {
        if (audioPath.length() != 0) {
            audioPlayerDialog.show();
            AppController.getInstance().show_toast("Please Wait", Home.this, Toast.LENGTH_SHORT);
            AppController.getInstance().initAudioPlayer(audioPath, dialogPlay, dialogPause, dialogClose, dialogSeekbar, audioPlayerDialog);
        } else {
            AppController.getInstance().show_toast("Invalid Audio File", Home.this, Toast.LENGTH_SHORT);
        }


    }

    public void ask_permission() {

        Dexter.withActivity(Home.this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                if (report.areAllPermissionsGranted()) {

                    Log.e("**", "areAllPermissionsGranted()**");
                    if (current_id == R.id.fees) {
                        change_fragment(new Fees(), "Fees");
                    } else if (current_id == R.id.marks) {
                        change_fragment(new Marks(), "Marks");
                    }

                } else if (report.isAnyPermissionPermanentlyDenied()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    builder.setTitle("Permission");
                    builder.setMessage("Application needs Storage permissions to continue");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", Home.this.getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, 1);
                            Toast.makeText(Home.this.getBaseContext(), "Go to Permissions to Grant STORAGE", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    builder.setTitle("Permission");
                    builder.setMessage("Application needs Storage permissions to continue");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ask_permission();
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

                Log.e("report", "areAllPermissionsGranted" + report.areAllPermissionsGranted() + "");
                Log.e("report", "getDeniedPermissionResponses" + report.getDeniedPermissionResponses() + "");
                Log.e("report", "getGrantedPermissionResponses" + report.getGrantedPermissionResponses() + "");
                Log.e("report", "isAnyPermissionPermanentlyDenied" + report.isAnyPermissionPermanentlyDenied() + "");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                token.continuePermissionRequest();
            }

        }).check();
    }

    @Override
    protected void onResume() {
        super.onResume();

        update_did_check();

//       subscribeUser();

        /*

        PARENTS APP
        academicYear
        academicYear_class
        academicYear_class_section
        academicYear_section

        TEACHERS APP
        academicYear_teacher
        academicYear_class_teacher
        academicYear_class_section_teacher
        academicYear_section_teacher

         */

    }

    public void update_did_check() {

        Log.e("Firebase", "condition " + (!AppController.getInstance().preferences.getString("did", "").equals("")) + " " + (!AppController.getInstance().preferences.getBoolean("update_did", false)));
        Log.e("Firebase", "id " + AppController.getInstance().preferences.getString("did", ""));
        FirebaseMessaging.getInstance().subscribeToTopic("general_notifications");
        if (!AppController.getInstance().preferences.getString("did", "").equals("") && !AppController.getInstance().preferences.getBoolean("update_did", false)) {
            Log.e("Firebase", "token " + FirebaseInstanceId.getInstance().getToken());
            AppController.getInstance().update_did();
        }
    }

    private void getZoomMemberIDDialog() {
        ZoomMeetingID zoomMeetingID = new ZoomMeetingID(Home.this, mAvailMeetingList);
        if (!zoomMeetingID.isShowing()) {
            zoomMeetingID.show();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = zoomMeetingID.getWindow();
            lp.copyFrom(window != null ? window.getAttributes() : null);
            lp.width = displaymetrics.widthPixels - 100;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            if (window != null) {
                window.setAttributes(lp);
            }
        }
    }

    public void initZoom(String meetingNo) {
        ZoomSDK zoomSDK = ZoomSDK.getInstance();
        if (!zoomSDK.isInitialized()) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG).show();
            return;
        }
        MeetingService meetingService = zoomSDK.getMeetingService();
        JoinMeetingOptions opts = new JoinMeetingOptions();
        // Some available options
        //		opts.no_driving_mode = true;
        //		opts.no_invite = true;
        //		opts.no_meeting_end_message = true;
        //		opts.no_titlebar = true;
        //		opts.no_bottom_toolbar = true;
        //		opts.no_dial_in_via_phone = true;
        //		opts.no_dial_out_to_phone = true;
        //		opts.no_disconnect_audio = true;
        //		opts.no_share = true;
        opts.invite_options = InviteOptions.INVITE_VIA_EMAIL + InviteOptions.INVITE_VIA_SMS;
        //		opts.no_audio = true;
        //		opts.no_video = true;
        //		opts.meeting_views_options = MeetingViewsOptions.NO_BUTTON_SHARE;
        //		opts.no_meeting_error_message = true;
        //		opts.participant_id = "participant id";
        JoinMeetingParams params = new JoinMeetingParams();
        params.displayName = toolbar_title.getText().toString();
        params.meetingNo = meetingNo;
        meetingService.joinMeetingWithParams(this, params, opts);
    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int i, int i1) {

    }

    @Override
    public void onZoomSDKLoginResult(long l) {

    }

    @Override
    public void onZoomSDKLogoutResult(long l) {

    }

    @Override
    public void onZoomIdentityExpired() {

    }

    @Override
    public void onZoomSDKInitializeResult(int i, int i1) {

    }

    @Override
    public void onZoomAuthIdentityExpired() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void handleClose(boolean isDone, String meetingID) {
        if (isDone && !meetingID.equals("")) {
            initZoom(meetingID);
        }
    }

    private void fetchDataForAvailableMeetings() {
        mAvailMeetingList = new ArrayList<>();
        try {
            String url = URLforAvailMeetings + AppController.getInstance().preferences.getString("user_id", "");
            url = url.replace(" ", "%20");
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            AvailMeetingDetailsBO availMeetingDetailsBO = new AvailMeetingDetailsBO();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            availMeetingDetailsBO.setRefId(jsonObject.optString("refid").trim());
                            availMeetingDetailsBO.setMeetingId(jsonObject.optString("meeting_id").trim());
                            availMeetingDetailsBO.setMeetingPwd(jsonObject.optString("password").trim());
                            availMeetingDetailsBO.setEmpRefId(jsonObject.optString("emp_refid").trim());
                            availMeetingDetailsBO.setDate(AppController.convertFromServerDateToRequestedFormat(jsonObject.optString("dated").trim(),"dd/MM/yyyy"));
                            availMeetingDetailsBO.setUpdate(jsonObject.optString("upated").trim());
                            availMeetingDetailsBO.setClassId(jsonObject.optString("class").trim());
                            availMeetingDetailsBO.setSection(jsonObject.optString("section").trim());
                            availMeetingDetailsBO.setStartingTime(jsonObject.optString("time").trim());
                            availMeetingDetailsBO.setMeetingTitle(jsonObject.optString("title").trim());
                            availMeetingDetailsBO.setName(jsonObject.optString("name").trim());
                            availMeetingDetailsBO.setGender(jsonObject.optString("gender").trim());
                            availMeetingDetailsBO.setClassName(jsonObject.optString("clsname").trim());
                            availMeetingDetailsBO.setSectionName(jsonObject.optString("sec_name").trim());
                            mAvailMeetingList.add(availMeetingDetailsBO);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    getZoomMemberIDDialog();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    String message = null;
                    if (error instanceof NetworkError) {
                        message = Message.NetworkError;
                    } else if (error instanceof ServerError) {
                        message = Message.ServerError;
                    } else if (error instanceof AuthFailureError) {
                        message = Message.AuthFailureError;
                    } else if (error instanceof ParseError) {
                        message = Message.ParseError;
                    } else if (error instanceof NoConnectionError) {
                        message = Message.NoConnectionError;
                    } else if (error instanceof TimeoutError) {
                        message = Message.TimeOutError;
                    }
                    Toast.makeText(Home.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            stringRequest.setShouldCache(true);
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    private void subscribeUser() {
//
//        String academic_year,classId,section;
//
//        academic_year = AppController.getInstance().preferences.getString("academic_year","");
//        classId = AppController.getInstance().preferences.getString("class","");
//        section = AppController.getInstance().preferences.getString("section","");
//
//        Log.e("subscribeUser",academic_year+" "+classId+" "+section);
//        Log.e("academicYear",academic_year);
//        Log.e("academicYear_class",academic_year+"_"+classId);
//        Log.e("academicYear_class_section",academic_year+"_"+classId+"_"+section);
//
//        if(!AppController.getInstance().preferences.getBoolean("academicYear",false))
//        {
//            FirebaseMessaging.getInstance().subscribeToTopic(academic_year)
//
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//
//                            Log.e("academicYear",task.isSuccessful()+"");
//                            AppController.getInstance().editor.putBoolean("academicYear",true);
//                            AppController.getInstance().editor.commit();
//                        }
//                    });
//        }
//
//
//        if(!AppController.getInstance().preferences.getBoolean("academicYear_class",false))
//        {
//            FirebaseMessaging.getInstance().subscribeToTopic(academic_year+"_"+classId)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//
//                            Log.e("academicYear_class",task.isSuccessful()+"");
//
//                            AppController.getInstance().editor.putBoolean("academicYear_class",true);
//                            AppController.getInstance().editor.commit();
//                        }
//                    });
//        }
//
//        if(!AppController.getInstance().preferences.getBoolean("academicYear_class_section",false))
//        {
//            FirebaseMessaging.getInstance().subscribeToTopic(academic_year+"_"+classId+"_"+section)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//
//                            Log.e("academicYear_class_section",task.isSuccessful()+"");
//
//                            AppController.getInstance().editor.putBoolean("academicYear_class_section",true);
//                            AppController.getInstance().editor.commit();
//                        }
//                    });
//        }
//
//        if(!AppController.getInstance().preferences.getBoolean("general_notification",false))
//        {
//            FirebaseMessaging.getInstance().subscribeToTopic("general_notification")
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//
//                            Log.e("general_notification",task.isSuccessful()+"");
//
//                            AppController.getInstance().editor.putBoolean("general_notification",true);
//                            AppController.getInstance().editor.commit();
//                        }
//                    });
//        }
//
//    }
}
