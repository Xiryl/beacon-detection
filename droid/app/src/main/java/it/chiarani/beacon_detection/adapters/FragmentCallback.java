package it.chiarani.beacon_detection.adapters;

import java.util.List;

import it.chiarani.beacon_detection.controllers.FragmentCallbackType;

/**
 * Click listener
 */
public interface FragmentCallback {
    /**
     *
     * @param fragmentType 1 for Discovery, 2 for datacollected
     * @param type message
     */
    void onFragmentCallback(int fragmentType, FragmentCallbackType messageType, List<String> MACFilterList);
}
