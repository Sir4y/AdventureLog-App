package com.appweb.webapk;

import com.appweb.webapk.R;
import android.content.DialogInterface;
import android.net.http.SslError;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Handler;
import android.webkit.WebSettings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Intent;
import android.net.Uri;
import android.content.res.Configuration;
import android.widget.EditText;
import android.webkit.JsResult;
import android.webkit.JsPromptResult;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.graphics.Color;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceError;
import androidx.annotation.Nullable;
import java.io.ByteArrayInputStream;
import android.webkit.JavascriptInterface;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.os.Looper;
import android.webkit.GeolocationPermissions;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.TextView;
import android.app.Activity;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient.FileChooserParams;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.webkit.DownloadListener;
import android.app.DownloadManager;
import android.webkit.URLUtil;
import android.os.Environment;
import static android.content.Context.DOWNLOAD_SERVICE;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import org.unifiedpush.android.connector.UnifiedPush;
import static org.unifiedpush.android.connector.ConstantsKt.INSTANCE_DEFAULT;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.webkit.PermissionRequest;
import android.view.MotionEvent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    private static final int MEDIA_PERMISSION_REQUEST_CODE = 1001;
    private static final String NOTIFICATION_CHANNEL_ID = "web_app_notifications";
    private static final String NOTIFICATION_CHANNEL_NAME = "Web App Notifications";

    private WebView webview;
    private UserScriptManager userScriptManager;
    private View mainLayout;
    private View errorLayout;
    private boolean errorOccurred = false;
    private ValueCallback<Uri[]> mFilePathCallback;       // Image upload
    private ActivityResultLauncher<Intent> fileChooserLauncher; // Image upload
    private WebAppInterface webAppInterface;
    private BroadcastReceiver unifiedPushEndpointReceiver;
    private BroadcastReceiver mediaActionReceiver;
    private PermissionRequest currentPermissionRequest;
    private GeolocationPermissions.Callback geoCallback;
    private String geoOrigin;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_MAIN_URL = "mainURL";
    private static final String KEY_SELECTED_ICON = "selectedIcon";
    private static final String KEY_BYPASS_SSL = "bypassSSL";
    private static final String ALIAS_DARK = "com.appweb.webapk.MainActivityAliasDark";
    private static final String ALIAS_LIGHT = "com.appweb.webapk.MainActivityAliasLight";
    private static final String ALIAS_COLOR = "com.appweb.webapk.MainActivityAliasColor";
    
    private static final long LONG_PRESS_THRESHOLD = 3000; // 3 seconds
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private final Runnable longPressRunnable = this::showUrlChangeDialog;
    private boolean isLongPressActive = false;
    private final float[][] initialTouchPoints = new float[10][2]; // Support up to 10 pointers
    private static final int MOVE_THRESHOLD = 80; // slightly increased for tolerance

    private String mainURL;
    private boolean bypassSSL = false;
    boolean requireDoubleBackToExit = true;
    boolean allowSubdomains = true;

    boolean enableExternalLinks = true;
    boolean openExternalLinksInBrowser = true;
    boolean confirmOpenInBrowser = true;

    boolean allowOpenMobileApp = false;
    boolean confirmOpenExternalApp = true;

    String cookies = "";
    String basicAuth = "";
    String userAgent = "";
    boolean blockLocalhostRequests = true;
    boolean JSEnabled = true;
    boolean JSCanOpenWindowsAutomatically = true;
    boolean DomStorageEnabled = true;
    boolean DatabaseEnabled = true;
    boolean MediaPlaybackRequiresUserGesture = true;
    boolean SavePassword = true;
    boolean AllowFileAccess = true;
    boolean AllowFileAccessFromFileURLs = true;
    boolean showDetailsOnErrorScreen = false;
    boolean forceLandscapeMode = false;
    boolean edgeToEdge = false;
    boolean forceDarkTheme = false;
    boolean allowMixedContent = false;
    String cacheMode = "default";
    int fadeInDuration = 400;
    boolean DebugWebView = false;

    boolean geolocationEnabled = true;
    boolean cameraEnabled = false;
    boolean microphoneEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (forceDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (edgeToEdge) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        }

        super.onCreate(savedInstanceState);

        if (edgeToEdge) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }

        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            channel.setDescription("Channel for web app notifications");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            Log.d("WebToApk", "Notification channel created.");
        }

        if (forceLandscapeMode) {
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        initMainURL();
        checkAndRequestLocationPermission();

        setContentView(R.layout.activity_main);
        mainLayout = findViewById(android.R.id.content);
        errorLayout = findViewById(R.id.errorLayout);
        userScriptManager = new UserScriptManager(this, mainURL);

        webview = findViewById(R.id.webView);
        webview.setAlpha(0f);
        webview.setBackgroundColor(Color.BLACK);

        webview.setWebViewClient(new CustomWebViewClient());
        webview.setWebChromeClient(new CustomWebChrome());
        webAppInterface = new WebAppInterface(this);
        webview.addJavascriptInterface(webAppInterface, "WebToApk");

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(JSEnabled);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(JSCanOpenWindowsAutomatically);
        webSettings.setGeolocationEnabled(geolocationEnabled);
        webSettings.setDomStorageEnabled(DomStorageEnabled);
        webSettings.setDatabaseEnabled(DatabaseEnabled);
        webSettings.setMediaPlaybackRequiresUserGesture(MediaPlaybackRequiresUserGesture);
        webSettings.setSavePassword(SavePassword);
        webSettings.setAllowFileAccess(AllowFileAccess);
        webSettings.setAllowFileAccessFromFileURLs(AllowFileAccessFromFileURLs);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webview.setWebContentsDebuggingEnabled(DebugWebView);

        if (allowMixedContent) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (!userAgent.isEmpty()) {
            webSettings.setUserAgentString(userAgent);
        }

        switch (cacheMode) {
            case "aggressive":
                webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                break;
            case "no_cache":
                webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                webview.clearCache(true);
                break;
            default:
                webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
                break;
        }

        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webview, true);
        cookieManager.flush();

        // Image upload support
        fileChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Uri[] results = null;
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent intentData = result.getData();
                    if (intentData.getClipData() != null) {
                        int count = intentData.getClipData().getItemCount();
                        results = new Uri[count];
                        for (int i = 0; i < count; i++) {
                            results[i] = intentData.getClipData().getItemAt(i).getUri();
                        }
                    } else if (intentData.getData() != null) {
                        results = new Uri[]{intentData.getData()};
                    }
                }
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                }
            }
        );

        // File downloading support
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription(getString(R.string.download_description));
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                try {
                    downloadManager.enqueue(request);
                    Toast.makeText(getApplicationContext(), R.string.download_started, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.download_failed, Toast.LENGTH_LONG).show();
                    Log.e("WebToApk", "Failed to start download", e);
                }
            }
        });


        // Broadcast receiver to get the endpoint from the PushServiceImpl
        unifiedPushEndpointReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String endpoint = intent.getStringExtra("endpoint");
                String p256dh = intent.getStringExtra("p256dh");
                String auth = intent.getStringExtra("auth");
                if (endpoint != null && p256dh != null && auth != null && webview != null) {
                    try {
                        JSONObject keys = new JSONObject();
                        keys.put("p256dh", p256dh);
                        keys.put("auth", auth);
                        JSONObject subscription = new JSONObject();
                        subscription.put("endpoint", endpoint);
                        subscription.put("expirationTime", JSONObject.NULL);
                        subscription.put("keys", keys);
                        String subscriptionJson = subscription.toString();
                        webview.post(() -> {
                            String js = "if (typeof window.__shim_onNewEndpoint === 'function') { window.__shim_onNewEndpoint('" + subscriptionJson.replace("'", "\\'") + "'); }";
                            webview.evaluateJavascript(js, null);
                        });
                    } catch (JSONException e) {
                         Log.e("WebToApk", "Failed to create subscription JSON for shim", e);
                    }
                }
            }
        };
        ContextCompat.registerReceiver(this, unifiedPushEndpointReceiver, new IntentFilter("com.appweb.webapk.NEW_ENDPOINT"), ContextCompat.RECEIVER_NOT_EXPORTED);

        if (edgeToEdge) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                float density = v.getResources().getDisplayMetrics().density;
                float top = insets.top / density;
                float bottom = insets.bottom / density;
                float left = insets.left / density;
                float right = insets.right / density;
                String js = String.format(java.util.Locale.US,
                    "document.documentElement.style.setProperty('--safe-area-inset-top', '%.2fpx');" +
                    "document.documentElement.style.setProperty('--safe-area-inset-bottom', '%.2fpx');" +
                    "document.documentElement.style.setProperty('--safe-area-inset-left', '%.2fpx');" +
                    "document.documentElement.style.setProperty('--safe-area-inset-right', '%.2fpx');" +
                    "document.dispatchEvent(new CustomEvent('WebToApkInsetsApplied'));",
                    top, bottom, left, right
                );
                webview.evaluateJavascript(js, null);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        boolean stateRestored = false;
        if (savedInstanceState != null) {
            stateRestored = webview.restoreState(savedInstanceState) != null;
        }
        if (!stateRestored) {
            webview.loadUrl(mainURL);
        }

        mediaActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && MediaPlaybackService.BROADCAST_MEDIA_ACTION.equals(intent.getAction())) {
                    String action = intent.getStringExtra(MediaPlaybackService.EXTRA_MEDIA_ACTION);
                    if (action != null) {
                        executeMediaActionInWebView(action);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mediaActionReceiver, new IntentFilter(MediaPlaybackService.BROADCAST_MEDIA_ACTION));
    }

    private void registerForUnifiedPush(final String vapidPublicKey) {
        if (vapidPublicKey == null || vapidPublicKey.isEmpty()) {
            return;
        }
        UnifiedPush.tryUseCurrentOrDefaultDistributor(this, new Function1<Boolean, Unit>() {
            @Override
            public Unit invoke(Boolean success) {
                if (success) {
                    UnifiedPush.register(MainActivity.this, INSTANCE_DEFAULT, null, vapidPublicKey);
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.push_distributor_required_title)
                            .setMessage(R.string.push_distributor_required_message)
                            .setPositiveButton(R.string.learn_more, (dialog, which) -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://unifiedpush.org/users/distributors/"));
                                startActivity(browserIntent);
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    });
                }
                return Unit.INSTANCE;
            }
        });
    }

    private void executeMediaActionInWebView(String action) {
        if (webview != null) {
            webview.post(() -> {
                String js = "if (typeof window.__runMediaAction === 'function') { window.__runMediaAction('" + action + "'); }";
                webview.evaluateJavascript(js, null);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unifiedPushEndpointReceiver != null) {
            unregisterReceiver(unifiedPushEndpointReceiver);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mediaActionReceiver);
        Intent intent = new Intent(this, MediaPlaybackService.class);
        stopService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
        if (webview != null) {
            webview.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webview != null) {
            webview.onResume();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            int action = ev.getActionMasked();
            int pointerCount = ev.getPointerCount();

            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_DOWN:
                    if (pointerCount == 4) {
                        isLongPressActive = true;
                        for (int i = 0; i < pointerCount; i++) {
                            int id = ev.getPointerId(i);
                            // Safety for Poco/Xiaomi weird IDs
                            if (id >= 0 && id < 10) {
                                initialTouchPoints[id][0] = ev.getX(i);
                                initialTouchPoints[id][1] = ev.getY(i);
                            }
                        }
                        longPressHandler.removeCallbacks(longPressRunnable);
                        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_THRESHOLD);
                    } else if (pointerCount > 4) {
                        cancelLongPress();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isLongPressActive) {
                        if (pointerCount == 4) {
                            for (int i = 0; i < pointerCount; i++) {
                                int id = ev.getPointerId(i);
                                if (id >= 0 && id < 10) {
                                    float dx = Math.abs(ev.getX(i) - initialTouchPoints[id][0]);
                                    float dy = Math.abs(ev.getY(i) - initialTouchPoints[id][1]);
                                    if (dx > MOVE_THRESHOLD || dy > MOVE_THRESHOLD) {
                                        cancelLongPress();
                                        break;
                                    }
                                }
                            }
                        } else {
                            cancelLongPress();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    cancelLongPress();
                    break;
            }
        } catch (Exception e) {
            cancelLongPress();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void cancelLongPress() {
        if (isLongPressActive) {
            longPressHandler.removeCallbacks(longPressRunnable);
            isLongPressActive = false;
        }
    }

    private void showUrlChangeDialog() {
        try {
            isLongPressActive = false;
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_url, null);
            final TextInputEditText input = dialogView.findViewById(R.id.urlInput);
            final View btnIconColor = dialogView.findViewById(R.id.btnIconColor);
            final View btnIconDark = dialogView.findViewById(R.id.btnIconDark);
            final View btnIconLight = dialogView.findViewById(R.id.btnIconLight);
            final View btnReloadPage = dialogView.findViewById(R.id.btnReloadPage);
            final SwitchMaterial switchBypassSsl = dialogView.findViewById(R.id.switchBypassSsl);
            final View pingIndicator = dialogView.findViewById(R.id.pingIndicator);
            final View pingHalo = dialogView.findViewById(R.id.pingHalo);
            final TextView pingText = dialogView.findViewById(R.id.pingText);
            
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            final String currentIcon = prefs.getString(KEY_SELECTED_ICON, "color");
            final boolean currentBypassSsl = prefs.getBoolean(KEY_BYPASS_SSL, false);
            final String[] pendingIcon = {currentIcon};

            if (input != null) {
                input.setText(mainURL);
                input.setSelection(mainURL.length());
            }
            if (switchBypassSsl != null) switchBypassSsl.setChecked(currentBypassSsl);

            if (btnIconDark != null && btnIconLight != null && btnIconColor != null) {
                updateIconSelectionUI(currentIcon, btnIconColor, btnIconDark, btnIconLight);
                btnIconColor.setOnClickListener(v -> { pendingIcon[0] = "color"; updateIconSelectionUI("color", btnIconColor, btnIconDark, btnIconLight); });
                btnIconDark.setOnClickListener(v -> { pendingIcon[0] = "dark"; updateIconSelectionUI("dark", btnIconColor, btnIconDark, btnIconLight); });
                btnIconLight.setOnClickListener(v -> { pendingIcon[0] = "light"; updateIconSelectionUI("light", btnIconColor, btnIconDark, btnIconLight); });
            }

            final AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.ModernOledDialog)
                .setView(dialogView)
                .setPositiveButton("Save & Reload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (input == null) return;
                        String newUrl = input.getText().toString().trim();
                        boolean newBypassSsl = switchBypassSsl != null && switchBypassSsl.isChecked();
                        if (newBypassSsl != currentBypassSsl) {
                            bypassSSL = newBypassSsl;
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_BYPASS_SSL, bypassSSL).apply();
                        }
                        boolean iconChanged = !pendingIcon[0].equals(currentIcon);
                        if (iconChanged) {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_SELECTED_ICON, pendingIcon[0]).apply();
                            applyIconChange(pendingIcon[0]);
                        }
                        if (!newUrl.isEmpty()) {
                            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) newUrl = "https://" + newUrl;
                            mainURL = newUrl;
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_MAIN_URL, mainURL).apply();
                            userScriptManager = new UserScriptManager(MainActivity.this, mainURL);
                            CookieManager.getInstance().flush();
                            
                            // Reset state and clear error screen for fresh load
                            if (!iconChanged) {
                                errorOccurred = false;
                                if (errorLayout != null) errorLayout.setVisibility(View.GONE);
                                webview.setVisibility(View.VISIBLE);
                                webview.loadUrl(mainURL);
                                Toast.makeText(MainActivity.this, "URL updated and reloading...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

            if (btnReloadPage != null) {
                btnReloadPage.setOnClickListener(v -> {
                    if (webview != null) {
                        webview.reload();
                    }
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Reloading page...", Toast.LENGTH_SHORT).show();
                });
            }

            // Perform Robust Ping
            if (pingIndicator != null && pingText != null && pingHalo != null) {
                checkServerResponse(mainURL, pingIndicator, pingHalo, pingText);
            }

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error opening parameters", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkServerResponse(String urlStr, View indicator, View halo, TextView text) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            long startTime = System.currentTimeMillis();
            boolean success = false;
            long responseTime = -1;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                
                // If SSL bypass is enabled, configure the connection to trust any certificate for the ping
                if (connection instanceof HttpsURLConnection && bypassSSL) {
                    HttpsURLConnection httpsConn = (HttpsURLConnection) connection;
                    TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() { return null; }
                            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                        }
                    };
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    httpsConn.setSSLSocketFactory(sc.getSocketFactory());
                    httpsConn.setHostnameVerifier((hostname, session) -> true);
                }

                // Spoof standard browser to avoid blocking
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // Increased timeout for slow local servers
                connection.setReadTimeout(5000);
                connection.setInstanceFollowRedirects(true);
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                // Any response (even error codes like 404 or 403) means the server is reachable
                if (responseCode != -1) {
                    responseTime = System.currentTimeMillis() - startTime;
                    success = true;
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e("WebToApk", "Ping failed: " + e.getMessage());
                success = false;
            }

            final boolean finalSuccess = success;
            final long finalTime = responseTime;
            handler.post(() -> {
                if (finalSuccess) {
                    text.setText(finalTime + " ms");
                    int statusColor;
                    if (finalTime < 150) statusColor = Color.parseColor("#00C853"); // Material Green
                    else if (finalTime < 600) statusColor = Color.parseColor("#FFAB00"); // Material Amber/Orange
                    else statusColor = Color.parseColor("#D50000"); // Material Red
                    
                    indicator.getBackground().setTint(statusColor);
                    halo.getBackground().setTint(statusColor);
                    startPulseAnimation(halo);
                } else {
                    text.setText("Offline");
                    indicator.getBackground().setTint(Color.GRAY);
                    halo.setVisibility(View.GONE);
                }
            });
        });
    }

    private void startPulseAnimation(View halo) {
        halo.setVisibility(View.VISIBLE);
        ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(
            halo,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 2.5f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 2.5f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0.6f, 0f)
        );
        pulse.setDuration(1500);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.start();
    }

    private void updateIconSelectionUI(String selected, View vColor, View vDark, View vLight) {
        vColor.setBackgroundResource(selected.equals("color") ? R.drawable.icon_selector_border : 0);
        vDark.setBackgroundResource(selected.equals("dark") ? R.drawable.icon_selector_border : 0);
        vLight.setBackgroundResource(selected.equals("light") ? R.drawable.icon_selector_border : 0);
    }

    private void applyIconChange(String iconName) {
        PackageManager pm = getPackageManager();
        String activeAlias;
        List<String> inactiveAliases = new ArrayList<>(Arrays.asList(ALIAS_DARK, ALIAS_LIGHT, ALIAS_COLOR));
        switch (iconName) {
            case "color": activeAlias = ALIAS_COLOR; break;
            case "dark": activeAlias = ALIAS_DARK; break;
            default: activeAlias = ALIAS_LIGHT; break;
        }
        inactiveAliases.remove(activeAlias);
        pm.setComponentEnabledSetting(new android.content.ComponentName(this, activeAlias), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        for (String alias : inactiveAliases) {
            pm.setComponentEnabledSetting(new android.content.ComponentName(this, alias), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        Toast.makeText(this, "Applying changes... App will close.", Toast.LENGTH_LONG).show();
    }

    private void initMainURL() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String defaultUrl = getString(R.string.default_url);
        mainURL = prefs.getString(KEY_MAIN_URL, defaultUrl);
        bypassSSL = prefs.getBoolean(KEY_BYPASS_SSL, false);
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && data != null) {
            mainURL = data.toString();
        }
    }

    private void checkAndRequestLocationPermission() {
        if (geolocationEnabled) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webview.saveState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = grantResults.length > 0;
        for (int result : grantResults) if (result != PackageManager.PERMISSION_GRANTED) isGranted = false;

        if (requestCode == MEDIA_PERMISSION_REQUEST_CODE) {
            if (currentPermissionRequest != null) { if (isGranted) currentPermissionRequest.grant(currentPermissionRequest.getResources()); else currentPermissionRequest.deny(); currentPermissionRequest = null; }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (geoCallback != null) {
                boolean hasAnyLocationGranted = false;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        hasAnyLocationGranted = true;
                        break;
                    }
                }
                if (hasAnyLocationGranted) geoCallback.invoke(geoOrigin, true, true); else geoCallback.invoke(geoOrigin, false, false);
                geoCallback = null; geoOrigin = null;
            }
        }
    }

    private class CustomWebChrome extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return true;
        }
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
            new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm()).setCancelable(false).create().show();
            return true;
        }
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm()).setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel()).setCancelable(false).create().show();
            return true;
        }
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            final EditText input = new EditText(MainActivity.this); input.setText(defaultValue);
            new AlertDialog.Builder(MainActivity.this).setMessage(message).setView(input).setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm(input.getText().toString())).setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel()).setCancelable(false).create().show();
            return true;
        }
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (!geolocationEnabled) { callback.invoke(origin, false, false); return; }
            boolean fineGranted = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarseGranted = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!fineGranted && !coarseGranted) {
                geoCallback = callback;
                geoOrigin = origin;
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                callback.invoke(origin, true, true);
            }
        }
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;
        @Override
        public void onHideCustomView() {
            ((FrameLayout)getWindow().getDecorView()).removeView(mCustomView); mCustomView = null; getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility); setRequestedOrientation(mOriginalOrientation); mCustomViewCallback.onCustomViewHidden(); mCustomViewCallback = null;
        }
        @Override
        public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
            if (mCustomView != null) { onHideCustomView(); return; }
            mCustomView = view; mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility(); mOriginalOrientation = getRequestedOrientation(); mCustomViewCallback = callback;
            ((FrameLayout)getWindow().getDecorView()).addView(mCustomView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
            mFilePathCallback = filePathCallback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT); intent.addCategory(Intent.CATEGORY_OPENABLE); intent.setType("image/*");
            String[] acceptTypes = fileChooserParams.getAcceptTypes();
            if (acceptTypes.length > 0 && acceptTypes[0] != null && !acceptTypes[0].isEmpty()) { if (acceptTypes[0].contains("image")) intent.setType("image/*"); else intent.setType("*/*"); }
            if (fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            try { fileChooserLauncher.launch(Intent.createChooser(intent, "Select File")); } catch (ActivityNotFoundException e) { mFilePathCallback = null; return false; }
            return true;
        }
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            for (String resource : request.getResources()) { if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource) && !cameraEnabled) { request.deny(); return; } if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource) && !microphoneEnabled) { request.deny(); return; } }
            List<String> permissionsNeeded = new ArrayList<>();
            for (String resource : request.getResources()) { if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) { if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) permissionsNeeded.add(Manifest.permission.CAMERA); } if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) { if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) permissionsNeeded.add(Manifest.permission.RECORD_AUDIO); } }
            if (permissionsNeeded.isEmpty()) request.grant(request.getResources()); else { currentPermissionRequest = request; ActivityCompat.requestPermissions(MainActivity.this, permissionsNeeded.toArray(new String[0]), MEDIA_PERMISSION_REQUEST_CODE); }
        }
        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) { super.onPermissionRequestCanceled(request); currentPermissionRequest = null; }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            if (bypassSSL) { handler.proceed(); return; }
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); builder.setMessage(R.string.notification_error_ssl_cert_invalid);
            builder.setPositiveButton("continue", (dialog, which) -> handler.proceed());
            builder.setNegativeButton("cancel", (dialog, which) -> handler.cancel());
            builder.create().show();
        }
        @Override
        public void onReceivedHttpAuthRequest(final WebView view, final android.webkit.HttpAuthHandler handler, String host, String realm) {
            final View dialogView = getLayoutInflater().inflate(R.layout.auth_dialog, null);
            final EditText usernameInput = dialogView.findViewById(R.id.username);
            final EditText passwordInput = dialogView.findViewById(R.id.password);
            new AlertDialog.Builder(MainActivity.this).setTitle("Authentication Required").setView(dialogView).setPositiveButton("OK", (dialog, which) -> handler.proceed(usernameInput.getText().toString(), passwordInput.getText().toString())).setNegativeButton("Cancel", (dialog, which) -> handler.cancel()).show();
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                if (allowOpenMobileApp) {
                    if (confirmOpenExternalApp) {
                        new AlertDialog.Builder(view.getContext()).setTitle(R.string.external_link).setMessage(R.string.open_in_external_app).setPositiveButton(android.R.string.yes, (dialog, which) -> { try { view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { } }).setNegativeButton(android.R.string.no, null).show();
                    } else { try { view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { } }
                }
                return true;
            }
            String urlDomain = request.getUrl().getHost();
            String mainDomain = Uri.parse(mainURL).getHost();
            if (urlDomain == null || mainDomain == null) return handleExternalLink(url, view);
            boolean isInternalLink = allowSubdomains ? (urlDomain.endsWith(mainDomain) || mainDomain.endsWith(urlDomain)) : urlDomain.equals(mainDomain);
            if (isInternalLink) return false;
            return handleExternalLink(url, view);
        }
        private boolean handleExternalLink(String url, WebView view) {
            if (!enableExternalLinks) return true;
            if (openExternalLinksInBrowser) {
                if (confirmOpenInBrowser) { new AlertDialog.Builder(view.getContext()).setTitle(R.string.external_link).setMessage(R.string.open_in_browser).setPositiveButton(android.R.string.yes, (dialog, which) -> view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)))).setNegativeButton(android.R.string.no, null).show(); return true; }
                else { view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true; }
            }
            return false;
        }
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String host = request.getUrl().getHost();
            if (blockLocalhostRequests && ("127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host) || "::1".equals(host))) return new WebResourceResponse("text/plain", "UTF-8", null);
            return super.shouldInterceptRequest(view, request);
        }
        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon) { super.onPageStarted(webview, url, favicon); userScriptManager.injectScripts(webview, url); }
        @Override
        public void onPageFinished(WebView webview, String url) {
            if (!errorOccurred) {
                if (webview.getAlpha() == 0f) {
                    webview.animate().alpha(1f).setDuration(fadeInDuration).start();
                }
            }
            super.onPageFinished(webview, url);
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) {
                errorOccurred = true;
                if (errorLayout != null) {
                    errorLayout.setVisibility(View.VISIBLE);
                    webview.setVisibility(View.GONE);
                    if (showDetailsOnErrorScreen) {
                        TextView errorTextView = errorLayout.findViewById(R.id.errorText);
                        if (errorTextView != null) errorTextView.setText(error.getDescription());
                    }
                }
            }
        }
        @Override
        public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
            if (webview != null) { ((ViewGroup)webview.getParent()).removeView(webview); webview.destroy(); webview = null; }
            finish(); startActivity(getIntent()); return true;
        }
    }

    public void tryAgain(View v) {
        errorOccurred = false;
        if (errorLayout != null) {
            errorLayout.setVisibility(View.GONE);
        }
        webview.setVisibility(View.VISIBLE);
        webview.setAlpha(0f);
        webview.reload();
    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (webview != null && webview.canGoBack()) {
            webview.goBack();
        } else {
            if (doubleBackToExitPressedOnce || !requireDoubleBackToExit) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.exit_app, Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    private class WebAppInterface {
        private Context context;
        WebAppInterface(Context context) { this.context = context; }
        @JavascriptInterface
        public void getNativeLocation(String successCallbackJs, String errorCallbackJs) {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager == null) {
                        if (webview != null) webview.evaluateJavascript(errorCallbackJs + "({code: 2, message: 'Location service unavailable'});", null);
                        return;
                    }
                    boolean isGpsEnabled = false;
                    boolean isNetworkEnabled = false;
                    try { isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); } catch (Exception ignored) {}
                    try { isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); } catch (Exception ignored) {}

                    if (!isGpsEnabled && !isNetworkEnabled) {
                        if (webview != null) webview.evaluateJavascript(errorCallbackJs + "({code: 2, message: 'Position unavailable - Location services disabled on device'});", null);
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (webview != null) webview.evaluateJavascript(errorCallbackJs + "({code: 1, message: 'Permission denied'});", null);
                        return;
                    }

                    Location lastKnownLocation = null;
                    if (isGpsEnabled) {
                        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    if (lastKnownLocation == null && isNetworkEnabled) {
                        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                    if (lastKnownLocation != null) {
                        sendLocationToJs(lastKnownLocation, successCallbackJs);
                    } else {
                        LocationListener locationListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                sendLocationToJs(location, successCallbackJs);
                                try { locationManager.removeUpdates(this); } catch (SecurityException ignored) {}
                            }
                            @Override public void onProviderEnabled(@NonNull String provider) {}
                            @Override public void onProviderDisabled(@NonNull String provider) {}
                        };
                        if (isGpsEnabled) {
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper());
                        } else if (isNetworkEnabled) {
                            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.getMainLooper());
                        }
                    }
                } catch (Exception e) {
                    Log.e("WebToApk", "Error getting location", e);
                    if (webview != null) webview.evaluateJavascript(errorCallbackJs + "({code: 2, message: '" + e.getMessage() + "'});", null);
                }
            });
        }

        private void sendLocationToJs(Location location, String successCallbackJs) {
            if (webview == null) return;
            long time = location.getTime();
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            float accuracy = location.getAccuracy();
            double altitude = location.getAltitude();
            float heading = location.getBearing();
            float speed = location.getSpeed();

            String js = String.format(java.util.Locale.US,
                "%s({coords: {latitude: %.8f, longitude: %.8f, accuracy: %.2f, altitude: %s, heading: %s, speed: %s}, timestamp: %d});",
                successCallbackJs, lat, lng, accuracy,
                location.hasAltitude() ? String.format(java.util.Locale.US, "%.2f", altitude) : "null",
                location.hasBearing() ? String.format(java.util.Locale.US, "%.2f", heading) : "null",
                location.hasSpeed() ? String.format(java.util.Locale.US, "%.2f", speed) : "null",
                time
            );
            webview.evaluateJavascript(js, null);
        }

        @JavascriptInterface
        public void showShortToast(String message) { new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show()); }
        @JavascriptInterface
        public void showLongToast(String message) { new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show()); }
        @JavascriptInterface
        public boolean hasNotificationPermission() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED; return true; }
        @JavascriptInterface
        public void requestNotificationPermission() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) new Handler(Looper.getMainLooper()).post(() -> { if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE); }); }
        @JavascriptInterface
        public void showNotification(String title, String message) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Intent intent = new Intent(context, MainActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(message).setPriority(NotificationCompat.PRIORITY_DEFAULT).setContentIntent(pendingIntent).setAutoCancel(true);
            NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
        }
        @JavascriptInterface
        public void share(String title, String text, String url) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND); shareIntent.setType("text/plain"); shareIntent.putExtra(Intent.EXTRA_SUBJECT, title); shareIntent.putExtra(Intent.EXTRA_TEXT, (text != null ? text : "") + (url != null ? "\n" + url : ""));
            context.startActivity(Intent.createChooser(shareIntent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        @JavascriptInterface
        public void unifiedPushSubscribe(String vapidPublicKey) { new Handler(Looper.getMainLooper()).post(() -> MainActivity.this.registerForUnifiedPush(vapidPublicKey)); }
        @JavascriptInterface
        public void unifiedPushUnregister() { UnifiedPush.unregister(context, INSTANCE_DEFAULT); }
        @JavascriptInterface
        public String getUnifiedPushSubscriptionJson() {
            SharedPreferences prefs = context.getSharedPreferences("unifiedpush", Context.MODE_PRIVATE); String endpoint = prefs.getString("endpoint_" + INSTANCE_DEFAULT, null); String p256dh = prefs.getString("p256dh_" + INSTANCE_DEFAULT, null); String auth = prefs.getString("auth_" + INSTANCE_DEFAULT, null);
            if (endpoint == null) return "";
            try { JSONObject keys = new JSONObject(); keys.put("p256dh", p256dh); keys.put("auth", auth); JSONObject sub = new JSONObject(); sub.put("endpoint", endpoint); sub.put("keys", keys); return sub.toString(); } catch (Exception e) { return ""; }
        }
        @JavascriptInterface
        public String getNotificationPermissionState() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ? "granted" : "prompt"; return "granted"; }
        @JavascriptInterface
        public void updateMediaMetadata(String title, String artist, String album, @Nullable String artworkUrl) { Intent intent = new Intent(context, MediaPlaybackService.class); intent.setAction(MediaPlaybackService.ACTION_UPDATE_METADATA); intent.putExtra("title", title); intent.putExtra("artist", artist); intent.putExtra("album", album); intent.putExtra("artworkUrl", artworkUrl); context.startService(intent); }
        @JavascriptInterface
        public void updateMediaPlaybackState(String state) { Intent intent = new Intent(context, MediaPlaybackService.class); intent.setAction(MediaPlaybackService.ACTION_UPDATE_STATE); intent.putExtra("state", state); context.startService(intent); }
        @JavascriptInterface
        public void setMediaActionHandlers(String[] actions) { Intent intent = new Intent(context, MediaPlaybackService.class); intent.setAction(MediaPlaybackService.ACTION_SET_HANDLERS); intent.putExtra("actions", actions); context.startService(intent); }
        @JavascriptInterface
        public void updateMediaPositionState(double duration, double playbackRate, double position) { Intent intent = new Intent(context, MediaPlaybackService.class); intent.setAction(MediaPlaybackService.ACTION_UPDATE_POSITION); intent.putExtra("duration", duration); intent.putExtra("playbackRate", playbackRate); intent.putExtra("position", position); context.startService(intent); }
        @JavascriptInterface
        public void clearAppCache() { new Handler(Looper.getMainLooper()).post(() -> { if (webview != null) webview.clearCache(true); }); }
    }
}
