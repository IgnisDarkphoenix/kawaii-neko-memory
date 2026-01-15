package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla principal del menú
 * 
 * @author DarkphoenixTeam
 */
public class HomeScreen extends BaseScreen {
    
    // UI
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // Background
    private Texture patternTexture;
    
    // Botones
    private Array<SimpleButton> buttons;
    
    // === LAYOUT CONFIG ===
    // Zona de título: 35% superior de la pantalla
    private static final float TITLE_ZONE_PERCENT = 0.35f;
    // Margen inferior
    private static final float BOTTOM_MARGIN = 40f;
    // Espaciado entre botones
    private static final float BUTTON_SPACING = 10f;
    // Número de botones
    private static final int BUTTON_COUNT = 5;
    // Aspect ratio de los botones (1024x512 = 2:1, o sea height = width * 0.5)
    private static final float BUTTON_ASPECT_RATIO = 0.5f;
    // Ancho máximo del botón (porcentaje del viewport)
    private static final float MAX_BUTTON_WIDTH_PERCENT = 0.60f;
    
    // Calculados
    private float buttonWidth;
    private float buttonHeight;
    
    private float animTimer = 0f;
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        // Obtener fuentes del FontManager
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        
        layout = new GlyphLayout();
        buttons = new Array<>();
        
        // Calcular dimensiones de botones
        calculateButtonDimensions();
        
        loadAssets();
        createButtons();
        
        Gdx.app.log("HomeScreen", "Inicializado - Botones: " + buttonWidth + "x" + buttonHeight);
    }
    
    /**
     * Calcula las dimensiones de los botones para que quepan en el espacio disponible
     */
    private void calculateButtonDimensions() {
        float viewportWidth = Constants.VIRTUAL_WIDTH;
        float viewportHeight = Constants.VIRTUAL_HEIGHT;
        
        // Espacio vertical disponible para botones
        float titleZoneHeight = viewportHeight * TITLE_ZONE_PERCENT;
        float availableHeight = viewportHeight - titleZoneHeight - BOTTOM_MARGIN;
        
        // Espacio ocupado por los espaciados entre botones
        float totalSpacing = BUTTON_SPACING * (BUTTON_COUNT - 1);
        
        // Altura disponible para los botones en sí
        float heightForButtons = availableHeight - totalSpacing;
        
        // Altura máxima por botón basada en espacio disponible
        float maxButtonHeight = heightForButtons / BUTTON_COUNT;
        
        // Ancho correspondiente a esa altura (respetando aspect ratio)
        float widthFromHeight = maxButtonHeight / BUTTON_ASPECT_RATIO;
        
        // Ancho máximo permitido (60% del viewport)
        float maxWidth = viewportWidth * MAX_BUTTON_WIDTH_PERCENT;
        
        // Usar el menor de los dos para no desbordar
        buttonWidth = Math.min(widthFromHeight, maxWidth);
        buttonHeight = buttonWidth * BUTTON_ASPECT_RATIO;
        
        Gdx.app.log("HomeScreen", String.format(
            "Layout calculado: disponible=%.0f, maxHeight=%.0f, final=%.0fx%.0f",
            availableHeight, maxButtonHeight, buttonWidth, buttonHeight
        ));
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            Gdx.app.log("HomeScreen", "Pattern cargado");
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Pattern no encontrado");
        }
    }
    
    private void createButtons() {
        float viewportWidth = Constants.VIRTUAL_WIDTH;
        float viewportHeight = Constants.VIRTUAL_HEIGHT;
        
        // Centrar horizontalmente
        float buttonX = (viewportWidth - buttonWidth) / 2f;
        
        // Calcular Y inicial (debajo de la zona del título)
        float titleZoneHeight = viewportHeight * TITLE_ZONE_PERCENT;
        float startY = viewportHeight - titleZoneHeight - buttonHeight;
        
        float currentY = startY;
        
        // Array de datos de botones para crear en loop
        String[][] buttonData = {
            {AssetPaths.BTN_PLAY, "JUGAR"},
            {AssetPaths.BTN_DECK, "MAZO"},
            {AssetPaths.BTN_BAZAAR, "BAZAAR"},
            {AssetPaths.BTN_ACHIEVEMENTS, "LOGROS"},
            {AssetPaths.BTN_SETTINGS, "AJUSTES"}
        };
        
        for (int i = 0; i < buttonData.length; i++) {
            String texturePath = buttonData[i][0];
            String buttonText = buttonData[i][1];
            
            SimpleButton btn = createButton(texturePath, buttonText, buttonX, currentY);
            if (btn != null) {
                final String logText = buttonText;
                btn.setOnClick(() -> {
                    Gdx.app.log("HomeScreen", ">>> " + logText + " presionado <<<");
                    handleButtonClick(logText);
                });
                buttons.add(btn);
            }
            
            currentY -= (buttonHeight + BUTTON_SPACING);
        }
        
        Gdx.app.log("HomeScreen", "Botones creados: " + buttons.size);
    }
    
    private SimpleButton createButton(String texturePath, String text, float x, float y) {
        try {
            Texture texture = new Texture(Gdx.files.internal(texturePath));
            // Usar constructor con width Y height explícitos para control total
            return new SimpleButton(texture, text, x, y, buttonWidth, buttonHeight);
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Error cargando: " + texturePath);
            return null;
        }
    }
    
    /**
     * Maneja los clicks de los botones
     */
    private void handleButtonClick(String buttonName) {
        switch (buttonName) {
            case "JUGAR":
                // TODO: game.setScreen(new LevelSelectScreen(game));
                break;
            case "MAZO":
                // TODO: game.setScreen(new DeckScreen(game));
                break;
            case "BAZAAR":
                // TODO: game.setScreen(new BazaarScreen(game));
                break;
            case "LOGROS":
                // TODO: game.setScreen(new AchievementsScreen(game));
                break;
            case "AJUSTES":
                // TODO: game.setScreen(new SettingsScreen(game));
                break;
        }
    }
    
    @Override
    protected void update(float delta) {
        animTimer += delta;
        
        for (SimpleButton button : buttons) {
            button.update(viewport);
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Pattern de fondo
        if (patternTexture != null) {
            game.getBatch().setColor(1f, 1f, 1f, 0.3f);
            int tileSize = 512;
            for (int x = 0; x < Constants.VIRTUAL_WIDTH; x += tileSize) {
                for (int y = 0; y < Constants.VIRTUAL_HEIGHT; y += tileSize) {
                    game.getBatch().draw(patternTexture, x, y, tileSize, tileSize);
                }
            }
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        }
        
        // === TÍTULO ===
        // Centrado en la zona superior (35% de la pantalla)
        String title = "Kawaii Neko Memory";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleZoneCenter = Constants.VIRTUAL_HEIGHT - (Constants.VIRTUAL_HEIGHT * TITLE_ZONE_PERCENT / 2f);
        float titleY = titleZoneCenter + (layout.height / 2f);
        titleFont.draw(game.getBatch(), title, titleX, titleY);
        
        // === BOTONES ===
        for (SimpleButton button : buttons) {
            button.draw(game.getBatch(), buttonFont);
        }
        
        // === VERSIÓN ===
        String version = "v1.0.0 - DarkphoenixTeam";
        layout.setText(smallFont, version);
        float verX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        smallFont.draw(game.getBatch(), version, verX, 25f);
        
        game.getBatch().end();
    }
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        
        for (SimpleButton button : buttons) {
            button.dispose();
        }
    }
}