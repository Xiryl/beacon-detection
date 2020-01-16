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

        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.item_Data_collected_address);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull BeaconDataAdapter.ViewHolder holder, int position) {
        holder.txtName.setText("MAC: " + mItems.get(position).getAddress() + ", RSSI:" + mItems.get(position).getRssi() + " (" + mItems.get(position).getTimestamp() + ")");
    }


    @Override
    public int getItemCount() {
        return this.mItems.size();
    }

}
