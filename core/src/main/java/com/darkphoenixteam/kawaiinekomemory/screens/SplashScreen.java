package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;

/**
 * Pantalla de Splash inicial
 * Muestra el logo de DarkphoenixTeam y luego el logo del juego
 * 
 * @author DarkphoenixTeam
 */
public class SplashScreen extends BaseScreen {
    
    private static final float SPLASH_DURATION = 2.0f; // Segundos por splash
    private static final float FADE_DURATION = 0.5f;
    
    private enum SplashState {
        TEAM_LOGO,
        GAME_LOGO,
        TRANSITIONING
    }
    
    private SplashState currentState;
    private float stateTimer;
    private float alpha;
    
    // Fuente temporal (después cargaremos assets reales)
    private BitmapFont font;
    private GlyphLayout layout;
    
    // Textos a mostrar
    private String currentText;
    private static final String TEAM_TEXT = "DarkphoenixTeam";
    private static final String GAME_TEXT = "Kawaii Neko Memory";
    
    public SplashScreen(KawaiiNekoMemory game) {
        super(game);
        
        // Color de fondo oscuro para splash
        setBackgroundColor(0.1f, 0.1f, 0.15f);
        
        // Crear fuente temporal
        font = new BitmapFont();
        font.getData().setScale(3f);
        layout = new GlyphLayout();
        
        // Iniciar con logo del equipo
        currentState = SplashState.TEAM_LOGO;
        currentText = TEAM_TEXT;
        stateTimer = 0f;
        alpha = 0f;
    }
    
    @Override
    protected void update(float delta) {
        stateTimer += delta;
        
        switch (currentState) {
            case TEAM_LOGO:
            case GAME_LOGO:
                updateSplash(delta);
                break;
            case TRANSITIONING:
                // Ir a HomeScreen cuando termine
                goToHome();
                break;
        }
    }
    
    private void updateSplash(float delta) {
        // Fade in
        if (stateTimer < FADE_DURATION) {
            alpha = stateTimer / FADE_DURATION;
        }
        // Mantener visible
        else if (stateTimer < SPLASH_DURATION - FADE_DURATION) {
            alpha = 1f;
        }
        // Fade out
        else if (stateTimer < SPLASH_DURATION) {
            alpha = 1f - ((stateTimer - (SPLASH_DURATION - FADE_DURATION)) / FADE_DURATION);
        }
        // Cambiar estado
        else {
            alpha = 0f;
            stateTimer = 0f;
            
            if (currentState == SplashState.TEAM_LOGO) {
                currentState = SplashState.GAME_LOGO;
                currentText = GAME_TEXT;
            } else {
                currentState = SplashState.TRANSITIONING;
            }
        }
    }
    
    private void goToHome() {
        Gdx.app.log("SplashScreen", "Transitioning to HomeScreen");
        game.setScreen(new HomeScreen(game));
        dispose();
    }
    
    @Override
    protected void draw() {
        if (currentState == SplashState.TRANSITIONING) return;
        
        // Calcular posición centrada
        layout.setText(font, currentText);
        float x = (KawaiiNekoMemory.VIRTUAL_WIDTH - layout.width) / 2f;
        float y = (KawaiiNekoMemory.VIRTUAL_HEIGHT + layout.height) / 2f;
        
        // Dibujar texto con alpha
        game.getBatch().begin();
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(game.getBatch(), currentText, x, y);
        game.getBatch().end();
    }
    
    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
    }
}