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

public class BeaconAdapter  extends RecyclerView.Adapter<BeaconAdapter.ViewHolder> {

    private List<BeaconDevice> mItems;

    public BeaconAdapter(List<BeaconDevice> items) {
        this.mItems = items;
    }

    @NonNull
    @Override
    public BeaconAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beacon, parent, false);

        return new BeaconAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

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
        }
    }


    @Override
    public void onBindViewHolder(@NonNull BeaconAdapter.ViewHolder holder, int position) {
        holder.txtName.setText(mItems.get(position).getAddress());
        holder.txtInfo.setText("RSSI: " +mItems.get(position).getRssi()+", Distance: ~" + String.format("%.2f", mItems.get(position).getDistance())   + "mt");
        holder.minor.setText(mItems.get(position).getType() + "");
        holder.major.setText(mItems.get(position).getId2() + "");
    }


    @Override
    public int getItemCount() {
        return this.mItems.size();
    }

}