package com.example.TeleprompterAndroid;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static android.app.Activity.RESULT_OK;
import static com.example.TeleprompterAndroid.Consts.CREATE_HTML_FILE;
import static com.example.TeleprompterAndroid.Consts.FILE_NAME;
import static com.example.TeleprompterAndroid.Consts.FILE_SCRIPT;
import static com.example.TeleprompterAndroid.Consts.PICK_HTML_FILE;
import static com.example.TeleprompterAndroid.Consts.PICK_JPEG_FILE;
import static com.example.TeleprompterAndroid.Consts.PICK_JPEG_FILE_FOR_CROPPING;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FileHelper {

    private Activity activity;
    private Uri finalUri;
    private String fileName;

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

    public void createFile(String fileName) {
        if (isStoragePermissionGranted()) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            activity.startActivityForResult(intent, CREATE_HTML_FILE);
        }
    }

    public void openNewAvatar() {
        if (isStoragePermissionGranted()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            activity.startActivityForResult(intent, PICK_JPEG_FILE_FOR_CROPPING);
        }
    }

    public String getFileName() {
        return fileName;
    }

    private String getScriptFromUri(Uri uri) {
        String content;
        try {
            ContentResolver contentResolver = activity.getContentResolver();
            InputStream in = contentResolver.openInputStream(uri);
            Cursor cursor = contentResolver.query(uri, new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst())
                {
                    cursor.moveToFirst();
                    fileName = cursor.getString(1);
                }
            } catch(Exception e)
            {
                e.printStackTrace();
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            content = total.toString();
            r.close();
        } catch (Exception e) {content = e.toString();}
        return content;
    }

    private Bitmap getImageFromUri(Uri uri) throws IOException {
        return MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
    }

    public static void writeScriptToUri(String script, Uri uri, Activity activity) {
        try {
            ContentResolver contentResolver = activity.getContentResolver();
            OutputStream out = contentResolver.openOutputStream(uri);
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));
            w.write(script);
            w.close();
        } catch (Exception e) {Toast.makeText(activity.getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();}
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
        if (requestCode == PICK_HTML_FILE && resultCode == RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.

            Uri uri;

            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.

                finalUri = uri;

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
                    m.what = requestCode;
                    handler.sendMessage(m);
                }); thread.start();
            }
        } else if (requestCode == CREATE_HTML_FILE && resultCode == RESULT_OK) {
            Uri uri;

            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.

                finalUri = uri;

                Message message = new Message();
                message.what = requestCode;
                handler.sendMessage(message);
            }
        } else if (requestCode == PICK_JPEG_FILE && resultCode == RESULT_OK) {
            Uri uri;

            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.

                finalUri = uri;

                Message message = new Message();
                message.what = requestCode;
                new Thread(() -> {
                    try {
                        message.obj = getImageFromUri(finalUri);
                        handler.sendMessage(message);
                    } catch (Exception ignored) {}
                }).start();
            }
        } else if (requestCode == PICK_JPEG_FILE_FOR_CROPPING && resultCode == RESULT_OK) {
            Uri uri;

            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.

                finalUri = uri;

                Message message = new Message();
                message.what = requestCode;
                new Thread(() -> {
                    try {
                        message.obj = finalUri;
                        handler.sendMessage(message);
                    } catch (Exception ignored) {}
                }).start();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(resultData);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Message message = new Message();
                message.what = requestCode;
                try {
                    message.obj = getImageFromUri(resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.sendMessage(message);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(activity.getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Uri getFinalUri () { return finalUri; }

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

    public Bundle getArguments(Message message) {
        Bundle bundle = (Bundle) message.obj;
        String fileName = bundle.getString(FILE_NAME);
        if (fileName.equals(activity.getString(R.string.no_file))) {
            Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.no_file), Toast.LENGTH_SHORT).show();
            return null;
        }
        bundle.putString(FILE_NAME, getFileName());
        return bundle;
    }

    public String readContentFromFile(File file) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuilder total = new StringBuilder();
        for (String line; (line = r.readLine()) != null; ) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    public static InputStream writeContentToInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(UTF_8));
    }
}
