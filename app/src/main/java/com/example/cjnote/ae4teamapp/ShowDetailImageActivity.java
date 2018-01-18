package com.example.cjnote.ae4teamapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ShowDetailImageActivity extends AppCompatActivity {
    private ImageView m_imageview;
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail_image);

        Intent intent = getIntent();

        byte[] imageBytes = intent.getByteArrayExtra("image");
        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        m_imageview = (ImageView) findViewById(R.id.detailImageView);
        m_imageview.setImageBitmap(image);

        mAttacher = new PhotoViewAttacher(m_imageview);
    }
}