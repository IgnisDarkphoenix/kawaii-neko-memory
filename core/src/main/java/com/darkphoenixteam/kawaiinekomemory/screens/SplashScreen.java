package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.utils.Constants;

public class SplashScreen extends BaseScreen {
    
    private float timer = 0f;
    
    public SplashScreen(KawaiiNekoMemory game) {
        super(game);
        // Fondo oscuro
        setBackgroundColor(0.1f, 0.1f, 0.15f);
        Gdx.app.log("SplashScreen", "Created successfully");
    }
    
    @Override
    public void render(float delta) {
        super.render(delta);
        timer += delta;
        
        // Después de 3 segundos, ir a HomeScreen
        if (timer >= 3.0f) {
            Gdx.app.log("SplashScreen", "Going to HomeScreen...");
            game.setScreen(new HomeScreen(game));
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.log("SplashScreen", "Disposed");
    }
}