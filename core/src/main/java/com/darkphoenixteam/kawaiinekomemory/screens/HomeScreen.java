package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.models.Achievement;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.LocaleManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla principal del menú con localización
 * 
 * @author DarkphoenixTeam
 * @version 2.1 - Localización completa
 */
public class HomeScreen extends BaseScreen {
    
    private static final String TAG = "HomeScreen";
    
    // === FONTS ===
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // === TEXTURAS ===
    private Texture patternTexture;
    private Texture logoTexture;
    private Texture nekoinIconTexture;
    
    // === BOTONES ===
    private Array<SimpleButton> buttons;
    private SimpleButton rankingsButton;
    
    // === LOGO CLICKEABLE ===
    private Rectangle logoBounds;
    private int logoClickCount = 0;
    private static final int CLICKS_FOR_ACHIEVEMENT = 10;
    private float logoClickResetTimer = 0f;
    private static final float LOGO_CLICK_RESET_TIME = 3f;
    private boolean achievementUnlocked = false;
    
    // === LAYOUT ===
    private static final float TITLE_ZONE_PERCENT = 0.32f;
    private static final float BOTTOM_MARGIN = 40f;
    private static final float BUTTON_SPACING = 8f;
    private static final int BUTTON_COUNT = 5;
    private static final float MAX_BUTTON_WIDTH_PERCENT = 0.60f;
    
    private float buttonWidth;
    private float buttonHeight;
    private float animTimer = 0f;
    
    // === SISTEMAS ===
    private AudioManager audioManager;
    private SaveManager saveManager;
    private LocaleManager locale;
    
    // === INPUT ===
    private final Vector2 touchPoint = new Vector2();
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        buttons = new Array<>();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        locale = LocaleManager.getInstance();
        
        achievementUnlocked = saveManager.isAchievementUnlocked(Achievement.CLICKER_CAT);
        
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        calculateButtonDimensions();
        loadAssets();
        createButtons();
        createRankingsButton();
        createLogoBounds();
        
        Gdx.app.log(TAG, "Inicializado | Idioma: " + locale.getCurrentLanguage().displayName);
    }
    
    private void calculateButtonDimensions() {
        float viewportWidth = Constants.VIRTUAL_WIDTH;
        float viewportHeight = Constants.VIRTUAL_HEIGHT;
        
        float titleZoneHeight = viewportHeight * TITLE_ZONE_PERCENT;
        float availableHeight = viewportHeight - titleZoneHeight - BOTTOM_MARGIN;
        float totalSpacing = BUTTON_SPACING * (BUTTON_COUNT - 1);
        float heightForButtons = availableHeight - totalSpacing;
        float maxButtonHeight = heightForButtons / BUTTON_COUNT;
        
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
            Gdx.app.log(TAG, "Pattern no encontrado");
        }
        
        try {
            logoTexture = new Texture(Gdx.files.internal(AssetPaths.LOGO_GAME));
        } catch (Exception e) {
            Gdx.app.log(TAG, "Logo no encontrado");
        }
        
        try {
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
        } catch (Exception e) {
            Gdx.app.log(TAG, "Nekoin icon no encontrado");
        }
    }
    
    private void createButtons() {
        float viewportWidth = Constants.VIRTUAL_WIDTH;
        float viewportHeight = Constants.VIRTUAL_HEIGHT;
        
        float buttonX = (viewportWidth - buttonWidth) / 2f;
        float titleZoneHeight = viewportHeight * TITLE_ZONE_PERCENT;
        float startY = viewportHeight - titleZoneHeight - buttonHeight;
        float currentY = startY;
        
        // Datos de botones: [textura, key de localización, acción]
        String[][] buttonData = {
            {AssetPaths.BTN_PLAY, "home.play", "PLAY"},
            {AssetPaths.BTN_DECK, "home.deck", "DECK"},
            {AssetPaths.BTN_BAZAAR, "home.bazaar", "BAZAAR"},
            {AssetPaths.BTN_ACHIEVEMENTS, "home.achievements", "ACHIEVEMENTS"},
            {AssetPaths.BTN_SETTINGS, "home.settings", "SETTINGS"}
        };
        
        for (int i = 0; i < buttonData.length; i++) {
            String texturePath = buttonData[i][0];
            String localeKey = buttonData[i][1];
            String action = buttonData[i][2];
            
            String buttonText = locale.get(localeKey);
            
            SimpleButton btn = createButton(texturePath, buttonText, buttonX, currentY);
            if (btn != null) {
                final String actionName = action;
                btn.setOnClick(() -> handleButtonClick(actionName));
                buttons.add(btn);
            }
            
            currentY -= (buttonHeight + BUTTON_SPACING);
        }
    }
    
    private void createRankingsButton() {
        try {
            Texture btnTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_EMPTY));
            float btnSize = 50f;
            float btnX = Constants.VIRTUAL_WIDTH - btnSize - 10f;
            float btnY = Constants.VIRTUAL_HEIGHT - btnSize - 10f;
            
            rankingsButton = new SimpleButton(btnTexture, "🏆", btnX, btnY, btnSize, btnSize);
            rankingsButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new RankingsScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error creando botón rankings");
        }
    }
    
    private void createLogoBounds() {
        if (logoTexture != null) {
            float logoWidth = Constants.VIRTUAL_WIDTH * 0.6f;
            float logoHeight = logoWidth * ((float) logoTexture.getHeight() / logoTexture.getWidth());
            float logoX = (Constants.VIRTUAL_WIDTH - logoWidth) / 2f;
            float titleZoneCenter = Constants.VIRTUAL_HEIGHT - (Constants.VIRTUAL_HEIGHT * TITLE_ZONE_PERCENT / 2f);
            float logoY = titleZoneCenter - (logoHeight / 2f) + 20f;
            
            logoBounds = new Rectangle(logoX, logoY, logoWidth, logoHeight);
        } else {
            logoBounds = new Rectangle(
                Constants.VIRTUAL_WIDTH * 0.1f,
                Constants.VIRTUAL_HEIGHT - Constants.VIRTUAL_HEIGHT * TITLE_ZONE_PERCENT,
                Constants.VIRTUAL_WIDTH * 0.8f,
                Constants.VIRTUAL_HEIGHT * TITLE_ZONE_PERCENT * 0.8f
            );
        }
    }
    
    private SimpleButton createButton(String texturePath, String text, float x, float y) {
        try {
            Texture texture = new Texture(Gdx.files.internal(texturePath));
            return new SimpleButton(texture, text, x, y, buttonWidth, buttonHeight);
        } catch (Exception e) {
            Gdx.app.log(TAG, "Error cargando: " + texturePath);
            return null;
        }
    }
    
    private void handleButtonClick(String action) {
        audioManager.playSound(AssetPaths.SFX_BUTTON);
        
        switch (action) {
            case "PLAY":
                game.setScreen(new LevelSelectScreen(game));
                break;
            case "DECK":
                game.setScreen(new DeckEditorScreen(game));
                break;
            case "BAZAAR":
                game.setScreen(new BazaarScreen(game));
                break;
            case "ACHIEVEMENTS":
                game.setScreen(new AchievementsScreen(game));
                break;
            case "SETTINGS":
                game.setScreen(new SettingsScreen(game));
                break;
        }
    }
    
    private void handleLogoClick() {
        if (achievementUnlocked) {
            audioManager.playSound(AssetPaths.SFX_BUTTON);
            return;
        }
        
        logoClickCount++;
        logoClickResetTimer = LOGO_CLICK_RESET_TIME;
        
        audioManager.playSound(AssetPaths.SFX_CARD_FLIP);
        
        if (logoClickCount >= CLICKS_FOR_ACHIEVEMENT) {
            saveManager.unlockAchievement(Achievement.CLICKER_CAT);
            achievementUnlocked = true;
            audioManager.playSound(AssetPaths.SFX_VICTORY);
        }
    }
    
    @Override
    protected void update(float delta) {
        animTimer += delta;
        
        if (logoClickResetTimer > 0) {
            logoClickResetTimer -= delta;
            if (logoClickResetTimer <= 0) {
                logoClickCount = 0;
            }
        }
        
        if (!isInputEnabled()) return;
        
        for (SimpleButton button : buttons) {
            button.update(viewport);
        }
        
        if (rankingsButton != null) {
            rankingsButton.update(viewport);
        }
        
        if (Gdx.input.justTouched()) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            
            if (logoBounds != null && logoBounds.contains(touchPoint.x, touchPoint.y)) {
                handleLogoClick();
            }
        }
    }
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Fondo
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
        
        // Header
        drawHeader();
        
        // Logo o título
        drawLogoOrTitle();
        
        // Botones
        for (SimpleButton button : buttons) {
            button.draw(game.getBatch(), buttonFont);
        }
        
        // Rankings
        if (rankingsButton != null) {
            rankingsButton.draw(game.getBatch(), buttonFont);
        }
        
        // Versión
        String version = locale.get("game.version");
        layout.setText(smallFont, version);
        smallFont.setColor(Color.GRAY);
        smallFont.draw(game.getBatch(), version, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 20f);
        smallFont.setColor(Color.WHITE);
        
        // Progreso de clicks
        if (logoClickCount > 0 && !achievementUnlocked) {
            drawClickProgress();
        }
        
        game.getBatch().end();
    }
    
    private void drawHeader() {
        if (nekoinIconTexture != null) {
            float iconSize = 28f;
            float iconX = 10f;
            float iconY = Constants.VIRTUAL_HEIGHT - iconSize - 10f;
            
            game.getBatch().draw(nekoinIconTexture, iconX, iconY, iconSize, iconSize);
            
            String nekoins = String.valueOf(saveManager.getNekoins());
            buttonFont.setColor(Color.GOLD);
            buttonFont.draw(game.getBatch(), nekoins, iconX + iconSize + 8f, iconY + iconSize - 5f);
            buttonFont.setColor(Color.WHITE);
        }
    }
    
    private void drawLogoOrTitle() {
        float titleZoneCenter = Constants.VIRTUAL_HEIGHT - (Constants.VIRTUAL_HEIGHT * TITLE_ZONE_PERCENT / 2f);
        
        if (logoTexture != null) {
            float scale = 1f + (float) Math.sin(animTimer * 2f) * 0.02f;
            float logoWidth = logoBounds.width * scale;
            float logoHeight = logoBounds.height * scale;
            float logoX = (Constants.VIRTUAL_WIDTH - logoWidth) / 2f;
            float logoY = titleZoneCenter - (logoHeight / 2f) + 20f;
            
            if (logoClickCount > 0 && !achievementUnlocked) {
                float alpha = 0.5f + (logoClickCount / (float) CLICKS_FOR_ACHIEVEMENT) * 0.5f;
                game.getBatch().setColor(1f, 1f, alpha, 1f);
            }
            
            game.getBatch().draw(logoTexture, logoX, logoY, logoWidth, logoHeight);
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        } else {
            String title = locale.get("game.title");
            layout.setText(titleFont, title);
            float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
            float titleY = titleZoneCenter + (layout.height / 2f);
            titleFont.draw(game.getBatch(), title, titleX, titleY);
        }
    }
    
    private void drawClickProgress() {
        float progressY = logoBounds.y - 15f;
        
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < logoClickCount; i++) dots.append("●");
        for (int i = logoClickCount; i < CLICKS_FOR_ACHIEVEMENT; i++) dots.append("○");
        
        smallFont.setColor(Color.PINK);
        layout.setText(smallFont, dots.toString());
        smallFont.draw(game.getBatch(), dots.toString(), 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, progressY);
        smallFont.setColor(Color.WHITE);
    }
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        
        for (SimpleButton button : buttons) {
            button.dispose();
        }
        
        if (rankingsButton != null) rankingsButton.dispose();
    }
    }
