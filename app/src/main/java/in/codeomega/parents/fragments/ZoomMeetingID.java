package in.codeomega.parents.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import in.codeomega.parents.Home;
import in.codeomega.parents.R;
import in.codeomega.parents.interfaces.DialogListener;
import in.codeomega.parents.model.AvailMeetingDetailsBO;

public class ZoomMeetingID extends Dialog {

    private Context context;
    private View view;
    private boolean isDone;
    private String meetingID = "";
    private ListView mListView;
    private EditText ETMemberID;
    private Button BTSave;

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Activity activity = ((Activity) context);
        if (activity instanceof DialogListener)
            ((DialogListener) activity).handleClose(isDone, meetingID);
    }

    public ZoomMeetingID(final Context context, ArrayList<AvailMeetingDetailsBO> mData) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.member_id_dialog);
        this.context = context;
        this.setCancelable(false);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        BTSave = (Button) findViewById(R.id.btn_submit);
        Button BTCancel = (Button) findViewById(R.id.btn_cancel);
        ETMemberID = (EditText) findViewById(R.id.etMemberId);
        mListView = (ListView) findViewById(R.id.listOfAvailMeetings);
        TextView textView = (TextView)findViewById(R.id.tvAvailClassess);
        if (mData != null && mData.size() > 0) {
            MyAdapter myAdapter = new MyAdapter(mData);
            mListView.setAdapter(myAdapter);
        }
        else {
            textView.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
        }
        BTCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDone = false;
                dismiss();
            }
        });

        BTSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meetingID = ETMemberID.getText().toString().trim();
                if (meetingID.isEmpty()) {
                    ETMemberID.setError(context.getString(R.string.error_meeting_id));
                } else {
                    meetingID = meetingID.replaceAll(" ","");
                    isDone = true;
                    dismiss();
                }
            }
        });
    }

    private class MyAdapter extends BaseAdapter {

        private ArrayList<AvailMeetingDetailsBO> mData;

        private MyAdapter(ArrayList<AvailMeetingDetailsBO> mData) {
            this.mData = mData;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final MyViewHolder holder;
            if (convertView == null) {
                holder = new MyViewHolder();
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.row_available_classes, parent, false);

                holder.TVMeetingTitle = (TextView) convertView.findViewById(R.id.tvMeetingTitle);
                holder.TVDateTime = (TextView) convertView.findViewById(R.id.tvMeetingDate);
                holder.TVName = (TextView) convertView.findViewById(R.id.tvName);
                holder.TVStdSec = (TextView) convertView.findViewById(R.id.tvClassStdSec);
                holder.TVMeetingID = (TextView) convertView.findViewById(R.id.tvMeetingID);
                holder.TVMeetingPwd = (TextView) convertView.findViewById(R.id.tvMeetingPassword);
                holder.BTAttend = (Button) convertView.findViewById(R.id.btAttend);
                convertView.setTag(holder);
            } else {
                holder = (MyViewHolder) convertView.getTag();
            }
            holder.meetingDetailsBO = mData.get(position);
            holder.TVMeetingTitle.setText(holder.meetingDetailsBO.getMeetingTitle());
            holder.TVName.setText(holder.meetingDetailsBO.getName());
            String stdSec = "STD: "+holder.meetingDetailsBO.getClassId()+", SEC: "+holder.meetingDetailsBO.getSectionName();
            holder.TVStdSec.setText(stdSec);
            holder.TVMeetingID.setText("Meeting ID: "+holder.meetingDetailsBO.getMeetingId());
            if(!holder.meetingDetailsBO.getMeetingPwd().isEmpty()) {
                holder.TVMeetingPwd.setText("Password: " + holder.meetingDetailsBO.getMeetingPwd());
            } else {
                holder.TVMeetingPwd.setVisibility(View.GONE);
            }

            holder.TVDateTime.setText(holder.meetingDetailsBO.getDate()+" "+holder.meetingDetailsBO.getStartingTime());

            holder.BTAttend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!holder.meetingDetailsBO.getMeetingId().isEmpty()) {
                        ETMemberID.setText(holder.meetingDetailsBO.getMeetingId());
                        BTSave.performClick();
                    }
                }
            });
            return convertView;
        }

        class MyViewHolder {
            private TextView TVMeetingTitle, TVDateTime, TVName, TVStdSec, TVMeetingID, TVMeetingPwd;
            private Button BTAttend;
            private AvailMeetingDetailsBO meetingDetailsBO;
        }
    }
}
