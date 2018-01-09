package com.example.cjnote.ae4teamapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_FROM_ALBUM=0;
    private static final int PICK_FROM_CAMERA=1;
    private static final int CROP_FROM_IMAGE=2;
    private Uri mlmageCaptureUri;
    private ImageView iv_UserPhoto;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private TextView nicknameTextView;
    private TextView emailTextView;
    private TextView contackTextView;
    private TextView snsTextView;
    private String TAG = "dbCheck";
    private CollectionReference userColRef = FirebaseFirestore.getInstance().collection("user");
    private RadioButton womanbutton;
    private RadioButton manbutton;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private Bitmap bitmap;
    private ImageView psa;
    private StorageReference islandRef;
    private FirebaseUser user = mAuth.getCurrentUser();
    Calendar calendar;
    private int mYear;
    private int mMonth;
    private int mDay;
    static final int DATE_DIALOG_ID = 1;
    private TextView calendarView;
    DatePickerDialog datePickerDialog ;
    private SpannableStringBuilder snssps;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        psa = findViewById(R.id.userProfileFicture);
        womanbutton = findViewById(R.id.womanButton);
        manbutton = findViewById(R.id.manButton);
        contackTextView = findViewById(R.id.userContactInfoTextView);
        nicknameTextView = findViewById(R.id.userNickNameTextView);
        emailTextView = findViewById(R.id.userEmailTextView);
        snsTextView = findViewById(R.id.snsConnectTextView);
        iv_UserPhoto = this.findViewById(R.id.userProfileFicture);
        iv_UserPhoto.setOnClickListener(this);
        calendar = Calendar.getInstance();
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        calendarView = (TextView)findViewById(R.id.birthTextView);

        if ((mAuth.getCurrentUser() != null)) {
            userGenderCheck();
            psaSetting();
        } else {
            Intent loginintent = new Intent(UserProfileActivity.this, LoginActivity.class);
            startActivity(loginintent);
            finish();
        }
        findViewById(R.id.userContactInfoTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactinfointent = new Intent(UserProfileActivity.this,  ContactInfoActivity.class);
                startActivity(contactinfointent);
                finish();
                overridePendingTransition(R.anim.rightin_activity,R.anim.leftout_activity);
            }
        });
        findViewById(R.id.womanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeGenderButton(R.id.womanButton);
            }
        });
        findViewById(R.id.manButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeGenderButton(R.id.manButton);
            }
        });
        findViewById(R.id.birthTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
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

    public void IntentBack () {
        Intent test3Intent = new Intent(UserProfileActivity.this,  SettingActivity.class);
        startActivity(test3Intent);
        finish();
        overridePendingTransition(R.anim.leftin_activity,R.anim.rightout_activity);
    }

    public void changeGenderButton(int index) {

        Map<String, Object> userMap = new HashMap<>();
        switch (index) {
            case R.id.womanButton:
                userMap.put("gender", 0);
                break;
            case R.id.manButton:
                userMap.put("gender", 1);
                break;
        }
        userColRef.document(user.getUid())
                .update(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserProfileActivity.this, "변경되었습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    public void userGenderCheck() {
        userColRef.document(user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map userInfo = task.getResult().getData();
                            if (userInfo.get("gender") != null) {
                                int gendercheck = Integer.parseInt(userInfo.get("gender").toString());

                                textViewSetting();

                                switch (gendercheck) {
                                    case 0:
                                        womanbutton.setChecked(true);
                                        break;
                                    case 1:
                                        manbutton.setChecked(true);
                                        break;
                                }
                            } else {
                                textViewSetting();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    public void textViewSetting() {
        userColRef.document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map snap = documentSnapshot.getData();
//                        Users users = new Users(snap.get("email").toString(), snap.get("nickname").toString());

                        //텍스트뷰 세팅
                        nicknameTextView.setText(snap.get("nickname").toString());

                        final SpannableStringBuilder contactsps = new SpannableStringBuilder("연락처 정보\n휴대폰 번호, 카톡아이디");
                        contactsps.setSpan(new AbsoluteSizeSpan(45), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        contactsps.setSpan(new ForegroundColorSpan(Color.rgb(160,160,160)), 7, contactsps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        contactsps.setSpan(new StyleSpan(Typeface.BOLD),0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        contackTextView.setText(contactsps);

                        if(snap.get("email") != null){
                            final SpannableStringBuilder emailsps = new SpannableStringBuilder("이메일\n"+snap.get("email").toString());
                            emailsps.setSpan(new AbsoluteSizeSpan(45), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            emailsps.setSpan(new ForegroundColorSpan(Color.rgb(170,170,170)), 4, emailsps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            emailsps.setSpan(new StyleSpan(Typeface.BOLD),0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            emailTextView.setText(emailsps);
                        }

                        if(mAuth.getCurrentUser().getProviders() != null) {
                            List snscheck = mAuth.getCurrentUser().getProviders();

                            snssps = new SpannableStringBuilder( "SNS연동\n"+ snscheck.get(0));

                            snssps.setSpan(new AbsoluteSizeSpan(45), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            snssps.setSpan(new ForegroundColorSpan(Color.rgb(160,160,160)), 5, snssps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            snssps.setSpan(new StyleSpan(Typeface.BOLD),0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            snsTextView.setText(snssps);
                        }

                        if(snap.get("birth")!=null){
                            calendarView.setText("생년월일\n" + snap.get("birth").toString());
                        }
                    }
                });
    }

    public void psaSetting () {
        islandRef = storageRef.child("user").child(mAuth.getUid()).child("profile.jpg");
        Log.i(TAG, "islanRef: " + islandRef);
        Log.i(TAG, "psaSetting");
        final long ONE_MEGABYTE = 1024 * 1024;
        Log.i(TAG, "check: "+islandRef.getBytes(ONE_MEGABYTE));
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.i(TAG, "Success");
                // Data for "images/island.jpg" is returns, use this as needed
                try {
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    psa.setImageBitmap(bitmap);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "fail");
                // Handle any errors
            }
        });
    }

    @Override
    public void onClick(View v) {
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doTakePhotoAction();
            }
        };
        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doTakeAlbumAction();
            }
        };
        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        new AlertDialog.Builder(this)
                .setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("앨범선택", albumListener)
                .setNegativeButton("취소", cancelListener)
                .show();

    }

    //카메라로 사진 촬영하기
    public void doTakePhotoAction() {
        int permissionCheck = ContextCompat.checkSelfPermission(UserProfileActivity.this, Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(UserProfileActivity.this, new String[]{Manifest.permission.CAMERA},0);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String url = "profile.jpg";
            mlmageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,mlmageCaptureUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
            Log.i(TAG, "test1-camera");
        }
    }

    //앨범에서 사진 가져오기
    public void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
        Log.i(TAG, "test1-album");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == 0) {
                Toast.makeText(this, "카메라 권한이 승인됨", Toast.LENGTH_SHORT).show();
            } else {
                //권한 거절된 경우
                Toast.makeText(this, "카메라 권한이 거절 되었습니다. 카메라를 이용하려면 권한을 승낙하여야 합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, String.valueOf(requestCode));
        Log.i(TAG, String.valueOf(resultCode));

        if(resultCode != RESULT_OK) {
            return;
        }

        switch(requestCode) {
            case PICK_FROM_ALBUM:
            {
                mlmageCaptureUri = data.getData();
                Log.i(TAG, "test2");
            }

            case PICK_FROM_CAMERA:
            {
                Log.i(TAG, "test3");

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mlmageCaptureUri, "image/*");

                intent.putExtra("outputX", 100);
                intent.putExtra("outputY", 100);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_IMAGE);
                break;
            }

            case CROP_FROM_IMAGE:
            {
                if(resultCode != RESULT_OK) {
                    return;
                }
                Log.i(TAG, "test4");
                final Bundle extras = data.getExtras();
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/temp/"+System.currentTimeMillis()+".jpg";

                if(extras != null) {
                    bitmap = extras.getParcelable("data");
                    iv_UserPhoto.setImageBitmap(bitmap);
                    imageUpload();
                    storeCropImage(bitmap, filePath);
                    break;
                }

                //임시파일 삭제
                File f = new File(mlmageCaptureUri.getPath());
                if(f.exists()) {
                    f.delete();
                }
                break;
            }
        }
    }

    private void storeCropImage(Bitmap bitmap, String filePath) {
        File copyFile = new File(filePath);
        BufferedOutputStream out = null;
        Log.i(TAG, "test5");

        try {
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Log.i(TAG, "test6");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void imageUpload () {
        Log.i(TAG, "imageupload");
        StorageReference mountainsRef = storageRef.child("user").child(mAuth.getUid()).child("profile.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG, String.valueOf(downloadUrl));
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {

            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }
    private void updateBirth() {
        calendarView.setText("생년월일\n" + mYear + "년 " + (mMonth+1) + "월 " + mDay + "일");
    }
    private void uploadBirth() {
        Map<String, Object> birthMap = new HashMap<>();
        birthMap.put("birth",mYear + "년 " + (mMonth+1) + "월 " + mDay + "일");
        userColRef.document(user.getUid())
                .update(birthMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserProfileActivity.this, "변경되었습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    uploadBirth();
                    updateBirth();
                    Log.i(TAG, "hi");
                }
            };
}




