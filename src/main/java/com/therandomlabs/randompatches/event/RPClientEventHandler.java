package com.therandomlabs.randompatches.event;

import com.therandomlabs.randompatches.RPConfig;
import com.therandomlabs.randompatches.RandomPatches;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RPClientEventHandler {
	private static boolean clientInitialized;

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.getModID().equals(RandomPatches.MODID)) {
			RPConfig.reload();
		}
	}

	@SubscribeEvent
	public void guiOpenEvent(GuiOpenEvent event) {
		if(!clientInitialized) {
			clientInitialized = true;
			initialize();
		}
	}

	public static void initialize() {
		RPConfig.reload();
	}
}
