package com.example.qingzhou.adapter;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qingzhou.R;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;

import java.io.IOException;
import java.util.List;

/**
 * 这是讨论吧的msg
 * 特别注意！这里为了防止等候室的内容传入到讨论界面里，因此要加一个属性用来标识这是讨论吧的信息
 *         刷新视图时只接受带有这个属性的信息
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private boolean isPlaying=false;    //判断语音消息是否在被播放中

    private List<EMMessage> messageList;    //语音消息列表
    private MediaPlayer mediaPlayer;    //声明一个媒体播放器对象

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView others_pic;     //非当前用户的头像
        TextView others_name;    //非当前用户的名字
        ImageView others_message;    //非当前用户的消息气泡框

        ImageView mine_pic;      //当前用户的头像
        TextView mine_name;  //当前用户的名字
        ImageView mine_message;  //当前用户的消息气泡框

        EMMessage emMessage;    //用户信息气泡框对应的录音

        TextView textView;      //提示信息窗口


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            others_pic=itemView.findViewById(R.id.others_pic);
            others_name=itemView.findViewById(R.id.others_name);
            others_message=itemView.findViewById(R.id.others_message);

            mine_pic=itemView.findViewById(R.id.mine_pic);
            mine_name=itemView.findViewById(R.id.mine_name);
            mine_message=itemView.findViewById(R.id.mine_message);

            textView=itemView.findViewById(R.id.tip);

            EMClient.getInstance().groupManager().loadAllGroups();
            EMClient.getInstance().chatManager().loadAllConversations();
        }
    }

    public MessageAdapter(List<EMMessage> messageList){
        this.messageList=messageList;   //从GroupDiscussionActivity获取消息列表
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate
                (R.layout.chatmsg_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.emMessage=messageList.get(position);    //获取当前视图所对应的消息
       //如果是语音消息
        if(holder.emMessage.getType().equals(EMMessage.Type.VOICE)){
            //如果是当前用户
            if (holder.emMessage.getFrom().equals(EMClient.getInstance().getCurrentUser())){
                //若当前位置的消息的拥有者为登录到当前设备的用户，则只显示当前用户的名字，头像，消息框
                holder.mine_pic.setVisibility(View.VISIBLE);
                holder.mine_name.setVisibility(View.VISIBLE);
                holder.mine_message.setVisibility(View.VISIBLE);
                //将其它用户的名字，头像，消息框设置为不可见
                holder.others_pic.setVisibility(View.INVISIBLE);
                holder.others_name.setVisibility(View.INVISIBLE);
                holder.others_message.setVisibility(View.INVISIBLE);
                //系统提示不可见
                holder.textView.setVisibility(View.INVISIBLE);
                //为我方消息气泡框设置点击事件（点击即播放已发送的语音消息）
                holder.mine_message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecordPlayThread thread=new RecordPlayThread((EMVoiceMessageBody)
                                holder.emMessage.getBody());
                        new Thread(thread).start();
                    }
                });
            }
            else {
                //若当前位置的消息的拥有者不是登录到当前设备的用户，则不显示当前用户的名字，头像，消息框
                holder.mine_pic.setVisibility(View.INVISIBLE);
                holder.mine_name.setVisibility(View.INVISIBLE);
                holder.mine_message.setVisibility(View.INVISIBLE);
                //同时显示其他用户的头像，名字，消息框
                holder.others_pic.setVisibility(View.VISIBLE);
                holder.others_name.setVisibility(View.VISIBLE);
                holder.others_message.setVisibility(View.VISIBLE);
                //系统提示不可见
                holder.textView.setVisibility(View.INVISIBLE);
                //为其他人的消息气泡框设置点击事件（点击即播放已发送的语音消息）
                holder.others_message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecordPlayThread thread=new RecordPlayThread((EMVoiceMessageBody)
                                holder.emMessage.getBody());
                        new Thread(thread).start();
                    }
                });
            }           //如果是文本消息
        } else if(holder.emMessage.getType().equals(EMMessage.Type.TXT)){
            //若当前位置的消息的拥有者不是登录到当前设备的用户，则不显示当前用户的名字，头像，消息框
            holder.mine_pic.setVisibility(View.INVISIBLE);
            holder.mine_name.setVisibility(View.INVISIBLE);
            holder.mine_message.setVisibility(View.INVISIBLE);
            //同时显示其他用户的头像，名字，消息框
            holder.others_pic.setVisibility(View.INVISIBLE);
            holder.others_name.setVisibility(View.INVISIBLE);
            holder.others_message.setVisibility(View.INVISIBLE);
            //系统提示不可见
            holder.textView.setVisibility(View.VISIBLE);
            EMTextMessageBody textMessageBody = (EMTextMessageBody) holder.emMessage.getBody();
            holder.textView.setText(textMessageBody.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    //后台播放录音子线程
    private class RecordPlayThread implements Runnable{
        private EMVoiceMessageBody messageBody;

        public RecordPlayThread(EMVoiceMessageBody messageBody){
            this.messageBody=messageBody;
        }

        @Override
        public void run() {
            if (!isPlaying){
                mediaPlayer=null;
                mediaPlayer=new MediaPlayer();      //初始化录音播放器MediaPlayer
                EMVoiceMessageBody emVoiceMessageBody=messageBody;
                //设置MediaPlayer的播放路径为语音消息体内的远程URL地址
                try {
                    mediaPlayer.setDataSource(emVoiceMessageBody.getRemoteUrl());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
            else {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            isPlaying=!isPlaying;   //每次点击语音消息气泡框都会改变当前播放状态
        }
    }
}
