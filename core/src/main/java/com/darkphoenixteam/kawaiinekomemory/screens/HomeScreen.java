package com.darkphoenixteam.kawaiinekomemory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.darkphoenixteam.kawaiinekomemory.KawaiiNekoMemory;
import com.darkphoenixteam.kawaiinekomemory.utils.Constants;

public class HomeScreen extends BaseScreen {
    
    private BitmapFont font;
    
    public HomeScreen(KawaiiNekoMemory game) {
        super(game);
        
        // Fondo rosa
        setBackgroundColor(1f, 0.85f, 0.9f);
        
        setupUI();
    }
    
    private void setupUI() {
        font = new BitmapFont();
        
        // Título
        LabelStyle titleStyle = new LabelStyle();
        titleStyle.font = font;
        titleStyle.fontColor = new Color(0.4f, 0.2f, 0.3f, 1f);
        
        Label title = new Label(Constants.GAME_TITLE, titleStyle);
        title.setFontScale(3f);
        title.setPosition(
            (Constants.WORLD_WIDTH - title.getWidth() * 3f) / 2f,
            Constants.WORLD_HEIGHT - 200f
        );
        stage.addActor(title);
        
        // Botón Play
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;
        
        TextButton playButton = new TextButton("[ PLAY ]", buttonStyle);
        playButton.getLabel().setFontScale(2.5f);
        playButton.setPosition(
            (Constants.WORLD_WIDTH - 200f) / 2f,
            Constants.WORLD_HEIGHT / 2f
        );
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("HomeScreen", "Play clicked!");
                // TODO: Ir a LevelSelectScreen
            }
        });
        stage.addActor(playButton);
        
        // Versión
        Label versionLabel = new Label("v" + Constants.VERSION, titleStyle);
        versionLabel.setPosition(20f, 20f);
        stage.addActor(versionLabel);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (font != null) font.dispose();
    }
}