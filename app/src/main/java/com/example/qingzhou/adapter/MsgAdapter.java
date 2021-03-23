package com.example.qingzhou.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.qingzhou.R;
import com.example.qingzhou.Third.ThirdMsg;

import java.util.List;

/**
 * 这是小课的msg
 */
public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder>{
    private List<ThirdMsg> mMsgList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        LinearLayout middleLayout;
        TextView leftMsg;
        TextView rightMsg;

        public ViewHolder(View view){
            super(view);
            leftLayout = view.findViewById(R.id.left_layout);
            rightLayout = view.findViewById(R.id.right_layout);
            middleLayout = view.findViewById(R.id.middle_layout);
            leftMsg = view.findViewById(R.id.left_msg);
            rightMsg = view.findViewById(R.id.right_msg);
        }
    }

    public MsgAdapter(List<ThirdMsg> msgList){
        mMsgList = msgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thirdmsg_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThirdMsg msg = mMsgList.get(position);
        if(msg.getType() == ThirdMsg.TYPE_RECEIVED){
            //收到消息，在左布局显示
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.middleLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else if (msg.getType() == ThirdMsg.TYPE_SENT) {
            //发出消息，在右布局显示
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.middleLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
        } else if(msg.getType() == ThirdMsg.TYPE_CHOOSE) {
            //选择信息，在中布局显示
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.middleLayout.setVisibility(View.VISIBLE);
            // 1.根据信息中按钮数量添加按钮，并绑定函数
            // 2.添加标题和描述


            //下面对中布局的标题，按钮等信息进行修改
        }
    }


    @Override
    public int getItemCount() {
        return mMsgList.size();
    }
}
