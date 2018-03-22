package com.example.cjnote.ae4teamapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;
    private static final String TAG = "Google SignIn Firebase";
    private static final int RC_GOOGLE_SIGN_IN = 123;
    private static final int RC_FACEBOOK_SIGN_IN = 64206;
    private CollectionReference userColRef = FirebaseFirestore.getInstance().collection("user");
    private ProgressDialog dialog;
    private Boolean status = false;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    CarouselView carouselView;
    int[] sampleImages = {R.drawable.sample1, R.drawable.sample2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            toMainActivity();
        }

        // Carousel Image Listener
        ImageListener imageListener = new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                imageView.setImageResource(sampleImages[position]);
                Drawable alpha = imageView.getDrawable();
                alpha.setAlpha(150);
            }
        };

        // Carousel
        carouselView = (CarouselView) findViewById(R.id.carouselView);
        carouselView.setPageCount(sampleImages.length);
        carouselView.setImageListener(imageListener);


        //Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);


        //Google Login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("로딩중..");
        dialog.setCancelable(false);


        findViewById(R.id.googleButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null) {
                    status = true;
                    dialog.show();
                }
                googleSignIn();
            }
        });

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton facebookBtn = (LoginButton) findViewById(R.id.facebookButton);
        facebookBtn.setReadPermissions("email", "public_profile");
        facebookBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                dialog.show();
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

        Button githubBtn = (Button) findViewById(R.id.githubButton);
        githubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                githubSignIn();
            }
        });

        Button AnonymousBtn = (Button) findViewById(R.id.AnonymousButton);
        AnonymousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                AnonymousSignIn();
            }
        });
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로가기 버튼을 한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            String code = intent.getStringExtra("code");
            String state = intent.getStringExtra("state");

            if (code != null && state != null) {
                // POST https://github.com/login/oauth/access_token
                sendPost(code, state);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        } else if (requestCode == RC_FACEBOOK_SIGN_IN) {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }


    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        dialog.dismiss();
    }


    // Google Login
    private void googleSignIn() {
        if (mAuth.getCurrentUser() != null) {
            return;
        }
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getUserInfo();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // Facebook Login
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getUserInfo();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    // Github Login
    private void githubSignIn() {

        // 1. Users are redirected to request their GitHub identity
        // GET http://github.com/login/oauth/authorize
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host("github.com")
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", getString(R.string.github_client_id))
                .addQueryParameter("redirect_uri", getString(R.string.github_redirect_url))
                .addQueryParameter("state", "chanjoong")//UUID.randomUUID().toString()
                .addQueryParameter("scope", "user:email")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl.toString()));
        startActivityForResult(intent, 2222);
    }

    private void sendPost(String code, String state) {
        OkHttpClient okHttpClient = new OkHttpClient();

        FormBody form = new FormBody.Builder()
                .add("client_id", getString(R.string.github_client_id))
                .add("client_secret", getString(R.string.github_client_secret))
                .add("code", code)
                .add("redirect_uri", getString(R.string.github_redirect_url))
                .add("state", state)
                .build();

        Request request = new Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .post(form)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                // onFailure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // e.g. Response form : access_token=e72e16c7e42f292c6912e7710c838347ae178b4a&token_type=bearer
                String responseBody = response.body().string();
                String[] splittedBody = responseBody.split("=|&");

                if (splittedBody[0].equalsIgnoreCase("access_token")) {
                    signInWithToken(splittedBody[1]);
                }
            }
        });
    }

    private void signInWithToken(String token) {
        // credential object from the token
        AuthCredential credential = GithubAuthProvider.getCredential(token);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            writeUserInfo();
                        } else {
                            Log.d("github signInWithToken", "@3" + task.getException());
                        }
                    }
                });
    }



    private void updateUI() {
        user = mAuth.getCurrentUser();
    }

    private void getUserInfo() {
        updateUI();

        userColRef.document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Toast.makeText(LoginActivity.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                            toMainActivity();
                            dialog.dismiss();
                        } else {
                            writeUserInfo();
                        }
                    }
                });
    }

    private void writeUserInfo() {
        updateUI();
        Map<String, Object> userMap = new HashMap<>();
        if (user == null) return;
        if (user.getEmail() != null) {
            userMap.put("email", user.getEmail());
        } else {
            userMap.put("email", "temp@email.com");
        }
        if (user.getDisplayName() != null) {
            userMap.put("nickname", user.getDisplayName());
        } else {
            userMap.put("nickname", "no nickname");
        }

        userColRef.document(user.getUid())
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toMainActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        mAuth.getCurrentUser().delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User account deleted.");
                                        }
                                    }
                                });
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                    }
                });
    }

    private void toMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    //Anonymous
    public void AnonymousSignIn() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            getUserInfo();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
