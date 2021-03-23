package com.example.qingzhou.adapter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qingzhou.R;
import com.example.qingzhou.discussion.GroupDiscussionActivity;
import com.example.qingzhou.discussion.WaitingRoom;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private static final String TAG = "RoomAdapter";
    private List<EMChatRoom> mRoomInfoList;     //存储着从服务器获取的EMChatRoom房间实例

    static class ViewHolder extends RecyclerView.ViewHolder {   //静态内部类
        View roomView;      //整个房间子项目视图
        ImageView roomImage;    //房间图片
        TextView roomTheme;      //房间主题
        TextView roomId;        //房间ID号
        TextView roomCreator;   //房间创始人
        TextView roomMembers;      //目前房间人数/总容量


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //将整个子项目视图（包括图片+文字）与以room_item.xml文件建立的view绑定
            roomView=itemView;
            roomImage=itemView.findViewById(R.id.room_pic);
            roomTheme=itemView.findViewById(R.id.tv_roomtheme2);
            roomId=itemView.findViewById(R.id.tv_roomid2);
            roomCreator=itemView.findViewById(R.id.tv_roomcreator2);
            roomMembers=itemView.findViewById(R.id.tv_roommembers2);
        }
    }

    public RoomAdapter(List<EMChatRoom> roomInfoList){
        //roomInfoList从TabSecondActivity中传来
        mRoomInfoList=roomInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.room_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.roomView.setOnClickListener(new View.OnClickListener() {
            //为每一个ViewHolder（视图持有者，即每个子项目）设置点击事件
            @Override
            public void onClick(View v) {   //点击后弹出AlertDialog，询问是否加入所选的讨论
                AlertDialog.Builder builder=new AlertDialog.Builder(parent.getContext());
                String userName=EMClient.getInstance().getCurrentUser();    //获取当前用户名称
                if(holder.roomCreator.getText().toString().equals(userName)){
                    //若当前环信用户的用户名与点击的房间的拥有者名字相同
                    builder.setTitle("进入/删除当前房间");
                    builder.setMessage("当前用户为房间的拥有者，可选择加入/删除房间");
                    builder.setNegativeButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //先将当前用户退出房间
                            EMClient.getInstance().chatroomManager().leaveChatRoom(userName);
                            //再对房间执行销毁操作
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        EMClient.getInstance().chatroomManager().
                                                destroyChatRoom(holder.roomId.getText().toString());
                                    } catch (HyphenateException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    });
                }
                else {  //当前用户不是该房间的拥有者
                    builder.setTitle("加入/不加入当前房间");
                    builder.setMessage("当前用户非该房间拥有者，只能选择加入与否");
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();   //关闭当前对话框
                        }
                    });
                }
                //“加入”按钮为房间拥有者与非拥有者的共同设置
                builder.setPositiveButton("加入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EMClient.getInstance().chatroomManager().joinChatRoom(holder.roomId.getText().toString(), new EMValueCallBack<EMChatRoom>() {

                            @SuppressLint("ResourceType")
                            @Override
                            public void onSuccess(EMChatRoom value) {
                                //加入聊天室成功，在此跳转到讨论页面
                            /*    Intent intent=new Intent(v.getContext(), GroupDiscussionActivity.class);
                                intent.putExtra("chatroom_id",value.getId());   //房间ID传递
                                v.getContext().startActivity(intent);   */
                                WaitingRoom.actionStart(v.getContext(),value.getId());
                                Log.d(TAG,userName+"加入聊天室"+value.getId()+"成功！");
                            }

                            @Override
                            public void onError(final int error, String errorMsg) {
                                //加入聊天室失败
                                Log.d(TAG,userName+"加入聊天室失败！"+errorMsg);
                            }
                        });
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });
        return holder;
    }

    //将数据与视图绑定
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EMChatRoom roomInfo=mRoomInfoList.get(position);
        holder.roomImage.setImageResource(R.drawable.tab_second_normal);
        holder.roomTheme.setText(roomInfo.getName());
        holder.roomId.setText(roomInfo.getId());
        holder.roomCreator.setText(roomInfo.getOwner());
        String string=roomInfo.getMemberCount()+"/"+roomInfo.getMaxUsers();
        holder.roomMembers.setText(string);
    }

    @Override
    public int getItemCount() {
        return mRoomInfoList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}
