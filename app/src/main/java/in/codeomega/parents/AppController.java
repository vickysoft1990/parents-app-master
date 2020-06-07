package in.codeomega.parents;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;


import android.os.CountDownTimer;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static in.codeomega.parents.interfaces.AppConstants.URLforUpdateDeviceID;

public class AppController extends Application {

    private MediaPlayer audioPlayer = null;

    static AppController mInstance;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public RequestQueue requestQueue;

    public int current_timeout = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
    public int retry_count = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;

    public CountDownTimer countDownTimer;
    Handler handler = new Handler();

    public NotificationManager notificationManager;

    int notification_count = 0;


    public AppController() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        mInstance = this;
        requestQueue = Volley.newRequestQueue(this);
        preferences = getSharedPreferences("preferences",0);
        editor = preferences.edit();

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public void show_toast(String msg, Context context, int duration) {

        try {

            if (duration == 0) {

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }

        }catch (Exception e)
        {

            //_Log.e("Exception",e.toString());
        }

    }

    public void show_error_popup_alert(VolleyError volleyError, Context activity) {
        try {
            String error_msg = "";

            //_Log.e("error", "" + volleyError.toString());

            error_msg = get_error_msg(volleyError);

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(error_msg).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                    dialog.dismiss();
                }
            });
            final AlertDialog alert = builder.create();
            alert.setTitle("Message");
            alert.show();

        } catch (Exception e) {

        }

    }

    public String get_error_msg(VolleyError volleyError) {
        {

            String error_msg = "";

            if (volleyError instanceof TimeoutError) {
                error_msg = "Server is taking too long to respond";

            } else if (volleyError instanceof NoConnectionError) {
                error_msg = "No Internet Connection";

            } else if (volleyError instanceof AuthFailureError) {
                error_msg = "Error Occured, Please try later";

            } else if (volleyError instanceof ServerError) {
                error_msg = "Server Error, Please try later";

            } else if (volleyError instanceof NetworkError) {
                error_msg = "Network Error, Please try later";

            } else if (volleyError instanceof ParseError) {
                error_msg = "Error Occured, Please try later";

            } else if (volleyError instanceof NetworkError) {
                error_msg = "Network Error, Please try later";
            }


            return error_msg;
        }

    }

    public void show_popup_alert(String msg, Context activity) {
        try {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(msg).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                    dialog.dismiss();
                }
            });
            final AlertDialog alert = builder.create();
            alert.setTitle("Message");
            alert.show();

        } catch (Exception e) {
            //_Log.e("Exception", e.toString());
        }

    }

    public void show_popup_alert_without_title(String msg, Context activity) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(msg).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                dialog.dismiss();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();


    }

    public void setMinimizedToolbarDesign(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, getResources().getColor(R.color.logoColor));
            activity.setTaskDescription(taskDesc);
        }

    }


    public ProgressDialog createProgressDialog(ProgressDialog progressDialog,Context context) {

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        return progressDialog;

    }

    public void printError(Exception e) {

        String tempMsg = e.toString()+"\n";
        StackTraceElement[] stfckTrfce = e.getStackTrace();

        for (int i = 0; i < stfckTrfce.length; i++) {

            tempMsg = tempMsg+stfckTrfce[i]+"\n";
        }

        Log.e("Exception",tempMsg);
    }

    public void initAudioPlayer(String audioFilePath, final ImageView dialogPlay, final ImageView dialogPause, ImageView dialogClose, final SeekBar dialogSeekbar, final Dialog audioPlayerDialog)
    {
        final ProgressBar loading = audioPlayerDialog.findViewById(R.id.loading);

        loading.setVisibility(View.VISIBLE);
        dialogSeekbar.setVisibility(View.GONE);

        long start = System.currentTimeMillis();
        Log.e("f1",start-System.currentTimeMillis()+"");

        try {

            if(audioPlayer == null)
            {
                audioPlayer = new MediaPlayer();
                Log.e("audioPlayer","null");

            }else
            {
                audioPlayer.reset();
                Log.e("audioPlayer","not null");

            }

            audioFilePath = "http://"+audioFilePath;
            Log.e("audioFilePath",audioFilePath+"");

            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            audioPlayer.setDataSource(audioFilePath);

            Log.e("f3",start-System.currentTimeMillis()+"");

            dialogPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogPlay.setVisibility(View.VISIBLE);
                    dialogPause.setVisibility(View.GONE);
                    audioPlayer.pause();
                }
            });

            dialogPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogPlay.setVisibility(View.GONE);
                    dialogPause.setVisibility(View.VISIBLE);
                    audioPlayer.start();
                }
            });

            dialogClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    audioPlayer.stop();
                    audioPlayer.reset();
                    audioPlayerDialog.dismiss();
                }
            });

            Log.e("f3",start-System.currentTimeMillis()+"");

            audioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    loading.setVisibility(View.GONE);
                    dialogSeekbar.setVisibility(View.VISIBLE);

                    dialogSeekbar.setMax(audioPlayer.getDuration());
                    dialogSeekbar.setProgress(0);

                    audioPlayer.start();
                    Log.e("audioPlayer","OnPrepared");

                    handler = new Handler();
                    final int delay = 500; //milliseconds

                    handler.postDelayed(new Runnable(){
                        public void run(){
                            //do something
                            dialogSeekbar.setProgress((int) (audioPlayer.getCurrentPosition()));
                            handler.postDelayed(this, delay);
                        }
                    }, delay);

                }
            });

            audioPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Log.e("onError",i+" "+i1);

                    show_popup_alert("Unable to play audio. Please try later.",mInstance);
                    audioPlayerDialog.dismiss();

                    try {

                        audioPlayer.stop();
                        audioPlayer.reset();

                    }catch (Exception e)
                    {

                    }

                    return false;
                }
            });

            Log.e("f4",start-System.currentTimeMillis()+"");

            audioPlayer.prepareAsync();

            Log.e("f5",start-System.currentTimeMillis()+"");

            dialogSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    Log.e("onProgressChanged",""+i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    Log.e("onStartTrackingTouch","onStartTrackingTouch");
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    Log.e("onStopTrackingTouch","onStopTrackingTouch");
                    audioPlayer.pause();
                    audioPlayer.seekTo(dialogSeekbar.getProgress());
                    audioPlayer.start();

                }
            });

            Log.e("f6",start-System.currentTimeMillis()+"");


        }catch(IOException e)
        {
            show_popup_alert("Unable to play audio. Please try later.",mInstance);
            audioPlayerDialog.dismiss();

            try {

                audioPlayer.stop();
                audioPlayer.reset();

            }catch (Exception e1)
            {

            }

            AppController.getInstance().printError(e);
        }
    }

    public void update_did() {

        StringRequest updateDeviceId = new StringRequest(Request.Method.POST, URLforUpdateDeviceID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                Log.e("response", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("status")) {
                        AppController.getInstance().editor.putBoolean("update_did", true);
                        AppController.getInstance().editor.commit();

                    } else {



                    }


                } catch (Exception e) {
                    Log.e("e", e.toString());

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub

                Log.e("onErrorResponse error", "" + volleyError.toString());

            }
        }) {

            @SuppressLint("MissingPermission")
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                Map<String, String> params = new HashMap<String, String>();

                params.put("btnupdatedly", "1");
                params.put("user_id", AppController.getInstance().preferences.getString("user_id", ""));
                params.put("did", AppController.getInstance().preferences.getString("did", ""));

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

        Log.e("request", "update_did :" + "sent");

        updateDeviceId.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return AppController.getInstance().current_timeout;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Log.e("setRetryPolicy error", error.toString());
            }
        });

        AppController.getInstance().requestQueue.add(updateDeviceId);

    }


    public void sendNotification(String title, String messageBody, String Id, String type) {

        Intent intent = null;
        intent = new Intent(this, Home.class);
        intent.putExtra("type",type);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
//                        .setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap())
                        .setColor(getResources().getColor(R.color.primaryColor))
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(messageBody));



        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(channelId,
                    title,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notification_count /* ID of notification */, notificationBuilder.build());
        notification_count++;
    }

    public static String convertFromServerDateToRequestedFormat(String dateInput, String outDateFormat) {
        String outDate;
        String  serverDateFormat = "yyyy-MM-dd";
        try {

            SimpleDateFormat sdf = new SimpleDateFormat(serverDateFormat, Locale.ENGLISH);
            if (outDateFormat.equals(serverDateFormat)) {
                return dateInput;
            } else {
                Date date = sdf.parse(dateInput);
                sdf = new SimpleDateFormat(outDateFormat, Locale.ENGLISH);
                outDate = sdf.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                Date date = sdf.parse(dateInput);
                sdf = new SimpleDateFormat(outDateFormat, Locale.ENGLISH);
                outDate = sdf.format(date);
                return outDate;
            } catch (Exception e1) {
                e.printStackTrace();
                return dateInput;
            }
        }
        return outDate;
    }



    public void setRetryPolicies(StringRequest stringRequest) {

//        stringRequest.setRetryPolicy(new RetryPolicy() {
//            @Override
//            public int getCurrentTimeout() {
//                return AppController.getInstance().current_timeout;
//            }
//
//            @Override
//            public int getCurrentRetryCount() {
//                return AppController.getInstance().retry_count;
//            }
//
//            @Override
//            public void retry(VolleyError error) throws VolleyError {
//                Log.e("error", error.toString());
//            }
//        });
    }
}
