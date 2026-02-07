package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla principal del menú
 * 
 * @author DarkphoenixTeam
 */
public class HomeScreen extends BaseScreen {
    
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    private Texture patternTexture;
    private Array<SimpleButton> buttons;
    private AudioManager audioManager;
    
    private static final float TITLE_ZONE_PERCENT = 0.35f;
    private static final float BOTTOM_MARGIN = 40f;
    private static final float BUTTON_SPACING = 10f;
    private static final int BUTTON_COUNT = 5;
    private static final float MAX_BUTTON_WIDTH_PERCENT = 0.60f;
    
    private float buttonWidth;
    private float buttonHeight;
    private float animTimer = 0f;
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        buttons = new Array<>();
        
        audioManager = AudioManager.getInstance();
        
        // SIEMPRE iniciar música del menú al entrar
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        calculateButtonDimensions();
        loadAssets();
        createButtons();
        
        Gdx.app.log("HomeScreen", "Inicializado");
    }
    
    private void calculateButtonDimensions() {
        float viewportWidth = Constants.VIRTUAL_WIDTH;
        float viewportHeight = Constants.VIRTUAL_HEIGHT;
        
        float titleZoneHeight = viewportHeight * TITLE_ZONE_PERCENT;
        float availableHeight = viewportHeight - titleZoneHeight - BOTTOM_MARGIN;
        float totalSpacing = BUTTON_SPACING * (BUTTON_COUNT - 1);
        float heightForButtons = availableHeight - totalSpacing;
        float maxButtonHeight = heightForButtons / BUTTON_COUNT;
        
        // Usar aspect ratio de los nuevos botones (512x256 = 0.5)
        float widthFromHeight = maxButtonHeight / AssetPaths.BTN_ASPECT_RATIO;
        float maxWidth = viewportWidth * MAX_BUTTON_WIDTH_PERCENT;
        
        buttonWidth = Math.min(widthFromHeight, maxWidth);
        buttonHeight = buttonWidth * AssetPaths.BTN_ASPECT_RATIO;
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Pattern no encontrado");
        }
    }
    
    private void createButtons() {
        float viewportWidth = Constants.VIRTUAL_WIDTH;
        float viewportHeight = Constants.VIRTUAL_HEIGHT;
        
        float buttonX = (viewportWidth - buttonWidth) / 2f;
        float titleZoneHeight = viewportHeight * TITLE_ZONE_PERCENT;
        float startY = viewportHeight - titleZoneHeight - buttonHeight;
        float currentY = startY;
        
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
                    handleButtonClick(logText);
                });
                buttons.add(btn);
            }
            
            currentY -= (buttonHeight + BUTTON_SPACING);
        }
    }
    
    private SimpleButton createButton(String texturePath, String text, float x, float y) {
        try {
            Texture texture = new Texture(Gdx.files.internal(texturePath));
            return new SimpleButton(texture, text, x, y, buttonWidth, buttonHeight);
        } catch (Exception e) {
            Gdx.app.log("HomeScreen", "Error cargando: " + texturePath);
            return null;
        }
    }
    
    private void handleButtonClick(String buttonName) {
        audioManager.playSound(AssetPaths.SFX_BUTTON);
        
        switch (buttonName) {
            case "JUGAR":
                game.setScreen(new LevelSelectScreen(game));
                break;
            case "MAZO":
                game.setScreen(new DeckEditorScreen(game));
                break;
            case "BAZAAR":
                game.setScreen(new BazaarScreen(game));
                break;
            case "LOGROS":
            game.setScreen(new AchievementsScreen(game));  // ← ACTUALIZADO
            break;
        case "AJUSTES":
            game.setScreen(new SettingsScreen(game));
            break;
        }
    }
    
    @Override
    protected void update(float delta) {
        animTimer += delta;
        
        if (isInputEnabled()) {
            for (SimpleButton button : buttons) {
                button.update(viewport);
            }
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
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
        
        String title = "Kawaii Neko Memory";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleZoneCenter = Constants.VIRTUAL_HEIGHT - (Constants.VIRTUAL_HEIGHT * TITLE_ZONE_PERCENT / 2f);
        float titleY = titleZoneCenter + (layout.height / 2f);
        titleFont.draw(game.getBatch(), title, titleX, titleY);
        
        for (SimpleButton button : buttons) {
            button.draw(game.getBatch(), buttonFont);
        }
        
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
