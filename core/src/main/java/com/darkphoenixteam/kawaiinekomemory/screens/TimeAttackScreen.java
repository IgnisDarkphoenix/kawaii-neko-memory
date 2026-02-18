package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.models.Card;
import com.darkphoenixteam.kawaiinekomemory.systems.AdController;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.LocaleManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de modo Time Attack con dos modos:
 * - MODE_12: Grid 3x4 (6 pares) - requiere 6 cartas activas
 * - MODE_30: Grid 5x6 (15 pares) - requiere 15 cartas activas
 * 
 * @author DarkphoenixTeam
 * @version 2.0 - Dual mode support
 */
public class TimeAttackScreen extends BaseScreen {
    
    private static final String TAG = "TimeAttackScreen";
    
    // ==================== MODOS ====================
    
    public enum Mode {
        MODE_12(Constants.TIME_ATTACK_12_COLS, Constants.TIME_ATTACK_12_ROWS, Constants.TIME_ATTACK_12_PAIRS),
        MODE_30(Constants.TIME_ATTACK_30_COLS, Constants.TIME_ATTACK_30_ROWS, Constants.TIME_ATTACK_30_PAIRS);
        
        public final int cols;
        public final int rows;
        public final int pairs;
        
        Mode(int cols, int rows, int pairs) {
            this.cols = cols;
            this.rows = rows;
            this.pairs = pairs;
        }
    }
    
    private Mode currentMode;
    
    // ==================== ESTADOS ====================
    
    public enum GameState {
        STARTING, PLAYING, CHECKING, GRID_TRANSITION, GAME_OVER, SHOWING_RESULTS
    }
    
    private GameState gameState;
    
    // ==================== CONFIGURACIÓN ====================
    
    private int cols;
    private int rows;
    private int pairs;
    
    // ==================== TABLERO ====================
    
    private Array<Card> cards;
    private Card firstRevealed;
    private Card secondRevealed;
    
    private Texture cardBackTexture;
    private Array<Texture> cardFrontTextures;
    private Texture backgroundTexture;
    
    private float boardX, boardY;
    private float boardWidth, boardHeight;
    private float cardWidth, cardHeight;
    
    // ==================== TIMER Y PUNTUACIÓN ====================
    
    private float timeRemaining;
    private float timeLimit;
    private int pairsFoundThisGrid;
    private int pairsFoundTotal;
    private int gridsCompleted;
    private int nekoinsEarned;
    
    // ==================== RÉCORDS ====================
    
    private int bestPairs;
    private boolean isNewRecord;
    
    // ==================== HUD ====================
    
    private BitmapFont hudFont;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    private SimpleButton pauseButton;
    private Texture pauseIconTexture;
    private Texture nekoinIconTexture;
    
    // ==================== PANELES ====================
    
    private Texture panelTexture;
    private SimpleButton continueButton;
    private SimpleButton exitButton;
    private SimpleButton watchAdButton;
    private Texture buttonTexture;
    
    private boolean adWatched;
    private boolean showingAdOption;
    
    // ==================== TIMERS ====================
    
    private float checkDelayTimer;
    private float gridTransitionTimer;
    private static final float GRID_TRANSITION_DURATION = 1.0f;
    private static final float STARTING_DURATION = 1.5f;
    private float startingTimer;
    
    // ==================== SISTEMAS ====================
    
    private AudioManager audioManager;
    private SaveManager saveManager;
    private LocaleManager locale;
    private AdController adController;
    
    private final Vector2 touchPoint = new Vector2();
    
    // ==================== CONSTRUCTORES ====================
    
    /**
     * Constructor por defecto - usa MODE_30 para compatibilidad
     */
    public TimeAttackScreen(KawaiiNekoMemory game) {
        this(game, Mode.MODE_30, null);
    }
    
    /**
     * Constructor con modo específico
     */
    public TimeAttackScreen(KawaiiNekoMemory game, Mode mode) {
        this(game, mode, null);
    }
    
    /**
     * Constructor completo con modo y AdController
     */
    public TimeAttackScreen(KawaiiNekoMemory game, Mode mode, AdController adController) {
        super(game);
        
        this.currentMode = mode;
        this.cols = mode.cols;
        this.rows = mode.rows;
        this.pairs = mode.pairs;
        
        this.audioManager = AudioManager.getInstance();
        this.saveManager = SaveManager.getInstance();
        this.locale = LocaleManager.getInstance();
        this.adController = adController;
        
        this.hudFont = game.getFontManager().getButtonFont();
        this.titleFont = game.getFontManager().getTitleFont();
        this.buttonFont = game.getFontManager().getButtonFont();
        this.smallFont = game.getFontManager().getSmallFont();
        this.layout = new GlyphLayout();
        
        this.cards = new Array<>();
        this.cardFrontTextures = new Array<>();
        
        this.timeLimit = saveManager.getTimeAttackTime();
        this.timeRemaining = timeLimit;
        
        this.pairsFoundThisGrid = 0;
        this.pairsFoundTotal = 0;
        this.gridsCompleted = 0;
        this.nekoinsEarned = 0;
        
        this.bestPairs = saveManager.getTimeAttackBestPairs();
        this.isNewRecord = false;
        this.adWatched = false;
        this.showingAdOption = false;
        
        this.gameState = GameState.STARTING;
        this.startingTimer = STARTING_DURATION;
        
        loadAssets();
        createBoard();
        createHUD();
        createPanels();
        playRandomGameMusic();
        
        Gdx.app.log(TAG, "=== TIME ATTACK " + (mode == Mode.MODE_12 ? "12" : "30") + " ===");
        Gdx.app.log(TAG, "Grid: " + cols + "x" + rows + " = " + pairs + " pares");
        Gdx.app.log(TAG, "Tiempo: " + timeLimit + "s | Récord: " + bestPairs);
    }
    
    // ==================== ASSETS ====================
    
    private void loadAssets() {
        try {
            backgroundTexture = new Texture(Gdx.files.internal(AssetPaths.BG_HARD));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando fondo");
        }
        
        try {
            cardBackTexture = new Texture(Gdx.files.internal(AssetPaths.CARD_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando card back");
        }
        
        loadDeckTextures();
        
        try {
            pauseIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_PAUSE));
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando iconos");
        }
        
        try {
            panelTexture = new Texture(Gdx.files.internal(AssetPaths.PANEL_DEFEAT));
            buttonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando paneles");
        }
    }
    
    private void loadDeckTextures() {
        Array<Integer> activeCards = saveManager.getActiveCards();
        
        Array<Integer> validCardIds = new Array<>();
        for (int i = 0; i < activeCards.size; i++) {
            int cardId = activeCards.get(i);
            if (cardId >= 0) validCardIds.add(cardId);
        }
        
        // Solo repetir cartas si es absolutamente necesario
        // (esto no debería pasar si el bloqueo funciona correctamente)
        while (validCardIds.size < pairs && validCardIds.size > 0) {
            validCardIds.add(validCardIds.get(validCardIds.size % validCardIds.size));
            Gdx.app.log(TAG, "WARN: Repitiendo carta por falta de cartas activas");
        }
        
        for (int i = 0; i < pairs; i++) {
            int cardId = validCardIds.get(i);
            int deck = SaveManager.getDeckFromCardId(cardId);
            int cardIndex = SaveManager.getCardIndexFromCardId(cardId);
            String path = AssetPaths.getCardPath(deck, cardIndex);
            
            try {
                Texture tex = new Texture(Gdx.files.internal(path));
                cardFrontTextures.add(tex);
            } catch (Exception e) {
                cardFrontTextures.add(null);
            }
        }
        
        Gdx.app.log(TAG, "Cartas cargadas: " + cardFrontTextures.size + "/" + pairs);
    }
    
    // ==================== TABLERO ====================
    
    private void createBoard() {
        cards.clear();
        
        float hudHeight = Constants.HUD_HEIGHT;
        float padding = Constants.GRID_PADDING;
        float margin = Constants.CARD_MARGIN_PERCENT;
        
        boardWidth = Constants.VIRTUAL_WIDTH - (padding * 2);
        boardHeight = Constants.VIRTUAL_HEIGHT - hudHeight - (padding * 2);
        boardX = padding;
        boardY = padding;
        
        float totalMarginX = boardWidth * margin * (cols + 1);
        float totalMarginY = boardHeight * margin * (rows + 1);
        
        cardWidth = (boardWidth - totalMarginX) / cols;
        cardHeight = (boardHeight - totalMarginY) / rows;
        
        float desiredRatio = 1.4f;
        if (cardHeight > cardWidth * desiredRatio) {
            cardHeight = cardWidth * desiredRatio;
        } else {
            cardWidth = cardHeight / desiredRatio;
        }
        
        float actualMarginX = (boardWidth - (cardWidth * cols)) / (cols + 1);
        float actualMarginY = (boardHeight - (cardHeight * rows)) / (rows + 1);
        
        float startX = boardX + actualMarginX;
        float startY = boardY + actualMarginY;
        
        Array<Integer> cardIds = new Array<>();
        for (int i = 0; i < pairs; i++) {
            cardIds.add(i);
            cardIds.add(i);
        }
        cardIds.shuffle();
        
        Array<Integer> activeCardIds = saveManager.getActiveCards();
        
        int cardIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (cardIndex >= cardIds.size) break;
                
                float x = startX + col * (cardWidth + actualMarginX);
                float y = startY + (rows - 1 - row) * (cardHeight + actualMarginY);
                
                int pairId = cardIds.get(cardIndex);
                Texture frontTex = (pairId < cardFrontTextures.size) ? 
                                   cardFrontTextures.get(pairId) : null;
                
                Card card = new Card(pairId, frontTex, cardBackTexture, x, y, cardWidth, cardHeight);
                
                int realCardId = -1;
                int validIndex = 0;
                for (int i = 0; i < activeCardIds.size && validIndex <= pairId; i++) {
                    if (activeCardIds.get(i) >= 0) {
                        if (validIndex == pairId) {
                            realCardId = activeCardIds.get(i);
                            break;
                        }
                        validIndex++;
                    }
                }
                
                if (realCardId >= 0) {
                    card.setDeckIndex(SaveManager.getDeckFromCardId(realCardId));
                    card.setNekoinValue(saveManager.getCardNekoinValue(realCardId));
                } else {
                    card.setNekoinValue(Constants.TIME_ATTACK_NEKOIN_PER_PAIR);
                }
                
                cards.add(card);
                cardIndex++;
            }
        }
        
        pairsFoundThisGrid = 0;
    }
    
    // ==================== HUD ====================
    
    private void createHUD() {
        float hudY = Constants.VIRTUAL_HEIGHT - Constants.HUD_HEIGHT + 10f;
        float buttonSize = 50f;
        
        if (pauseIconTexture != null) {
            pauseButton = new SimpleButton(pauseIconTexture, "", 10f, hudY, buttonSize, buttonSize);
            pauseButton.setOnClick(() -> {
                if (gameState == GameState.PLAYING) onTimeUp();
            });
        }
    }
    
    // ==================== PANELES ====================
    
    private void createPanels() {
        float panelWidth = Constants.VIRTUAL_WIDTH * 0.85f;
        float btnWidth = panelWidth * 0.7f;
        float btnHeight = 55f;
        float btnX = (Constants.VIRTUAL_WIDTH - btnWidth) / 2f;
        float btnSpacing = 15f;
        float baseY = Constants.VIRTUAL_HEIGHT * 0.18f;
        
        if (buttonTexture != null) {
            exitButton = new SimpleButton(buttonTexture, locale.get("game.btn.exit"), 
                btnX, baseY, btnWidth, btnHeight);
            exitButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new LevelSelectScreen(game));
            });
            
            continueButton = new SimpleButton(buttonTexture, locale.get("game.btn.restart"),
                btnX, baseY + btnHeight + btnSpacing, btnWidth, btnHeight);
            continueButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new TimeAttackScreen(game, currentMode, adController));
            });
            
            watchAdButton = new SimpleButton(buttonTexture, locale.get("game.btn.watchad"),
                btnX, baseY + (btnHeight + btnSpacing) * 2, btnWidth, btnHeight);
            watchAdButton.setOnClick(this::onWatchAdClicked);
        }
    }
    
    // ==================== MÚSICA ====================
    
    private void playRandomGameMusic() {
        int trackIndex = MathUtils.random(0, Constants.GAME_MUSIC_TRACKS - 1);
        String musicPath = AssetPaths.getGameMusicPath(trackIndex);
        audioManager.playMusic(musicPath, true);
    }
    
    // ==================== UPDATE ====================
    
    @Override
    protected void update(float delta) {
        for (Card card : cards) {
            card.update(delta);
        }
        
        switch (gameState) {
            case STARTING: updateStarting(delta); break;
            case PLAYING: updatePlaying(delta); break;
            case CHECKING: updateChecking(delta); break;
            case GRID_TRANSITION: updateGridTransition(delta); break;
            case GAME_OVER:
            case SHOWING_RESULTS: updateResults(delta); break;
        }
    }
    
    private void updateStarting(float delta) {
        startingTimer -= delta;
        if (startingTimer <= 0) {
            gameState = GameState.PLAYING;
        }
    }
    
    private void updatePlaying(float delta) {
        timeRemaining -= delta;
        
        if (timeRemaining <= 0) {
            timeRemaining = 0;
            onTimeUp();
            return;
        }
        
        if (!isInputEnabled()) return;
        
        if (pauseButton != null) pauseButton.update(viewport);
        
        if (Gdx.input.justTouched()) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            handleCardTouch(touchPoint.x, touchPoint.y);
        }
    }
    
    private void updateChecking(float delta) {
        timeRemaining -= delta;
        
        if (timeRemaining <= 0) {
            timeRemaining = 0;
            onTimeUp();
            return;
        }
        
        checkDelayTimer -= delta;
        if (checkDelayTimer <= 0) checkForMatch();
    }
    
    private void updateGridTransition(float delta) {
        gridTransitionTimer -= delta;
        
        if (gridTransitionTimer <= 0) {
            createBoard();
            gameState = GameState.PLAYING;
            audioManager.playSound(AssetPaths.SFX_CARD_SHUFFLE);
        }
    }
    
    private void updateResults(float delta) {
        if (!isInputEnabled()) return;
        
        if (continueButton != null) continueButton.update(viewport);
        if (exitButton != null) exitButton.update(viewport);
        
        if (showingAdOption && watchAdButton != null && !adWatched) {
            watchAdButton.update(viewport);
        }
    }
    
    // ==================== LÓGICA DE CARTAS ====================
    
    private void handleCardTouch(float x, float y) {
        if (firstRevealed != null && secondRevealed != null) return;
        
        for (Card card : cards) {
            if (card.contains(x, y) && card.canBeClicked()) {
                onCardClicked(card);
                break;
            }
        }
    }
    
    private void onCardClicked(Card card) {
        audioManager.playSound(AssetPaths.SFX_CARD_FLIP);
        card.flip();
        
        if (firstRevealed == null) {
            firstRevealed = card;
        } else {
            secondRevealed = card;
            gameState = GameState.CHECKING;
            checkDelayTimer = Constants.MATCH_CHECK_DELAY;
        }
    }
    
    private void checkForMatch() {
        if (firstRevealed == null || secondRevealed == null) {
            gameState = GameState.PLAYING;
            return;
        }
        
        boolean isMatch = firstRevealed.getCardId() == secondRevealed.getCardId();
        
        if (isMatch) {
            audioManager.playSound(AssetPaths.SFX_MATCH);
            firstRevealed.setMatched();
            secondRevealed.setMatched();
            
            int pairValue = firstRevealed.getNekoinValue();
            nekoinsEarned += pairValue;
            
            pairsFoundThisGrid++;
            pairsFoundTotal++;
            
            firstRevealed = null;
            secondRevealed = null;
            
            if (pairsFoundThisGrid >= pairs) {
                onGridComplete();
            } else {
                gameState = GameState.PLAYING;
            }
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            
            firstRevealed.flipBack();
            secondRevealed.flipBack();
            
            firstRevealed = null;
            secondRevealed = null;
            
            gameState = GameState.PLAYING;
        }
    }
    
    private void onGridComplete() {
        gridsCompleted++;
        audioManager.playSound(AssetPaths.SFX_VICTORY);
        
        gameState = GameState.GRID_TRANSITION;
        gridTransitionTimer = GRID_TRANSITION_DURATION;
    }
    
    // ==================== FIN DEL JUEGO ====================
    
    private void onTimeUp() {
        gameState = GameState.GAME_OVER;
        audioManager.playSound(AssetPaths.SFX_DEFEAT);
        
        isNewRecord = saveManager.updateTimeAttackBestPairs(pairsFoundTotal);
        
        saveManager.addTimeAttackPairs(pairsFoundTotal);
        saveManager.incrementTimeAttackGamesPlayed();
        saveManager.addPairsFound(pairsFoundTotal);
        saveManager.addNekoins(nekoinsEarned);
        
        showingAdOption = (adController != null && adController.isRewardedLoaded() && !adWatched);
        
        gameState = GameState.SHOWING_RESULTS;
    }
    
    private void onWatchAdClicked() {
        if (adController == null || !adController.isRewardedLoaded() || adWatched) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            return;
        }
        
        audioManager.playSound(AssetPaths.SFX_BUTTON);
        
        adController.showRewarded(new AdController.RewardedAdListener() {
            @Override
            public void onRewardEarned(String rewardType, int rewardAmount) {
                int bonus = (int)(nekoinsEarned * (Constants.TIME_ATTACK_AD_MULTIPLIER - 1f));
                saveManager.addNekoins(bonus);
                nekoinsEarned += bonus;
                adWatched = true;
                showingAdOption = false;
            }
            
            @Override
            public void onRewardCancelled() {}
        });
    }
    
    // ==================== DRAW ====================
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        if (backgroundTexture != null) {
            game.getBatch().draw(backgroundTexture, 0, 0, 
                                 Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        }
        
        drawBoard();
        drawHUD();
        
        game.getBatch().end();
        
        if (gameState == GameState.SHOWING_RESULTS) drawResultsPanel();
        if (gameState == GameState.STARTING) drawStartingCountdown();
        if (gameState == GameState.GRID_TRANSITION) drawGridTransition();
    }
    
    private void drawBoard() {
        for (Card card : cards) {
            card.draw(game.getBatch());
        }
    }
    
    private void drawHUD() {
        float hudY = Constants.VIRTUAL_HEIGHT - Constants.HUD_HEIGHT;
        
        game.getBatch().setColor(0, 0, 0, 0.5f);
        game.getBatch().draw(cardBackTexture, 0, hudY, Constants.VIRTUAL_WIDTH, Constants.HUD_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        if (pauseButton != null) pauseButton.drawNoText(game.getBatch());
        
        // Timer
        String timeText = formatTime(timeRemaining);
        if (timeRemaining < 10) {
            hudFont.setColor(Color.RED);
        } else if (timeRemaining < 30) {
            hudFont.setColor(Color.YELLOW);
        } else {
            hudFont.setColor(Color.WHITE);
        }
        layout.setText(hudFont, timeText);
        float timeX = Constants.VIRTUAL_WIDTH - layout.width - 15f;
        float timeY = hudY + (Constants.HUD_HEIGHT + layout.height) / 2f;
        hudFont.draw(game.getBatch(), timeText, timeX, timeY);
        hudFont.setColor(Color.WHITE);
        
        // Título con modo
        String modeText = currentMode == Mode.MODE_12 ? "12" : "30";
        String title = locale.get("timeattack.title") + " " + modeText;
        layout.setText(hudFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        hudFont.setColor(Color.ORANGE);
        hudFont.draw(game.getBatch(), title, titleX, timeY + 15f);
        hudFont.setColor(Color.WHITE);
        
        // Pares
        String pairsText = locale.format("game.pairs", pairsFoundTotal);
        layout.setText(hudFont, pairsText);
        float pairsX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        hudFont.draw(game.getBatch(), pairsText, pairsX, timeY - 15f);
        
        // Récord
        if (bestPairs > 0) {
            String recordText = locale.format("rankings.best", bestPairs);
            layout.setText(hudFont, recordText);
            hudFont.setColor(Color.GOLD);
            hudFont.draw(game.getBatch(), recordText, 70f, timeY);
            hudFont.setColor(Color.WHITE);
        }
        
        // Nekoins
        if (nekoinIconTexture != null && nekoinsEarned > 0) {
            String nekoinText = "+" + nekoinsEarned;
            layout.setText(hudFont, nekoinText);
            float iconSize = 24f;
            float iconX = 70f;
            float iconY = timeY - 35f;
            
            game.getBatch().draw(nekoinIconTexture, iconX, iconY - iconSize + 5f, iconSize, iconSize);
            hudFont.setColor(Color.GOLD);
            hudFont.draw(game.getBatch(), nekoinText, iconX + iconSize + 5f, iconY);
            hudFont.setColor(Color.WHITE);
        }
    }
    
    private void drawStartingCountdown() {
        game.getBatch().begin();
        
        game.getBatch().setColor(0, 0, 0, 0.6f);
        game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        int countdown = (int) Math.ceil(startingTimer);
        String text = countdown > 0 ? String.valueOf(countdown) : locale.get("timeattack.go");
        
        titleFont.setColor(Color.WHITE);
        layout.setText(titleFont, text);
        titleFont.draw(game.getBatch(), text,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      (Constants.VIRTUAL_HEIGHT + layout.height) / 2f);
        
        game.getBatch().end();
    }
    
    private void drawGridTransition() {
        game.getBatch().begin();
        
        game.getBatch().setColor(0, 0, 0, 0.7f);
        game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        String text = locale.format("timeattack.grid", gridsCompleted + 1);
        titleFont.setColor(Color.GREEN);
        layout.setText(titleFont, text);
        titleFont.draw(game.getBatch(), text,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      (Constants.VIRTUAL_HEIGHT + layout.height) / 2f);
        titleFont.setColor(Color.WHITE);
        
        game.getBatch().end();
    }
    
    private void drawResultsPanel() {
        game.getBatch().begin();
        
        game.getBatch().setColor(0, 0, 0, 0.85f);
        game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        if (panelTexture != null) {
            float panelWidth = Constants.VIRTUAL_WIDTH * 0.9f;
            float panelHeight = panelWidth * 1.1f;
            float panelX = (Constants.VIRTUAL_WIDTH - panelWidth) / 2f;
            float panelY = (Constants.VIRTUAL_HEIGHT - panelHeight) / 2f;
            game.getBatch().draw(panelTexture, panelX, panelY, panelWidth, panelHeight);
        }
        
        // Título
        String title = locale.get("game.defeat");
        titleFont.setColor(Color.ORANGE);
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.78f);
        titleFont.setColor(Color.WHITE);
        
        float statsY = Constants.VIRTUAL_HEIGHT * 0.65f;
        float lineHeight = 35f;
        
        // Modo
        String modeText = "Mode: " + (currentMode == Mode.MODE_12 ? "12" : "30");
        layout.setText(buttonFont, modeText);
        buttonFont.setColor(Color.ORANGE);
        buttonFont.draw(game.getBatch(), modeText,
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY + lineHeight);
        buttonFont.setColor(Color.WHITE);
        
        // Pares
        String pairsText = locale.format("game.pairs", pairsFoundTotal);
        layout.setText(buttonFont, pairsText);
        buttonFont.draw(game.getBatch(), pairsText,
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY);
        
        // Grids
        String gridsText = locale.format("game.grids", gridsCompleted);
        layout.setText(buttonFont, gridsText);
        buttonFont.draw(game.getBatch(), gridsText,
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY - lineHeight);
        
        // Nuevo récord
        if (isNewRecord) {
            buttonFont.setColor(Color.GOLD);
            String recordText = locale.get("game.newrecord");
            layout.setText(buttonFont, recordText);
            buttonFont.draw(game.getBatch(), recordText,
                           (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY - lineHeight * 2);
            buttonFont.setColor(Color.WHITE);
        }
        
        // Nekoins
        String nekoinText;
        if (adWatched) {
            nekoinText = locale.get("common.nekoins") + ": " + nekoinsEarned + " (x2.5!)";
            buttonFont.setColor(Color.LIME);
        } else {
            nekoinText = locale.get("common.nekoins") + ": " + nekoinsEarned;
            buttonFont.setColor(Color.GOLD);
        }
        layout.setText(buttonFont, nekoinText);
        buttonFont.draw(game.getBatch(), nekoinText,
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY - lineHeight * 3);
        buttonFont.setColor(Color.WHITE);
        
        // Tiempo
        String timeText = locale.format("game.time", formatTime(timeLimit));
        layout.setText(hudFont, timeText);
        hudFont.setColor(Color.LIGHT_GRAY);
        hudFont.draw(game.getBatch(), timeText,
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY - lineHeight * 4);
        hudFont.setColor(Color.WHITE);
        
        // Botones
        if (showingAdOption && !adWatched && watchAdButton != null) {
            watchAdButton.draw(game.getBatch(), buttonFont);
        }
        if (continueButton != null) continueButton.draw(game.getBatch(), buttonFont);
        if (exitButton != null) exitButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
    }
    
    // ==================== UTILIDADES ====================
    
    private String formatTime(float seconds) {
        int mins = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }
    
    // ==================== DISPOSE ====================
    
    @Override
    public void dispose() {
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (cardBackTexture != null) cardBackTexture.dispose();
        
        for (Texture tex : cardFrontTextures) {
            if (tex != null) tex.dispose();
        }
        cardFrontTextures.clear();
        
        if (pauseIconTexture != null) pauseIconTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        if (panelTexture != null) panelTexture.dispose();
        if (buttonTexture != null) buttonTexture.dispose();
    }
}
