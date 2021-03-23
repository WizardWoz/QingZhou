package com.example.qingzhou.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qingzhou.R;
import com.example.qingzhou.bean.UserInfo;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder>{
    private List<UserInfo> mlist;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView userimage;
        TextView username;

        public ViewHolder(View view){
            super(view);
            userimage = view.findViewById(R.id.itemimage);
            username = view.findViewById(R.id.itemname);
        }
    }

    public MemberAdapter(List<UserInfo> memberlist){
        mlist = memberlist;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memberitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserInfo user = mlist.get(position);
//        holder.userimage.setImageResource();
        holder.username.setText(user.getUserName());
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public void removeData(int position) {
        mlist.remove(position);
        //删除动画
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }
}
