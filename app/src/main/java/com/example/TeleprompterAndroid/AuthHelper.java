package com.example.TeleprompterAndroid;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.example.TeleprompterAndroid.Consts.IS_AUTHED;

public class AuthHelper {

    private Activity activity;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    public AuthHelper (Activity activity) {
        this.activity = activity;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public boolean isPasswordCorrect (String password) {
        boolean statement = (!password.contains(" ")) && password.length() > 5 && password.length() < 15;
        if (!statement) Toast.makeText(activity.getApplicationContext(), "Password shouldn't contain spaces and should be more than 5 and less than 15 syms long", Toast.LENGTH_SHORT).show();
        return statement;
    }

    public boolean isEmailCorrect(String email) {
        String[] parts = email.split("@");
        boolean statement = parts.length == 2 && parts[1].contains(".");
        if (!statement) Toast.makeText(activity.getApplicationContext(), "Enter correct email address", Toast.LENGTH_SHORT).show();
        return statement;
    }

    public void registerUser(String nameString, String emailString, String passString) {
        firebaseAuth.createUserWithEmailAndPassword(emailString, passString)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseAuth", "signUpWithEmail:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        saveUserData(user.getUid(), nameString, emailString);
                        activity.startActivity(new Intent(activity.getApplicationContext(), NewMainActivity.class).putExtra(IS_AUTHED, true));
                    } else {
                        Log.w("FirebaseAuth", "signUpWithEmail:failure", task.getException());
                        Toast.makeText(activity.getApplicationContext(), "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loginUser(String emailString, String passString) {
        firebaseAuth.signInWithEmailAndPassword(emailString, passString)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseAuth", "signInWithEmail:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        activity.startActivity(new Intent(activity.getApplicationContext(), NewMainActivity.class).putExtra(IS_AUTHED, true));
                    } else {
                        Log.w("FirebaseAuth", "signInWithEmail:failure", task.getException());
                        Toast.makeText(activity.getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public String getCurrentProfileName (DataSnapshot snapshot) { return snapshot.getValue(String.class); }

    public void saveUserData (String userId, String name, String email) {getUserReference().setValue(new User(email, name)); }

    public String getUId() {return firebaseAuth.getCurrentUser().getUid();}

    public DatabaseReference getDatabaseReference () {return databaseReference;}

    public DatabaseReference getNameReference () {return getUserReference().child("name");}

    public DatabaseReference getSettingsReference () {return getUserReference().child("settings");}

    public DatabaseReference getUserReference () {return getDatabaseReference().child("users").child(getUId());}

    public void saveSpeed (int progress) {getSettingsReference().child("speed").setValue(progress);}

    public void saveTextSize (int textSize) {getSettingsReference().child("textSize").setValue(textSize);}

    public int getSpeedFromSnapshot(DataSnapshot snapshot) {
        try {
            return snapshot.child("speed").getValue(Integer.class);
        } catch (Exception e) {
            return -1;
        }
    }

    public int getTextSizeFromSnapshot(DataSnapshot snapshot) {
        try {
            return snapshot.child("textSize").getValue(Integer.class);
        } catch (Exception e) {
            return -1;
        }
    }

    public StorageReference getFileReference(String fileName) {
        return storageReference.child(getUId()).child(fileName);
    }

    public void uploadFile (String fileName) {

    }
}
