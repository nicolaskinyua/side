package mph.trunksku.apps.myssh.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import mph.trunksku.apps.myssh.api.StrongholdApiClient;
import mph.trunksku.apps.myssh.security.SecurityChecker;

public class VerifySignatureAsync extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "VerifySignatureAsync";
    
    private Context context;
    private OnVerificationCompleteListener listener;
    private boolean localSecurityPassed;
    
    public interface OnVerificationCompleteListener {
        void onVerified(boolean isValid);
        void onSecurityThreat(String reason);
    }
    
    public VerifySignatureAsync(Context context, OnVerificationCompleteListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            if (SecurityChecker.isDeviceRooted()) {
                localSecurityPassed = false;
                return false;
            }
            
            if (SecurityChecker.isDangerousAppInstalled(context)) {
                localSecurityPassed = false;
                return false;
            }
            
            if (SecurityChecker.isAppDebuggable(context)) {
                localSecurityPassed = false;
                return false;
            }
            
            localSecurityPassed = true;
            
            StrongholdApiClient api = new StrongholdApiClient(context);
            return api.verifySignature();
            
        } catch (Exception e) {
            Log.e(TAG, "Error during verification: " + e.getMessage());
            return true;
        }
    }
    
    @Override
    protected void onPostExecute(Boolean isValid) {
        super.onPostExecute(isValid);
        
        if (listener != null) {
            if (!localSecurityPassed) {
                listener.onSecurityThreat("Security check failed. Please use original app version.");
            } else if (!isValid) {
                listener.onSecurityThreat("Invalid app signature detected.");
            } else {
                listener.onVerified(true);
            }
        }
    }
}
