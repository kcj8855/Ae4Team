package com.example.cjnote.ae4teamapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.text.NumberFormat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class FragmentFour extends android.support.v4.app.Fragment {
    private String TAG = "dbCheck";
    private CollectionReference postColRef = FirebaseFirestore.getInstance().collection("post");
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    LinearLayout contentLL4;
    Map<String, ImageView> temp;
    LinearLayout postView;
    LinearLayout LL;
    TextView titleTV;
    TextView priceTV;
    SpannableStringBuilder titleSps;
    SpannableStringBuilder priceSps;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_four, container, false);
        temp = new HashMap<>();

        contentLL4 = (LinearLayout) v.findViewById(R.id.contentLL4);
        setImageView();
        return v;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setImageView() {
        postColRef
                .whereEqualTo("category", "category4")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (getActivity() != null) {
                                int taskSize = task.getResult().size();
                                int count = 0;
                                for (final DocumentSnapshot document : task.getResult()) {
                                    if (count % 3 == 0) {
                                        LL = new LinearLayout(getActivity());
                                        LL.setOrientation(LinearLayout.HORIZONTAL);
                                    }

                                    Drawable drawable2 = getResources().getDrawable(R.drawable.postview_border);
                                    titleTV = new TextView(getActivity());
                                    priceTV = new TextView(getActivity());
                                    LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(280, 85);
                                    titleLp.setMargins(10, 10, 0, 0);
                                    titleTV.setLayoutParams(titleLp);

                                    NumberFormat nf = NumberFormat.getInstance();


                                    LinearLayout.LayoutParams priceLp = new LinearLayout.LayoutParams(300, 50);
                                    priceLp.setMargins(10, 0, 10, 10);
                                    priceTV.setLayoutParams(priceLp);
                                    String strTitle = document.getData().get("title").toString();
                                    String strPrice = nf.format(document.getData().get("price"));

                                    titleSps = new SpannableStringBuilder(strTitle.replace(" ", "\u00A0"));
                                    titleSps.setSpan(new AbsoluteSizeSpan(33), 0, titleSps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    titleSps.setSpan(new ForegroundColorSpan(Color.rgb(30, 30, 30)), 0, titleSps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    if (document.getData().get("userId").equals(mAuth.getCurrentUser().getUid())) {
                                        strTitle = "[내 상품] " + document.getData().get("title").toString();
                                        titleSps = new SpannableStringBuilder(strTitle.replace(" ", "\u00A0"));
                                        titleSps.setSpan(new AbsoluteSizeSpan(33), 0, titleSps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        titleSps.setSpan(new ForegroundColorSpan(Color.rgb(0, 0, 255)), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        titleSps.setSpan(new ForegroundColorSpan(Color.rgb(30, 30, 30)), 6, titleSps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }

                                    priceSps = new SpannableStringBuilder(strPrice + "원");
                                    priceSps.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, priceSps.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    titleTV.setText(titleSps);
                                    priceTV.setText(priceSps);


                                    //텍스트가 일정 길이 초과시 ..으로 변환
                                    titleTV.setMaxLines(2);
                                    titleTV.setEllipsize(TextUtils.TruncateAt.END);

                                    temp.put("postimageView" + count, new ImageView(getActivity()));
                                    temp.get("postimageView" + count).setMinimumHeight(300);
                                    temp.get("postimageView" + count).setMinimumWidth(300);
                                    temp.get("postimageView" + count).setBackground(drawable2);

                                    drawable2 = getResources().getDrawable(R.drawable.postview_border);
                                    postView = new LinearLayout(getActivity());
                                    postView.setOrientation(LinearLayout.VERTICAL);
                                    postView.setPadding(2, 1, 2, 2);

                                    postView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Map tempMap = document.getData();
                                            Intent intent = new Intent(getActivity(), ShowPostActivity.class);
                                            intent.putExtra("postId", document.getId());
                                            intent.putExtra("data", (Serializable) tempMap);
                                            startActivity(intent);
                                        }
                                    });

                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(300, 450);
                                    lp.setMargins(45, 50, 0, 50);
                                    postView.setLayoutParams(lp);
                                    postView.setBackground(drawable2);
                                    postView.addView(temp.get("postimageView" + count));
                                    postView.addView(titleTV);
                                    postView.addView(priceTV);
                                    LL.addView(postView);
                                    if (count % 3 == 2 || count == taskSize - 1) {
                                        contentLL4.addView(LL);
                                    }

                                    MommooAsyncTask asyncTask = new MommooAsyncTask();
                                    String[] string = {String.valueOf(count), document.getId()};
                                    asyncTask.execute(string);
                                    count++;
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    class MommooAsyncTask extends AsyncTask<String, Void, Void> {
        Bitmap resize;
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final String... params) {
            final long ONE_MEGABYTE = 1024 * 1024 * 10;
            StorageReference islandRef = storageRef.child("post").child(params[1]).child("photo.jpg");
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    resize = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                    temp.get("postimageView" + params[0]).setImageBitmap(resize);
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }
}

