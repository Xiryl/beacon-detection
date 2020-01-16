package it.chiarani.beacon_detection.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.models.BeaconData;
import it.chiarani.beacon_detection.models.BeaconDevice;

public class BeaconDataAdapter extends RecyclerView.Adapter<BeaconDataAdapter.ViewHolder>{
    private List<BeaconData> mItems;

    public BeaconDataAdapter(List<BeaconData> items) {
        this.mItems = items;
    }

    @NonNull
    @Override
    public BeaconDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_collected, parent, false);

        return new BeaconDataAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtInfo;
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtInfo = itemView.findViewById(R.id.item_Data_collected_info);
            txtName = itemView.findViewById(R.id.item_Data_collected_address);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull BeaconDataAdapter.ViewHolder holder, int position) {
        holder.txtName.setText(mItems.get(position).getAddress());
        holder.txtInfo.setText("RSSI:" + mItems.get(position).getRssi());
    }


    @Override
    public int getItemCount() {
        return this.mItems.size();
    }

}
