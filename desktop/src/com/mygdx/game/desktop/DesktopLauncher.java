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
		config.addIcon("icon/WindowsAppIcon.png", Files.FileType.Internal);
		config.addIcon("icon/LinuxAppIcon.png", Files.FileType.Internal);
		config.addIcon("icon/MacAppIcon.png", Files.FileType.Internal);
		System.out.println("AUDIO: " + config.audioDeviceBufferCount);
		config.audioDeviceBufferCount = 4096*10;
		System.out.println("AUDIO: " + config.audioDeviceBufferCount);

		new LwjglApplication(new GridWars(), config);
	}
}
