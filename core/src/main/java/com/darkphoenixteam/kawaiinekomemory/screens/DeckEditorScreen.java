package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.config.AssetPaths;
import com.darkphoenixteam.kawaiinekomemory.config.Constants;
import com.darkphoenixteam.kawaiinekomemory.systems.AudioManager;
import com.darkphoenixteam.kawaiinekomemory.systems.SaveManager;
import com.darkphoenixteam.kawaiinekomemory.ui.SimpleButton;

/**
 * Pantalla de edición/selección de mazo
 * Permite ver y cambiar el deck activo
 * 
 * @author DarkphoenixTeam
 */
public class DeckEditorScreen extends BaseScreen {
    
    private static final String TAG = "DeckEditorScreen";
    
    // ==================== LAYOUT CONSTANTS ====================
    
    private static final float HEADER_HEIGHT = 100f;
    private static final float FOOTER_HEIGHT = 100f;
    private static final float SECTION_SPACING = 20f;
    
    // Grid de cartas activas (3x5)
    private static final int ACTIVE_GRID_COLS = 5;
    private static final int ACTIVE_GRID_ROWS = 3;
    private static final float ACTIVE_CARD_SIZE = 60f;
    private static final float ACTIVE_CARD_SPACING = 8f;
    
    // Selector de decks
    private static final float DECK_SELECTOR_HEIGHT = 80f;
    private static final float DECK_BUTTON_SIZE = 60f;
    
    // Preview de cartas del deck
    private static final int PREVIEW_CARDS = 7;
    private static final float PREVIEW_CARD_WIDTH = 55f;
    private static final float PREVIEW_CARD_HEIGHT = 77f;
    private static final float PREVIEW_SPACING = 6f;
    
    // Colores de dificultad para indicadores
    private static final Color COLOR_EASY = new Color(0.4f, 0.9f, 0.4f, 1f);      // Verde
    private static final Color COLOR_NORMAL = new Color(1f, 0.85f, 0.4f, 1f);     // Amarillo
    private static final Color COLOR_ADVANCED = new Color(1f, 0.6f, 0.2f, 1f);    // Naranja
    private static final Color COLOR_HARD = new Color(0.9f, 0.3f, 0.3f, 1f);      // Rojo
    private static final Color COLOR_LOCKED = new Color(0.3f, 0.3f, 0.3f, 0.8f);  // Gris
    
    // ==================== FONTS ====================
    
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // ==================== TEXTURAS ====================
    
    private Texture patternTexture;
    private Texture nekoinIconTexture;
    private Texture cardBackTexture;
    private Texture lockIconTexture;
    
    // Texturas de cartas por deck
    private Array<Array<Texture>> deckTextures;  // [deck][card]
    
    // ==================== UI ====================
    
    private Array<SimpleButton> deckButtons;
    private SimpleButton backButton;
    private SimpleButton activateButton;
    
    // ==================== ESTADO ====================
    
    private int selectedDeck;       // Deck que estamos viendo
    private int activeDeck;         // Deck actualmente en uso
    
    // ==================== MANAGERS ====================
    
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    // ==================== RENDERING ====================
    
    private ShapeRenderer shapeRenderer;
    private final Vector2 touchPoint = new Vector2();
    
    // ==================== CONSTRUCTOR ====================
    
    public DeckEditorScreen(KawaiiNekoMemory game) {
        super(game);
        
        setBackgroundColor(1f, 0.92f, 0.95f);
        
        titleFont = game.getFontManager().getTitleFont();
        buttonFont = game.getFontManager().getButtonFont();
        smallFont = game.getFontManager().getSmallFont();
        layout = new GlyphLayout();
        
        audioManager = AudioManager.getInstance();
        saveManager = SaveManager.getInstance();
        
        shapeRenderer = new ShapeRenderer();
        
        // Estado inicial
        activeDeck = saveManager.getCurrentDeck();
        selectedDeck = activeDeck;
        
        // Inicializar colecciones
        deckTextures = new Array<>();
        deckButtons = new Array<>();
        
        loadAssets();
        createUI();
        
        Gdx.app.log(TAG, "Inicializado - Deck activo: " + activeDeck);
    }
    
    // ==================== CARGA DE ASSETS ====================
    
    private void loadAssets() {
        // Patrón de fondo
        try {
            patternTexture = new Texture(Gdx.files.internal(AssetPaths.PATTERN_HOME));
            patternTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando pattern");
        }
        
        // Iconos
        try {
            nekoinIconTexture = new Texture(Gdx.files.internal(AssetPaths.ICON_NEKOIN));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando icono nekoin");
        }
        
        // Card back
        try {
            cardBackTexture = new Texture(Gdx.files.internal(AssetPaths.CARD_BACK));
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando card back");
        }
        
        // Cargar texturas de todos los decks
        for (int deck = 0; deck < AssetPaths.TOTAL_DECKS; deck++) {
            Array<Texture> cards = new Array<>();
            
            for (int card = 0; card < AssetPaths.CARDS_PER_DECK; card++) {
                String path = AssetPaths.getCardPath(deck, card);
                try {
                    Texture tex = new Texture(Gdx.files.internal(path));
                    cards.add(tex);
                } catch (Exception e) {
                    Gdx.app.error(TAG, "Error cargando: " + path);
                    cards.add(null);
                }
            }
            
            deckTextures.add(cards);
        }
        
        Gdx.app.log(TAG, "Cargados " + deckTextures.size + " decks");
    }
    
    // ==================== CREACIÓN DE UI ====================
    
    private void createUI() {
        createDeckSelector();
        createButtons();
    }
    
    private void createDeckSelector() {
        float totalWidth = (DECK_BUTTON_SIZE * AssetPaths.TOTAL_DECKS) + 
                          (SECTION_SPACING * (AssetPaths.TOTAL_DECKS - 1));
        float startX = (Constants.VIRTUAL_WIDTH - totalWidth) / 2f;
        float selectorY = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT - 
                         (ACTIVE_GRID_ROWS * (ACTIVE_CARD_SIZE + ACTIVE_CARD_SPACING)) - 
                         SECTION_SPACING - DECK_SELECTOR_HEIGHT;
        
        // Nombres de los decks
        String[] deckNames = {"Base", "☆", "☆☆", "☆☆☆", "♡"};
        
        for (int i = 0; i < AssetPaths.TOTAL_DECKS; i++) {
            final int deckIndex = i;
            float x = startX + i * (DECK_BUTTON_SIZE + SECTION_SPACING);
            
            // Usar card back como textura base del botón
            SimpleButton btn = new SimpleButton(
                cardBackTexture, 
                deckNames[i],
                x, selectorY,
                DECK_BUTTON_SIZE, DECK_BUTTON_SIZE
            );
            
            btn.setOnClick(() -> {
                selectDeck(deckIndex);
            });
            
            deckButtons.add(btn);
        }
    }
    
    private void createButtons() {
        // Botón Volver
        try {
            Texture backTex = new Texture(Gdx.files.internal(AssetPaths.BTN_BACK));
            float btnWidth = Constants.VIRTUAL_WIDTH * 0.35f;
            float btnHeight = btnWidth * 0.4f;
            float btnX = 20f;
            float btnY = 20f;
            
            backButton = new SimpleButton(backTex, "VOLVER", btnX, btnY, btnWidth, btnHeight);
            backButton.setOnClick(() -> {
                audioManager.playSound(AssetPaths.SFX_BUTTON);
                game.setScreen(new HomeScreen(game));
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando botón back");
        }
        
        // Botón Activar
        try {
            Texture btnTex = new Texture(Gdx.files.internal(AssetPaths.BTN_PLAY));
            float btnWidth = Constants.VIRTUAL_WIDTH * 0.35f;
            float btnHeight = btnWidth * 0.4f;
            float btnX = Constants.VIRTUAL_WIDTH - btnWidth - 20f;
            float btnY = 20f;
            
            activateButton = new SimpleButton(btnTex, "ACTIVAR", btnX, btnY, btnWidth, btnHeight);
            activateButton.setOnClick(() -> {
                activateSelectedDeck();
            });
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error cargando botón activar");
        }
    }
    
    // ==================== LÓGICA ====================
    
    private void selectDeck(int deckIndex) {
        if (deckIndex != selectedDeck) {
            selectedDeck = deckIndex;
            audioManager.playSound(AssetPaths.SFX_BUTTON);
            Gdx.app.log(TAG, "Deck seleccionado: " + deckIndex);
        }
    }
    
    private void activateSelectedDeck() {
        // Verificar si está desbloqueado
        if (!saveManager.isDeckUnlocked(selectedDeck)) {
            audioManager.playSound(AssetPaths.SFX_NO_MATCH);
            Gdx.app.log(TAG, "Deck " + selectedDeck + " está bloqueado");
            return;
        }
        
        if (selectedDeck != activeDeck) {
            activeDeck = selectedDeck;
            saveManager.setCurrentDeck(activeDeck);
            audioManager.playSound(AssetPaths.SFX_MATCH);
            Gdx.app.log(TAG, "Deck activado: " + activeDeck);
        } else {
            audioManager.playSound(AssetPaths.SFX_BUTTON);
        }
    }
    
    // ==================== UPDATE ====================
    
    @Override
    protected void update(float delta) {
        if (!isInputEnabled()) return;
        
        // Actualizar botones de deck
        for (SimpleButton btn : deckButtons) {
            btn.update(viewport);
        }
        
        // Actualizar botones de acción
        if (backButton != null) backButton.update(viewport);
        if (activateButton != null) activateButton.update(viewport);
    }
    
    // ==================== DRAW ====================
    
    @Override
    protected void draw() {
        game.getBatch().begin();
        
        // Patrón de fondo
        drawBackground();
        
        // Header
        drawHeader();
        
        game.getBatch().end();
        
        // Grid de cartas activas (con indicadores de color)
        drawActiveCardsGrid();
        
        // Selector de decks
        drawDeckSelector();
        
        // Preview del deck seleccionado
        drawDeckPreview();
        
        // Info del deck
        game.getBatch().begin();
        drawDeckInfo();
        game.getBatch().end();
        
        // Botones
        game.getBatch().begin();
        if (backButton != null) backButton.draw(game.getBatch(), buttonFont);
        if (activateButton != null) {
            // Cambiar texto según estado
            if (selectedDeck == activeDeck) {
                activateButton.draw(game.getBatch(), buttonFont);
            } else if (saveManager.isDeckUnlocked(selectedDeck)) {
                activateButton.draw(game.getBatch(), buttonFont);
            } else {
                // Mostrar como bloqueado
                game.getBatch().setColor(0.5f, 0.5f, 0.5f, 1f);
                activateButton.draw(game.getBatch(), buttonFont);
                game.getBatch().setColor(1f, 1f, 1f, 1f);
            }
        }
        game.getBatch().end();
    }
    
    private void drawBackground() {
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
    }
    
    private void drawHeader() {
        // Título
        String title = "MAZO";
        layout.setText(titleFont, title);
        float titleX = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = Constants.VIRTUAL_HEIGHT - 30f;
        titleFont.draw(game.getBatch(), title, titleX, titleY);
        
        // Nekoins
        if (nekoinIconTexture != null) {
            float iconSize = 30f;
            float iconX = Constants.VIRTUAL_WIDTH - 120f;
            float iconY = Constants.VIRTUAL_HEIGHT - 50f;
            
            game.getBatch().draw(nekoinIconTexture, iconX, iconY, iconSize, iconSize);
            
            String nekoins = String.valueOf(saveManager.getNekoins());
            layout.setText(buttonFont, nekoins);
            buttonFont.draw(game.getBatch(), nekoins, iconX + iconSize + 8f, iconY + iconSize - 5f);
        }
    }
    
    private void drawActiveCardsGrid() {
        // Calcular posición del grid
        float gridWidth = ACTIVE_GRID_COLS * (ACTIVE_CARD_SIZE + ACTIVE_CARD_SPACING);
        float gridX = (Constants.VIRTUAL_WIDTH - gridWidth) / 2f;
        float gridY = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT - 20f;
        
        // Dibujar título de sección
        game.getBatch().begin();
        String sectionTitle = "Cartas Activas";
        layout.setText(smallFont, sectionTitle);
        smallFont.draw(game.getBatch(), sectionTitle, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      gridY + 15f);
        game.getBatch().end();
        
        // Dibujar indicadores de color y cartas
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        int cardIndex = 0;
        for (int row = 0; row < ACTIVE_GRID_ROWS; row++) {
            for (int col = 0; col < ACTIVE_GRID_COLS; col++) {
                float x = gridX + col * (ACTIVE_CARD_SIZE + ACTIVE_CARD_SPACING);
                float y = gridY - (row + 1) * (ACTIVE_CARD_SIZE + ACTIVE_CARD_SPACING);
                
                // Determinar color según posición
                Color indicatorColor = getCardDifficultyColor(cardIndex);
                
                // Dibujar indicador de fondo
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(indicatorColor);
                shapeRenderer.rect(x - 3f, y - 3f, 
                                  ACTIVE_CARD_SIZE + 6f, ACTIVE_CARD_SIZE + 6f);
                shapeRenderer.end();
                
                // Dibujar carta
                game.getBatch().begin();
                
                // Obtener textura del deck seleccionado (con wrapping)
                int texIndex = cardIndex % AssetPaths.CARDS_PER_DECK;
                Texture cardTex = null;
                if (selectedDeck < deckTextures.size && 
                    texIndex < deckTextures.get(selectedDeck).size) {
                    cardTex = deckTextures.get(selectedDeck).get(texIndex);
                }
                
                if (cardTex != null) {
                    game.getBatch().draw(cardTex, x, y, ACTIVE_CARD_SIZE, ACTIVE_CARD_SIZE);
                } else if (cardBackTexture != null) {
                    game.getBatch().draw(cardBackTexture, x, y, ACTIVE_CARD_SIZE, ACTIVE_CARD_SIZE);
                }
                
                game.getBatch().end();
                
                cardIndex++;
            }
        }
    }
    
    private Color getCardDifficultyColor(int index) {
        // Distribución: 6 Verde (EASY), 2 Amarillo (NORMAL), 2 Naranja (ADVANCED), 5 Rojo (HARD)
        if (index < 6) {
            return COLOR_EASY;
        } else if (index < 8) {
            return COLOR_NORMAL;
        } else if (index < 10) {
            return COLOR_ADVANCED;
        } else {
            return COLOR_HARD;
        }
    }
    
    private void drawDeckSelector() {
        game.getBatch().begin();
        
        // Título de sección
        float selectorY = Constants.VIRTUAL_HEIGHT - HEADER_HEIGHT - 
                         (ACTIVE_GRID_ROWS * (ACTIVE_CARD_SIZE + ACTIVE_CARD_SPACING)) - 
                         SECTION_SPACING - 10f;
        
        String sectionTitle = "Seleccionar Mazo";
        layout.setText(smallFont, sectionTitle);
        smallFont.draw(game.getBatch(), sectionTitle, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      selectorY + 20f);
        
        // Dibujar botones de deck
        for (int i = 0; i < deckButtons.size; i++) {
            SimpleButton btn = deckButtons.get(i);
            
            // Resaltar según estado
            if (i == activeDeck && i == selectedDeck) {
                // Activo y seleccionado - dorado
                game.getBatch().setColor(1f, 0.85f, 0.3f, 1f);
            } else if (i == activeDeck) {
                // Solo activo - verde
                game.getBatch().setColor(0.5f, 1f, 0.5f, 1f);
            } else if (i == selectedDeck) {
                // Solo seleccionado - blanco
                game.getBatch().setColor(1f, 1f, 1f, 1f);
            } else if (!saveManager.isDeckUnlocked(i)) {
                // Bloqueado - gris oscuro
                game.getBatch().setColor(0.4f, 0.4f, 0.4f, 0.8f);
            } else {
                // Disponible pero no seleccionado - gris claro
                game.getBatch().setColor(0.7f, 0.7f, 0.7f, 1f);
            }
            
            btn.draw(game.getBatch(), smallFont);
            game.getBatch().setColor(1f, 1f, 1f, 1f);
        }
        
        game.getBatch().end();
    }
    
    private void drawDeckPreview() {
        // Calcular posición del preview
        float previewWidth = PREVIEW_CARDS * (PREVIEW_CARD_WIDTH + PREVIEW_SPACING);
        float previewX = (Constants.VIRTUAL_WIDTH - previewWidth) / 2f;
        float previewY = FOOTER_HEIGHT + 80f;
        
        // Título
        game.getBatch().begin();
        String[] deckNames = {"Mazo Base", "Mazo ☆", "Mazo ☆☆", "Mazo ☆☆☆", "Mazo ♡"};
        String previewTitle = deckNames[selectedDeck];
        layout.setText(buttonFont, previewTitle);
        buttonFont.draw(game.getBatch(), previewTitle, 
                       (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                       previewY + PREVIEW_CARD_HEIGHT + 30f);
        game.getBatch().end();
        
        // Verificar si está bloqueado
        boolean isLocked = !saveManager.isDeckUnlocked(selectedDeck);
        
        // Dibujar cartas del deck
        game.getBatch().begin();
        
        if (selectedDeck < deckTextures.size) {
            Array<Texture> cards = deckTextures.get(selectedDeck);
            
            for (int i = 0; i < cards.size && i < PREVIEW_CARDS; i++) {
                float x = previewX + i * (PREVIEW_CARD_WIDTH + PREVIEW_SPACING);
                
                Texture cardTex = cards.get(i);
                
                if (isLocked) {
                    // Mostrar bloqueado (oscurecido)
                    game.getBatch().setColor(0.3f, 0.3f, 0.3f, 1f);
                }
                
                if (cardTex != null) {
                    game.getBatch().draw(cardTex, x, previewY, 
                                        PREVIEW_CARD_WIDTH, PREVIEW_CARD_HEIGHT);
                } else if (cardBackTexture != null) {
                    game.getBatch().draw(cardBackTexture, x, previewY, 
                                        PREVIEW_CARD_WIDTH, PREVIEW_CARD_HEIGHT);
                }
                
                game.getBatch().setColor(1f, 1f, 1f, 1f);
            }
        }
        
        // Si está bloqueado, mostrar overlay
        if (isLocked) {
            String lockText = "BLOQUEADO";
            layout.setText(buttonFont, lockText);
            buttonFont.setColor(Color.RED);
            buttonFont.draw(game.getBatch(), lockText, 
                           (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                           previewY + PREVIEW_CARD_HEIGHT / 2f);
            buttonFont.setColor(Color.WHITE);
        }
        
        game.getBatch().end();
    }
    
    private void drawDeckInfo() {
        // Información del deck seleccionado
        float infoY = FOOTER_HEIGHT + 60f;
        
        // Valor por par
        int nekoinValue = Constants.NEKOIN_PER_DECK[selectedDeck];
        String valueText = "Valor por par: " + nekoinValue + " Nekoin" + (nekoinValue > 1 ? "s" : "");
        layout.setText(smallFont, valueText);
        smallFont.setColor(Color.DARK_GRAY);
        smallFont.draw(game.getBatch(), valueText, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      infoY);
        smallFont.setColor(Color.WHITE);
        
        // Estado
        String statusText;
        if (selectedDeck == activeDeck) {
            statusText = "★ ACTIVO ★";
            smallFont.setColor(Color.GREEN);
        } else if (saveManager.isDeckUnlocked(selectedDeck)) {
            statusText = "Disponible";
            smallFont.setColor(Color.WHITE);
        } else {
            statusText = "Desbloquear en Bazaar";
            smallFont.setColor(Color.GRAY);
        }
        
        layout.setText(smallFont, statusText);
        smallFont.draw(game.getBatch(), statusText, 
                      (Constants.VIRTUAL_WIDTH - layout.width) / 2f, 
                      infoY - 25f);
        smallFont.setColor(Color.WHITE);
    }
    
    // ==================== DISPOSE ====================
    
    @Override
    public void dispose() {
        Gdx.app.log(TAG, "Liberando recursos...");
        
        if (patternTexture != null) patternTexture.dispose();
        if (nekoinIconTexture != null) nekoinIconTexture.dispose();
        if (cardBackTexture != null) cardBackTexture.dispose();
        
        // Disponer texturas de cartas
        for (Array<Texture> deck : deckTextures) {
            for (Texture tex : deck) {
                if (tex != null) tex.dispose();
            }
        }
        deckTextures.clear();
        
        // Disponer botones
        for (SimpleButton btn : deckButtons) {
            // No disponer cardBackTexture aquí, ya lo hicimos arriba
        }
        
        if (backButton != null) backButton.dispose();
        if (activateButton != null) activateButton.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}