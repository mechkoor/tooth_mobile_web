package com.example.darssa.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.darssa.R;
import com.example.darssa.beans.StudentPW;

import java.util.List;

public class StudentPWadapter extends RecyclerView.Adapter<StudentPWadapter.ViewHolder> {

    private Activity activity;

    private List<StudentPW> pws;
    private Context context;

    public StudentPWadapter(Activity activity,Context context, List<StudentPW> pws) {
        this.activity = activity;
        this.context = context;
        this.pws = pws;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        StudentPW studentpw = pws.get(position);
        Bitmap bitmap = BitmapFactory.decodeByteArray(studentpw.getBitmapByteArray(), 0, studentpw.getBitmapByteArray().length);
        holder.title.setText(studentpw.getTitle());
        holder.remarque.setText(studentpw.getRemarque());
        holder.validation.setText(studentpw.getValidation());
        holder.image.setImageBitmap(bitmap);

    }

    @Override
    public int getItemCount() {
        return pws.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView remarque,validation,title;

        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            remarque = itemView.findViewById(R.id.remarque);
            validation=itemView.findViewById(R.id.validation);
            title = itemView.findViewById(R.id.title);
            image=itemView.findViewById(R.id.imageView3);

        }
    }
}
