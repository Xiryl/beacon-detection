package it.chiarani.beacon_detection.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.models.BeaconDevice;

public class BeaconDiscoveryAdapter extends RecyclerView.Adapter<BeaconDiscoveryAdapter.ViewHolder>{
    private List<BeaconDevice> mItems;
    private ItemClickListener clickListener;

    public BeaconDiscoveryAdapter(List<BeaconDevice> items, ItemClickListener clickListener) {
        this.mItems = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BeaconDiscoveryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discovery_beacon, parent, false);

        return new BeaconDiscoveryAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CheckBox.OnCheckedChangeListener {

        TextView txtInfo;
        TextView txtName;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtInfo = itemView.findViewById(R.id.item_discovery_beacon_info);
            txtName = itemView.findViewById(R.id.item_discovery_beacon_address);
            checkBox = itemView.findViewById(R.id.item_discovery_beacon_chbox);
            checkBox.setOnCheckedChangeListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            checkBox.setChecked(!checkBox.isChecked());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            clickListener.onItemClick(getAdapterPosition());
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
