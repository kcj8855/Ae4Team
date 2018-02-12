package com.example.cjnote.ae4teamapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {

    final Context context = this;
    private FirebaseAuth mAuth;
    private Button changeNumberBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        mAuth = FirebaseAuth.getInstance();

        // 유저 세션 없을시 로그인화면으로
        if (mAuth.getCurrentUser() == null) {
            Intent loginintent = new Intent(SettingActivity.this, LoginActivity.class);
            startActivity(loginintent);
            finish();
        }

        changeNumberBtn = (Button) findViewById(R.id.changeNumberButton);

        if (mAuth.getCurrentUser().getPhoneNumber() != null) {
            phoneNBSetting();
        }

        findViewById(R.id.userProfileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userProFileIntent = new Intent(SettingActivity.this, UserProfileActivity.class);
                startActivity(userProFileIntent);
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);
            }
        });

        findViewById(R.id.changeNumberButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changenumberIntent = new Intent(SettingActivity.this, ChangePhoneNumberActivity.class);
                startActivity(changenumberIntent);
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);
            }
        });

        findViewById(R.id.logOutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutClick();
            }
        });

    }

    @Override
    public void onBackPressed() {
        IntentBack();
    }

    public void IntentBack() {
        finish();
        overridePendingTransition(R.anim.leftin_activity, R.anim.rightout_activity);
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


    public void phoneNBSetting() {
        String userNB = mAuth.getCurrentUser().getPhoneNumber();
        if (userNB.length() > 6) {
            String miniuserNB = userNB.substring(userNB.length() - 4, userNB.length());
            changeNumberBtn.setText("연락처 변경(010-****-" + miniuserNB + ")");
        }
    }

    public void logoutClick() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        signOut();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("로그아웃");
        alertDialogBuilder.setMessage("로그아웃 시 30일 이상 경과된 번개톡 대화내용은 모두 삭제됩니다.\n\n로그아웃 하시겠습니까?");
        alertDialogBuilder.setPositiveButton("확인", dialogClickListener).setNegativeButton("취소", dialogClickListener).show();
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(SettingActivity.this, "로그아웃 되었습니다", Toast.LENGTH_LONG).show();
        finish();
    }
}
