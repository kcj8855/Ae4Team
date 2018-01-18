package com.example.cjnote.ae4teamapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowPostActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private String postId;
    private String userId;
    private ImageView postIV;
    private HashMap postData;
    private TextView titleTV;
    private TextView priceTV;
    private TextView contentTV;
    private TextView postInfoTV;
    private Button dibsBtn;
    private Boolean dibButtonFalg = true;
    private Button footerBtn;
    int width;
    int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            postId = null;
            Toast.makeText(this, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        postId = extras.getString("postId");

        postData = (HashMap) intent.getSerializableExtra("data");
        userId = postData.get("userId").toString();
        postIV = (ImageView) findViewById(R.id.postImageView);
        titleTV = (TextView) findViewById(R.id.titleTV);
        priceTV = (TextView) findViewById(R.id.priceTV);
        contentTV = (TextView) findViewById(R.id.contentTV);
        postInfoTV = (TextView) findViewById(R.id.postInfoTV);
        dibsBtn = (Button) findViewById(R.id.showPostDibsButton);

        setDibsImage();

        postIV.getLayoutParams().width = width;
        postIV.getLayoutParams().height = height / 2;
        postIV.setScaleType(ImageView.ScaleType.FIT_XY);


        dibsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dibButtonFalg) {
                    dibButtonFalg = false;
                    firestore.collection("dib").whereEqualTo("dibUserId", user.getUid()).whereEqualTo("postId", postId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() == 0) {
                                    postData.put("dibUserId", user.getUid());
                                    postData.put("postId", postId);
                                    postData.put("dibTimestamp", new Date().getTime());
                                    postData.put("dibNickname", user.getDisplayName());
                                    firestore.collection("dib").add(postData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dibsBtn.setBackgroundResource(R.drawable.favorite);
                                                Toast.makeText(ShowPostActivity.this, "찜목록에 추가됐습니다.", Toast.LENGTH_SHORT).show();
                                                dibButtonFalg = true;
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(ShowPostActivity.this, "이미 찜목록에 존재하는 상품입니다.", Toast.LENGTH_SHORT).show();
                                    dibButtonFalg = true;

                                }

                            }
                        }
                    });
                }

            }
        });

        // 상품 삭제 dialog
        final DialogInterface.OnClickListener deleteDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        firestore.collection("post").document(postId).delete();
                        storageRef.child("post").child("postId").delete();
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        // 상품 구매 dialog
        final DialogInterface.OnClickListener buyDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        firestore.collection("user").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Map userMap = task.getResult().getData();
                                    List<String> purchaseList = new ArrayList<>();
                                    if (userMap.get("purchaseList") != null) {
                                        purchaseList = (List) userMap.get("purchaseList");
                                    }
                                    purchaseList.add(postId);
                                    postData.put("buyerId", user.getUid());
                                    postData.put("buyerNickname", user.getDisplayName());
                                    postData.put("postId", postId);
                                    firestore.collection("purchase").add(postData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ShowPostActivity.this, "해당 상품을 구매하셨습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        Toast.makeText(ShowPostActivity.this, "구매를 취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        footerBtn = (Button) findViewById(R.id.footerButton);
        if (user.getUid().equals(userId)) {
            footerBtn.setText("삭제하기");
            footerBtn.setBackgroundColor(Color.argb(70, 25, 25, 70));
            footerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ShowPostActivity.this);
                    alertDialogBuilder.setTitle("삭제하기");
                    alertDialogBuilder.setMessage("해당 상품을 삭제하시겠습니까?");
                    alertDialogBuilder.setPositiveButton("확인", deleteDialogClickListener).setNegativeButton("취소", deleteDialogClickListener).show();

                }
            });
        } else {
            footerBtn.setText("구매하기");
            footerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ShowPostActivity.this);
                    alertDialogBuilder.setTitle("구매하기");
                    alertDialogBuilder.setMessage("해당 상품을 구매하시겠습니까?");
                    alertDialogBuilder.setPositiveButton("확인", buyDialogClickListener).setNegativeButton("취소", buyDialogClickListener).show();
                }
            });
        }
        setPost();
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

    @Override
    public void onBackPressed() {
        IntentBack();
    }

    public void IntentBack() {
        finish();
        overridePendingTransition(R.anim.leftin_activity, R.anim.rightout_activity);
    }

    private void setPost() {
//        String price = String.format("%,s", postData.get("price"));

        titleTV.setText("" + postData.get("title"));
        priceTV.setText(new java.text.DecimalFormat("#,###").format(Integer.parseInt(postData.get("price").toString())) + "원");
        contentTV.setText("" + postData.get("content"));

        long time = new Date().getTime() - (long) postData.get("timestamp");
        Log.d("timetest", postData.get("timestamp").toString() + " " + new Date().getTime());
        time /= 1000;
        if (time < 60) {
            postInfoTV.setText(time + "초 전");

        } else if (time >= 60 && time < 3600) {
            postInfoTV.setText(time / 60 + "분 전");
        } else if (time >= 3600 && time < 86400) {
            postInfoTV.setText(time / 3600 + "시간 전");

        }

        MommooAsyncTask asyncTask = new MommooAsyncTask();
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, postId);
    }


    class MommooAsyncTask extends AsyncTask<String, Void, String> {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(final String... params) {
            Log.d("AsyncTest", params[0]);
            final long ONE_MEGABYTE = 1024 * 1024 * 10;
            storageReference.child("post").child(params[0]).child("photo.jpg")
                    .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {

                    // Data for "images/island.jpg" is returns, use this as needed
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    postIV.setImageBitmap(bitmap);
                    postIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ShowPostActivity.this, ShowDetailImageActivity.class);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] bytes = stream.toByteArray();
                            intent.putExtra("image", bytes);
                            startActivity(intent);
                        }
                    });
                }
            });
            return "";
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public void setDibsImage() {
        firestore.collection("dib").whereEqualTo("dibUserId", user.getUid()).whereEqualTo("postId", postId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {
                        dibsBtn.setBackgroundResource(android.R.drawable.btn_star_big_off);
                    } else {
                        dibsBtn.setBackgroundResource(R.drawable.favorite);

                    }
                }
            }
        });
    }
}