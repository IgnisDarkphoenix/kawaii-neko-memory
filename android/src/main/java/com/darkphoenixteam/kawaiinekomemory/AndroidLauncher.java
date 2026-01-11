package com.darkphoenixteam.kawaiinekomemory;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * Launcher de Android para Kawaii Neko Memory
 * Configura LibGDX y prepara el contenedor para Ads
 * 
 * @author DarkphoenixTeam
 */
public class AndroidLauncher extends AndroidApplication {
    
    private static final String TAG = "AndroidLauncher";
    
    // Layout principal que contendrá el juego y los ads
    private RelativeLayout mainLayout;
    
    // Controlador de anuncios
    private AndroidAdController adController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configuración de LibGDX
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useImmersiveMode = true;  // Pantalla completa
        config.numSamples = 2;  // Anti-aliasing ligero
        
        // Mantener pantalla encendida durante el juego
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Crear el controlador de ads
        adController = new AndroidAdController(this);
        
        // Crear el juego con el controlador de ads
        KawaiiNekoMemory game = new KawaiiNekoMemory(adController);
        
        // Crear layout principal
        mainLayout = new RelativeLayout(this);
        
        // Crear vista del juego LibGDX
        View gameView = initializeForView(game, config);
        
        // Agregar vista del juego al layout
        RelativeLayout.LayoutParams gameParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        );
        mainLayout.addView(gameView, gameParams);
        
        // Configurar el layout principal como vista
        setContentView(mainLayout);
        
        // Inicializar SDK de ads
        adController.initialize(mainLayout);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (adController != null) {
            adController.onResume();
        }
    }
    
    @Override
    protected void onPause() {
        if (adController != null) {
            adController.onPause();
        }
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        if (adController != null) {
            adController.onDestroy();
        }
        super.onDestroy();
    }
}