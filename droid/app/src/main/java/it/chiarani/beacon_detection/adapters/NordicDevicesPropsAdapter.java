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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.db.entities.NordicDeviceEntity;
import it.chiarani.beacon_detection.models.NordicEvents;

public class NordicDevicesPropsAdapter extends RecyclerView.Adapter<NordicDevicesPropsAdapter.ViewHolder> {

    private List<NordicEvents> mItems = Arrays.asList(NordicEvents.values());
    private List<NordicEvents> events;
    private ItemPropsClickListener clickListener;
    private int processedIndex = 0;

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

        TextView txtProp, txtDescription;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
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
        processedIndex++;

        if(events.contains(mItems.get(position))) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        /*

        if(NordicEvents.accelerometerValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valori di accellerazione");
        }
        else if(NordicEvents.airQualityValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Qualità dell'aria");
        }
        if(NordicEvents.batteryLevelChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valore della batteria");
        }
        if(NordicEvents.buttonStateChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Click del pulsante fisico");
        }
        if(NordicEvents.colorIntensityValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("idk");
        }
        if(NordicEvents.colorIntensityValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("idk");
        }
        if(NordicEvents.compassValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("idk");
        }
        if(NordicEvents.eulerAngleChangedE.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("idk");
        }
        if(NordicEvents.gravityVectorChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Vettore di gravità");
        }
        if(NordicEvents.gyroscopeValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valori del giroscopio");
        }
        if(NordicEvents.headingValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("idk");
        }
        if(NordicEvents.humidityValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valori di umidità");
        }
        if(NordicEvents.microphoneValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valori del microfono");
        }
        if(NordicEvents.orientationValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Cambiamento orientamento");
        }
        if(NordicEvents.pedometerValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valori di conta passi");
        }
        if(NordicEvents.pressureValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valori di pressione");
        }
        if(NordicEvents.quaternionValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("idk");
        }
        if(NordicEvents.rotationMatrixValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("idk");
        }
        if(NordicEvents.speakerStatusValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valori di speaker");
        }
        if(NordicEvents.tapValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("idk");
        }
        if(NordicEvents.temperatureValueChanged.equals(mItems.get(position))){
            holder.checkBox.setChecked(true);
            holder.txtDescription.setText("Valori di temperatura");
        }*/
    }

    @Override
    public int getItemCount() {
        return this.mItems.size();
    }
}