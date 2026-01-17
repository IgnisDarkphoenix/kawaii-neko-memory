package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.models.LevelData;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de selección de niveles
 * 4 tabs de dificultad × 50 niveles cada uno
 * Grid de 5×10 con scroll vertical
 * 
 * @author DarkphoenixTeam
 */
public class LevelSelectScreen extends BaseScreen {
    
    private static final String TAG = "LevelSelectScreen";
    
    // Fonts
    private BitmapFont titleFont;
    private BitmapFont tabFont;
    private BitmapFont levelFont;
    private GlyphLayout layout;
    
    // Background
    private Texture patternTexture;
    
    // Botón volver
    private SimpleButton backButton;
    
    // Tab actual
    private LevelData.Difficulty currentDifficulty;
    
    // Niveles
    private Array<LevelData> allLevels;
    private Array<LevelData> currentTabLevels;
    
    // Scroll
    private float scrollY = 0f;
    private float maxScrollY = 0f;
    private Vector2 touchStart = new Vector2();
    private Vector2 touchCurrent = new Vector2();
    private boolean isDragging = false;
    
    // Layout
    private static final float TAB_HEIGHT = 60f;
    private static final float TAB_Y = Constants.VIRTUAL_HEIGHT - 100f;
    private static final float GRID_START_Y = TAB_Y - 20f;
    private static final float BUTTON_SIZE = 70f;
    private static final float BUTTON_SPACING = 10f;
    private static final int COLS = 5;
    
    // Shapes para debug/rectangulos
    private ShapeRenderer shapeRenderer;
    
    // Audio
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    public LevelSelectScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        titleFont = game.getFontManager().getTitleFont();
        tabFont = game.getFontManager().getButtonFont();
        levelFont = game.getFontManager().getButtonFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        
        shapeRenderer = new ShapeRenderer();
        
        // Inicializar con dificultad EASY
        currentDifficulty = LevelData.Difficulty.EASY;
        
        loadAssets();
        createLevels();
        createBackButton();
        
        Gdx.app.log(TAG, "Inicializado - Dificultad: " + currentDifficulty.name);
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_LEVELS));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.log(TAG, "Pattern no encontrado");
        }
    }
    
    private void createLevels() {
        allLevels = new Array<>();
        
        // Crear los 200 niveles
        for (int i = 0; i < 200; i++) {
            LevelData level = new LevelData(i);
            level.setUnlocked(saveManager.isLevelUnlocked(i));
            level.setCompleted(saveManager.isLevelCompleted(i));
            allLevels.add(level);
        }
        
        updateCurrentTabLevels();
    }
    
    private void updateCurrentTabLevels() {
        currentTabLevels = new Array<>();
        
        // Filtrar niveles de la dificultad actual
        for (LevelData level : allLevels) {
            if (level.getDifficulty() == currentDifficulty) {
                currentTabLevels.add(level);
            }
        }
        
        // Calcular scroll máximo
        int rows = (int) Math.ceil(currentTabLevels.size / (float) COLS);
        float gridHeight = rows * (BUTTON_SIZE + BUTTON_SPACING);
        float viewportHeight = GRID_START_Y - 100f; // Espacio visible
        maxScrollY = Math.max(0, gridHeight - viewportHeight);
        scrollY = 0f; // Reset scroll al cambiar tab
        
        Gdx.app.log(TAG, "Tab " + currentDifficulty.name + ": " + 
                         currentTabLevels.size + " niveles, maxScroll=" + maxScrollY);
    }
    
    private void createBackButton() {
        try {
            Texture buttonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float buttonWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float aspectRatio = (float) buttonTexture.getHeight() / buttonTexture.getWidth();
            float buttonHeight = buttonWidth * aspectRatio;
            float buttonX = (Constants.VIRTUAL_WIDTH - buttonWidth) / 2f;
            float buttonY = 20f;
            
            backButton = new SimpleButton(
                buttonTexture,
                "VOLVER",
                buttonX,
                buttonY,
                buttonWidth,
                buttonHeight
            );
            
            backButton.setOnClick(() -> {
                Gdx.app.log(TAG, "Volviendo al menú principal");
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando botón back: " + e.getMessage());
        }
    }
    
    @Override
    protected void update(float delta) {
        if (!isInputEnabled()) {
            return;
        }
        
        // Actualizar botón volver
        if (backButton != null) {
            backButton.update(viewport);
        }
        
        // Manejo de scroll y toques
        handleInput();
    }
    
    private void handleInput() {
        if (Gdx.input.justTouched()) {
            viewport.unproject(touchStart.set(Gdx.input.getX(), Gdx.input.getY()));
            isDragging = false;
        }
        
        if (Gdx.input.isTouched()) {
            viewport.unproject(touchCurrent.set(Gdx.input.getX(), Gdx.input.getY()));
            
            float deltaY = touchCurrent.y - touchStart.y;
            
            // Detectar drag
            if (Math.abs(deltaY) > 5f) {
                isDragging = true;
                scrollY -= deltaY * 0.5f;
                scrollY = Math.max(0, Math.min(maxScrollY, scrollY));
                touchStart.set(touchCurrent);
            }
        } else if (isDragging) {
            isDragging = false;
        } else {
            // Solo detectar clicks si no estaba dragging
            if (Gdx.input.justTouched()) {
                checkLevelClick();
                checkTabClick();
            }
        }
    }
    
    private void checkTabClick() {
        viewport.unproject(touchCurrent.set(Gdx.input.getX(), Gdx.input.getY()));
        
        float tabWidth = Constants.VIRTUAL_WIDTH / 4f;
        float tabY = TAB_Y;
        
        // Verificar cuál tab se tocó
        for (int i = 0; i < 4; i++) {
            float tabX = i * tabWidth;
            Rectangle tabBounds = new Rectangle(tabX, tabY, tabWidth, TAB_HEIGHT);
            
            if (tabBounds.contains(touchCurrent.x, touchCurrent.y)) {
                LevelData.Difficulty newDifficulty = LevelData.Difficulty.values()[i];
                if (newDifficulty != currentDifficulty) {
                    currentDifficulty = newDifficulty;
                    updateCurrentTabLevels();
                    audioManager.playSound(AssetPaths.SFX_BUTTON);
                    Gdx.app.log(TAG, "Tab cambiado a: " + currentDifficulty.name);
                }
                break;
            }
        }
    }
    
    private void checkLevelClick() {
        viewport.unproject(touchCurrent.set(Gdx.input.getX(), Gdx.input.getY()));
        
        float gridStartX = (Constants.VIRTUAL_WIDTH - (COLS * (BUTTON_SIZE + BUTTON_SPACING))) / 2f;
        
        for (int i = 0; i < currentTabLevels.size; i++) {
            int row = i / COLS;
            int col = i % COLS;
            
            float x = gridStartX + col * (BUTTON_SIZE + BUTTON_SPACING);
            float y = GRID_START_Y - scrollY - row * (BUTTON_SIZE + BUTTON_SPACING);
            
            Rectangle levelBounds = new Rectangle(x, y, BUTTON_SIZE, BUTTON_SIZE);
            
            if (levelBounds.contains(touchCurrent.x, touchCurrent.y)) {
                LevelData level = currentTabLevels.get(i);
                
                if (level.isUnlocked()) {
                    audioManager.playSound(AssetPaths.SFX_BUTTON);
                    Gdx.app.log(TAG, "Nivel seleccionado: " + level.getGlobalId());
                    // TODO: game.setScreen(new GameScreen(game, level));
                } else {
                    Gdx.app.log(TAG, "Nivel bloqueado: " + level.getGlobalId());
                }
                
                break;
            }
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
        
        // Título
        String title = "SELECCIONA NIVEL";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      Constants.VIRTUAL_HEIGHT - 20f);
        
        game.getBatch().end();
        
        // === TABS (con ShapeRenderer) ===
        drawTabs();
        
        // === GRID DE NIVELES ===
        game.getBatch().begin();
        drawLevelGrid();
        
        // Botón volver
        if (backButton != null) {
            backButton.draw(game.getBatch(), tabFont);
        }
        
        game.getBatch().end();
    }
    
    private void drawTabs() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        float tabWidth = Constants.VIRTUAL_WIDTH / 4f;
        
        for (int i = 0; i < 4; i++) {
            LevelData.Difficulty difficulty = LevelData.Difficulty.values()[i];
            float x = i * tabWidth;
            
            // Color según dificultad
            Color color;
            switch (difficulty) {
                case EASY: color = new Color(0x90EE90FF); break;      // Verde
                case NORMAL: color = new Color(0xFFD700FF); break;    // Amarillo
                case ADVANCED: color = new Color(0xFF8C00FF); break;  // Naranja
                case HARD: color = new Color(0xFF4500FF); break;      // Rojo
                default: color = Color.GRAY;
            }
            
            // Tab activo más brillante
            if (difficulty == currentDifficulty) {
                shapeRenderer.setColor(color);
            } else {
                shapeRenderer.setColor(color.r * 0.6f, color.g * 0.6f, color.b * 0.6f, 1f);
            }
            
            shapeRenderer.rect(x, TAB_Y, tabWidth - 2f, TAB_HEIGHT);
        }
        
        shapeRenderer.end();
        
        // Texto de tabs
        game.getBatch().begin();
        for (int i = 0; i < 4; i++) {
            LevelData.Difficulty difficulty = LevelData.Difficulty.values()[i];
            String name = difficulty.name.substring(0, Math.min(3, difficulty.name.length())).toUpperCase();
            layout.setText(tabFont, name);
            float x = i * tabWidth + (tabWidth - layout.width) / 2f;
            tabFont.draw(game.getBatch(), name, x, TAB_Y + (TAB_HEIGHT + layout.height) / 2f);
        }
        game.getBatch().end();
    }
    
    private void drawLevelGrid() {
        float gridStartX = (Constants.VIRTUAL_WIDTH - (COLS * (BUTTON_SIZE + BUTTON_SPACING))) / 2f;
        
        for (int i = 0; i < currentTabLevels.size; i++) {
            LevelData level = currentTabLevels.get(i);
            
            int row = i / COLS;
            int col = i % COLS;
            
            float x = gridStartX + col * (BUTTON_SIZE + BUTTON_SPACING);
            float y = GRID_START_Y - scrollY - row * (BUTTON_SIZE + BUTTON_SPACING);
            
            // Solo dibujar si está visible
            if (y + BUTTON_SIZE < 0 || y > Constants.VIRTUAL_HEIGHT) {
                continue;
            }
            
            game.getBatch().end();
            
            // Dibujar fondo del botón
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            if (level.isUnlocked()) {
                shapeRenderer.setColor(level.isCompleted() ? Color.GREEN : Color.WHITE);
            } else {
                shapeRenderer.setColor(Color.DARK_GRAY);
            }
            
            shapeRenderer.rect(x, y, BUTTON_SIZE, BUTTON_SIZE);
            shapeRenderer.end();
            
            game.getBatch().begin();
            
            // Número del nivel
            String levelNum = String.valueOf(level.getLocalId());
            layout.setText(levelFont, levelNum);
            levelFont.setColor(level.isUnlocked() ? Color.BLACK : Color.GRAY);
            levelFont.draw(game.getBatch(), levelNum, 
                          x + (BUTTON_SIZE - layout.width) / 2f,
                          y + (BUTTON_SIZE + layout.height) / 2f);
            levelFont.setColor(Color.WHITE);
        }
    }
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        if (backButton != null) backButton.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}