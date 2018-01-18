package com.example.cjnote.ae4teamapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private CollectionReference postColRef = FirebaseFirestore.getInstance().collection("post");
    private FirebaseUser user;
    NavigationView navigationView;
    private Fragment fr;
    private TextView rightNavHeaderNicknameTV;
    DrawerLayout drawer;
    NavigationView rightNavigationView;
    NavigationView leftNavigationView;
    private String TAG = "MainTest";
    private final long FINSH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    Button womanDressButton;
    Button manDressButton;
    Button fashionAndStuffButton;
    Button beautyButton;
    Button childAndBirthButton;
    Button sportAndLeisureButton;
    Button digitalAndHomeButton;
    Button bookButton;
    Button lifeButton;
    Button vehicleButton;
    Button etcButton;
    Button stargoodsButton;
    Button regionServiceButton;
    Button oneRoomButton;
    Button abilityButton;
    Button communityButton;
    Button lightningButton;
    Button eventButton;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private Bitmap bitmap;
    private ImageView psa;
    private StorageReference islandRef;
    LinearLayout dibsLL;
    LinearLayout myPostLL;
    Map<String, ImageView> temp;
    Map<String, ImageView> myProductImageMap = new HashMap<>();
    List<String> dibs;
    private SectionsPagerAdapter mSectionsPagerAdater;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        firestore = FirebaseFirestore.getInstance();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.right_nav_view);

        rightNavigationView = (NavigationView) findViewById(R.id.right_nav_view);
        leftNavigationView = (NavigationView) findViewById(R.id.left_nav_view);

        psa = (ImageView) rightNavigationView.getHeaderView(0).findViewById(R.id.imageView);

        LinearLayout leftNavHeader = (LinearLayout) leftNavigationView.getHeaderView(0);
        LinearLayout rightNavHeader = (LinearLayout) navigationView.getHeaderView(0);

        rightNavHeaderNicknameTV = (TextView) rightNavHeader.findViewById(R.id.right_nav_header_nickname);
        if (user.getDisplayName() == null) {
            rightNavHeaderNicknameTV.setText("no nickname");
        } else {
            rightNavHeaderNicknameTV.setText(user.getDisplayName());
        }

        temp = new HashMap<>();
        dibs = new ArrayList<>();
        dibsLL = (LinearLayout) rightNavHeader.findViewById(R.id.dibsLL);
        myPostLL = (LinearLayout) rightNavHeader.findViewById(R.id.myPostLL);

        mSectionsPagerAdater = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mViewPager.setAdapter(mSectionsPagerAdater);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.mainTabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        if ((mAuth.getCurrentUser() != null)) {
            psaSetting();
            setDibsImageView();
            setMyProductImageView();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rightNavHeader.setFitsSystemWindows(true);
            leftNavHeader.setFitsSystemWindows(true);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button dibsBtn = (Button) rightNavHeader.findViewById(R.id.dibsButton);
        dibsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DibsListActivity.class));
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);

            }
        });

        ImageView toSettingActivityIV = (ImageView) rightNavHeader.findViewById(R.id.toSettingActivityImageView);
        toSettingActivityIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);

            }
        });

        Button sellBtn = (Button) rightNavHeader.findViewById(R.id.sellButton);
        sellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddProductActivity.class));
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);

            }
        });


        Button myProductBtn = (Button) rightNavHeader.findViewById(R.id.myProductButton);
        myProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MyProductActivity.class));
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);

            }
        });

        Button purchaseHistoryBtn = (Button) rightNavHeader.findViewById(R.id.purchaseHistoryButton);
        purchaseHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PurchaseHistoryActivity.class));
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);
            }
        });


        Button salesHistoryBtn = (Button) rightNavHeader.findViewById(R.id.salesHistoryButton);
        salesHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SalesHistoryActivity.class));
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);
            }
        });

        findViewById(R.id.openLeftNavButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(leftNavigationView);
            }
        });
        findViewById(R.id.openRightNavButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(rightNavigationView);
            }
        });

        womanDressButton = (Button) leftNavHeader.findViewById(R.id.womanDressButton);
        manDressButton = (Button) leftNavHeader.findViewById(R.id.manDressButton);
        fashionAndStuffButton = (Button) leftNavHeader.findViewById(R.id.fashionAndStuffButton);
        beautyButton = (Button) leftNavHeader.findViewById(R.id.beautyButton);
        childAndBirthButton = (Button) leftNavHeader.findViewById(R.id.childAndBirthButton);
        sportAndLeisureButton = (Button) leftNavHeader.findViewById(R.id.sportAndLeisureButton);
        digitalAndHomeButton = (Button) leftNavHeader.findViewById(R.id.digitalAndHomeButton);
        bookButton = (Button) leftNavHeader.findViewById(R.id.bookButton);
        lifeButton = (Button) leftNavHeader.findViewById(R.id.lifeButton);
        vehicleButton = (Button) leftNavHeader.findViewById(R.id.vehicleButton);
        etcButton = (Button) leftNavHeader.findViewById(R.id.etcButton);
        stargoodsButton = (Button) leftNavHeader.findViewById(R.id.stargoodsButton);
        regionServiceButton = (Button) leftNavHeader.findViewById(R.id.regionServiceButton);
        oneRoomButton = (Button) leftNavHeader.findViewById(R.id.oneRoomButton);
        abilityButton = (Button) leftNavHeader.findViewById(R.id.abilityButton);
        communityButton = (Button) leftNavHeader.findViewById(R.id.communityButton);
        lightningButton = (Button) leftNavHeader.findViewById(R.id.lightningButton);
        eventButton = (Button) leftNavHeader.findViewById(R.id.eventButton);
        womanDressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, womanDressButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        manDressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, manDressButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        fashionAndStuffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, fashionAndStuffButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        beautyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, beautyButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        childAndBirthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, childAndBirthButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        sportAndLeisureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, sportAndLeisureButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        digitalAndHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, digitalAndHomeButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, bookButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        lifeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, lifeButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        vehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, vehicleButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        etcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, etcButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        stargoodsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, stargoodsButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        regionServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, regionServiceButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        oneRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, oneRoomButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        abilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, abilityButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        communityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, communityButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        lightningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, lightningButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
        eventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, eventButton.getText() + "버튼 클릭됨", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (drawer.isDrawerOpen(leftNavigationView)) {
            drawer.closeDrawer(leftNavigationView);
            return;
        }
        if (drawer.isDrawerOpen(rightNavigationView)) {
            drawer.closeDrawer(rightNavigationView);
            return;
        }

        if (0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void psaSetting() {
        islandRef = storageRef.child("user").child(mAuth.getUid()).child("profile.jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.i(TAG, "Success");
                // Data for "images/island.jpg" is returns, use this as needed
                try {
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    psa.setImageBitmap(bitmap);
                } catch (Exception e) {
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
            StorageReference islandRef = storageRef.child("post").child(params[0]).child("photo.jpg");
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    resize = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                    temp.get(params[0]).setImageBitmap(resize);
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    class GetMyPostAsyncTask extends AsyncTask<String, Void, Void> {
        Bitmap resize;
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final String... params) {
            final long ONE_MEGABYTE = 1024 * 1024 * 10;
            StorageReference islandRef = storageRef.child("post").child(params[0]).child("photo.jpg");
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    resize = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                    myProductImageMap.get(params[0]).setImageBitmap(resize);
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    public void setDibsImageView() {
        firestore.collection("dib")
                .whereEqualTo("dibUserId", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot snap = task.getResult();
                            if(snap.size() == 0) {
                                return;
                            } else {
                                int len = (snap.size() < 3) ? snap.size() : 3;
                                int count = 0;
                                for(DocumentSnapshot document : snap) {
                                    count++;
                                    ImageView tempIV = new ImageView(MainActivity.this);
                                    String postId = document.getData().get("postId").toString();

                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(300, 400);
                                    lp.setMargins(20, 20, 20, 20);
                                    lp.weight = 1;
                                    tempIV.setLayoutParams(lp);
                                    temp.put(postId, tempIV);

                                    dibsLL.addView(tempIV);
                                    MommooAsyncTask asyncTask = new MommooAsyncTask();

                                    asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, postId);
                                    if(count == 3) {
                                        break;
                                    }
                                }
                                for (int i = 0; i < 3 - task.getResult().size(); i++) {

                                    ImageView tempIV = new ImageView(MainActivity.this);
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(300, 400);
                                    lp.setMargins(20, 20, 20, 20);
                                    lp.weight = 1;
                                    tempIV.setLayoutParams(lp);

                                    dibsLL.addView(tempIV);
                                }
                            }
                        }
                    }
                });
    }

    public void setMyProductImageView() {
        postColRef
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) return;
                            for (DocumentSnapshot document : task.getResult()) {
                                ImageView tempIV = new ImageView(MainActivity.this);
                                String postId = document.getId();

                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(300, 400);
                                lp.setMargins(20, 20, 20, 20);
                                lp.weight = 1;
                                tempIV.setLayoutParams(lp);
                                myProductImageMap.put(postId, tempIV);

                                myPostLL.addView(tempIV);
                                GetMyPostAsyncTask asyncTask = new GetMyPostAsyncTask();

                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, postId);
                            }
                            for (int i = 0; i < 3 - task.getResult().size(); i++) {

                                ImageView tempIV = new ImageView(MainActivity.this);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(300, 400);
                                lp.setMargins(20, 20, 20, 20);
                                lp.weight = 1;
                                tempIV.setLayoutParams(lp);

                                myPostLL.addView(tempIV);
                            }
                        } else {
                            Log.d("asynctest", task.getException().toString());
                        }
                    }
                });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    fr = new FragmentOne();
                    break;
                case 1:
                    fr = new FragmentTwo();
                    break;
                case 2:
                    fr = new FragmentThree();
                    break;
                case 3:
                    fr = new FragmentFour();
                    break;
            }
            return fr;
        }

        @Override
        public int getCount() {
            // Show total pages.
            return 4;
        }
    }
}