package mph.trunksku.apps.myssh.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import mph.trunksku.apps.myssh.ApplicationBase;
import mph.trunksku.apps.myssh.model.ServerModel;

public class StrongholdApiClient {
    private static final String TAG = "StrongholdApi";
    
    private static String API_BASE_URL = "https://mobile-vet-app--luxsev.replit.app/api/v1";
    
    private Context context;
    private SharedPreferences sp;
    
    public StrongholdApiClient(Context context) {
        this.context = context;
        this.sp = ApplicationBase.getSharedPreferences();
        
        String customUrl = sp.getString("api_base_url", "");
        if (!customUrl.isEmpty()) {
            API_BASE_URL = customUrl;
        }
    }
    
    public static void setApiBaseUrl(String url) {
        API_BASE_URL = url;
    }
    
    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    
    public String getApkSignature() {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                return hashSignature(signatures[0]);
            } else {
                packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
                return hashSignature(packageInfo.signatures[0]);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting signature: " + e.getMessage());
            return null;
        }
    }
    
    private String hashSignature(Signature sig) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sig.toByteArray());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    public JSONObject fetchServers() {
        try {
            URL url = new URL(API_BASE_URL + "/servers");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readStream(conn.getInputStream());
                return new JSONObject(response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching servers: " + e.getMessage());
        }
        return null;
    }
    
    public JSONObject fetchServerConfigs(int serverId) {
        try {
            URL url = new URL(API_BASE_URL + "/servers/" + serverId + "/config");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readStream(conn.getInputStream());
                return new JSONObject(response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching configs: " + e.getMessage());
        }
        return null;
    }
    
    public JSONObject fetchSettings() {
        try {
            URL url = new URL(API_BASE_URL + "/settings");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readStream(conn.getInputStream());
                return new JSONObject(response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching settings: " + e.getMessage());
        }
        return null;
    }
    
    public JSONObject registerDevice() {
        try {
            URL url = new URL(API_BASE_URL + "/register-device");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            JSONObject body = new JSONObject();
            body.put("deviceId", getDeviceId());
            body.put("username", Build.MODEL);
            
            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes("UTF-8"));
            os.close();
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readStream(conn.getInputStream());
                return new JSONObject(response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering device: " + e.getMessage());
        }
        return null;
    }
    
    public boolean verifySignature() {
        try {
            String signature = getApkSignature();
            if (signature == null) return false;
            
            URL url = new URL(API_BASE_URL + "/verify-signature");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            JSONObject body = new JSONObject();
            body.put("signature", signature);
            body.put("deviceId", getDeviceId());
            
            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes("UTF-8"));
            os.close();
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readStream(conn.getInputStream());
                JSONObject result = new JSONObject(response);
                return result.optBoolean("verified", false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying signature: " + e.getMessage());
        }
        return true;
    }
    
    private String readStream(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    
    public List<ServerModel> parseServers(JSONObject response) {
        List<ServerModel> servers = new ArrayList<>();
        try {
            if (response != null && response.optBoolean("success", false)) {
                JSONArray serversArray = response.optJSONArray("servers");
                if (serversArray != null) {
                    for (int i = 0; i < serversArray.length(); i++) {
                        JSONObject serverJson = serversArray.getJSONObject(i);
                        ServerModel server = new ServerModel();
                        server.setId(serverJson.optInt("id"));
                        server.setName(serverJson.optString("name"));
                        server.setHost(serverJson.optString("host"));
                        server.setPort(serverJson.optInt("port", 22));
                        server.setSshPort(serverJson.optInt("sshPort", 22));
                        server.setSslPort(serverJson.optInt("sslPort", 443));
                        server.setCountry(serverJson.optString("country"));
                        server.setCountryCode(serverJson.optString("countryCode"));
                        server.setFlagUrl(serverJson.optString("flagUrl"));
                        server.setPremium(serverJson.optBoolean("isPremium", false));
                        servers.add(server);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing servers: " + e.getMessage());
        }
        return servers;
    }
}
