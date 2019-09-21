package com.example.imageuploader.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.imageuploader.Models.Upload;
import com.example.imageuploader.R;
import com.example.imageuploader.Utils.FileUtil;
import com.example.imageuploader.Utils.InterfaceUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    public static final int IMAGE_CHOOSER_REQUEST_CODE = 1;
    private Button UploadImage;
    private Button ChooseImage;
    private MaterialButton ShowUploadedImages;
    private EditText ChooseImageName;
    private ImageView SelectedImage;
    private Uri SelectedImageUri;
    private StorageReference storageReference;
    private ProgressBar UploadingProgressBar;
    private UploadTask uploadTask;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private boolean compressed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UploadImage = findViewById(R.id.start_upload_button);
        ChooseImage = findViewById(R.id.choose_file_button);
        ShowUploadedImages = findViewById(R.id.show_uploads_text_button);
        ChooseImageName = findViewById(R.id.image_to_upload_name);
        SelectedImage = findViewById(R.id.selected_image);
        UploadingProgressBar = findViewById(R.id.progressBar);
        UploadingProgressBar.setMax(100);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = firebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("imageUploaded");

        ChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenFileChooser();
            }
        });

        ShowUploadedImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //moving to another activity to show uploaded images
                Intent showUploadsActivity = new Intent(MainActivity.this,ShowUploads.class);
                startActivity(showUploadsActivity);

            }
        });

        UploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(SelectedImageUri != null){
                if(uploadTask != null && uploadTask.isInProgress())
                    InterfaceUtil.showSynchronizedToast(getApplicationContext(),"Upload on progress...");
                else
                    StartUpload();
            }
            else
                InterfaceUtil.showSynchronizedToast(getApplicationContext(),"Load an image and retry");
            }
        });

    }

    private void OpenFileChooser() {
        Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(in,IMAGE_CHOOSER_REQUEST_CODE);
    }

    public void StartUpload(){
        //setting the image title
        final String ImageTitle;
        if(!ChooseImageName.getText().toString().trim().isEmpty())
            ImageTitle = ChooseImageName.getText().toString();
        else
            ImageTitle = "Example" + new Random().nextInt(Integer.MAX_VALUE);



        final StorageReference imageReference = storageReference.child("imageUploaded/"+ImageTitle);


        //create a compress confirmation alert dialog
        final AlertDialog alertDialog = new MaterialAlertDialogBuilder(getThemedContext(), R.style.Theme_Tasker_Dialog)
                .setTitle("Compress Image")
                .setMessage("Do you want to compress the image before uploading ?")
                .setPositiveButton("Compress",/*Compress Clicked*/new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //create compressed image
                        Uri imageUri = createCompressedImage();
                        // create an uploadtask for compressed image
                        uploadTask = imageReference.putFile(imageUri);
                        compressed = true;
                        Log.d("TAG","PositiveButton");
                    }
                })
                .setNegativeButton("Don't Compress", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //create uploadtask for original image
                        uploadTask = imageReference.putFile(SelectedImageUri);
                        compressed = false;
                        Log.d("TAG","NegativeButton");
                    }
                })
                .create();
        alertDialog.show();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //register listener to the uploadtask object
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        UploadingProgressBar.setProgress((int)progress);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(),"Upload failed !!"+exception,Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        //saving image information in realtime database
                        Log.d("TAG",imageReference.getDownloadUrl().toString());
                        //Generate a unique key to save image information in the database
                        final String uploadId = databaseReference.push().getKey();

                        //showing a succesfull upload toast
                        InterfaceUtil.showSynchronizedToast(getApplicationContext(),"Uploaded . ");
                        //Getting the download url
                        imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d("TAG",uri.toString());
                                Upload upload = new Upload(ImageTitle,uri.toString(),compressed);
                                databaseReference.child(uploadId).setValue(upload);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d("TAG","NOOOOOOOOOOO"+exception.getMessage());
                            }
                        });
                    }
                });
            }
        });

    }


    public Uri createCompressedImage(){
        //getting image extension(optional)
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeType = MimeTypeMap.getSingleton();
        final String imageExtension = mimeType.getExtensionFromMimeType(contentResolver.getType(SelectedImageUri));

        File compressFile = null;
        try {
             compressFile = new Compressor(this)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToFile(FileUtil.from(this,SelectedImageUri));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(compressFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            SelectedImageUri = data.getData();
            Glide.with(this)
                    .load(SelectedImageUri)
                    .into(SelectedImage);
            UploadingProgressBar.setProgress(0);
            ChooseImageName.setText("");
        }
    }
}



