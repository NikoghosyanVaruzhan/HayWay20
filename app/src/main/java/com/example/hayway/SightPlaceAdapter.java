package com.example.hayway;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class SightPlaceAdapter
        extends RecyclerView.Adapter<SightPlaceAdapter.PlaceViewHolder>
        implements Filterable {

    private final List<SightPlace> fullList = new ArrayList<>();
    private final List<SightPlace> filteredList = new ArrayList<>();
    private final Context context;

    public SightPlaceAdapter(List<SightPlace> initialList, Context context) {
        this.context = context;
        setPlaces(initialList);
    }

    public void setPlaces(List<SightPlace> places) {
        fullList.clear();
        fullList.addAll(places);
        filteredList.clear();
        filteredList.addAll(places);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        SightPlace place = filteredList.get(position);
        holder.placeName.setText(place.name);
        holder.placeDescription.setText(place.description);
        Glide.with(context).load(place.photoUrl).into(holder.placeImage);
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, PlaceDetailActivity.class);
            i.putExtra("name", place.name);
            i.putExtra("description", place.description);
            i.putExtra("imageUrl", place.photoUrl);
            i.putExtra("latitude", place.latitude);
            i.putExtra("longitude", place.longitude);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView placeName, placeDescription;
        ImageView placeImage;
        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.placeName);
            placeDescription = itemView.findViewById(R.id.placeDescription);
            placeImage = itemView.findViewById(R.id.placeImage);
        }
    }

    @Override
    public Filter getFilter() {
        return placeFilter;
    }

    private final Filter placeFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SightPlace> temp = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                temp.addAll(fullList);
            } else {
                String pat = constraint.toString().toLowerCase().trim();
                for (SightPlace p : fullList) {
                    if (p.name.toLowerCase().startsWith(pat)) {
                        temp.add(p);
                    }
                }
            }
            FilterResults res = new FilterResults();
            res.values = temp;
            return res;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            //noinspection unchecked
            filteredList.addAll((List<SightPlace>) results.values);
            notifyDataSetChanged();
        }
    };
}
