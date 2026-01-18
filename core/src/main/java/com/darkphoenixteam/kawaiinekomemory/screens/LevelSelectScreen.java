package com.darkphoenixteam.kawaiinekomemory.screens;

import com.darkphoenixteam.kawaiinekomemory.screens.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.models.LevelData;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de selección de niveles - REESCRITA
 * Navegación por botones de flecha, sin scroll táctil
 * 
 * @author DarkphoenixTeam
 */
public class LevelSelectScreen extends BaseScreen {
    
    private static final String TAG = "LevelSelectScreen";
    
    // === LAYOUT CONSTANTS ===
    private static final float HEADER_HEIGHT = 120f;           // Espacio para tabs
    private static final float FOOTER_HEIGHT = 120f;           // Espacio para botón atrás
    private static final float TAB_HEIGHT = 50f;
    private static final float TAB_Y = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT + 20f;
    
    private static final float GRID_MARGIN_TOP = HEADER_HEIGHT + 10f;
    private static final float GRID_MARGIN_BOTTOM = FOOTER_HEIGHT + 10f;
    
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 10;
    private static final float LEVEL_BUTTON_SIZE = 60f;
    private static final float LEVEL_BUTTON_SPACING = 8f;
    
    private static final float ARROW_BUTTON_SIZE = 60f;
    
    // Fonts
    private BitmapFont titleFont;
    private BitmapFont tabFont;
    private BitmapFont levelFont;
    private GlyphLayout layout;
    
    // Background
    private Texture patternTexture;
    
    // Tabs de dificultad
    private Array<SimpleButton> tabButtons;
    private LevelData.Difficulty currentDifficulty;
    
    // Grid de niveles
    private Array<SimpleButton> levelButtons;
    private Array<LevelData> currentLevels;
    
    // Navegación (flechas)
    private SimpleButton arrowUpButton;
    private SimpleButton arrowDownButton;
    private int currentPage = 0;
    private int maxPages = 1;  // Por ahora 1 página = todas las 10 filas visibles
    
    // Botón volver
    private SimpleButton backButton;
    
    // ShapeRenderer para niveles
    private ShapeRenderer shapeRenderer;
    
    // Managers
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
        
        currentDifficulty = LevelData.Difficulty.EASY;
        
        loadAssets();
        createTabs();
        createNavigationButtons();
        createBackButton();
        loadLevelsForCurrentDifficulty();
        
        Gdx.app.log(TAG, "Inicializado - Layout sin scroll táctil");
    }
    
    // ==================== CARGA DE ASSETS ====================
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_LEVELS));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            Gdx.app.log(TAG, "Pattern cargado");
        } catch (Exception e) {
            Gdx.app.log(TAG, "Pattern no encontrado");
        }
    }
    
    // ==================== CREACIÓN DE UI ====================
    
    private void createTabs() {
        tabButtons = new Array<>();
        
        float tabWidth = Constants.VIRTUAL_WIDTH / 4f;
        
        String[] tabPaths = {
            AssetPaths.TAB_EASY,
            AssetPaths.TAB_NORMAL,
            AssetPaths.TAB_ADVANCED,
            AssetPaths.TAB_HARD
        };
        
        String[] tabLabels = {"FÁCIL", "NORMAL", "AVANZADO", "DIFÍCIL"};
        
        for (int i = 0; i < 4; i++) {
            final int index = i;
            float tabX = i * tabWidth;
            
            try {
                Texture tabTexture = new Texture(Gdx.files.internal(tabPaths[i]));
                SimpleButton tab = new SimpleButton(
                    tabTexture,
                    tabLabels[i],
                    tabX,
                    TAB_Y,
                    tabWidth - 4f,  // Pequeño margen entre tabs
                    TAB_HEIGHT
                );
                
                tab.setOnClick(() -> switchDifficulty(LevelData.Difficulty.values()[index]));
                tabButtons.add(tab);
                
            } catch (Exception e) {
                Gdx.app.error(TAG, "Error cargando tab " + i + ": " + e.getMessage());
            }
        }
        
        Gdx.app.log(TAG, "Tabs creados: " + tabButtons.size);
    }
    
    private void createNavigationButtons() {
        // Botones de flecha a la derecha del grid
        float arrowX = Constants.VIRTUAL_WIDTH - ARROW_BUTTON_SIZE - 10f;
        float gridCenterY = (Constants.VIRTUAL_HEIGHT - GRID_MARGIN_TOP - GRID_MARGIN_BOTTOM) / 2f + GRID_MARGIN_BOTTOM;
        
        // Flecha arriba
        try {
            Texture upTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_ARROW_UP));
            arrowUpButton = new SimpleButton(
                upTexture,
                "",
                arrowX,
                gridCenterY + 10f,
                ARROW_BUTTON_SIZE,
                ARROW_BUTTON_SIZE
            );
            
            arrowUpButton.setOnClick(() -> changePage(-1));
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando flecha arriba: " + e.getMessage());
        }
        
        // Flecha abajo
        try {
            Texture downTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_ARROW_DOWN));
            arrowDownButton = new SimpleButton(
                downTexture,
                "",
                arrowX,
                gridCenterY - ARROW_BUTTON_SIZE - 10f,
                ARROW_BUTTON_SIZE,
                ARROW_BUTTON_SIZE
            );
            
            arrowDownButton.setOnClick(() -> changePage(1));
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando flecha abajo: " + e.getMessage());
        }
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
    
    // ==================== LÓGICA DE NIVELES ====================
    
    private void loadLevelsForCurrentDifficulty() {
        currentLevels = new Array<>();
        
        // Obtener niveles de la dificultad actual (0-49, 50-99, 100-149, 150-199)
        int startId = currentDifficulty.index * 50;
        int endId = startId + 50;
        
        for (int i = startId; i < endId; i++) {
            LevelData level = new LevelData(i);
            level.setUnlocked(saveManager.isLevelUnlocked(i));
            level.setCompleted(saveManager.isLevelCompleted(i));
            currentLevels.add(level);
        }
        
        currentPage = 0;
        maxPages = 1; // Por ahora 1 página muestra todos los 50 niveles
        
        Gdx.app.log(TAG, "Cargados " + currentLevels.size + " niveles de " + currentDifficulty.name);
    }
    
    private void switchDifficulty(LevelData.Difficulty newDifficulty) {
        if (newDifficulty != currentDifficulty) {
            currentDifficulty = newDifficulty;
            loadLevelsForCurrentDifficulty();
            audioManager.playSound(AssetPaths.SFX_BUTTON);
            Gdx.app.log(TAG, "Cambiado a dificultad: " + newDifficulty.name);
        }
    }
    
    private void changePage(int delta) {
        int newPage = currentPage + delta;
        if (newPage >= 0 && newPage < maxPages) {
            currentPage = newPage;
            audioManager.playSound(AssetPaths.SFX_BUTTON);
            Gdx.app.log(TAG, "Página: " + currentPage + "/" + maxPages);
        }
    }
    
    private void onLevelClick(LevelData level) {
    if (level.isUnlocked()) {
        audioManager.playSound(AssetPaths.SFX_BUTTON);
        Gdx.app.log(TAG, "Nivel seleccionado: " + level.getGlobalId() + 
                         " (" + level.getDifficulty().name + " " + level.getLocalId() + ")");
        
        // ¡Ahora sí navegamos a GameScreen!
        game.setScreen(new GameScreen(game, level));
    } else {
        audioManager.playSound(AssetPaths.SFX_NO_MATCH);  // Sonido de "bloqueado"
        Gdx.app.log(TAG, "Nivel bloqueado: " + level.getGlobalId());
    }
    }
    
    // ==================== UPDATE ====================
    
    @Override
    protected void update(float delta) {
        if (!isInputEnabled()) {
            return;
        }
        
        // Actualizar tabs
        for (SimpleButton tab : tabButtons) {
            tab.update(viewport);
        }
        
        // Actualizar flechas
        if (arrowUpButton != null) {
            arrowUpButton.update(viewport);
        }
        if (arrowDownButton != null) {
            arrowDownButton.update(viewport);
        }
        
        // Actualizar botón volver
        if (backButton != null) {
            backButton.update(viewport);
        }
        
        // Detectar clicks en niveles
        if (Gdx.input.justTouched()) {
            checkLevelClick();
        }
    }
    
    private void checkLevelClick() {
        float touchX = Gdx.input.getX();
        float touchY = Gdx.input.getY();
        viewport.unproject(touchPoint.set(touchX, touchY));
        
        // Calcular dimensiones del grid
        float gridWidth = GRID_COLS * (LEVEL_BUTTON_SIZE + LEVEL_BUTTON_SPACING);
        float gridStartX = (Constants.VIRTUAL_WIDTH - gridWidth) / 2f;
        float gridStartY = Constants.VIRTUAL_HEIGHT - GRID_MARGIN_TOP;
        
        // Verificar cada nivel visible
        for (int i = 0; i < currentLevels.size; i++) {
            int row = i / GRID_COLS;
            int col = i % GRID_COLS;
            
            float x = gridStartX + col * (LEVEL_BUTTON_SIZE + LEVEL_BUTTON_SPACING);
            float y = gridStartY - row * (LEVEL_BUTTON_SIZE + LEVEL_BUTTON_SPACING);
            
            if (touchPoint.x >= x && touchPoint.x <= x + LEVEL_BUTTON_SIZE &&
                touchPoint.y >= y - LEVEL_BUTTON_SIZE && touchPoint.y <= y) {
                onLevelClick(currentLevels.get(i));
                break;
            }
        }
    }
    
    private final com.badlogic.gdx.math.Vector2 touchPoint = new com.badlogic.gdx.math.Vector2();
    
    // ==================== DRAW ====================
    
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
        
        // === TABS ===
        drawTabs();
        
        // === GRID DE NIVELES ===
        game.getBatch().begin();
        drawLevelGrid();
        game.getBatch().end();
        
        // === NAVEGACIÓN ===
        game.getBatch().begin();
        if (arrowUpButton != null) {
            arrowUpButton.drawNoText(game.getBatch());
        }
        if (arrowDownButton != null) {
            arrowDownButton.drawNoText(game.getBatch());
        }
        game.getBatch().end();
        
        // === BOTÓN VOLVER ===
        game.getBatch().begin();
        if (backButton != null) {
            backButton.draw(game.getBatch(), tabFont);
        }
        game.getBatch().end();
    }
    
    private void drawTabs() {
        game.getBatch().begin();
        
        for (int i = 0; i < tabButtons.size; i++) {
            SimpleButton tab = tabButtons.get(i);
            
            // Resaltar tab activo
            if (LevelData.Difficulty.values()[i] == currentDifficulty) {
                game.getBatch().setColor(1f, 1f, 1f, 1f);
            } else {
                game.getBatch().setColor(0.6f, 0.6f, 0.6f, 1f);
            }
            
            tab.draw(game.getBatch(), tabFont);
        }
        
        game.getBatch().setColor(1f, 1f, 1f, 1f);
        game.getBatch().end();
    }
    
    private void drawLevelGrid() {
        float gridWidth = GRID_COLS * (LEVEL_BUTTON_SIZE + LEVEL_BUTTON_SPACING);
        float gridStartX = (Constants.VIRTUAL_WIDTH - gridWidth) / 2f;
        float gridStartY = Constants.VIRTUAL_HEIGHT - GRID_MARGIN_TOP;
        
        for (int i = 0; i < currentLevels.size; i++) {
            LevelData level = currentLevels.get(i);
            
            int row = i / GRID_COLS;
            int col = i % GRID_COLS;
            
            float x = gridStartX + col * (LEVEL_BUTTON_SIZE + LEVEL_BUTTON_SPACING);
            float y = gridStartY - row * (LEVEL_BUTTON_SIZE + LEVEL_BUTTON_SPACING);
            
            game.getBatch().end();
            
            // Dibujar fondo del botón de nivel
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            if (level.isUnlocked()) {
                if (level.isCompleted()) {
                    shapeRenderer.setColor(Color.GREEN);
                } else {
                    shapeRenderer.setColor(Color.WHITE);
                }
            } else {
                shapeRenderer.setColor(Color.DARK_GRAY);
            }
            
            shapeRenderer.rect(x, y - LEVEL_BUTTON_SIZE, LEVEL_BUTTON_SIZE, LEVEL_BUTTON_SIZE);
            shapeRenderer.end();
            
            game.getBatch().begin();
            
            // Número del nivel
            String levelNum = String.valueOf(level.getLocalId());
            layout.setText(levelFont, levelNum);
            
            levelFont.setColor(level.isUnlocked() ? Color.BLACK : Color.GRAY);
            levelFont.draw(game.getBatch(), levelNum, 
                          x + (LEVEL_BUTTON_SIZE - layout.width) / 2f,
                          y - LEVEL_BUTTON_SIZE + (LEVEL_BUTTON_SIZE + layout.height) / 2f);
            levelFont.setColor(Color.WHITE);
        }
    }
    
    // ==================== DISPOSE ====================
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        
        for (SimpleButton tab : tabButtons) {
            tab.dispose();
        }
        
        if (arrowUpButton != null) arrowUpButton.dispose();
        if (arrowDownButton != null) arrowDownButton.dispose();
        if (backButton != null) backButton.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
