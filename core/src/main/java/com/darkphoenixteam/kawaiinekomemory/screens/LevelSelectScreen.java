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
import com.darkphoenixteam.kawaiinekomemory.systems.LocaleManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de selección de nivel con:
 * - Detección de cartas activas para bloqueo de dificultades
 * - Popup de selección de modo Time Attack
 * 
 * @author DarkphoenixTeam
 * @version 2.2 - Card detection + Time Attack popup
 */
public class LevelSelectScreen extends BaseScreen {
    
    private static final String TAG = "LevelSelectScreen";
    
    // === LAYOUT ===
    private static final float HEADER_HEIGHT = 120f;
    private static final float FOOTER_HEIGHT = 120f;
    private static final float TAB_HEIGHT = 50f;
    private static final float TAB_Y = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT + 20f;
    
    private static final float GRID_MARGIN_TOP = HEADER_HEIGHT + 10f;
    private static final float GRID_MARGIN_BOTTOM = FOOTER_HEIGHT + 10f;
    
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 5;
    private static final int LEVELS_PER_PAGE = GRID_COLS * GRID_ROWS;   // 25
    private static final float LEVEL_BUTTON_SIZE = 60f;
    private static final float LEVEL_BUTTON_SPACING = 8f;
    private static final float ARROW_BUTTON_SIZE = 44f;
    
    // Colores por dificultad (para pestañas dibujadas por código)
    private static final Color[] DIFFICULTY_COLORS = {
        new Color(0.36f, 0.72f, 0.36f, 1f),   // Fácil
        new Color(0.95f, 0.78f, 0.15f, 1f),   // Normal
        new Color(0.95f, 0.52f, 0.10f, 1f),   // Avanzado
        new Color(0.85f, 0.22f, 0.22f, 1f)    // Difícil
    };
    
    // === FONTS ===
    private BitmapFont titleFont;
    private BitmapFont tabFont;
    private BitmapFont levelFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // === TEXTURAS ===
    private Texture patternTexture;
    
    // === TABS ===
    private Array<SimpleButton> tabButtons;
    private LevelData.Difficulty currentDifficulty;
    
    // === NIVELES ===
    private Array<LevelData> currentLevels;
    
    // === NAVEGACIÓN ===
    private SimpleButton arrowUpButton;
    private SimpleButton arrowDownButton;
    private int currentPage = 0;
    private int maxPages = 1;
    
    // === BOTONES ===
    private SimpleButton backButton;
    private SimpleButton timeAttackButton;
    
    // === TIME ATTACK POPUP ===
    private boolean showingTimeAttackPopup = false;
    private SimpleButton timeAttack12Button;
    private SimpleButton timeAttack30Button;
    private SimpleButton popupCancelButton;
    private Texture popupPanelTexture;
    private Texture popupButtonTexture;   // BTN_EMPTY compartida por los 2 botones del popup
    private Texture popupCancelTexture;   // BTN_BACK del botón cancelar
    
    // === CARTAS ACTIVAS ===
    private int activeCardCount;
    private boolean[] difficultyAvailable;
    
    // === RENDER ===
    private ShapeRenderer shapeRenderer;
    
    // === SISTEMAS ===
    private AudioManager audioManager;
    private SaveManager saveManager;
    private LocaleManager locale;
    
    // === INPUT ===
    private final Vector2 touchPoint = new Vector2();
    
    public LevelSelectScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        titleFont = game.getFontManager().getTitleFont();
        tabFont = game.getFontManager().getButtonFont();
        levelFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        locale = LocaleManager.getInstance();
        
        shapeRenderer = new ShapeRenderer();
        
        // Calcular cartas activas disponibles
        calculateActiveCards();
        
        currentDifficulty = LevelData.Difficulty.EASY;
        
        audioManager.playMusic(AssetPaths.MUSIC_MENU, true);
        
        loadAssets();
        createTabs();
        createNavigationButtons();
        createBackButton();
        createTimeAttackButton();
        createTimeAttackPopup();
        loadLevelsForCurrentDifficulty();
        
        Gdx.app.log(TAG, "Inicializado | Cartas activas: " + activeCardCount);
        Gdx.app.log(TAG, "Dificultades disponibles: Easy=" + difficultyAvailable[0] + 
                         " Normal=" + difficultyAvailable[1] + 
                         " Advanced=" + difficultyAvailable[2] + 
                         " Hard=" + difficultyAvailable[3]);
    }
    
    /**
     * Calcula cuántas cartas activas tiene el jugador y qué dificultades están disponibles
     */
    private void calculateActiveCards() {
        activeCardCount = saveManager.getActiveCardCount();
        
        difficultyAvailable = new boolean[4];
        difficultyAvailable[0] = activeCardCount >= Constants.MIN_CARDS_EASY;      // Easy: 6
        difficultyAvailable[1] = activeCardCount >= Constants.MIN_CARDS_NORMAL;    // Normal: 8
        difficultyAvailable[2] = activeCardCount >= Constants.MIN_CARDS_ADVANCED;  // Advanced: 10
        difficultyAvailable[3] = activeCardCount >= Constants.MIN_CARDS_HARD;      // Hard: 15
    }
    
    private void loadAssets() {
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_LEVELS));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.log(TAG, "Pattern no encontrado");
        }
        
        try {
            popupPanelTexture = new Texture(Gdx.files.internal(AssetPaths.PANEL_CONFIRM));
        } catch (Exception e) {
            Gdx.app.log(TAG, "Panel popup no encontrado");
        }
    }
    
    private void createTabs() {
        tabButtons = new Array<>();
        
        float tabWidth = Constants.VIRTUAL_WIDTH / 4f;
        
        String[] tabKeys = {
            "levels.tab.easy",
            "levels.tab.normal",
            "levels.tab.advanced",
            "levels.tab.hard"
        };
        
        // Las pestañas se dibujan con ShapeRenderer + texto:
        // antes dependían de 4 PNG que no existen en assets/ y por eso
        // las dificultades Normal/Avanzado/Difícil eran inaccesibles.
        for (int i = 0; i < 4; i++) {
            final int index = i;
            float tabX = i * tabWidth;
            
            SimpleButton tab = new SimpleButton(
                null,
                locale.get(tabKeys[i]),
                tabX,
                TAB_Y,
                tabWidth - 4f,
                TAB_HEIGHT
            );
            
            tab.setOnClick(() -> switchDifficulty(LevelData.Difficulty.values()[index]));
            tabButtons.add(tab);
        }
    }
    
    private void createNavigationButtons() {
        float arrowX = Constants.VIRTUAL_WIDTH - ARROW_BUTTON_SIZE - 10f;
        
        // Zonas táctiles invisibles; las flechas se pintan con ShapeRenderer
        arrowUpButton = new SimpleButton(null, "", arrowX, getGridTopY() - ARROW_BUTTON_SIZE,
            ARROW_BUTTON_SIZE, ARROW_BUTTON_SIZE);
        arrowUpButton.setOnClick(() -> changePage(-1));
        
        arrowDownButton = new SimpleButton(null, "", arrowX,
            getGridTopY() - getGridHeight() - 8f,
            ARROW_BUTTON_SIZE, ARROW_BUTTON_SIZE);
        arrowDownButton.setOnClick(() -> changePage(1));
    }
    
    private float getGridHeight() {
        return GRID_ROWS * LEVEL_BUTTON_SIZE + (GRID_ROWS - 1) * LEVEL_BUTTON_SPACING;
    }
    
    private float getGridWidth() {
        return GRID_COLS * LEVEL_BUTTON_SIZE + (GRID_COLS - 1) * LEVEL_BUTTON_SPACING;
    }
    
    private float getGridStartX() {
        return (Constants.VIRTUAL_WIDTH - getGridWidth()) / 2f;
    }
    
    private float getGridTopY() {
        float top = Constants.VIRTUAL_HEIGHT - GRID_MARGIN_TOP;
        float bottom = GRID_MARGIN_BOTTOM;
        return (top + bottom) / 2f + getGridHeight() / 2f;
    }
    
    /** Rectángulo del nivel visible en la página actual. */
    private Rectangle getLevelBounds(int indexInPage) {
        int row = indexInPage / GRID_COLS;
        int col = indexInPage % GRID_COLS;
        float x = getGridStartX() + col * (LEVEL_BUTTON_SIZE + LEVEL_BUTTON_SPACING);
        float top = getGridTopY() - row * (LEVEL_BUTTON_SIZE + LEVEL_BUTTON_SPACING);
        return new Rectangle(x, top - LEVEL_BUTTON_SIZE, LEVEL_BUTTON_SIZE, LEVEL_BUTTON_SIZE);
    }
    
    private void createBackButton() {
        try {
            Texture buttonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float buttonWidth = Constants.VIRTUAL_WIDTH * 0.35f;
            float aspectRatio = (float) buttonTexture.getHeight() / buttonTexture.getWidth();
            float buttonHeight = buttonWidth * aspectRatio;
            
            backButton = new SimpleButton(
                buttonTexture,
                locale.get("common.back"),
                10f,
                20f,
                buttonWidth,
                buttonHeight
            );
            
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.changeScreen(new HomeScreen(game));
            });
            backButton.setOwnsTexture(true);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando boton back");
        }
    }
    
    private void createTimeAttackButton() {
        try {
            Texture buttonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            float buttonWidth = Constants.VIRTUAL_WIDTH * 0.35f;
            float aspectRatio = (float) buttonTexture.getHeight() / buttonTexture.getWidth();
            float buttonHeight = buttonWidth * aspectRatio;
            
            timeAttackButton = new SimpleButton(
                buttonTexture,
                locale.get("levels.timeattack"),
                Constants.VIRTUAL_WIDTH - buttonWidth - 10f,
                20f,
                buttonWidth,
                buttonHeight
            );
            
            timeAttackButton.setOwnsTexture(true);
            
            // Mostrar popup en lugar de ir directamente
            timeAttackButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                showingTimeAttackPopup = true;
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando boton Time Attack");
        }
    }
    
    private void createTimeAttackPopup() {
        try {
            Texture btnTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_EMPTY));
            popupButtonTexture = btnTexture;
            float btnWidth = Constants.VIRTUAL_WIDTH * 0.6f;
            float btnHeight = 55f;
            float btnX = (Constants.VIRTUAL_WIDTH - btnWidth) / 2f;
            float centerY = Constants.VIRTUAL_HEIGHT / 2f;
            
            // Botón Time Attack 12 (3x4)
            boolean canPlay12 = activeCardCount >= Constants.MIN_CARDS_TIME_ATTACK_12;
            String label12 = locale.get("timeattack.mode12");
            if (!canPlay12) {
                label12 += " (" + activeCardCount + "/" + Constants.MIN_CARDS_TIME_ATTACK_12 + ")";
            }
            
            timeAttack12Button = new SimpleButton(
                btnTexture,
                label12,
                btnX,
                centerY + 20f,
                btnWidth,
                btnHeight
            );
            timeAttack12Button.setOnClick(() -> {
                if (canPlay12) {
                    audioManager.playSound(AssetPaths.SFX_BUTTON);
                    showingTimeAttackPopup = false;
                    game.changeScreen(new TimeAttackScreen(game, TimeAttackScreen.Mode.MODE_12));
                } else {
                    audioManager.playSound(AssetPaths.SFX_NO_MATCH);
                }
            });
            
            // Botón Time Attack 30 (5x6)
            boolean canPlay30 = activeCardCount >= Constants.MIN_CARDS_TIME_ATTACK_30;
            String label30 = locale.get("timeattack.mode30");
            if (!canPlay30) {
                label30 += " (" + activeCardCount + "/" + Constants.MIN_CARDS_TIME_ATTACK_30 + ")";
            }
            
            timeAttack30Button = new SimpleButton(
                btnTexture,
                label30,
                btnX,
                centerY - 50f,
                btnWidth,
                btnHeight
            );
            timeAttack30Button.setOnClick(() -> {
                if (canPlay30) {
                    audioManager.playSound(AssetPaths.SFX_BUTTON);
                    showingTimeAttackPopup = false;
                    game.changeScreen(new TimeAttackScreen(game, TimeAttackScreen.Mode.MODE_30));
                } else {
                    audioManager.playSound(AssetPaths.SFX_NO_MATCH);
                }
            });
            
            // Botón Cancelar
            Texture cancelTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            popupCancelTexture = cancelTexture;
            float cancelWidth = Constants.VIRTUAL_WIDTH * 0.4f;
            float cancelHeight = cancelWidth * 0.35f;
            
            popupCancelButton = new SimpleButton(
                cancelTexture,
                locale.get("common.cancel"),
                (Constants.VIRTUAL_WIDTH - cancelWidth) / 2f,
                centerY - 130f,
                cancelWidth,
                cancelHeight
            );
            popupCancelButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                showingTimeAttackPopup = false;
            });
            
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error creando popup Time Attack");
        }
    }
    
    private void loadLevelsForCurrentDifficulty() {
        currentLevels = new Array<>();
        
        int startId = currentDifficulty.index * 50;
        int endId = startId + 50;
        
        boolean difficultyLocked = !difficultyAvailable[currentDifficulty.index];
        
        for (int i = startId; i < endId; i++) {
            LevelData level = new LevelData(i);
            
            if (difficultyLocked) {
                // Si no hay suficientes cartas, todos los niveles están bloqueados
                level.setUnlocked(false);
            } else {
                level.setUnlocked(saveManager.isLevelUnlocked(i));
            }
            
            level.setCompleted(saveManager.isLevelCompleted(i));
            currentLevels.add(level);
        }
        
        currentPage = 0;
        maxPages = (currentLevels.size + LEVELS_PER_PAGE - 1) / LEVELS_PER_PAGE;
        if (maxPages < 1) maxPages = 1;
    }
    
    private void switchDifficulty(LevelData.Difficulty newDifficulty) {
        if (newDifficulty != currentDifficulty) {
            currentDifficulty = newDifficulty;
            loadLevelsForCurrentDifficulty();
            audioManager.playSound(AssetPaths.SFX_BUTTON);
        }
    }
    
    private void changePage(int delta) {
        int newPage = currentPage + delta;
        if (newPage >= 0 && newPage < maxPages) {
            currentPage = newPage;
            audioManager.playSound(AssetPaths.SFX_BUTTON);
        }
    }
    
    private void onLevelClick(LevelData level) {
        // Verificar si la dificultad está disponible
        if (!difficultyAvailable[currentDifficulty.index]) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        
        if (level.isUnlocked()) {
            audioManager.playSound(AssetPaths.SFX_BUTTON);
            game.changeScreen(new GameScreen(game, level));
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
        }
    }
    
    @Override
    protected void update(float delta) {
        if (!isInputEnabled()) return;
        
        // Si el popup está visible, solo actualizar sus botones
        if (showingTimeAttackPopup) {
            if (timeAttack12Button != null) timeAttack12Button.update(viewport);
            if (timeAttack30Button != null) timeAttack30Button.update(viewport);
            if (popupCancelButton != null) popupCancelButton.update(viewport);
            return;
        }
        
        for (SimpleButton tab : tabButtons) {
            tab.update(viewport);
        }
        
        if (arrowUpButton != null) arrowUpButton.update(viewport);
        if (arrowDownButton != null) arrowDownButton.update(viewport);
        if (backButton != null) backButton.update(viewport);
        if (timeAttackButton != null) timeAttackButton.update(viewport);
        
        if (Gdx.input.justTouched()) {
            checkLevelClick();
        }
    }
    
    private void checkLevelClick() {
        viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
        
        int offset = currentPage * LEVELS_PER_PAGE;
        int visible = Math.min(LEVELS_PER_PAGE, currentLevels.size - offset);
        
        for (int i = 0; i < visible; i++) {
            if (getLevelBounds(i).contains(touchPoint.x, touchPoint.y)) {
                onLevelClick(currentLevels.get(offset + i));
                break;
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
        
        // Título
        String title = locale.get("levels.title");
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      Constants.VIRTUAL_HEIGHT - 20f);
        
        // Mostrar cartas activas
        String cardsInfo = locale.format("levels.activecards", activeCardCount);
        layout.setText(smallFont, cardsInfo);
        smallFont.setColor(activeCardCount >= 6 ? Color.WHITE : Color.RED);
        smallFont.draw(game.getBatch(), cardsInfo, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT - 50f);
        smallFont.setColor(Color.WHITE);
        
        game.getBatch().end();
        
        // Tabs
        drawTabs();
        
        // Grid de niveles
        game.getBatch().begin();
        drawLevelGrid();
        game.getBatch().end();
        
        // Flechas
        game.getBatch().begin();
        if (arrowUpButton != null) arrowUpButton.drawNoText(game.getBatch());
        if (arrowDownButton != null) arrowDownButton.drawNoText(game.getBatch());
        game.getBatch().end();
        
        // Botones inferiores
        game.getBatch().begin();
        if (backButton != null) backButton.draw(game.getBatch(), tabFont);
        if (timeAttackButton != null) timeAttackButton.draw(game.getBatch(), tabFont);
        drawTimeAttackInfo();
        game.getBatch().end();
        
        // Popup de Time Attack (encima de todo)
        if (showingTimeAttackPopup) {
            drawTimeAttackPopup();
        }
    }
    
    private void drawTabs() {
        float tabWidth = Constants.VIRTUAL_WIDTH / 4f;
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 4; i++) {
            boolean isCurrent = LevelData.Difficulty.values()[i] == currentDifficulty;
            boolean isAvailable = difficultyAvailable[i];
            
            Color base = DIFFICULTY_COLORS[i];
            if (!isAvailable) {
                shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
            } else if (isCurrent) {
                shapeRenderer.setColor(base);
            } else {
                shapeRenderer.setColor(base.r * 0.55f, base.g * 0.55f, base.b * 0.55f, 1f);
            }
            shapeRenderer.rect(i * tabWidth + 2f, TAB_Y, tabWidth - 4f, TAB_HEIGHT);
        }
        shapeRenderer.end();
        
        game.getBatch().begin();
        for (int i = 0; i < tabButtons.size; i++) {
            tabButtons.get(i).draw(game.getBatch(), tabFont);
        }
        game.getBatch().end();
    }
    
    private void drawLevelGrid() {
        boolean difficultyLocked = !difficultyAvailable[currentDifficulty.index];
        int offset = currentPage * LEVELS_PER_PAGE;
        int visible = Math.min(LEVELS_PER_PAGE, currentLevels.size - offset);
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        for (int i = 0; i < visible; i++) {
            LevelData level = currentLevels.get(offset + i);
            Rectangle b = getLevelBounds(i);
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            if (difficultyLocked) {
                shapeRenderer.setColor(0.3f, 0.2f, 0.2f, 1f);
            } else if (level.isUnlocked()) {
                if (level.isCompleted()) {
                    int stars = saveManager.getLevelStars(level.getGlobalId());
                    if (stars >= 3) {
                        shapeRenderer.setColor(Color.GOLD);
                    } else if (stars >= 2) {
                        shapeRenderer.setColor(Color.YELLOW);
                    } else {
                        shapeRenderer.setColor(Color.GREEN);
                    }
                } else {
                    shapeRenderer.setColor(Color.WHITE);
                }
            } else {
                shapeRenderer.setColor(Color.DARK_GRAY);
            }
            
            shapeRenderer.rect(b.x, b.y, b.width, b.height);
            shapeRenderer.end();
            
            game.getBatch().begin();
            
            String levelNum = String.valueOf(level.getLocalId());
            layout.setText(levelFont, levelNum);
            
            if (difficultyLocked) {
                levelFont.setColor(Color.DARK_GRAY);
            } else {
                levelFont.setColor(level.isUnlocked() ? Color.BLACK : Color.GRAY);
            }
            
            levelFont.draw(game.getBatch(), levelNum,
                          b.x + (b.width - layout.width) / 2f,
                          b.y + (b.height + layout.height) / 2f);
            levelFont.setColor(Color.WHITE);
            
            game.getBatch().end();
        }
        
        // Flechas de paginación (dibujadas, sin assets)
        drawPageArrows();
        
        // Indicador de página
        game.getBatch().begin();
        if (maxPages > 1) {
            String pageText = (currentPage + 1) + "/" + maxPages;
            layout.setText(smallFont, pageText);
            smallFont.setColor(Color.DARK_GRAY);
            smallFont.draw(game.getBatch(), pageText, getGridStartX(), getGridTopY() + 18f);
            smallFont.setColor(Color.WHITE);
        }
        
        // Mensaje si la dificultad está bloqueada
        if (difficultyLocked) {
            int required = Constants.getMinCardsForDifficulty(currentDifficulty.index);
            String lockMsg = locale.format("levels.locked", required);
            layout.setText(smallFont, lockMsg);
            smallFont.setColor(Color.RED);
            smallFont.draw(game.getBatch(), lockMsg,
                          (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                          Constants.VIRTUAL_HEIGHT / 2f);
            smallFont.setColor(Color.WHITE);
        }
        game.getBatch().end();
    }
    
    /**
     * Dibuja las flechas arriba/abajo con triángulos (no requieren PNG).
     */
    private void drawPageArrows() {
        float x = Constants.VIRTUAL_WIDTH - ARROW_BUTTON_SIZE - 10f;
        float upY = getGridTopY() - ARROW_BUTTON_SIZE;
        float downY = getGridTopY() - getGridHeight() - 8f;
        float sz = ARROW_BUTTON_SIZE;
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Arriba (página anterior)
        if (currentPage > 0) {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
        } else {
            shapeRenderer.setColor(0.55f, 0.55f, 0.55f, 0.6f);
        }
        shapeRenderer.triangle(x, upY, x + sz, upY, x + sz / 2f, upY + sz);
        
        // Abajo (página siguiente)
        if (currentPage < maxPages - 1) {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
        } else {
            shapeRenderer.setColor(0.55f, 0.55f, 0.55f, 0.6f);
        }
        shapeRenderer.triangle(x, downY + sz, x + sz, downY + sz, x + sz / 2f, downY);
        
        shapeRenderer.end();
    }
    
    private void drawTimeAttackInfo() {
        int bestPairs = saveManager.getTimeAttackBestPairs();
        if (bestPairs > 0) {
            String recordText = locale.format("levels.record", bestPairs);
            smallFont.setColor(Color.GOLD);
            layout.setText(smallFont, recordText);
            smallFont.draw(game.getBatch(), recordText,
                          Constants.VIRTUAL_WIDTH - layout.width - 15f, 15f);
            smallFont.setColor(Color.WHITE);
        }
    }
    
    private void drawTimeAttackPopup() {
        game.getBatch().begin();
        
        // Fondo oscuro
        game.getBatch().setColor(0, 0, 0, 0.8f);
        if (patternTexture != null) {
            game.getBatch().draw(patternTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        }
        game.getBatch().setColor(1, 1, 1, 1);
        
        // Panel
        if (popupPanelTexture != null) {
            float panelWidth = Constants.VIRTUAL_WIDTH * 0.9f;
            float panelHeight = panelWidth * 0.8f;
            float panelX = (Constants.VIRTUAL_WIDTH - panelWidth) / 2f;
            float panelY = (Constants.VIRTUAL_HEIGHT - panelHeight) / 2f;
            game.getBatch().draw(popupPanelTexture, panelX, panelY, panelWidth, panelHeight);
        }
        
        // Título
        String popupTitle = locale.get("timeattack.select");
        titleFont.setColor(Color.ORANGE);
        layout.setText(titleFont, popupTitle);
        titleFont.draw(game.getBatch(), popupTitle,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.68f);
        titleFont.setColor(Color.WHITE);
        
        // Descripción
        String desc12 = locale.get("timeattack.desc12");
        String desc30 = locale.get("timeattack.desc30");
        
        smallFont.setColor(Color.WHITE);
        layout.setText(smallFont, desc12);
        smallFont.draw(game.getBatch(), desc12,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.58f);
        
        layout.setText(smallFont, desc30);
        smallFont.draw(game.getBatch(), desc30,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.42f);
        
        // Botones
        boolean canPlay12 = activeCardCount >= Constants.MIN_CARDS_TIME_ATTACK_12;
        boolean canPlay30 = activeCardCount >= Constants.MIN_CARDS_TIME_ATTACK_30;
        
        if (timeAttack12Button != null) {
            if (!canPlay12) {
                game.getBatch().setColor(0.5f, 0.5f, 0.5f, 0.7f);
            }
            timeAttack12Button.draw(game.getBatch(), tabFont);
            game.getBatch().setColor(1, 1, 1, 1);
        }
        
        if (timeAttack30Button != null) {
            if (!canPlay30) {
                game.getBatch().setColor(0.5f, 0.5f, 0.5f, 0.7f);
            }
            timeAttack30Button.draw(game.getBatch(), tabFont);
            game.getBatch().setColor(1, 1, 1, 1);
        }
        
        if (popupCancelButton != null) {
            popupCancelButton.draw(game.getBatch(), tabFont);
        }
        
        game.getBatch().end();
    }
    
    @Override
    protected boolean onBackPressed() {
        if (showingTimeAttackPopup) {
            showingTimeAttackPopup = false;
            audioManager.playSound(AssetPaths.SFX_BUTTON);
            return true;
        }
        audioManager.playSound(AssetPaths.SFX_BUTTON);
        game.changeScreen(new HomeScreen(game));
        return true;
    }
    
    @Override
    public void dispose() {
        if (patternTexture != null) patternTexture.dispose();
        if (popupPanelTexture != null) popupPanelTexture.dispose();
        if (popupButtonTexture != null) popupButtonTexture.dispose();
        if (popupCancelTexture != null) popupCancelTexture.dispose();
        
        for (SimpleButton tab : tabButtons) {
            tab.dispose();
        }
        
        if (arrowUpButton != null) arrowUpButton.dispose();
        if (arrowDownButton != null) arrowDownButton.dispose();
        if (backButton != null) backButton.dispose();
        if (timeAttackButton != null) timeAttackButton.dispose();
        if (timeAttack12Button != null) timeAttack12Button.dispose();
        if (timeAttack30Button != null) timeAttack30Button.dispose();
        if (popupCancelButton != null) popupCancelButton.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
                                          }
