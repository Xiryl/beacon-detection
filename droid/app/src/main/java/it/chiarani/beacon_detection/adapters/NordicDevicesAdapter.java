package it.chiarani.beacon_detection.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.db.entities.NordicDeviceEntity;

public class NordicDevicesAdapter extends RecyclerView.Adapter<NordicDevicesAdapter.ViewHolder> {

    private List<NordicDeviceEntity> mItems;
    private ItemClickListener clickListener;

    public NordicDevicesAdapter(List<NordicDeviceEntity> items, ItemClickListener clickListener) {
        this.mItems = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NordicDevicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nordic_device_scan, parent, false);

        return new NordicDevicesAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtInfo;
        TextView txtName;
        TextView major;
        TextView minor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtInfo = itemView.findViewById(R.id.item_beacon_info);
            txtName = itemView.findViewById(R.id.item_beacon_name);
            major = itemView.findViewById(R.id.item_beacon_major);
            minor = itemView.findViewById(R.id.item_beacon_minor);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(this.getAdapterPosition());
        }
    }


    @Override
    public void onBindViewHolder(@NonNull NordicDevicesAdapter.ViewHolder holder, int position) {
        holder.txtName.setText(mItems.get(position).getName());
        holder.txtInfo.setText("RSSI: " +mItems.get(position).getRssi());
        holder.major.setText(mItems.get(position).getAddress() + "");
    }

    @Override
    public int getItemCount() {
        return this.mItems.size();
    }
}