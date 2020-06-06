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
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import in.codeomega.parents.Home;
import in.codeomega.parents.R;
import in.codeomega.parents.interfaces.DialogListener;

public class ZoomMeetingID extends Dialog  {

    private Context context;
    private View view;
    private boolean isDone;
    private String meetingID = "";

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Activity activity = ((Activity) context);
        if (activity instanceof DialogListener)
            ((DialogListener) activity).handleClose(isDone, meetingID);
    }

    public ZoomMeetingID (final Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.member_id_dialog);
        this.context = context;
        this.setCancelable(false);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button BTSave = (Button) findViewById(R.id.btn_submit);
        Button BTCancel = (Button) findViewById(R.id.btn_cancel);
        final EditText ETMemberID = (EditText) findViewById(R.id.etMemberId);

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
                if(meetingID.isEmpty()) {
                    ETMemberID.setError(context.getString(R.string.error_meeting_id));
                } else {
                    isDone = true;
                    dismiss();
                }
            }
        });
    }
}
