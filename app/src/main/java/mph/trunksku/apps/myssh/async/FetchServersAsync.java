package mph.trunksku.apps.myssh.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.util.List;
import mph.trunksku.apps.myssh.api.StrongholdApiClient;
import mph.trunksku.apps.myssh.model.ServerModel;

public class FetchServersAsync extends AsyncTask<Void, Void, List<ServerModel>> {
    private static final String TAG = "FetchServersAsync";
    
    private Context context;
    private ProgressDialog progressDialog;
    private OnServersLoadedListener listener;
    private boolean showProgress;
    private String errorMessage;
    
    public interface OnServersLoadedListener {
        void onServersLoaded(List<ServerModel> servers);
        void onError(String message);
    }
    
    public FetchServersAsync(Context context, OnServersLoadedListener listener) {
        this(context, listener, true);
    }
    
    public FetchServersAsync(Context context, OnServersLoadedListener listener, boolean showProgress) {
        this.context = context;
        this.listener = listener;
        this.showProgress = showProgress;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showProgress) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading servers...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }
    
    @Override
    protected List<ServerModel> doInBackground(Void... voids) {
        try {
            StrongholdApiClient api = new StrongholdApiClient(context);
            
            api.registerDevice();
            
            JSONObject response = api.fetchServers();
            
            if (response != null && response.optBoolean("success", false)) {
                return api.parseServers(response);
            } else {
                errorMessage = "Failed to load servers";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching servers: " + e.getMessage());
            errorMessage = "Network error: " + e.getMessage();
        }
        return null;
    }
    
    @Override
    protected void onPostExecute(List<ServerModel> servers) {
        super.onPostExecute(servers);
        
        if (showProgress && progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {}
        }
        
        if (listener != null) {
            if (servers != null && !servers.isEmpty()) {
                listener.onServersLoaded(servers);
            } else {
                listener.onError(errorMessage != null ? errorMessage : "No servers available");
            }
        }
    }
}
