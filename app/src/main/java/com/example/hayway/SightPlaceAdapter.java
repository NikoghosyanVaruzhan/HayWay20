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

public class SightPlaceAdapter extends RecyclerView.Adapter<SightPlaceAdapter.PlaceViewHolder> implements Filterable {

    private final List<SightPlace> placeList;
    private List<SightPlace> placeListFull;
    private final Context context;

    public SightPlaceAdapter(List<SightPlace> placeList, Context context) {
        this.placeList = placeList;
        this.placeListFull = new ArrayList<>(placeList);
        this.context = context;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        SightPlace place = placeList.get(position);
        holder.placeName.setText(place.name);
        holder.placeDescription.setText(place.description);
        Glide.with(context).load(place.photoUrl).into(holder.placeImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlaceDetailActivity.class);
            intent.putExtra("name", place.name);
            intent.putExtra("description", place.description);
            intent.putExtra("imageUrl", place.photoUrl);
            intent.putExtra("latitude", place.latitude);
            intent.putExtra("longitude", place.longitude);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView placeName, placeDescription;
        ImageView placeImage;

        public PlaceViewHolder(@NonNull View itemView) {
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
            List<SightPlace> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(placeListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (SightPlace item : placeListFull) {
                    if (item.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            placeList.clear();
            placeList.addAll((List<SightPlace>) results.values);
            notifyDataSetChanged();
        }
    };
}
