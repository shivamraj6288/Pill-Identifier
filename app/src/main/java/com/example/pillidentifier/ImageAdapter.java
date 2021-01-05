package com.example.pillidentifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder>{
    private ArrayList<Bitmap>imageBitmapList;
    private ArrayList<Boolean>analysisResult;
    private Context context;
    public ImageAdapter(ArrayList<Bitmap> receivedBitmapList, ArrayList<Boolean> receivedBoolList, Context mContext){
        this.imageBitmapList=receivedBitmapList;
        this.analysisResult=receivedBoolList;
        this.context=mContext;
    }
    @NonNull
    @NotNull
    @Override
    public ImageAdapter.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.imageitem,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ImageAdapter.MyViewHolder holder, int position) {
        holder.currentImage.setImageBitmap(imageBitmapList.get(position));
        if (analysisResult.get(position)){
            holder.resultImage.setImageResource(R.mipmap.correct_foreground);
        }
        else{
            holder.resultImage.setImageResource(R.mipmap.wrong_foreground);
        }
    }

    @Override
    public int getItemCount() {
        return analysisResult.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView currentImage, resultImage;
        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            currentImage=itemView.findViewById(R.id.capturedImage);
            resultImage=itemView.findViewById(R.id.result);
        }

        @Override
        public void onClick(View v) {

        }
    }
}