package in.codeomega.parents.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import in.codeomega.parents.AppController;
import in.codeomega.parents.Home;
import in.codeomega.parents.R;
import in.codeomega.parents.model.Notification;
import us.zoom.sdk.InviteOptions;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.ZoomSDK;

/**
 * Created by HP on 09-Mar-18.
 */

public class NotificationAdapter extends BaseAdapter {

    ArrayList<Notification> notifications;
    Activity activity;
    LayoutInflater inflater;

    Notification notification;

    public NotificationAdapter(Activity activity, ArrayList<Notification> notifications) {
        this.activity = activity;
        this.notifications = notifications;

    }

    @Override
    public int getCount() {

        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        notification = notifications.get(position);

        if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        convertView = inflater.inflate(R.layout.previous_audio_row, null);

        if (notification.voice_text.contains("voc")) {

            convertView = inflater.inflate(R.layout.previous_audio_row, null);

            TextView type = (TextView) convertView.findViewById(R.id.type);
            TextView date = (TextView) convertView.findViewById(R.id.date);
            TextView sec = (TextView) convertView.findViewById(R.id.sec);
            TextView std = (TextView) convertView.findViewById(R.id.std);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            ImageView userPic = (ImageView) convertView.findViewById(R.id.user_pic);
            ImageView play = (ImageView) convertView.findViewById(R.id.play);
            LinearLayout audioContainer = (LinearLayout) convertView.findViewById(R.id.audioContainer);

            Glide.with(activity).load(notification.pic).placeholder(R.drawable.circular_user_place_holder).into(userPic);
            name.setText(notification.name + "");
            std.setText("STD : " + notification.class_names);
            sec.setText("SEC : " + notification.section_names);

            audioContainer.setTag(position);
            audioContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("onClick", notifications.get(position).message);
                    ((Home) activity).playAudio(notifications.get(position).message);
                }
            });

            date.setText(notification.date);

            if (notification.message_type.equals("0")) {
                type.setText("NOTIFICATION + SMS");

            } else if (notification.message_type.equals("1")) {
                type.setText("SMS");

            } else if (notification.message_type.equals("2")) {
                type.setText("SMS");
            }


        } else if (notification.voice_text.contains("txt")) {

            convertView = inflater.inflate(R.layout.previous_message_row, null);

            TextView type = (TextView) convertView.findViewById(R.id.type);
            TextView date = (TextView) convertView.findViewById(R.id.date);
            TextView message = (TextView) convertView.findViewById(R.id.message);
            TextView sec = (TextView) convertView.findViewById(R.id.sec);
            TextView std = (TextView) convertView.findViewById(R.id.std);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            ImageView userPic = (ImageView) convertView.findViewById(R.id.user_pic);

            Glide.with(activity).load(notification.pic).placeholder(R.drawable.circular_user_place_holder).into(userPic);

            name.setText(notification.name + "");
            std.setText("STD : " + notification.class_names);
            sec.setText("SEC : " + notification.section_names);
            message.setText(notification.message);
            date.setText(notification.date);
            switch (notification.message_type) {
                case "0":
                    type.setText("NOTIFICATION + SMS");

                    break;
                case "1":
                    type.setText("SMS");

                    break;
                case "2":
                    type.setText("SMS");
                    break;
            }

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!notification.message.isEmpty() && notification.message.contains("Meeting ID")) {
//                        Log.e("Message",notification.message);
//                        Toast.makeText(activity, "Meeting ID Found", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                        alertDialogBuilder.setTitle("Zoom Meeting");
                        alertDialogBuilder.setMessage("Do you want to join this meeting?");
                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String meetingID = getMeetingIDFromMessage(notification.message);
                                Log.e("Meeting ID", meetingID);
                                initZoom(meetingID);
                            }
                        });
                        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialogBuilder.setCancelable(false);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
            });

        }

        return convertView;
    }

    private String getMeetingIDFromMessage(String message) {
        String meetingID = "";
        message = message.replaceAll("\\r","");
        message = message.replaceAll("\\n","");
        String[] abc = message.split("Meeting ID:");
        if(abc.length>1) {
            String xyz[] = abc[1].split("Password");
            meetingID = xyz[0].replaceAll(" ","");
        }
        return meetingID;
    }

    private void initZoom(String meetingNo) {
        ZoomSDK zoomSDK = ZoomSDK.getInstance();
        if (!zoomSDK.isInitialized()) {
            Toast.makeText(activity, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG).show();
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
        params.displayName = AppController.getInstance().preferences.getString("name", "");
        params.meetingNo = meetingNo;
        meetingService.joinMeetingWithParams(activity, params, opts);
    }
}