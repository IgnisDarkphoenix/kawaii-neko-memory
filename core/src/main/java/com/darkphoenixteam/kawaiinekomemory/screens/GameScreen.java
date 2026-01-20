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
import com.darkphoenixteam.kawaiinekomemory.models.LevelData;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla principal de juego
 * Maneja el tablero de cartas, timer, puntuación y paneles de resultado
 * 
 * @author DarkphoenixTeam
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
    private int moveCount;
    private int deckBonus;
    private boolean isTimeFrozen;
    private float timeFreezeRemaining;
    
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
        this.totalGrids = levelData.getGridCount();
        this.currentGrid = 0;
        this.pairsPerGrid = levelData.getDifficulty().getPairs();
        this.pairsFoundThisGrid = 0;
        this.pairsFoundTotal = 0;
        this.matchesSinceShuffle = 0;
        this.moveCount = 0;
        this.deckBonus = 0;
        this.isTimeFrozen = false;
        
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
        int currentDeck = saveManager.getCurrentDeck();
        int cardsNeeded = levelData.getUniqueCardsRequired();
        
        Gdx.app.log(TAG, "Cargando " + cardsNeeded + " cartas del deck " + currentDeck);
        
        for (int i = 0; i < cardsNeeded && i < AssetPaths.CARDS_PER_DECK; i++) {
            String path = AssetPaths.getCardPath(currentDeck, i);
            try {
                Texture tex = new Texture(Gdx.files.internal(path));
                cardFrontTextures.add(tex);
            } catch (Exception e) {
                Gdx.app.error(TAG, "Error cargando carta: " + path);
                cardFrontTextures.add(null);
            }
        }
        
        while (cardFrontTextures.size < cardsNeeded) {
            int index = cardFrontTextures.size % AssetPaths.CARDS_PER_DECK;
            if (index < cardFrontTextures.size) {
                cardFrontTextures.add(cardFrontTextures.get(index));
            } else {
                break;
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
        
        int cardIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (cardIndex >= cardIds.size) break;
                
                float x = startX + col * (cardWidth + actualMarginX);
                float y = startY + (rows - 1 - row) * (cardHeight + actualMarginY);
                
                int id = cardIds.get(cardIndex);
                Texture frontTex = (id < cardFrontTextures.size) ? 
                                   cardFrontTextures.get(id) : null;
                
                Card card = new Card(id, frontTex, cardBackTexture, x, y, cardWidth, cardHeight);
                
                int deckIndex = saveManager.getCurrentDeck();
                card.setDeckIndex(deckIndex);
                card.setNekoinValue(Constants.NEKOIN_PER_DECK[deckIndex]);
                
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
        checkDelayTimer -= delta;
        
        if (checkDelayTimer <= 0) {
            checkForMatch();
        }
    }
    
    private void updateNoMatchShake(float delta) {
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
            
            Gdx.app.log(TAG, "MATCH! Pares: " + pairsFoundThisGrid + "/" + pairsPerGrid + 
                             " | Bonus: +" + firstRevealed.getNekoinValue());
            
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
            
            noMatchCard1 = firstRevealed;
            noMatchCard2 = secondRevealed;
            
            noMatchCard1.startShake(NO_MATCH_SHAKE_DURATION);
            noMatchCard2.startShake(NO_MATCH_SHAKE_DURATION);
            
            noMatchShakeTimer = NO_MATCH_SHAKE_DURATION;
            gameState = GameState.NO_MATCH_SHAKE;
            
            Gdx.app.log(TAG, "No match - shake feedback");
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
    
    // ==================== RESULTADOS ====================
    
    private void onVictory() {
        gameState = GameState.VICTORY;
        audioManager.playSound(AssetPaths.SFX_VICTORY);
        
        starsEarned = levelData.calculateStars(timeRemaining);
        levelReward = levelData.calculateLevelReward(starsEarned, isFirstClear);
        totalNekoins = levelReward + deckBonus;
        
        if (isFirstClear) {
            saveManager.setLevelCompleted(levelData.getGlobalId());
        }
        saveManager.addNekoins(totalNekoins);
        
        Gdx.app.log(TAG, "=== VICTORIA ===");
        Gdx.app.log(TAG, "Estrellas: " + starsEarned);
        Gdx.app.log(TAG, "Tiempo restante: " + (int)timeRemaining + "s");
        Gdx.app.log(TAG, "Level Reward: " + levelReward);
        Gdx.app.log(TAG, "Deck Bonus: " + deckBonus);
        Gdx.app.log(TAG, "Total Nekoins: " + totalNekoins);
        Gdx.app.log(TAG, "First Clear: " + isFirstClear);
    }
    
    private void onDefeat() {
        gameState = GameState.DEFEAT;
        audioManager.playSound(AssetPaths.SFX_DEFEAT);
        
        Gdx.app.log(TAG, "=== DERROTA ===");
        Gdx.app.log(TAG, "Pares encontrados: " + pairsFoundTotal);
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
    
    private void useHint() {
        audioManager.playSound(AssetPaths.SFX_BUTTON);
        Gdx.app.log(TAG, "Hint usado (TODO: implementar lógica)");
    }
    
    private void useTimeFreeze() {
        audioManager.playSound(AssetPaths.SFX_TIMEFREEZE);
        Gdx.app.log(TAG, "TimeFreeze usado (TODO: implementar lógica)");
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
        
        if (pauseButton != null) pauseButton.drawNoText(game.getBatch());
        if (hintButton != null) hintButton.drawNoText(game.getBatch());
        if (timeFreezeButton != null) timeFreezeButton.drawNoText(game.getBatch());
        
        String timeText = formatTime(timeRemaining);
        if (isTimeFrozen) {
            hudFont.setColor(Color.CYAN);
            timeText = "❄ " + timeText;
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
                      Constants.VIRTUAL_HEIGHT * 0.75f);
        titleFont.setColor(Color.WHITE);
        
        String stars = "";
        for (int i = 0; i < starsEarned; i++) stars += "★ ";
        for (int i = starsEarned; i < 3; i++) stars += "☆ ";
        layout.setText(titleFont, stars);
        titleFont.setColor(Color.YELLOW);
        titleFont.draw(game.getBatch(), stars,
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                      Constants.VIRTUAL_HEIGHT * 0.65f);
        titleFont.setColor(Color.WHITE);
        
        float statsY = Constants.VIRTUAL_HEIGHT * 0.52f;
        float lineHeight = 35f;
        
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
        
        String pairsText = "Pares encontrados: " + pairsFoundTotal + "/" + (pairsPerGrid * totalGrids);
        layout.setText(hudFont, pairsText);
        hudFont.draw(game.getBatch(), pairsText, 
                    (Constants.VIRTUAL_WIDTH - layout.width) / 2f,
                    Constants.VIRTUAL_HEIGHT * 0.5f);
        
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
