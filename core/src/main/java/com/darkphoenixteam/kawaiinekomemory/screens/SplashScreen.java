package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;

/**
 * Pantalla de splash (DIAGNÓSTICO V2)
 * Corregido para usar game.getBatch() y cumplir contrato de BaseScreen
 */
public class SplashScreen extends BaseScreen {

    private static final String TAG = "SplashScreen";

    private Texture teamLogo;
    private Texture gameLogo;
    private BitmapFont debugFont; 

    private float timer = 0f;
    private boolean showingTeamLogo = true;
    private boolean assetsLoaded = false;
    private String errorMessage = "";

    public SplashScreen(KawaiiNekoMemory game) {
        super(game);
        setBackgroundColor(0.2f, 0.2f, 0.2f); 
        debugFont = new BitmapFont(); 
        debugFont.getData().setScale(2.0f);
        
        loadAssets();
    }

    // === CORRECCIÓN: Método obligatorio por BaseScreen ===
    // Lo dejamos vacío porque usaremos render() para tener control total
    // Si la compilación falla pidiendo argumentos aquí, avísame.
    @Override
    public void draw() {
    }

    private void loadAssets() {
        try {
            // Verificamos existencia antes de cargar
            if (!Gdx.files.internal(AssetPaths.LOGO_DARKPHOENIX).exists()) {
                errorMessage += "NO EXISTE: " + AssetPaths.LOGO_DARKPHOENIX + "\n";
            } else {
                teamLogo = new Texture(Gdx.files.internal(AssetPaths.LOGO_DARKPHOENIX));
            }

            if (!Gdx.files.internal(AssetPaths.LOGO_GAME).exists()) {
                errorMessage += "NO EXISTE: " + AssetPaths.LOGO_GAME + "\n";
            } else {
                gameLogo = new Texture(Gdx.files.internal(AssetPaths.LOGO_GAME));
            }

            assetsLoaded = true;

        } catch (Exception e) {
            Gdx.app.error(TAG, "Error fatal cargando assets", e);
            errorMessage += "EXCEPCION:\n" + e.getMessage();
            assetsLoaded = false;
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta); 

        // === CORRECCIÓN: Obtener el batch desde 'game' ===
        SpriteBatch batch = game.getBatch();

        batch.begin();

        if (assetsLoaded && errorMessage.isEmpty()) {
            // === MODO NORMAL ===
            timer += delta;
            
            float alpha = 1f;
            if (timer < Constants.FADE_DURATION) {
                alpha = timer / Constants.FADE_DURATION;
            }

            batch.setColor(1f, 1f, 1f, alpha);

            Texture currentLogo = showingTeamLogo ? teamLogo : gameLogo;
            
            if (currentLogo != null) {
                float logoWidth = Constants.VIRTUAL_WIDTH * 0.7f;
                float logoHeight = logoWidth * ((float) currentLogo.getHeight() / currentLogo.getWidth());
                float logoX = (Constants.VIRTUAL_WIDTH - logoWidth) / 2f;
                float logoY = (Constants.VIRTUAL_HEIGHT - logoHeight) / 2f;
                batch.draw(currentLogo, logoX, logoY, logoWidth, logoHeight);
            }

            if (timer > Constants.SPLASH_DURATION) {
                if (showingTeamLogo) {
                    showingTeamLogo = false;
                    timer = 0;
                } else {
                    game.setScreen(new HomeScreen(game));
                }
            }
        } else {
            // === MODO PÁNICO (Mostrar Error) ===
            batch.setColor(1f, 0.2f, 0.2f, 1f); 
            
            debugFont.draw(batch, "ERROR CRITICO DETECTADO:", 20, Constants.VIRTUAL_HEIGHT - 50);
            
            batch.setColor(1f, 1f, 1f, 1f); 
            debugFont.draw(batch, errorMessage, 20, Constants.VIRTUAL_HEIGHT - 150, 
                           Constants.VIRTUAL_WIDTH - 40, -1, true);
                           
            debugFont.draw(batch, "Buscando: " + AssetPaths.LOGO_DARKPHOENIX, 
                           20, 200, Constants.VIRTUAL_WIDTH - 40, -1, true);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        if (teamLogo != null) teamLogo.dispose();
        if (gameLogo != null) gameLogo.dispose();
        if (debugFont != null) debugFont.dispose();
    }
}
