package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;

/**
 * Pantalla de Splash
 * Muestra: DarkphoenixTeam logo → Game logo → HomeScreen
 * 
 * @author DarkphoenixTeam
 */
public class SplashScreen extends BaseScreen {
    
    private enum State {
        TEAM_LOGO,
        GAME_LOGO,
        DONE
    }
    
    private State state;
    private float timer;
    private float alpha;
    
    private Texture teamLogoTexture;
    private Sprite teamLogo;
    
    // Game logo será agregado después cuando tengas el asset
    private boolean hasGameLogo = false;
    
    public SplashScreen(KawaiiNekoMemory game) {
        super(game);
        
        // Fondo oscuro para splash
        setBackgroundColor(0.1f, 0.1f, 0.12f);
        
        state = State.TEAM_LOGO;
        timer = 0f;
        alpha = 0f;
        
        loadAssets();
    }
    
    private void loadAssets() {
        try {
            teamLogoTexture = new Texture(Gdx.files.internal(AssetPaths.LOGO_DARKPHOENIX));
            teamLogo = new Sprite(teamLogoTexture);
            
            // Escalar logo para que quepa en pantalla (máximo 80% del ancho)
            float maxWidth = Constants.VIRTUAL_WIDTH * 0.8f;
            float scale = maxWidth / teamLogo.getWidth();
            if (scale > 1f) scale = 1f; // No agrandar
            
            teamLogo.setSize(teamLogo.getWidth() * scale, teamLogo.getHeight() * scale);
            teamLogo.setPosition(
                (Constants.VIRTUAL_WIDTH - teamLogo.getWidth()) / 2f,
                (Constants.VIRTUAL_HEIGHT - teamLogo.getHeight()) / 2f
            );
            
            Gdx.app.log("SplashScreen", "Logo cargado OK");
        } catch (Exception e) {
            Gdx.app.error("SplashScreen", "Error cargando logo: " + e.getMessage());
        }
    }
    
    @Override
    protected void update(float delta) {
        timer += delta;
        
        if (state == State.DONE) {
            goToHome();
            return;
        }
        
        // Calcular alpha para fade in/out
        float fadeDuration = Constants.FADE_DURATION;
        float totalDuration = Constants.SPLASH_DURATION;
        
        if (timer < fadeDuration) {
            // Fade in
            alpha = timer / fadeDuration;
        } else if (timer < totalDuration - fadeDuration) {
            // Visible
            alpha = 1f;
        } else if (timer < totalDuration) {
            // Fade out
            alpha = 1f - ((timer - (totalDuration - fadeDuration)) / fadeDuration);
        } else {
            // Siguiente estado
            alpha = 0f;
            timer = 0f;
            
            if (state == State.TEAM_LOGO) {
                if (hasGameLogo) {
                    state = State.GAME_LOGO;
                } else {
                    state = State.DONE;
                }
            } else if (state == State.GAME_LOGO) {
                state = State.DONE;
            }
        }
    }
    
    @Override
    protected void draw() {
        if (state == State.DONE) return;
        
        game.getBatch().begin();
        
        if (state == State.TEAM_LOGO && teamLogo != null) {
            teamLogo.setAlpha(alpha);
            teamLogo.draw(game.getBatch());
        }
        
        // Game logo se agregará después
        
        game.getBatch().end();
    }
    
    private void goToHome() {
        Gdx.app.log("SplashScreen", "Ir a HomeScreen");
        game.setScreen(new HomeScreen(game));
        dispose();
    }
    
    @Override
    public void dispose() {
        if (teamLogoTexture != null) {
            teamLogoTexture.dispose();
        }
    }
}