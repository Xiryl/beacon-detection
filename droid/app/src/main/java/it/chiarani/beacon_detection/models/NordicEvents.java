package it.chiarani.beacon_detection.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

public enum  NordicEvents {
      batteryLevelChanged,
      temperatureValueChanged,
      pressureValueChanged,
      humidityValueChanged,
      airQualityValueChanged,
      colorIntensityValueChanged,
      buttonStateChanged,
      tapValueChanged,
      orientationValueChanged,
      quaternionValueChanged,
      pedometerValueChanged,
      accelerometerValueChanged,
      gyroscopeValueChanged,
      compassValueChanged,
      eulerAngleChanged,
      rotationMatrixValueChanged,
      headingValueChanged,
      gravityVectorChanged,
      speakerStatusValueChanged,
}
