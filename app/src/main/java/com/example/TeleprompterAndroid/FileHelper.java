package com.example.TeleprompterAndroid;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.example.TeleprompterAndroid.Consts.FILE_NAME;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.PICK_HTML_FILE;

public class FileHelper {

    private Activity activity;

    public FileHelper (Activity activity) {
        this.activity = activity;
    }

    public void openFile() {
        if (isStoragePermissionGranted()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");

            activity.startActivityForResult(intent, PICK_HTML_FILE);
        }
    }

    private String getScriptFromUri(Uri uri) {
        String content;
        try {
            InputStream in = activity.getContentResolver().openInputStream(uri);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            content = total.toString();
        } catch (Exception e) {content = e.toString();}
        return content;
    }

    private boolean isStoragePermissionGranted() {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("PERMISSION","Permission is granted");
            return true;
        } else {

            Log.v("PERMISSION","Permission is revoked");
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    public void sendFileToHandler (int requestCode, int resultCode, Intent resultData, Handler handler) {
        if (requestCode == PICK_HTML_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.

            Uri uri = null;

            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.

                Uri finalUri = uri;
                Thread thread = new Thread(() -> {
                    String filePath = finalUri.toString();
                    filePath = filePath.split(":")[1];
                    System.out.println(filePath);
                    Message m = new Message();
                    Bundle bundle = new Bundle();
                    try {
                        bundle.putString(FILE_SCRIPT, getScriptFromUri(finalUri));
                        String[] parts = filePath.split("/");
                        Log.d("MainActivity", parts[parts.length - 1]);
                        bundle.putString(FILE_NAME, parts[parts.length - 1]);
                    } catch (Exception e) {e.printStackTrace(); bundle.putString(FILE_SCRIPT, activity.getString(R.string.no_file)); bundle.putString(FILE_NAME, activity.getString(R.string.no_file)); Toast.makeText(activity.getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();}
                    m.obj = bundle;
                    handler.sendMessage(m);
                }); thread.start();
            }
        }
    }

    public Intent prepareIntent(Message message, int textSize, int speed) {
        Bundle bundle = (Bundle) message.obj;
        String fileName = bundle.getString(FILE_NAME);
        String fileScript = bundle.getString(FILE_SCRIPT);
        if (fileName.equals(activity.getString(R.string.no_file))) {
            Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.no_file), Toast.LENGTH_SHORT).show();
            return null;
        }
        return new Intent(activity.getApplicationContext(), WriteActivity.class).putExtra("SCRIPT", fileScript).putExtra("TEXTSIZE", Integer.toString(textSize)).putExtra("SPEED", Integer.toString(speed));
    }
}