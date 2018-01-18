package com.example.cjnote.ae4teamapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ChangePhoneNumberActivity extends AppCompatActivity {
    private String TAG = "changeNumber Activity";
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthCredential credent;
    private boolean mVerificationInProgress = false;
    private FirebaseAuth mAuth;
    private TextView usernumber;
    private TextView authnumber;
    private String phonNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone_number);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        usernumber = findViewById(R.id.phonNumberTextView);
        authnumber = findViewById(R.id.authorizationNumberTextView);

        Log.i(TAG, "number:" + mAuth.getCurrentUser().getPhoneNumber());
        phoneNumberVerificationCB();

        findViewById(R.id.requestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phonNumber = usernumber.getText().toString();
                phoneNumberCheck();

                if (TextUtils.isEmpty(phonNumber)) {
                    usernumber.setError("Cannot be empty.");
                } else {
                    startPhoneNumberVerification(phonNumber);
                }

            }
        });

        findViewById(R.id.changeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numbercheck = authnumber.getText().toString();
                if (numbercheck.isEmpty()) {
                    authnumber.setError("Cannot be empty.");
                } else if (credent.getSmsCode().equals(numbercheck)) {
                    link();
                } else {
                    authnumber.setError("Disscorrect.");
                }

            }
        });


    }

    @Override
    public void onBackPressed() {
        IntentBack();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void IntentBack() {
        finish();
        overridePendingTransition(R.anim.leftin_activity, R.anim.rightout_activity);
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
        mVerificationInProgress = true;
    }

    private void phoneNumberVerificationCB() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                credent = credential;
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
            }
        };
    }

    private void link() {
        if (phoneNumberCheck() == true) {
            mAuth.getCurrentUser().linkWithCredential(credent)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "linkWithCredential:success");
                                Toast.makeText(ChangePhoneNumberActivity.this, "변경되었습니다", Toast.LENGTH_SHORT).show();
                                IntentBack();
                            } else {
                                Log.w(TAG, "linkWithCredential:failure", task.getException());
                                Toast.makeText(ChangePhoneNumberActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            // ...
                        }
                    });
        } else {
            Toast.makeText(ChangePhoneNumberActivity.this, "이미 등록되어있는 번호입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //번호 끝 8자리구하기
    private boolean phoneNumberCheck() {
        if (mAuth.getCurrentUser().getPhoneNumber() != null) {
            return true;
        }
        String currentnumber = mAuth.getCurrentUser().getPhoneNumber();
        String minicurrentNB = currentnumber.substring(3, currentnumber.length());
        String miniphoneNB = phonNumber.substring(1, phonNumber.length());

        if (minicurrentNB.equals(miniphoneNB)) {
            return false;
        }
        return true;
    }
}
