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
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de logros
 * Muestra todos los logros del juego con su estado
 * 
 * @author DarkphoenixTeam
 */
public class AchievementsScreen extends BaseScreen {
    
    private static final String TAG = "AchievementsScreen";
    
    // UI
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    
    private SimpleButton backButton;
    private SimpleButton upButton;
    private SimpleButton downButton;
    
    private ShapeRenderer shapeRenderer;
    
    // Scroll
    private float scrollOffset = 0f;
    private float maxScrollOffset = 0f;
    private static final float SCROLL_SPEED = 300f;
    private static final float ITEM_HEIGHT = 90f;
    private static final float ITEM_MARGIN = 10f;
    
    // Layout
    private static final float HEADER_HEIGHT = 100f;
    private static final float FOOTER_HEIGHT = 100f;
    private static final float CONTENT_PADDING = 15f;
    
    private AudioManager audioManager;
    private SaveManager saveManager;
    
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
        
        shapeRenderer = new ShapeRenderer();
        
        // Calcular scroll máximo
        float contentHeight = Achievement.count() * (ITEM_HEIGHT + ITEM_MARGIN);
        float visibleHeight = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT - FOOTER_HEIGHT;
        maxScrollOffset = Math.max(0, contentHeight - visibleHeight);
        
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        loadAssets();
        createButtons();
        
        Gdx.app.log(TAG, "Logros: " + saveManager.getUnlockedAchievementCount() + "/" + Achievement.count());
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
        // Botón volver
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float btnWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float btnHeight = btnWidth * 0.35f;
            backButton = new SimpleButton(backTex, "VOLVER",
                (Constants.VIRTUAL_WIDTH - btnWidth) / 2f, 20f, btnWidth, btnHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error botón back");
        }
        
        // Flechas de scroll
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
        
        // Scroll con arrastre
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
        
        game.getBatch().end();
        
        // Dibujar items de logros
        drawAchievementList();
        
        // Header y footer (sobre el contenido)
        game.getBatch().begin();
        drawHeader();
        drawFooter();
        
        // Flechas de scroll
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
        // Fondo del header
        game.getBatch().setColor(0.95f, 0.9f, 0.85f, 0.95f);
        game.getBatch().draw(patternTexture, 0, Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT, 
                            Constants.VIRTUAL_WIDTH, HEADER_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        // Título
        String title = "LOGROS";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT - 25f);
        
        // Contador
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
        float itemWidth = Constants.VIRTUAL_WIDTH - CONTENT_PADDING * 2 - 70f; // Espacio para flechas
        
        Achievement[] achievements = Achievement.values();
        
        for (int i = 0; i < achievements.length; i++) {
            Achievement achievement = achievements[i];
            float itemY = startY - (i * (ITEM_HEIGHT + ITEM_MARGIN));
            
            // Solo dibujar si es visible
            if (itemY < FOOTER_HEIGHT - ITEM_HEIGHT || itemY > Constants.VIRTUAL_HEIGHT) {
                continue;
            }
            
            boolean isUnlocked = saveManager.isAchievementUnlocked(achievement);
            
            // Fondo del item
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            if (isUnlocked) {
                shapeRenderer.setColor(0.85f, 0.95f, 0.85f, 0.9f); // Verde claro
            } else {
                shapeRenderer.setColor(0.9f, 0.9f, 0.9f, 0.7f); // Gris
            }
            
            shapeRenderer.rect(CONTENT_PADDING, itemY - ITEM_HEIGHT, itemWidth, ITEM_HEIGHT);
            shapeRenderer.end();
            
            // Borde
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            if (isUnlocked) {
                shapeRenderer.setColor(0.3f, 0.7f, 0.3f, 1f);
            } else {
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
            }
            shapeRenderer.rect(CONTENT_PADDING, itemY - ITEM_HEIGHT, itemWidth, ITEM_HEIGHT);
            shapeRenderer.end();
            
            // Contenido
            game.getBatch().begin();
            
            float textX = CONTENT_PADDING + 15f;
            float iconSize = 30f;
            
            // Icono de estado
            if (isUnlocked) {
                buttonFont.setColor(Color.GOLD);
                buttonFont.draw(game.getBatch(), "★", textX, itemY - 10f);
            } else {
                buttonFont.setColor(Color.GRAY);
                buttonFont.draw(game.getBatch(), "☆", textX, itemY - 10f);
            }
            
            textX += 35f;
            
            // Nombre del logro
            if (isUnlocked) {
                buttonFont.setColor(Color.DARK_GRAY);
            } else {
                buttonFont.setColor(Color.GRAY);
            }
            buttonFont.draw(game.getBatch(), achievement.name, textX, itemY - 12f);
            
            // Descripción
            smallFont.setColor(isUnlocked ? Color.DARK_GRAY : Color.GRAY);
            smallFont.draw(game.getBatch(), achievement.description, textX, itemY - 40f);
            
            // Recompensa
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