package com.example.cjnote.ae4teamapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AddProductActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_FROM_ALBUM = 0;
    private static final int PICK_FROM_CAMERA = 1;
    private Uri mlmageCaptureUri;
    private ImageView iv_UserPhoto;
    private String TAG = "Addproductcheck";
    private Bitmap bitmap;
    private CollectionReference postColRef = FirebaseFirestore.getInstance().collection("post");
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ProgressDialog dialog;
    private TextView category;
    final CharSequence[] categories = {"category1", "category2", "category3", "category4"};
    EditText priceET;
    EditText explanationET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        iv_UserPhoto = this.findViewById(R.id.addProductImageView);
        iv_UserPhoto.setOnClickListener(this);
        dialog = new ProgressDialog(AddProductActivity.this);
        dialog.setMessage("업로드중..");
        dialog.setCancelable(false);
        category = (TextView) findViewById(R.id.categorySelectTextView);

        findViewById(R.id.categorySelectTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCategori();
            }
        });

        findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postUpload();
            }
        });

        explanationET = (EditText) findViewById(R.id.explanationEditText);
        priceET = (EditText) findViewById(R.id.priceEditText);
        priceET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9), filter});
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
        int permissionCheck = ContextCompat.checkSelfPermission(AddProductActivity.this, android.Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(AddProductActivity.this, new String[]{android.Manifest.permission.CAMERA}, 0);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String url = "profile.jpg";
            mlmageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mlmageCaptureUri);
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

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                mlmageCaptureUri = data.getData();
                Log.i(TAG, "test2");
            }

            case PICK_FROM_CAMERA: {
                Log.i(TAG, "test3");
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mlmageCaptureUri);
                    iv_UserPhoto.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File f = new File(mlmageCaptureUri.getPath());
                if (f.exists()) {
                    f.delete();
                }
                break;
            }
        }
    }

    private void imageUpload(final String documentReference) {
        Log.i(TAG, "imageupload");


        StorageReference mountainsRef = storageRef.child("post").child(documentReference).child("photo.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Log.i(TAG, baos.toString());
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 6, bitmap.getHeight() / 6, true);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                dialog.dismiss();
                postColRef.document(documentReference).delete();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                dialog.dismiss();
                Toast.makeText(AddProductActivity.this, "업로드 되었습니다", Toast.LENGTH_SHORT).show();
                finish();
                Log.d(TAG, String.valueOf(downloadUrl));
            }
        });
    }

    private void postUpload() {
        EditText productName_ET = (EditText) findViewById(R.id.productNameEditText);
        EditText price_ET = (EditText) findViewById(R.id.priceEditText);
        TextView category_TV = (TextView) findViewById(R.id.categorySelectTextView);


        if (productName_ET.getText().toString().length() == 0) {
            Toast.makeText(this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bitmap == null) {
            Toast.makeText(this, "사진을 등록해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (price_ET.getText().toString().length() == 0) {
            Toast.makeText(this, "가격을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        dialog.show();
        Map<String, Object> data = new HashMap<>();
        data.put("title", productName_ET.getText().toString());
        data.put("nickname", mAuth.getCurrentUser().getDisplayName());
        data.put("userId", mAuth.getUid());
        data.put("category", category_TV.getText().toString());
        data.put("price", Integer.parseInt(price_ET.getText().toString()));
        data.put("timestamp", new Date().getTime());
        data.put("content", explanationET.getText().toString());

        postColRef
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        imageUpload(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        dialog.dismiss();
                    }
                });
    }

    public void selectCategori() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("액티비티를 선택하세요");
        alertDialogBuilder.setItems(categories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                // 액티비티 이동
                settingCategori(id);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void settingCategori(int id) {
        switch (id) {
            case 0:
                category.setText(categories[0].toString());
                break;
            case 1:
                category.setText(categories[1].toString());
                break;
            case 2:
                category.setText(categories[2].toString());
                break;
            case 3:
                category.setText(categories[3].toString());
                break;
            case 4:
                category.setText(categories[4].toString());
                break;
        }
    }

    // 첫번째 숫자 제한
    InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[1-9]+$");

            if (!priceET.getText().toString().equals("")) return null;

            if (!ps.matcher(source).matches()) return "";

            return null;
        }
    };

}
