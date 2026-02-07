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
import com.darkphoenixteam.kawaiinekomemory.models.Achievement;
import com.darkphoenixteam.kawaiinekomemory.models.Card;
import com.darkphoenixteam.kawaiinekomemory.models.LevelData;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla principal de juego
 * Maneja el tablero de cartas, timer, puntuación y paneles de resultado
 * 
 * @author DarkphoenixTeam
 * @version 1.3 - Powers + Timer total + Achievements tracking
 */
public class GameScreen extends BaseScreen {
    
    private static final String TAG = "GameScreen";
    
    // ==================== ESTADOS DEL JUEGO ====================
    
    public enum GameState {
        STARTING,
        PLAYING,
        CHECKING,
        NO_MATCH_SHAKE,
        SHUFFLING,
        PAUSED,
        VICTORY,
        DEFEAT
    }
    
    private GameState gameState;
    
    // ==================== DATOS DEL NIVEL ====================
    
    private LevelData levelData;
    private int currentGrid;
    private int totalGrids;
    private int pairsFoundThisGrid;
    private int pairsFoundTotal;
    private int pairsPerGrid;
    private int matchesSinceShuffle;
    
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
    private float elapsedTime;
    private int moveCount;
    private int deckBonus;
    private boolean isTimeFrozen;
    private float timeFreezeRemaining;
    
    // ==================== POWERS ====================
    
    private int hintUsesLeft;
    private int timeFreezeUsesLeft;
    private int hintsUsedThisGame;
    private int timeFreezeUsedThisGame;
    
    private static final int MAX_HINTS_PER_GAME = 5;
    private static final int MAX_TIMEFREEZE_PER_GAME = 5;
    private static final float TIMEFREEZE_DURATION = 5.0f;
    private static final float HINT_SHAKE_DURATION = 1.5f;
    
    // ==================== ESTADÍSTICAS DE PARTIDA ====================
    
    private int currentCombo;
    private int bestComboThisGame;
    private int mistakesThisGame;
    private int powersUsedThisGame;
    
    // ==================== HUD ====================
    
    private BitmapFont hudFont;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private GlyphLayout layout;
    
    private SimpleButton pauseButton;
    private SimpleButton hintButton;
    private SimpleButton timeFreezeButton;
    
    private Texture pauseIconTexture;
    private Texture hintIconTexture;
    private Texture timeFreezeIconTexture;
    private Texture nekoinIconTexture;
    
    // ==================== PANELES ====================
    
    private Texture panelPauseTexture;
    private Texture panelVictoryTexture;
    private Texture panelDefeatTexture;
    
    private SimpleButton continueButton;
    private SimpleButton restartButton;
    private SimpleButton exitButton;
    private SimpleButton nextLevelButton;
    
    private Texture buttonTexture;
    
    // ==================== RESULTADOS ====================
    
    private int starsEarned;
    private int levelReward;
    private int totalNekoins;
    private boolean isFirstClear;
    
    // ==================== TIMERS INTERNOS ====================
    
    private float checkDelayTimer;
    private float startingTimer;
    private static final float STARTING_DURATION = 2.0f;
    private boolean cardsRevealedAtStart = false;
    
    // No Match feedback
    private float noMatchShakeTimer;
    private static final float NO_MATCH_SHAKE_DURATION = 0.4f;
    private Card noMatchCard1;
    private Card noMatchCard2;
    
    // ==================== AUDIO ====================
    
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    // ==================== INPUT ====================
    
    private final Vector2 touchPoint = new Vector2();
    
    // ==================== CONSTRUCTOR ====================
    
    public GameScreen(KawaiiNekoMemory game, LevelData levelData) {
        super(game);
        
        this.levelData = levelData;
        this.audioManager = AudioManager.getInstance();
        this.saveManager = SaveManager.getInstance();
        
        this.hudFont = game.getFontManager().getButtonFont();
        this.titleFont = game.getFontManager().getTitleFont();
        this.buttonFont = game.getFontManager().getButtonFont();
        this.layout = new GlyphLayout();
        
        this.cards = new Array<>();
        this.cardFrontTextures = new Array<>();
        
        this.timeLimit = levelData.getTimeLimit();
        this.timeRemaining = timeLimit;
        this.elapsedTime = 0f;
        this.totalGrids = levelData.getGridCount();
        this.currentGrid = 0;
        this.pairsPerGrid = levelData.getDifficulty().getPairs();
        this.pairsFoundThisGrid = 0;
        this.pairsFoundTotal = 0;
        this.matchesSinceShuffle = 0;
        this.moveCount = 0;
        this.deckBonus = 0;
        this.isTimeFrozen = false;
        this.timeFreezeRemaining = 0f;
        
        // Powers
        this.hintUsesLeft = saveManager.getHintUses();
        this.timeFreezeUsesLeft = saveManager.getTimeFreezeUses();
        this.hintsUsedThisGame = 0;
        this.timeFreezeUsedThisGame = 0;
        
        // Estadísticas de partida
        this.currentCombo = 0;
        this.bestComboThisGame = 0;
        this.mistakesThisGame = 0;
        this.powersUsedThisGame = 0;
        
        this.gameState = GameState.STARTING;
        this.startingTimer = STARTING_DURATION;
        
        this.isFirstClear = !saveManager.isLevelCompleted(levelData.getGlobalId());
        
        loadAssets();
        createBoard();
        createHUD();
        createPanels();
        playRandomGameMusic();
        
        Gdx.app.log(TAG, "=== NIVEL INICIADO ===");
        Gdx.app.log(TAG, levelData.toString());
        Gdx.app.log(TAG, "Powers: Hint=" + hintUsesLeft + " | TimeFreeze=" + timeFreezeUsesLeft);
    }
    
    // ==================== CARGA DE ASSETS ====================
    
    private void loadAssets() {
        String bgPath = getBackgroundPath();
        try {
            backgroundTexture = new Texture(Gdx.files.internal(bgPath));
            Gdx.app.log(TAG, "Fondo cargado: " + bgPath);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando fondo: " + bgPath);
        }
        
        try {
            cardBackTexture = new Texture(Gdx.files.internal(AssetPaths.CARD_BACK));
            Gdx.app.log(TAG, "Card back cargado");
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando card back");
        }
        
        loadDeckTextures();
        
        try {
            pauseIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_PAUSE));
            hintIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_HINT));
            timeFreezeIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_TIMEFREEZE));
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando iconos HUD");
        }
        
        try {
            panelPauseTexture = new Texture(Gdx.files.internal(AssetPaths.PANEL_PAUSE));
            panelVictoryTexture = new Texture(Gdx.files.internal(AssetPaths.PANEL_VICTORY));
            panelDefeatTexture = new Texture(Gdx.files.internal(AssetPaths.PANEL_DEFEAT));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando paneles");
        }
        
        try {
            buttonTexture = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando textura de botón");
        }
    }
    
    private String getBackgroundPath() {
        switch (levelData.getDifficulty()) {
            case EASY: return AssetPaths.BG_EASY;
            case NORMAL: return AssetPaths.BG_NORMAL;
            case ADVANCED: return AssetPaths.BG_ADVANCED;
            case HARD: return AssetPaths.BG_HARD;
            default: return AssetPaths.BG_EASY;
        }
    }
    
    private void loadDeckTextures() {
        Array<Integer> activeCards = saveManager.getActiveCards();
        int cardsNeeded = levelData.getUniqueCardsRequired();
        
        Gdx.app.log(TAG, "Cargando " + cardsNeeded + " cartas del mazo activo");
        
        Array<Integer> validCardIds = new Array<>();
        for (int i = 0; i < activeCards.size; i++) {
            int cardId = activeCards.get(i);
            if (cardId >= 0) {
                validCardIds.add(cardId);
            }
        }
        
        if (validCardIds.size < cardsNeeded) {
            Gdx.app.error(TAG, "¡No hay suficientes cartas activas! Necesarias: " + 
                          cardsNeeded + ", Disponibles: " + validCardIds.size);
            while (validCardIds.size < cardsNeeded && validCardIds.size > 0) {
                validCardIds.add(validCardIds.get(validCardIds.size % validCardIds.size));
            }
        }
        
        for (int i = 0; i < cardsNeeded; i++) {
            int cardId = validCardIds.get(i);
            int deck = SaveManager.getDeckFromCardId(cardId);
            int cardIndex = SaveManager.getCardIndexFromCardId(cardId);
            String path = AssetPaths.getCardPath(deck, cardIndex);
            
            try {
                Texture tex = new Texture(Gdx.files.internal(path));
                cardFrontTextures.add(tex);
                Gdx.app.log(TAG, "Carta " + i + ": cardId=" + cardId + 
                            " (deck" + deck + "/char" + cardIndex + ")");
            } catch (Exception e) {
                Gdx.app.error(TAG, "Error cargando carta: " + path);
                cardFrontTextures.add(null);
            }
        }
        
        Gdx.app.log(TAG, "Texturas cargadas: " + cardFrontTextures.size);
    }
    
    // ==================== CREACIÓN DEL TABLERO ====================
    
    private void createBoard() {
        cards.clear();
        
        LevelData.Difficulty diff = levelData.getDifficulty();
        int cols = diff.cols;
        int rows = diff.rows;
        int pairs = diff.getPairs();
        
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
                    int deckIndex = SaveManager.getDeckFromCardId(realCardId);
                    card.setDeckIndex(deckIndex);
                    card.setCardIndex(SaveManager.getCardIndexFromCardId(realCardId));
                    card.setNekoinValue(saveManager.getCardNekoinValue(realCardId));
                } else {
                    card.setDeckIndex(0);
                    card.setNekoinValue(1);
                }
                
                cards.add(card);
                cardIndex++;
            }
        }
        
        Gdx.app.log(TAG, "Tablero creado: " + cols + "x" + rows + " = " + cards.size + " cartas");
    }
    
    // ==================== CREACIÓN DEL HUD ====================
    
    private void createHUD() {
        float hudY = Constants.VIRTUAL_HEIGHT - Constants.HUD_HEIGHT + 10f;
        float buttonSize = 50f;
        float spacing = 10f;
        
        if (pauseIconTexture != null) {
            pauseButton = new SimpleButton(
                pauseIconTexture, "",
                spacing, hudY,
                buttonSize, buttonSize
            );
            pauseButton.setOnClick(() -> {
                if (gameState == GameState.PLAYING) {
                    pauseGame();
                }
            });
        }
        
        if (hintIconTexture != null) {
            hintButton = new SimpleButton(
                hintIconTexture, "",
                spacing + buttonSize + spacing, hudY,
                buttonSize, buttonSize
            );
            hintButton.setOnClick(() -> {
                if (gameState == GameState.PLAYING) {
                    useHint();
                }
            });
        }
        
        if (timeFreezeIconTexture != null) {
            timeFreezeButton = new SimpleButton(
                timeFreezeIconTexture, "",
                spacing + (buttonSize + spacing) * 2, hudY,
                buttonSize, buttonSize
            );
            timeFreezeButton.setOnClick(() -> {
                if (gameState == GameState.PLAYING) {
                    useTimeFreeze();
                }
            });
        }
    }
    
    // ==================== CREACIÓN DE PANELES ====================
    
    private void createPanels() {
        float panelWidth = Constants.VIRTUAL_WIDTH * 0.8f;
        float panelHeight = panelWidth * 1.2f;
        float panelX = (Constants.VIRTUAL_WIDTH - panelWidth) / 2f;
        float panelY = (Constants.VIRTUAL_HEIGHT - panelHeight) / 2f;
        
        float btnWidth = panelWidth * 0.6f;
        float btnHeight = 50f;
        float btnX = (Constants.VIRTUAL_WIDTH - btnWidth) / 2f;
        float btnSpacing = 15f;
        
        float btnY = panelY + panelHeight * 0.15f;
        
        if (buttonTexture != null) {
            exitButton = new SimpleButton(buttonTexture, "SALIR", btnX, btnY, btnWidth, btnHeight);
            exitButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new LevelSelectScreen(game));
            });
            
            btnY += btnHeight + btnSpacing;
            
            restartButton = new SimpleButton(buttonTexture, "REINICIAR", btnX, btnY, btnWidth, btnHeight);
            restartButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new GameScreen(game, levelData));
            });
            
            btnY += btnHeight + btnSpacing;
            
            continueButton = new SimpleButton(buttonTexture, "CONTINUAR", btnX, btnY, btnWidth, btnHeight);
            continueButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                resumeGame();
            });
            
            nextLevelButton = new SimpleButton(buttonTexture, "SIGUIENTE", btnX, btnY, btnWidth, btnHeight);
            nextLevelButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                goToNextLevel();
            });
        }
    }
    
    // ==================== MÚSICA ====================
    
    private void playRandomGameMusic() {
        int trackIndex = MathUtils.random(0, Constants.GAME_MUSIC_TRACKS - 1);
        String musicPath = AssetPaths.getGameMusicPath(trackIndex);
        audioManager.playMusic(musicPath, true);
        Gdx.app.log(TAG, "Música: " + musicPath);
    }
    
    // ==================== UPDATE ====================
    
    @Override
    protected void update(float delta) {
        for (Card card : cards) {
            card.update(delta);
        }
        
        switch (gameState) {
            case STARTING:
                updateStarting(delta);
                break;
            case PLAYING:
                updatePlaying(delta);
                break;
            case CHECKING:
                updateChecking(delta);
                break;
            case NO_MATCH_SHAKE:
                updateNoMatchShake(delta);
                break;
            case SHUFFLING:
                updateShuffling(delta);
                break;
            case PAUSED:
                updatePaused(delta);
                break;
            case VICTORY:
            case DEFEAT:
                updateResult(delta);
                break;
        }
    }
    
    private void updateStarting(float delta) {
        if (!cardsRevealedAtStart) {
            for (Card card : cards) {
                if (card.getState() == Card.State.HIDDEN) {
                    card.flip();
                }
            }
            cardsRevealedAtStart = true;
            audioManager.playSound(AssetPaths.SFX_CARD_SHUFFLE);
            Gdx.app.log(TAG, "Preview: Revelando " + cards.size + " cartas");
        }
        
        startingTimer -= delta;
        
        if (startingTimer <= 0) {
            boolean allReady = true;
            for (Card card : cards) {
                if (card.isAnimating()) {
                    allReady = false;
                    break;
                }
            }
            
            if (allReady) {
                int flippedBack = 0;
                for (Card card : cards) {
                    if (card.getState() == Card.State.REVEALED) {
                        card.flipBack();
                        flippedBack++;
                    }
                }
                
                gameState = GameState.PLAYING;
                cardsRevealedAtStart = false;
                
                Gdx.app.log(TAG, "¡COMIENZA EL JUEGO! (" + flippedBack + " cartas volteadas)");
            }
        }
    }
    
    private void updatePlaying(float delta) {
        elapsedTime += delta;
        
        if (!isTimeFrozen) {
            timeRemaining -= delta;
            if (timeRemaining <= 0) {
                timeRemaining = 0;
                onDefeat();
                return;
            }
        } else {
            timeFreezeRemaining -= delta;
            if (timeFreezeRemaining <= 0) {
                isTimeFrozen = false;
                timeFreezeRemaining = 0f;
                Gdx.app.log(TAG, "TimeFreeze terminado");
            }
        }
        
        if (!isInputEnabled()) return;
        
        if (pauseButton != null) pauseButton.update(viewport);
        if (hintButton != null) hintButton.update(viewport);
        if (timeFreezeButton != null) timeFreezeButton.update(viewport);
        
        if (Gdx.input.justTouched()) {
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY()));
            handleCardTouch(touchPoint.x, touchPoint.y);
        }
    }
    
    private void updateChecking(float delta) {
        elapsedTime += delta;
        
        if (!isTimeFrozen) {
            timeRemaining -= delta;
            if (timeRemaining <= 0) {
                timeRemaining = 0;
                onDefeat();
                return;
            }
        } else {
            timeFreezeRemaining -= delta;
            if (timeFreezeRemaining <= 0) {
                isTimeFrozen = false;
                timeFreezeRemaining = 0f;
            }
        }
        
        checkDelayTimer -= delta;
        
        if (checkDelayTimer <= 0) {
            checkForMatch();
        }
    }
    
    private void updateNoMatchShake(float delta) {
        elapsedTime += delta;
        
        if (!isTimeFrozen) {
            timeRemaining -= delta;
            if (timeRemaining <= 0) {
                timeRemaining = 0;
                onDefeat();
                return;
            }
        } else {
            timeFreezeRemaining -= delta;
            if (timeFreezeRemaining <= 0) {
                isTimeFrozen = false;
                timeFreezeRemaining = 0f;
            }
        }
        
        noMatchShakeTimer -= delta;
        
        if (noMatchShakeTimer <= 0) {
            if (noMatchCard1 != null) {
                noMatchCard1.flipBack();
            }
            if (noMatchCard2 != null) {
                noMatchCard2.flipBack();
            }
            
            noMatchCard1 = null;
            noMatchCard2 = null;
            firstRevealed = null;
            secondRevealed = null;
            
            gameState = GameState.PLAYING;
        }
    }
    
    private void updateShuffling(float delta) {
        elapsedTime += delta;
        gameState = GameState.PLAYING;
    }
    
    private void updatePaused(float delta) {
        if (!isInputEnabled()) return;
        
        if (continueButton != null) continueButton.update(viewport);
        if (restartButton != null) restartButton.update(viewport);
        if (exitButton != null) exitButton.update(viewport);
    }
    
    private void updateResult(float delta) {
        if (!isInputEnabled()) return;
        
        if (gameState == GameState.VICTORY) {
            if (nextLevelButton != null) nextLevelButton.update(viewport);
        }
        if (restartButton != null) restartButton.update(viewport);
        if (exitButton != null) exitButton.update(viewport);
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
        moveCount++;
        
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
            
            deckBonus += firstRevealed.getNekoinValue();
            
            pairsFoundThisGrid++;
            pairsFoundTotal++;
            matchesSinceShuffle++;
            
            // Combo tracking
            currentCombo++;
            if (currentCombo > bestComboThisGame) {
                bestComboThisGame = currentCombo;
            }
            
            Gdx.app.log(TAG, "MATCH! Pares: " + pairsFoundThisGrid + "/" + pairsPerGrid + 
                             " | Bonus: +" + firstRevealed.getNekoinValue() +
                             " | Combo: " + currentCombo);
            
            firstRevealed = null;
            secondRevealed = null;
            
            if (pairsFoundThisGrid >= pairsPerGrid) {
                onGridComplete();
            } else if (levelData.isShuffleEnabled() && 
                       matchesSinceShuffle >= Constants.SHUFFLE_TRIGGER_PAIRS) {
                triggerShuffle();
            } else {
                gameState = GameState.PLAYING;
            }
        } else {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            
            // Romper combo y contar error
            currentCombo = 0;
            mistakesThisGame++;
            
            noMatchCard1 = firstRevealed;
            noMatchCard2 = secondRevealed;
            
            noMatchCard1.startShake(NO_MATCH_SHAKE_DURATION);
            noMatchCard2.startShake(NO_MATCH_SHAKE_DURATION);
            
            noMatchShakeTimer = NO_MATCH_SHAKE_DURATION;
            gameState = GameState.NO_MATCH_SHAKE;
            
            Gdx.app.log(TAG, "No match - Combo roto | Errores: " + mistakesThisGame);
        }
    }
    
    private void onGridComplete() {
        currentGrid++;
        
        if (currentGrid >= totalGrids) {
            onVictory();
        } else {
            Gdx.app.log(TAG, "Grid " + currentGrid + " completado. Siguiente grid...");
            pairsFoundThisGrid = 0;
            matchesSinceShuffle = 0;
            cardsRevealedAtStart = false;
            createBoard();
            gameState = GameState.STARTING;
            startingTimer = STARTING_DURATION;
        }
    }
    
    private void triggerShuffle() {
        Gdx.app.log(TAG, "¡SHUFFLE!");
        audioManager.playSound(AssetPaths.SFX_CARD_SHUFFLE);
        
        matchesSinceShuffle = 0;
        
        Array<Card> unmatched = new Array<>();
        Array<Float> positionsX = new Array<>();
        Array<Float> positionsY = new Array<>();
        
        for (Card card : cards) {
            if (!card.isMatched()) {
                unmatched.add(card);
                positionsX.add(card.getX());
                positionsY.add(card.getY());
            }
        }
        
        for (int i = positionsX.size - 1; i > 0; i--) {
            int j = MathUtils.random(i);
            float tempX = positionsX.get(i);
            positionsX.set(i, positionsX.get(j));
            positionsX.set(j, tempX);
            float tempY = positionsY.get(i);
            positionsY.set(i, positionsY.get(j));
            positionsY.set(j, tempY);
        }
        
        for (int i = 0; i < unmatched.size; i++) {
            unmatched.get(i).setPosition(positionsX.get(i), positionsY.get(i));
        }
        
        gameState = GameState.SHUFFLING;
    }
    
    // ==================== POWERS ====================
    
    private void useTimeFreeze() {
        if (timeFreezeUsesLeft <= 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "TimeFreeze: Sin usos disponibles");
            return;
        }
        
        if (timeFreezeUsedThisGame >= MAX_TIMEFREEZE_PER_GAME) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "TimeFreeze: Límite por partida alcanzado (" + MAX_TIMEFREEZE_PER_GAME + ")");
            return;
        }
        
        if (isTimeFrozen) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "TimeFreeze: Ya está activo");
            return;
        }
        
        isTimeFrozen = true;
        timeFreezeRemaining = TIMEFREEZE_DURATION;
        timeFreezeUsesLeft--;
        timeFreezeUsedThisGame++;
        powersUsedThisGame++;
        saveManager.decrementTimeFreezeUses();
        audioManager.playSound(AssetPaths.SFX_TIMEFREEZE);
        
        Gdx.app.log(TAG, "TimeFreeze activado! Duración: " + TIMEFREEZE_DURATION + "s | " +
                    "Restantes: " + timeFreezeUsesLeft + " | Powers esta partida: " + powersUsedThisGame);
    }
    
    private void useHint() {
        if (hintUsesLeft <= 0) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Hint: Sin usos disponibles");
            return;
        }
        
        if (hintsUsedThisGame >= MAX_HINTS_PER_GAME) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Hint: Límite por partida alcanzado (" + MAX_HINTS_PER_GAME + ")");
            return;
        }
        
        Array<Card> hiddenCards = new Array<>();
        for (Card card : cards) {
            if (card.getState() == Card.State.HIDDEN) {
                hiddenCards.add(card);
            }
        }
        
        if (hiddenCards.size < 2) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Hint: No hay suficientes cartas ocultas");
            return;
        }
        
        boolean useFullHint = hiddenCards.size > 12;
        int pairsToShow = useFullHint ? 2 : 1;
        
        Array<Card> cardsToShake = new Array<>();
        Array<Integer> usedCardIds = new Array<>();
        
        for (int p = 0; p < pairsToShow; p++) {
            for (int i = 0; i < hiddenCards.size; i++) {
                Card card1 = hiddenCards.get(i);
                if (usedCardIds.contains(card1.getCardId(), false)) continue;
                
                for (int j = i + 1; j < hiddenCards.size; j++) {
                    Card card2 = hiddenCards.get(j);
                    if (card1.getCardId() == card2.getCardId() && 
                        !cardsToShake.contains(card1, true) && 
                        !cardsToShake.contains(card2, true)) {
                        
                        cardsToShake.add(card1);
                        cardsToShake.add(card2);
                        usedCardIds.add(card1.getCardId());
                        break;
                    }
                }
                
                if (cardsToShake.size >= pairsToShow * 2) break;
            }
        }
        
        Card randomCard = null;
        int attempts = 0;
        while (attempts < 30) {
            Card candidate = hiddenCards.random();
            if (!cardsToShake.contains(candidate, true)) {
                randomCard = candidate;
                break;
            }
            attempts++;
        }
        
        if (randomCard != null) {
            cardsToShake.add(randomCard);
        }
        
        if (cardsToShake.size < 2) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Hint: No se encontraron pares disponibles");
            return;
        }
        
        for (Card c : cardsToShake) {
            c.startShake(HINT_SHAKE_DURATION);
        }
        
        hintUsesLeft--;
        hintsUsedThisGame++;
        powersUsedThisGame++;
        saveManager.decrementHintUses();
        audioManager.playSound(AssetPaths.SFX_BUTTON);
        
        Gdx.app.log(TAG, "Hint usado! " + cardsToShake.size + " cartas temblando (" + 
                    pairsToShow + " par(es) + 1 random) | Restantes: " + hintUsesLeft + 
                    " | Powers esta partida: " + powersUsedThisGame);
    }
    
    // ==================== RESULTADOS ====================
    
    private void onVictory() {
        gameState = GameState.VICTORY;
        audioManager.playSound(AssetPaths.SFX_VICTORY);
        
        starsEarned = levelData.calculateStars(timeRemaining);
        levelReward = levelData.calculateLevelReward(starsEarned, isFirstClear);
        totalNekoins = levelReward + deckBonus;
        
        // Guardar con estrellas
        saveManager.setLevelCompleted(levelData.getGlobalId(), starsEarned);
        saveManager.addNekoins(totalNekoins);
        
        // Estadísticas globales
        saveManager.addPairsFound(pairsFoundTotal);
        saveManager.updateBestCombo(bestComboThisGame);
        
        // === VERIFICAR LOGROS DE HABILIDAD ===
        
        // Vista de Lince: sin errores
        if (mistakesThisGame == 0) {
            saveManager.unlockAchievement(Achievement.NO_MISTAKES);
        }
        
        // Gato Veloz: menos de 15 segundos
        if (elapsedTime < 15f) {
            saveManager.unlockAchievement(Achievement.SPEED_DEMON);
        }
        
        // Por los Bigotes: menos de 3 segundos restantes
        if (timeRemaining < 3f && timeRemaining > 0) {
            saveManager.unlockAchievement(Achievement.CLOSE_CALL);
        }
        
        // ¿Era necesario?: 3 poderes en un nivel
        if (powersUsedThisGame >= 3) {
            saveManager.unlockAchievement(Achievement.POWER_OVERLOAD);
        }
        
        Gdx.app.log(TAG, "=== VICTORIA ===");
        Gdx.app.log(TAG, "Estrellas: " + starsEarned);
        Gdx.app.log(TAG, "Tiempo total: " + formatTimeComplete(elapsedTime));
        Gdx.app.log(TAG, "Tiempo restante: " + (int)timeRemaining + "s");
        Gdx.app.log(TAG, "Level Reward: " + levelReward);
        Gdx.app.log(TAG, "Deck Bonus: " + deckBonus);
        Gdx.app.log(TAG, "Total Nekoins: " + totalNekoins);
        Gdx.app.log(TAG, "First Clear: " + isFirstClear);
        Gdx.app.log(TAG, "Mejor combo: " + bestComboThisGame);
        Gdx.app.log(TAG, "Errores: " + mistakesThisGame);
        Gdx.app.log(TAG, "Powers usados: " + powersUsedThisGame);
    }
    
    private void onDefeat() {
        gameState = GameState.DEFEAT;
        audioManager.playSound(AssetPaths.SFX_DEFEAT);
        
        // Registrar derrota y estadísticas
        saveManager.recordLoss();
        saveManager.addPairsFound(pairsFoundTotal);
        saveManager.updateBestCombo(bestComboThisGame);
        
        Gdx.app.log(TAG, "=== DERROTA ===");
        Gdx.app.log(TAG, "Tiempo total: " + formatTimeComplete(elapsedTime));
        Gdx.app.log(TAG, "Pares encontrados: " + pairsFoundTotal);
        Gdx.app.log(TAG, "Mejor combo: " + bestComboThisGame);
        Gdx.app.log(TAG, "Errores: " + mistakesThisGame);
    }
    
    // ==================== ACCIONES DE UI ====================
    
    private void pauseGame() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            audioManager.playSound(AssetPaths.SFX_BUTTON);
            audioManager.pauseMusic();
            Gdx.app.log(TAG, "Juego pausado");
        }
    }
    
    private void resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            audioManager.resumeMusic();
            Gdx.app.log(TAG, "Juego reanudado");
        }
    }
    
    private void goToNextLevel() {
        int nextGlobalId = levelData.getGlobalId() + 1;
        
        if (nextGlobalId >= Constants.TOTAL_LEVELS) {
            Gdx.app.log(TAG, "¡Último nivel completado!");
            game.setScreen(new HomeScreen(game));
            return;
        }
        
        if (!saveManager.isLevelUnlocked(nextGlobalId)) {
            Gdx.app.log(TAG, "Siguiente nivel no desbloqueado");
            game.setScreen(new LevelSelectScreen(game));
            return;
        }
        
        LevelData nextLevel = new LevelData(nextGlobalId);
        game.setScreen(new GameScreen(game, nextLevel));
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
        
        if (gameState == GameState.PAUSED) {
            drawPausePanel();
        } else if (gameState == GameState.VICTORY) {
            drawVictoryPanel();
        } else if (gameState == GameState.DEFEAT) {
            drawDefeatPanel();
        }
    }
    
    private void drawBoard() {
        for (Card card : cards) {
            card.draw(game.getBatch());
        }
    }
    
    private void drawHUD() {
        float hudY = Constants.VIRTUAL_HEIGHT - Constants.HUD_HEIGHT;
        
        game.getBatch().setColor(0, 0, 0, 0.3f);
        game.getBatch().draw(cardBackTexture, 0, hudY, Constants.VIRTUAL_WIDTH, Constants.HUD_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        float buttonSize = 50f;
        float spacing = 10f;
        float hudButtonY = Constants.VIRTUAL_HEIGHT - Constants.HUD_HEIGHT + 10f;
        
        if (pauseButton != null) pauseButton.drawNoText(game.getBatch());
        
        if (hintButton != null) {
            hintButton.drawNoText(game.getBatch());
            if (hintUsesLeft > 0) {
                String count = String.valueOf(hintUsesLeft);
                layout.setText(hudFont, count);
                float hintBtnX = spacing + buttonSize + spacing;
                hudFont.setColor(Color.WHITE);
                hudFont.draw(game.getBatch(), count, 
                            hintBtnX + buttonSize - layout.width - 5f,
                            hudButtonY + 15f);
            }
        }
        
        if (timeFreezeButton != null) {
            if (isTimeFrozen) {
                game.getBatch().setColor(0.5f, 0.8f, 1f, 0.5f);
            }
            timeFreezeButton.drawNoText(game.getBatch());
            game.getBatch().setColor(1, 1, 1, 1);
            
            if (timeFreezeUsesLeft > 0) {
                String count = String.valueOf(timeFreezeUsesLeft);
                layout.setText(hudFont, count);
                float freezeBtnX = spacing + (buttonSize + spacing) * 2;
                hudFont.setColor(Color.WHITE);
                hudFont.draw(game.getBatch(), count, 
                            freezeBtnX + buttonSize - layout.width - 5f,
                            hudButtonY + 15f);
            }
        }
        
        String timeText = formatTime(timeRemaining);
        if (isTimeFrozen) {
            hudFont.setColor(Color.CYAN);
            timeText = "❄ " + timeText + " ❄";
        } else if (timeRemaining < 10) {
            hudFont.setColor(Color.RED);
        } else {
            hudFont.setColor(Color.WHITE);
        }
        layout.setText(hudFont, timeText);
        float timeX = Constants.VIRTUAL_WIDTH - layout.width - 15f;
        float timeY = hudY + (Constants.HUD_HEIGHT + layout.height) / 2f;
        hudFont.draw(game.getBatch(), timeText, timeX, timeY);
        hudFont.setColor(Color.WHITE);
        
        String levelText = levelData.getDifficulty().name + " " + levelData.getLocalId();
        layout.setText(hudFont, levelText);
        float levelX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        hudFont.draw(game.getBatch(), levelText, levelX, timeY);
        
        if (nekoinIconTexture != null && deckBonus > 0) {
            String bonusText = "+" + deckBonus;
            layout.setText(hudFont, bonusText);
            float iconSize = 24f;
            float totalWidth = iconSize + 5f + layout.width;
            float iconX = (Constants.VIRTUAL_WIDTH - totalWidth) / 2f;
            float iconY = timeY - layout.height - 10f;
            
            game.getBatch().draw(nekoinIconTexture, iconX, iconY - iconSize + 5f, iconSize, iconSize);
            hudFont.setColor(Color.GOLD);
            hudFont.draw(game.getBatch(), bonusText, iconX + iconSize + 5f, iconY);
            hudFont.setColor(Color.WHITE);
        }
    }
    
    private void drawPausePanel() {
        game.getBatch().begin();
        
        game.getBatch().setColor(0, 0, 0, 0.7f);
        game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        if (panelPauseTexture != null) {
            float panelWidth = Constants.VIRTUAL_WIDTH * 0.85f;
            float panelHeight = panelWidth * 1.0f;
            float panelX = (Constants.VIRTUAL_WIDTH - panelWidth) / 2f;
            float panelY = (Constants.VIRTUAL_HEIGHT - panelHeight) / 2f;
            game.getBatch().draw(panelPauseTexture, panelX, panelY, panelWidth, panelHeight);
        }
        
        String title = "PAUSA";
        layout.setText(titleFont, title);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.7f);
        
        if (continueButton != null) continueButton.draw(game.getBatch(), buttonFont);
        if (restartButton != null) restartButton.draw(game.getBatch(), buttonFont);
        if (exitButton != null) exitButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
    }
    
    private void drawVictoryPanel() {
        game.getBatch().begin();
        
        game.getBatch().setColor(0, 0, 0, 0.7f);
        game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        if (panelVictoryTexture != null) {
            float panelWidth = Constants.VIRTUAL_WIDTH * 0.85f;
            float panelHeight = panelWidth * 1.2f;
            float panelX = (Constants.VIRTUAL_WIDTH - panelWidth) / 2f;
            float panelY = (Constants.VIRTUAL_HEIGHT - panelHeight) / 2f;
            game.getBatch().draw(panelVictoryTexture, panelX, panelY, panelWidth, panelHeight);
        }
        
        String title = "¡VICTORIA!";
        layout.setText(titleFont, title);
        titleFont.setColor(Color.GOLD);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.78f);
        titleFont.setColor(Color.WHITE);
        
        String stars = "";
        for (int i = 0; i < starsEarned; i++) stars += "★ ";
        for (int i = starsEarned; i < 3; i++) stars += "☆ ";
        layout.setText(titleFont, stars);
        titleFont.setColor(Color.YELLOW);
        titleFont.draw(game.getBatch(), stars,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.70f);
        titleFont.setColor(Color.WHITE);
        
        String timeTotal = "Tiempo: " + formatTimeComplete(elapsedTime);
        layout.setText(hudFont, timeTotal);
        hudFont.setColor(Color.LIGHT_GRAY);
        hudFont.draw(game.getBatch(), timeTotal,
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                    Constants.VIRTUAL_HEIGHT * 0.62f);
        
        float statsY = Constants.VIRTUAL_HEIGHT * 0.52f;
        float lineHeight = 32f;
        
        hudFont.setColor(Color.WHITE);
        
        String movesText = "Movimientos: " + moveCount;
        layout.setText(hudFont, movesText);
        hudFont.draw(game.getBatch(), movesText, 
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY);
        
        String rewardText = "Recompensa: " + levelReward;
        layout.setText(hudFont, rewardText);
        hudFont.draw(game.getBatch(), rewardText, 
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY - lineHeight);
        
        String bonusText = "Bonus Deck: " + deckBonus;
        layout.setText(hudFont, bonusText);
        hudFont.draw(game.getBatch(), bonusText, 
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY - lineHeight * 2);
        
        hudFont.setColor(Color.GOLD);
        String totalText = "TOTAL: " + totalNekoins + " Nekoins";
        layout.setText(hudFont, totalText);
        hudFont.draw(game.getBatch(), totalText, 
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY - lineHeight * 3);
        hudFont.setColor(Color.WHITE);
        
        if (isFirstClear) {
            hudFont.setColor(Color.LIME);
            String firstText = "¡FIRST CLEAR BONUS!";
            layout.setText(hudFont, firstText);
            hudFont.draw(game.getBatch(), firstText, 
                        (Constants.VIRTUAL_WIDTH - layout.width) / 2f, statsY - lineHeight * 4);
            hudFont.setColor(Color.WHITE);
        }
        
        if (nextLevelButton != null) nextLevelButton.draw(game.getBatch(), buttonFont);
        if (restartButton != null) restartButton.draw(game.getBatch(), buttonFont);
        if (exitButton != null) exitButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
    }
    
    private void drawDefeatPanel() {
        game.getBatch().begin();
        
        game.getBatch().setColor(0, 0, 0, 0.7f);
        game.getBatch().draw(cardBackTexture, 0, 0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        game.getBatch().setColor(1, 1, 1, 1);
        
        if (panelDefeatTexture != null) {
            float panelWidth = Constants.VIRTUAL_WIDTH * 0.85f;
            float panelHeight = panelWidth * 1.0f;
            float panelX = (Constants.VIRTUAL_WIDTH - panelWidth) / 2f;
            float panelY = (Constants.VIRTUAL_HEIGHT - panelHeight) / 2f;
            game.getBatch().draw(panelDefeatTexture, panelX, panelY, panelWidth, panelHeight);
        }
        
        String title = "TIEMPO AGOTADO";
        layout.setText(titleFont, title);
        titleFont.setColor(Color.RED);
        titleFont.draw(game.getBatch(), title, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.65f);
        titleFont.setColor(Color.WHITE);
        
        String timeTotal = "Tiempo: " + formatTimeComplete(elapsedTime);
        layout.setText(hudFont, timeTotal);
        hudFont.setColor(Color.LIGHT_GRAY);
        hudFont.draw(game.getBatch(), timeTotal,
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                    Constants.VIRTUAL_HEIGHT * 0.55f);
        
        String pairsText = "Pares encontrados: " + pairsFoundTotal + "/" + (pairsPerGrid * totalGrids);
        layout.setText(hudFont, pairsText);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(game.getBatch(), pairsText, 
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                    Constants.VIRTUAL_HEIGHT * 0.48f);
        
        if (restartButton != null) restartButton.draw(game.getBatch(), buttonFont);
        if (exitButton != null) exitButton.draw(game.getBatch(), buttonFont);
        
        game.getBatch().end();
    }
    
    // ==================== UTILIDADES ====================
    
    private String formatTime(float seconds) {
        int mins = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        if (mins > 0) {
            return String.format("%d:%02d", mins, secs);
        }
        return String.valueOf(secs);
    }
    
    private String formatTimeComplete(float seconds) {
        int mins = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }
    
    // ==================== DISPOSE ====================
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos...");
        
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (cardBackTexture != null) cardBackTexture.dispose();
        
        for (Texture tex : cardFrontTextures) {
            if (tex != null) tex.dispose();
        }
        cardFrontTextures.clear();
        
        if (pauseIconTexture != null) pauseIconTexture.dispose();
        if (hintIconTexture != null) hintIconTexture.dispose();
        if (timeFreezeIconTexture != null) timeFreezeIconTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        
        if (panelPauseTexture != null) panelPauseTexture.dispose();
        if (panelVictoryTexture != null) panelVictoryTexture.dispose();
        if (panelDefeatTexture != null) panelDefeatTexture.dispose();
        
        if (buttonTexture != null) buttonTexture.dispose();
    }
                                               }
