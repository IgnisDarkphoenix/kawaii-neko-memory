package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.models.Achievement;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.LocaleManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de logros con localización completa
 * 
 * @author DarkphoenixTeam
 * @version 1.1 - Localización completa
 */
public class AchievementsScreen extends BaseScreen {
    
    private static final String TAG = "AchievementsScreen";
    
    // === FONTS ===
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // === TEXTURAS ===
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    
    // === BOTONES ===
    private SimpleButton backButton;
    private SimpleButton upButton;
    private SimpleButton downButton;
    
    // === RENDER ===
    private ShapeRenderer shapeRenderer;
    
    // === SCROLL ===
    private float scrollOffset = 0f;
    private float maxScrollOffset = 0f;
    private static final float ITEM_HEIGHT = 90f;
    private static final float ITEM_MARGIN = 10f;
    
    // === LAYOUT ===
    private static final float HEADER_HEIGHT = 100f;
    private static final float FOOTER_HEIGHT = 100f;
    private static final float CONTENT_PADDING = 15f;
    
    // === SISTEMAS ===
    private AudioManager audioManager;
    private SaveManager saveManager;
    private LocaleManager locale;
    
    private final Vector2 touchPoint = new Vector2();
    private float lastTouchY = 0f;
    private boolean isDragging = false;
    
    public AchievementsScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.95f, 0.9f);
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        locale = LocaleManager.getInstance();
        
        shapeRenderer = new ShapeRenderer();
        
        float contentHeight = Achievement.count() * (ITEM_HEIGHT + ITEM_MARGIN);
        float visibleHeight = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT - FOOTER_HEIGHT;
        maxScrollOffset = Math.max(0, contentHeight - visibleHeight);
        
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        loadAssets();
        createButtons();
        
        Gdx.app.log(TAG, "Achievements Screen inicializado");
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error pattern");
        }
        
        try {
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error nekoin icon");
        }
    }
    
    private void createButtons() {
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float btnWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float btnHeight = btnWidth * 0.35f;
            backButton = new SimpleButton(backTex, locale.get("common.back"),
                (Constants.VIRTUAL_WIDTH - btnWidth) / 2f, 20f, btnWidth, btnHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botón back");
        }
        
        float arrowSize = 50f;
        float arrowX = Constants.VIRTUAL_WIDTH - arrowSize - 10f;
        float centerY = Constants.VIRTUAL_HEIGHT / 2f;
        
        try {
            Texture upTex = new Texture(Gdx.files.internal(AssetPaths.BTN_ARROW_UP));
            upButton = new SimpleButton(upTex, "", arrowX, centerY + 30f, arrowSize, arrowSize);
            upButton.setOnClick(() -> {
                scrollOffset = Math.max(0, scrollOffset - 200f);
                audioManager.playSound(AssetPaths.SFX_BUTTON);
            });
        } catch (Exception e) {}
        
        try {
            Texture downTex = new Texture(Gdx.files.internal(AssetPaths.BTN_ARROW_DOWN));
            downButton = new SimpleButton(downTex, "", arrowX, centerY - 80f, arrowSize, arrowSize);
            downButton.setOnClick(() -> {
                scrollOffset = Math.min(maxScrollOffset, scrollOffset + 200f);
                audioManager.playSound(AssetPaths.SFX_BUTTON);
            });
        } catch (Exception e) {}
    }
    
    @Override
    protected void update(float delta) {
        if (!isInputEnabled()) return;
        
        if (backButton != null) backButton.update(viewport);
        if (upButton != null) upButton.update(viewport);
        if (downButton != null) downButton.update(viewport);
        
        handleScrollInput();
    }
    
    private void handleScrollInput() {
        if (Gdx.input.isTouched()) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            
            if (!isDragging) {
                isDragging = true;
                lastTouchY = touchPoint.y;
            } else {
                float deltaY = lastTouchY - touchPoint.y;
                scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset + deltaY));
                lastTouchY = touchPoint.y;
            }
        } else {
            isDragging = false;
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
        
        game.getBatch().end();
        
        drawAchievementList();
        
        game.getBatch().begin();
        drawHeader();
        drawFooter();
        
        if (upButton != null && scrollOffset > 0) {
            upButton.drawNoText(game.getBatch());
        }
        if (downButton != null && scrollOffset < maxScrollOffset) {
            downButton.drawNoText(game.getBatch());
        }
        
        if (backButton != null) backButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
    }
    
    private void drawHeader() {
        game.getBatch().setColor(0.95f, 0.9f, 0.85f, 0.95f);
        game.getBatch().draw(patternTexture, 0, Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT, 
                            Constants.VIRTUAL_WIDTH, HEADER_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        String title = locale.get("achievements.title");
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT - 25f);
        
        int unlocked = saveManager.getUnlockedAchievementCount();
        int total = Achievement.count();
        String count = unlocked + " / " + total;
        layout.setText(buttonFont, count);
        
        buttonFont.setColor(unlocked == total ? Color.GOLD : Color.WHITE);
        buttonFont.draw(game.getBatch(), count,
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                       Constants.VIRTUAL_HEIGHT - 65f);
        buttonFont.setColor(Color.WHITE);
    }
    
    private void drawFooter() {
        game.getBatch().setColor(0.95f, 0.9f, 0.85f, 0.95f);
        game.getBatch().draw(patternTexture, 0, 0, Constants.VIRTUAL_WIDTH, FOOTER_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
    }
    
    private void drawAchievementList() {
        float startY = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT - CONTENT_PADDING + scrollOffset;
        float itemWidth = Constants.VIRTUAL_WIDTH - CONTENT_PADDING * 2 - 70f;
        
        Achievement[] achievements = Achievement.values();
        
        for (int i = 0; i < achievements.length; i++) {
            Achievement achievement = achievements[i];
            float itemY = startY - (i * (ITEM_HEIGHT + ITEM_MARGIN));
            
            if (itemY < FOOTER_HEIGHT - ITEM_HEIGHT || itemY > Constants.VIRTUAL_HEIGHT) {
                continue;
            }
            
            boolean isUnlocked = saveManager.isAchievementUnlocked(achievement);
            
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            if (isUnlocked) {
                shapeRenderer.setColor(0.85f, 0.95f, 0.85f, 0.9f);
            } else {
                shapeRenderer.setColor(0.9f, 0.9f, 0.9f, 0.7f);
            }
            
            shapeRenderer.rect(CONTENT_PADDING, itemY - ITEM_HEIGHT, itemWidth, ITEM_HEIGHT);
            shapeRenderer.end();
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            if (isUnlocked) {
                shapeRenderer.setColor(0.3f, 0.7f, 0.3f, 1f);
            } else {
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
            }
            shapeRenderer.rect(CONTENT_PADDING, itemY - ITEM_HEIGHT, itemWidth, ITEM_HEIGHT);
            shapeRenderer.end();
            
            game.getBatch().begin();
            
            float textX = CONTENT_PADDING + 15f;
            
            if (isUnlocked) {
                buttonFont.setColor(Color.GOLD);
                buttonFont.draw(game.getBatch(), "★", textX, itemY - 10f);
            } else {
                buttonFont.setColor(Color.GRAY);
                buttonFont.draw(game.getBatch(), "☆", textX, itemY - 10f);
            }
            
            textX += 35f;
            
            // Usar nombre localizado del logro
            String achievementName = getLocalizedAchievementName(achievement);
            String achievementDesc = getLocalizedAchievementDesc(achievement);
            
            if (isUnlocked) {
                buttonFont.setColor(Color.DARK_GRAY);
            } else {
                buttonFont.setColor(Color.GRAY);
            }
            buttonFont.draw(game.getBatch(), achievementName, textX, itemY - 12f);
            
            smallFont.setColor(isUnlocked ? Color.DARK_GRAY : Color.GRAY);
            smallFont.draw(game.getBatch(), achievementDesc, textX, itemY - 40f);
            
            if (nekoinIconTexture != null) {
                float rewardX = CONTENT_PADDING + itemWidth - 80f;
                float rewardY = itemY - ITEM_HEIGHT + 15f;
                
                game.getBatch().setColor(isUnlocked ? 0.5f : 1f, isUnlocked ? 0.5f : 1f, 
                                        isUnlocked ? 0.5f : 1f, isUnlocked ? 0.5f : 1f);
                game.getBatch().draw(nekoinIconTexture, rewardX, rewardY, 20f, 20f);
                game.getBatch().setColor(1, 1, 1, 1);
                
                smallFont.setColor(isUnlocked ? Color.GRAY : Color.GOLD);
                smallFont.draw(game.getBatch(), "+" + achievement.reward, rewardX + 25f, rewardY + 17f);
            }
            
            buttonFont.setColor(Color.WHITE);
            smallFont.setColor(Color.WHITE);
            
            game.getBatch().end();
        }
    }
    
    /**
     * Obtiene el nombre localizado del logro
     */
    private String getLocalizedAchievementName(Achievement achievement) {
        String key = "achievement." + achievement.name().toLowerCase() + ".name";
        String localized = locale.get(key);
        // Si no hay traducción, usar el nombre del enum
        if (localized.startsWith("[")) {
            return achievement.name;
        }
        return localized;
    }
    
    /**
     * Obtiene la descripción localizada del logro
     */
    private String getLocalizedAchievementDesc(Achievement achievement) {
        String key = "achievement." + achievement.name().toLowerCase() + ".desc";
        String localized = locale.get(key);
        // Si no hay traducción, usar la descripción del enum
        if (localized.startsWith("[")) {
            return achievement.description;
        }
        return localized;
    }
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        if (backButton != null) backButton.dispose();
        if (upButton != null) upButton.dispose();
        if (downButton != null) downButton.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
