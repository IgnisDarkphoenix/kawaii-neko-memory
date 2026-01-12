package com.darkphoenixteam.kawaiinekomemory;

import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * Launcher de Android para Kawaii Neko Memory
 * 
 * @author DarkphoenixTeam
 */
public class AndroidLauncher extends AndroidApplication {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useImmersiveMode = true;
        config.numSamples = 2;
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        initialize(new KawaiiNekoMemory(), config);
    }
}