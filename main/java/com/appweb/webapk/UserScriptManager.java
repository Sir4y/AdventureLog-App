package com.appweb.webapk;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.webkit.WebView;
import android.util.Log;


public class UserScriptManager {
    private List<UserScript> userScripts = new ArrayList<>();
    private Context context;

    private static class UserScript {
        String name;
        String code;
        List<String> matches = new ArrayList<>();
        String runAt = "document-end"; 

        private static final List<String> VALID_RUN_AT = Arrays.asList("document-start", "document-body", "document-end", "document-idle");

        boolean matchesUrl(String url) {
            if (matches.isEmpty()) return true;
            for (String pattern : matches) {
                String regex = pattern.replace(".", "\\.").replace("*", ".*");
                if (url.matches(regex)) return true;
            }
            return false;
        }
    }

    public UserScriptManager(Context context, String mainURL) {
        this.context = context;
        loadUserScripts(mainURL + "/");
    }

    private void loadUserScripts(String mainUrl) {
        try {
            String[] files = context.getAssets().list("userscripts");
            if (files == null) return;
            for (String filename : files) {
                if (!filename.endsWith(".js")) continue;
                UserScript script = new UserScript();
                script.name = filename;
                try (InputStream input = context.getAssets().open("userscripts/" + filename);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                    StringBuilder code = new StringBuilder();
                    String line;
                    boolean inMetadata = false;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("==UserScript==")) inMetadata = true;
                        if (inMetadata) code.append("//"+line+"\n"); else code.append(line).append("\n");
                        if (line.contains("==/UserScript==")) inMetadata = false;
                        if (inMetadata) {
                            if (line.trim().startsWith("// @match")) script.matches.add(line.substring(line.indexOf("@match") + 6).trim());
                            if (line.trim().startsWith("// @run-at")) {
                                String runAt = line.substring(line.indexOf("@run-at") + 7).trim();
                                if (UserScript.VALID_RUN_AT.contains(runAt)) script.runAt = runAt;
                            }
                        }
                    }
                    script.code = code.toString();
                    userScripts.add(script);
                }
            }
        } catch (IOException e) { }
    }

    private String readAssetFile(String assetPath) {
        try (InputStream input = context.getAssets().open(assetPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        } catch (IOException e) { return ""; }
    }

    public void injectScripts(WebView webview, String url) {
        webview.evaluateJavascript(readAssetFile("helpers.js"), null);
        for (UserScript script : userScripts) {
            if (script.matchesUrl(url)) {
                String js;
                switch (script.runAt) {
                    case "document-start": js = script.code + "\n//# sourceURL=" + script.name; break;
                    case "document-body": js = "waitForBody().then(() => { " + script.code + "\n});\n" + "//# sourceURL=" + script.name; break;
                    case "document-idle": js = "runAtDocumentEnd(function() {" + "    setTimeout(() => {" + "        " + script.code + "\n" + "    }, 5);\n" + "});\n" + "//# sourceURL=" + script.name; break;
                    default: js = "runAtDocumentEnd(function() { " + script.code + "\n" + "});\n" + "//# sourceURL=" + script.name; break;
                }
                webview.evaluateJavascript(js, null);
            }
        }
    }
}
