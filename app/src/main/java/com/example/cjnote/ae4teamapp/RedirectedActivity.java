package com.example.cjnote.ae4teamapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class RedirectedActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("requestRedirectActivity", "!!");

        // 2. Users are redirected back to your site by GitHub
        Uri uri = getIntent().getData();

        // Called after the GitHub server redirect us to GITHUB_REDIRECT_URL
        if (uri != null && uri.toString().startsWith(getString(R.string.github_redirect_url))) {

            Intent intent = new Intent(this, LoginActivity.class);

            intent.putExtra("code", uri.getQueryParameter("code"));
            intent.putExtra("state", uri.getQueryParameter("state"));

            startActivity(intent);

            finish();
        }
    }
}
