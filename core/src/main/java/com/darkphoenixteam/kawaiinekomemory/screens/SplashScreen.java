package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;

/**
 * Pantalla de splash con logos del equipo y del juego
 * 
 * @author DarkphoenixTeam
 */
public class SplashScreen extends BaseScreen {
    
    private static final String TAG = "SplashScreen";
    
    private Texture teamLogo;
    private Texture gameLogo;
    
    private float timer = 0f;
    private boolean showingTeamLogo = true;
    private boolean assetsLoaded = false;
    
    public SplashScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(0.1f, 0.1f, 0.15f);
        
        loadAssets();
    }
    
    private void loadAssets() {
        try {
            Gdx.app.log(TAG, "Intentando cargar: " + AssetPaths.LOGO_DARKPHOENIX);
            teamLogo = new Texture(Gdx.files.internal(AssetPaths.LOGO_DARKPHOENIX));
            Gdx.app.log(TAG, "✓ Team logo cargado: " + teamLogo.getWidth() + "x" + teamLogo.getHeight());
        } catch (Exception e) {
            Gdx.app.error(TAG, "ERROR cargando team logo: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            Gdx.app.log(TAG, "Intentando cargar: " + AssetPaths.LOGO_GAME);
            gameLogo = new Texture(Gdx.files.internal(AssetPaths.LOGO_GAME));
            Gdx.app.log(TAG, "✓ Game logo cargado: " + gameLogo.getWidth() + "x" + gameLogo.getHeight());
        } catch (Exception e) {
            Gdx.app.error(TAG, "ERROR cargando game logo: " + e.getMessage());
            e.printStackTrace();
        }
        
        assetsLoaded = (teamLogo != null || gameLogo != null);
        
        if (!assetsLoaded) {
            Gdx.app.error(TAG, "⚠️ NO se cargaron logos, saltando directo a HomeScreen");
        }
    }
    
    @Override
    protected void update(float delta) {
        timer += delta;
        
        // Si no hay logos, saltar inmediatamente
        if (!assetsLoaded && timer > 0.5f) {
            Gdx.app.log(TAG, "Saltando a HomeScreen (sin logos)");
            game.setScreen(new HomeScreen(game));
            return;
        }
        
        // Cambio de logo del equipo al del juego
        if (showingTeamLogo && timer > Constants.SPLASH_DURATION) {
            showingTeamLogo = false;
            timer = 0f;
        }
        
        // Transición a HomeScreen
        if (!showingTeamLogo && timer > Constants.SPLASH_DURATION) {
            Gdx.app.log(TAG, "Transicionando a HomeScreen");
            game.setScreen(new HomeScreen(game));
        }
    }
    
    @Override
    protected void draw() {
        SpriteBatch batch = game.getBatch();
        batch.begin();
        
        // Calcular alpha para fade
        float fadeDuration = Constants.FADE_DURATION;
        float displayDuration = Constants.SPLASH_DURATION;
        float alpha = 1f;
        
        if (timer < fadeDuration) {
            // Fade in
            alpha = timer / fadeDuration;
        } else if (timer > displayDuration - fadeDuration) {
            // Fade out
            alpha = (displayDuration - timer) / fadeDuration;
        }
        
        alpha = Math.max(0f, Math.min(1f, alpha));
        batch.setColor(1f, 1f, 1f, alpha);
        
        // Determinar cuál logo mostrar
        Texture currentLogo = showingTeamLogo ? teamLogo : gameLogo;
        
        if (currentLogo != null) {
            // Calcular tamaño y posición para centrar
            float logoWidth = Constants.VIRTUAL_WIDTH * 0.7f;
            float logoHeight = logoWidth * ((float) currentLogo.getHeight() / currentLogo.getWidth());
            float logoX = (Constants.VIRTUAL_WIDTH - logoWidth) / 2f;
            float logoY = (Constants.VIRTUAL_HEIGHT - logoHeight) / 2f;
            
            batch.draw(currentLogo, logoX, logoY, logoWidth, logoHeight);
        } else {
            // Fallback: Texto si no hay texturas
            batch.setColor(1f, 1f, 1f, 1f);
            String text = showingTeamLogo ? "DarkphoenixTeam" : "Kawaii Neko Memory";
            // Aquí normalmente usarías BitmapFont, pero para diagnóstico es suficiente
        }
        
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }
    
    @Override
    public void dispose() {
        if (teamLogo != null) {
            teamLogo.dispose();
        }
        if (gameLogo != null) {
            gameLogo.dispose();
        }
    }
}