package com.mygdx.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.GridWars;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import javax.swing.ImageIcon;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1000;
		config.height = 900;
		config.addIcon("icon/WindowsAppIcon.png", Files.FileType.Internal);
		config.addIcon("icon/LinuxAppIcon.png", Files.FileType.Internal);
		config.addIcon("icon/MacAppIcon.png", Files.FileType.Internal);

		new LwjglApplication(new GridWars(), config);
	}
}
