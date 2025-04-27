package com.example.hayway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private final List<NewsItem> items = new ArrayList<>();
    private final Context context;

    public NewsAdapter(Context ctx) {
        this.context = ctx;
    }

    public void setNews(List<NewsItem> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int pos) {
        NewsItem news = items.get(pos);
        holder.title.setText(news.title);
        holder.desc.setText(news.description);
        Glide.with(context).load(news.imageUrl).into(holder.image);
    }

    @Override public int getItemCount() { return items.size(); }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        ImageView image;
        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.newsTitle);
            image = itemView.findViewById(R.id.newsImage);
            desc  = itemView.findViewById(R.id.newsDescription);
        }
    }
}
