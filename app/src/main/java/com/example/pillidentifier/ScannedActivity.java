package com.example.pillidentifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ScannedActivity extends AppCompatActivity {

    private RecyclerView imageRecycler;
    public ArrayList<Bitmap> imageList;
    public ArrayList<Boolean> analysisList;
    private ImageAdapter imageAdapter;
    private ImageView noImage;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned);
        setViews();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanIntent=new Intent(ScannedActivity.this, MainActivity.class);
                startActivity(scanIntent);
                finish();
            }
        });
        setRecyclerViewContent();
    }
    private void setViews(){
        imageRecycler=findViewById(R.id.scannedImagesRecycler);
        noImage=findViewById(R.id.noImage);
        fab=findViewById(R.id.fab);

    }

    private void setRecyclerViewContent(){
        imageRecycler.setLayoutManager(new GridLayoutManager(this,2));
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] images=storageDir.listFiles();
        if(images!=null){
            Arrays.sort(images, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return Long.compare(o2.lastModified(), o1.lastModified());

                }
            });
        }
        if(imageList!=null){
            imageList.clear();
        }
        if(analysisList!=null){
            analysisList.clear();
        }
        imageList=new ArrayList<Bitmap>();
        analysisList=new ArrayList<Boolean>();

        for (int i=0; i<images.length; i++){
            String name=images[i].getName();
            String check=name.substring(16,19);
            try {

                if (check.equals("YES")) {
                    imageList.add(BitmapFactory.decodeStream(new FileInputStream(images[i])));
                    analysisList.add(true);
                } else if (check.equals("NOO")) {
                    imageList.add(BitmapFactory.decodeStream(new FileInputStream(images[i])));
                    analysisList.add(false);
                }
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
        if(analysisList.size()==0){
            noImage.setVisibility(View.VISIBLE);
        }
        else{
            noImage.setVisibility(View.INVISIBLE);
        }
        imageAdapter=new ImageAdapter(imageList,analysisList,this);
        imageRecycler.setAdapter(imageAdapter);
    }

}