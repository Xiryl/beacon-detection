package it.chiarani.beacon_detection.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.models.BeaconDevice;

public class BeaconDiscoveryAdapter extends RecyclerView.Adapter<BeaconDiscoveryAdapter.ViewHolder>{
    private List<BeaconDevice> mItems;

    public BeaconDiscoveryAdapter(List<BeaconDevice> items) {
        this.mItems = items;
    }

    @NonNull
    @Override
    public BeaconDiscoveryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discovery_beacon, parent, false);

        return new BeaconDiscoveryAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtInfo;
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtInfo = itemView.findViewById(R.id.item_discovery_beaco_info);
            txtName = itemView.findViewById(R.id.item_discovery_beaco_name);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull BeaconDiscoveryAdapter.ViewHolder holder, int position) {
        holder.txtName.setText(mItems.get(position).getAddress());
        holder.txtInfo.setText(mItems.get(position).getRssi()+" Distance ~" + String.format("%.2f", mItems.get(position).getDistance())   + "mt");
    }


    @Override
    public int getItemCount() {
        return this.mItems.size();
    }

}
