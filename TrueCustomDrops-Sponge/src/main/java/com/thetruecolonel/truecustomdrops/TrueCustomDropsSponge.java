package com.thetruecolonel.truecustomdrops;

import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

@Plugin(id = "truecustomdrops", name = "True Custom Drops", version = "1.0.0")
public class TrueCustomDropsSponge {
	@Inject
	private Logger logger;

	@Listener
	public void onGameStartedServerEventEvent (GameStartedServerEvent e) {
		logger.info("Starting TrueCustomDrops...");
	}
}
