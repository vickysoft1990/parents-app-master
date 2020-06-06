package in.codeomega.parents.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import in.codeomega.parents.AppController;


/**
 * Created by HP
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + refreshedToken);

        if(!AppController.getInstance().preferences.getString("did","").equals(refreshedToken))
        {
            AppController.getInstance().editor.putString("did",refreshedToken);
            AppController.getInstance().editor.commit();

            if(AppController.getInstance().preferences.getString("token","").length()!=0 && !AppController.getInstance().preferences.getBoolean("update_did",false))
            {
                AppController.getInstance().update_did();

            }
        }


    }

}
