package com.example.qingzhou.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qingzhou.R;
import com.example.qingzhou.bean.CommentInfo;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private List<CommentInfo> mcommentInfoList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View CommentView;
        TextView CommentUserName;
        TextView CommentTime;
        TextView CommentText;

        public ViewHolder(@NonNull View view) {
            super(view);
            CommentView = view;
            CommentUserName = view.findViewById(R.id.comment_user_name);
            CommentTime = view.findViewById(R.id.comment_time);
            CommentText = view.findViewById(R.id.comment_text);
        }
    }

    public CommentAdapter(List<CommentInfo> commentInfoList){
        mcommentInfoList = commentInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        //设置整个的点击事件
        holder.CommentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                CommentInfo commentInfo = mcommentInfoList.get(position);
                Toast.makeText(v.getContext(), "you click view " + commentInfo.getComment_user_name(),Toast.LENGTH_SHORT).show();
            }
        });
//        //点击评论内容
//        holder.CommentUserName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position = holder.getAdapterPosition();
//                CommentInfo commentInfo = mcommentInfoList.get(position);
//                Toast.makeText(v.getContext(), "you click " + "CommentUserName",Toast.LENGTH_SHORT).show();
//            }
//        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentInfo commentInfo = mcommentInfoList.get(position);
        holder.CommentUserName.setText(commentInfo.getComment_user_name());
        holder.CommentTime.setText(commentInfo.getComment_time());
        holder.CommentText.setText(commentInfo.getComment_text());
    }

    @Override
    public int getItemCount() {
        return mcommentInfoList.size();
    }





}
