package it.chiarani.beacon_detection.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.models.NordicEvents;

/**
 * Adapter for nordic proprieties list. Shows the enum events.
 */
public class NordicDevicesPropsAdapter extends RecyclerView.Adapter<NordicDevicesPropsAdapter.ViewHolder> {

    // NOTE: the recyclerview cycle over all enums, not the items!!
    private List<NordicEvents> mItems = Arrays.asList(NordicEvents.values());
    private List<NordicEvents> events;
    private ItemPropsClickListener clickListener;

    public NordicDevicesPropsAdapter(List<NordicEvents> events, ItemPropsClickListener clickListener) {
        this.events = events;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NordicDevicesPropsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nordic_device_prop, parent, false);

        return new NordicDevicesPropsAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView txtProp;
        CheckBox checkBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtProp = itemView.findViewById(R.id.item_discovery_beacon_prop);
            checkBox = itemView.findViewById(R.id.item_discovery_beacon_chbox);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(mItems.get(this.getAdapterPosition()));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull NordicDevicesPropsAdapter.ViewHolder holder, int position) {
        holder.txtProp.setText(mItems.get(position).name());

        if(events.contains(mItems.get(position))) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return this.mItems.size();
    }
}