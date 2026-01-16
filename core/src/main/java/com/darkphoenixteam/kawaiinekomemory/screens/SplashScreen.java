package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;

/**
 * Pantalla de splash MODIFICADA PARA DIAGNÓSTICO
 * Si falla la carga, mostrará el error en pantalla.
 */
public class SplashScreen extends BaseScreen {

    private static final String TAG = "SplashScreen";

    private Texture teamLogo;
    private Texture gameLogo;
    private BitmapFont debugFont; // Fuente de sistema para mostrar errores

    private float timer = 0f;
    private boolean showingTeamLogo = true;
    private boolean assetsLoaded = false;
    
    // Variable para guardar el mensaje de error si algo falla
    private String errorMessage = "";

    public SplashScreen(KawaiiNekoMemory game) {
        super(game);
        // Fondo gris oscuro para que resalte el texto blanco
        setBackgroundColor(0.2f, 0.2f, 0.2f); 
        debugFont = new BitmapFont(); // Carga la fuente Arial por defecto de LibGDX
        debugFont.getData().setScale(2.0f); // Hacerla grande para leerla en el cel
        
        loadAssets();
    }

    private void loadAssets() {
        try {
            // Intentamos cargar PRIMERO el logo del equipo
            // Usamos FileHandle para verificar existencia antes de cargar textura
            if (!Gdx.files.internal(AssetPaths.LOGO_DARKPHOENIX).exists()) {
                errorMessage += "NO EXISTE: " + AssetPaths.LOGO_DARKPHOENIX + "\n";
            } else {
                teamLogo = new Texture(Gdx.files.internal(AssetPaths.LOGO_DARKPHOENIX));
            }

            // Intentamos cargar el logo del juego
            if (!Gdx.files.internal(AssetPaths.LOGO_GAME).exists()) {
                errorMessage += "NO EXISTE: " + AssetPaths.LOGO_GAME + "\n";
            } else {
                gameLogo = new Texture(Gdx.files.internal(AssetPaths.LOGO_GAME));
            }

            assetsLoaded = true;

        } catch (Exception e) {
            // AQUÍ ESTÁ LA CLAVE: Capturamos el error y lo guardamos para mostrarlo
            Gdx.app.error(TAG, "Error fatal cargando assets", e);
            errorMessage += "ERROR EXCEPCION:\n" + e.getMessage();
            assetsLoaded = false;
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta); // Limpia la pantalla con el color de fondo

        batch.begin();

        if (assetsLoaded && errorMessage.isEmpty()) {
            // === FLUJO NORMAL (Si todo cargó bien) ===
            timer += delta;
            
            // Lógica de fade simple
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

            // Cambio de pantalla
            if (timer > Constants.SPLASH_DURATION) {
                if (showingTeamLogo) {
                    showingTeamLogo = false;
                    timer = 0;
                } else {
                    game.setScreen(new HomeScreen(game));
                }
            }
        } else {
            // === MODO PÁNICO (Si hay errores) ===
            // NO cambiamos de pantalla, nos quedamos aquí mostrando el error
            batch.setColor(1f, 0.2f, 0.2f, 1f); // Texto rojo claro
            
            debugFont.draw(batch, "ERROR DE CARGA DETECTADO:", 20, Constants.VIRTUAL_HEIGHT - 50);
            
            batch.setColor(1f, 1f, 1f, 1f); // Texto blanco
            // Dibujamos el mensaje de error en el centro
            debugFont.draw(batch, errorMessage, 20, Constants.VIRTUAL_HEIGHT - 150, 
                           Constants.VIRTUAL_WIDTH - 40, -1, true);
                           
            debugFont.draw(batch, "Ruta buscada: " + AssetPaths.LOGO_DARKPHOENIX, 
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
