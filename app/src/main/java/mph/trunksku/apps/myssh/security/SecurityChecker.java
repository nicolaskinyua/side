package mph.trunksku.apps.myssh.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SecurityChecker {
    private static final String TAG = "SecurityChecker";
    
    private static final Set<String> ROOT_PATHS = new HashSet<>(Arrays.asList(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    ));
    
    private static final Set<String> ROOT_PACKAGES = new HashSet<>(Arrays.asList(
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.topjohnwu.magisk",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.smedialink.oneclickroot",
        "com.zhiqupk.root.global",
        "com.alephzain.framaroot"
    ));
    
    private static final Set<String> DANGEROUS_APPS = new HashSet<>(Arrays.asList(
        "com.guoshi.httpcanary",
        "jp.co.intip",
        "app01.greyshirts.sslcapture",
        "com.evbadroid.proxymon",
        "app.greyshirts.sslcapture",
        "com.minhui.networkcapture",
        "com.minhui.networkcapture.pro",
        "de.robv.android.xposed.installer",
        "me.weishu.exp",
        "org.meowcat.edxposed.manager",
        "com.saurik.substrate",
        "de.robv.android.xposed",
        "com.jrummyapps.rootbrowser.classic",
        "com.jrummyapps.rootbrowser"
    ));
    
    private static String VALID_APK_SIGNATURE = null;
    
    public static void setValidSignature(String signature) {
        VALID_APK_SIGNATURE = signature;
    }
    
    public static boolean isDeviceRooted() {
        return checkRootFiles() || checkRootPackages() || checkSuBinary() || checkBuildTags();
    }
    
    private static boolean checkRootFiles() {
        for (String path : ROOT_PATHS) {
            if (new File(path).exists()) {
                Log.w(TAG, "Root file found: " + path);
                return true;
            }
        }
        return false;
    }
    
    private static boolean checkRootPackages() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"pm", "list", "packages"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                for (String pkg : ROOT_PACKAGES) {
                    if (line.contains(pkg)) {
                        Log.w(TAG, "Root package found: " + pkg);
                        return true;
                    }
                }
            }
            reader.close();
        } catch (Exception e) {}
        return false;
    }
    
    private static boolean checkSuBinary() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (reader.readLine() != null) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }
    
    private static boolean checkBuildTags() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }
    
    public static boolean isDangerousAppInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String pkg : DANGEROUS_APPS) {
            try {
                pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
                Log.w(TAG, "Dangerous app found: " + pkg);
                return true;
            } catch (PackageManager.NameNotFoundException e) {}
        }
        return false;
    }
    
    public static boolean isAppDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
    
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.toLowerCase().contains("vbox")
            || Build.FINGERPRINT.toLowerCase().contains("test-keys")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk".equals(Build.PRODUCT)
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu");
    }
    
    public static boolean isValidSignature(Context context) {
        if (VALID_APK_SIGNATURE == null) return true;
        
        try {
            String currentSig = getApkSignatureHash(context);
            return VALID_APK_SIGNATURE.equals(currentSig);
        } catch (Exception e) {
            Log.e(TAG, "Error checking signature: " + e.getMessage());
            return false;
        }
    }
    
    public static String getApkSignatureHash(Context context) {
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
    
    private static String hashSignature(Signature sig) {
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
    
    public static boolean isInstalledFromPlayStore(Context context) {
        String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        return "com.android.vending".equals(installer);
    }
    
    public static int performSecurityCheck(Context context) {
        int threatLevel = 0;
        
        if (isDeviceRooted()) {
            threatLevel += 30;
        }
        
        if (isDangerousAppInstalled(context)) {
            threatLevel += 40;
        }
        
        if (isAppDebuggable(context)) {
            threatLevel += 20;
        }
        
        if (!isValidSignature(context)) {
            threatLevel += 100;
        }
        
        return threatLevel;
    }
    
    public static boolean shouldBlockApp(Context context) {
        return performSecurityCheck(context) >= 50;
    }
}
