package in.codeomega.parents.firebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ExecutionException;

import in.codeomega.parents.AppController;


/**
 * Created by HP on 13-Jul-18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String NOTIFICATION_VAN = "1";
    public static final String NOTIFICATION_ANNOUNCEMENT = "2";

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        Log.e("onMessageReceived","onMessageReceived");

        if (remoteMessage.getData().size() > 0) {

            String type = remoteMessage.getData().get("type");

            if (type.equals(NOTIFICATION_VAN)) {

                Log.e("NOTIFICATION_VAN","NOTIFICATION_VAN");
                Intent intent = new Intent();
                intent.putExtra("lat",Double.parseDouble(remoteMessage.getData().get("lat")));
                intent.putExtra("lng",Double.parseDouble(remoteMessage.getData().get("lng")));
                intent.setAction("in.codeomega.parents.NOTIFICATION_VAN");
                sendBroadcast(intent);

            }else  if (type.equals(NOTIFICATION_ANNOUNCEMENT)) {

                AppController.getInstance().sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"), "", type);

            }
        }


    }

}