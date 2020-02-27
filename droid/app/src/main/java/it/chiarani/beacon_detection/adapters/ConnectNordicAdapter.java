package it.chiarani.beacon_detection.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.db.entities.NordicDeviceEntity;
import it.chiarani.beacon_detection.models.NordicEvents;

public class ConnectNordicAdapter extends RecyclerView.Adapter<ConnectNordicAdapter.ViewHolder> {

    private List<NordicDeviceEntity> mItems;
    private ItemClickListener clickListener;

    public ConnectNordicAdapter(List<NordicDeviceEntity> mItems, ItemClickListener clickListener) {
        this.mItems = mItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ConnectNordicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_connect_nordic, parent, false);

        return new ConnectNordicAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView txtPrimary, txtSecondary;
        ConstraintLayout cl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPrimary = itemView.findViewById(R.id.item_connect_nordic_name);
            txtSecondary = itemView.findViewById(R.id.item_connect_nordic_description);
            cl = itemView.findViewById(R.id.item_connect_nordic_cl);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(this.getAdapterPosition());
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ConnectNordicAdapter.ViewHolder holder, int position) {
        holder.txtPrimary.setText(mItems.get(position).getName() + ": " + mItems.get(position).getAddress());

        if(mItems.get(position).isConnected()) {
            holder.txtSecondary.setText("Conn. status: CONNECTED");
            holder.cl.setBackgroundResource(R.drawable.background_red_filled);
        } else {
            holder.txtSecondary.setText("Conn. status: DISCONNECTED");
            holder.cl.setBackgroundResource(R.drawable.background_blue_filled);
        }
    }

    @Override
    public int getItemCount() {
        return this.mItems.size();
    }
}