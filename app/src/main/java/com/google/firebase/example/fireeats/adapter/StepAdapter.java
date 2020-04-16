package com.google.firebase.example.fireeats.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.example.fireeats.R;
import com.google.maps.model.DirectionsStep;

import java.util.List;

/**
 * RecyclerView adapter for a list of steps in a route
 */
public class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {

    private List<DirectionsStep> stepList;
    private Context context;

    public StepAdapter(List<DirectionsStep> stepList, Context context) {
        this.stepList = stepList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DirectionsStep step = stepList.get(position);
        holder.mDescription.setText(Html.fromHtml(Html.fromHtml(step.htmlInstructions).toString()));
        holder.mDuration.setText("Duration = " + step.duration.humanReadable);
        holder.mDistance.setText("Distance = " + step.distance.humanReadable);
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView mDescription;
        public TextView mDuration;
        public TextView mDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mDescription = itemView.findViewById(R.id.step_description);
            mDuration = itemView.findViewById(R.id.step_duration);
            mDistance = itemView.findViewById(R.id.step_distance);
        }
    }
}
