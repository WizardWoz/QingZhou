package com.example.qingzhou.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qingzhou.R;
import com.example.qingzhou.Third.NewsActicity;
import com.example.qingzhou.bean.News;

import java.util.List;

public class NewsAdater extends RecyclerView.Adapter<NewsAdater.ViewHolder> {

    private Context mContext;
    private List<News> mNewsList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView title;
        public ViewHolder(View view){
            super(view);
            cardView = (CardView)view;
            title = (TextView) view.findViewById(R.id.news_title);
        }
    }
    public NewsAdater(List<News> NewsList){
        mNewsList = NewsList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.news_item,
                parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                News news = mNewsList.get(position);
                Intent intent = new Intent(mContext, NewsActicity.class);
                intent.putExtra(NewsActicity.NEWS_TITLE, news.getTitle());
                intent.putExtra(NewsActicity.NEWS_PRECONTENT, news.getContent());
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        News news = mNewsList.get(position);

        String title = news.getTitle();
        String temp = title;
        if(title.length() > 20){
            temp = title.substring(0, 19);
            temp += "...";
        }
        holder.title.setText(temp);


    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }
}
