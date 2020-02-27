package it.chiarani.beacon_detection.db;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.chiarani.beacon_detection.models.NordicEvents;

/**
 * Converter for room. Convert {@link NordicEvents}
 */
public class NordicEventsConverter {

    @TypeConverter
    public List<NordicEvents> nordicEventsStringToEnum(String value) {
        List<String> dbValues = Arrays.asList(value.split("\\s*,\\s*"));
        List<NordicEvents> enums = new ArrayList<>();

        for (String s: dbValues)
            enums.add(NordicEvents.valueOf(s));

        return enums;
    }

    @TypeConverter
    public String nordicEventsToStoredString(List<NordicEvents> cl) {
        String value = "";

        for (NordicEvents lang : cl)
            value += lang.name() + ",";

        return value;
    }
}
