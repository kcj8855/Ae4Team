package com.example.cjnote.ae4teamapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class MyProductActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private CollectionReference postColRef;
    private String userId;

    private TextView textView;
    private LinearLayout postView;
    private Map<String, ImageView> postImageView = new HashMap<>();
    private LinearLayout LL;
    private LinearLayout coverLL;

    private String TAG = "myProduct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_product);

        // Firebase Setting
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        postColRef = firestore.collection("post");
        userId = user.getUid();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        coverLL = (LinearLayout) findViewById(R.id.myProductLL);

        setImageView();
    }

    @Override
    public void onBackPressed() {
        IntentBack();
    }

    public void IntentBack() {
        finish();
        overridePendingTransition(R.anim.leftin_activity, R.anim.rightout_activity);
    }


    public void setImageView() {
        postColRef
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            int taskSize = task.getResult().size();
                            if (taskSize == 0) {
                                TextView tempTV = new TextView(MyProductActivity.this);
                                tempTV.setText("내 상품이 없습니다.");
                                coverLL.addView(tempTV);

                            }
                            int count = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                if (count % 3 == 0) {
                                    LL = new LinearLayout(MyProductActivity.this);
                                    LL.setOrientation(LinearLayout.HORIZONTAL);
                                }
                                String tempId = new String(document.getId());

                                Drawable drawable2 = getResources().getDrawable(R.drawable.textviewborder);

                                textView = new TextView(MyProductActivity.this);
                                textView.setText(document.getData().get("title").toString());

                                postImageView.put(tempId, new ImageView(MyProductActivity.this));
                                postImageView.get(tempId).setMinimumHeight(300);
                                postImageView.get(tempId).setMinimumWidth(300);
                                postImageView.get(tempId).setBackground(drawable2);
                                drawable2 = getResources().getDrawable(R.drawable.textviewborder);

                                MommooAsyncTask asyncTask = new MommooAsyncTask();
                                asyncTask.execute(tempId);

                                postView = new LinearLayout(MyProductActivity.this);
                                postView.setOrientation(LinearLayout.VERTICAL);

                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(300, 400);
                                lp.setMargins(45, 50, 0, 0);
                                postView.setLayoutParams(lp);
                                postView.setBackground(drawable2);
                                postView.addView(postImageView.get(tempId));
                                postView.addView(textView);
                                LL.addView(postView);

                                if (count % 3 == 2 || count == taskSize - 1) {
                                    coverLL.addView(LL);
                                }

                                count++;
                            }

                        }
                    }

                });
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
            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.child("post").child(params[0]).child("photo.jpg")
                    .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap resize = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                    postImageView.get(params[0]).setImageBitmap(resize);
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
}