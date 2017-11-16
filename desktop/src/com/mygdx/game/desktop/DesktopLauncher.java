package com.mygdx.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.GridWars;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1000;
		config.height = 900;
		config.addIcon("WindowsAppIcon.png", Files.FileType.Internal);
		config.addIcon("LinuxAppIcon.png", Files.FileType.Internal);
		config.addIcon("MacAppIcon.png", Files.FileType.Internal);

		new LwjglApplication(new GridWars(), config);
	}
}
