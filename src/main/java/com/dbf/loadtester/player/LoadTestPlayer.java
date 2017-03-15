package com.dbf.loadtester.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbf.loadtester.player.config.PlayerOptions;

public class LoadTestPlayer
{
	private static final Logger log = LoggerFactory.getLogger(LoadTestPlayer.class);
	
	public static void main(String[] args)
	{
		try
		{
			PlayerOptions config = new PlayerOptions(args);
			new Thread(new LoadTestCoordinator(config),"Master Coordinator Thread").start();
		}
		catch(IllegalArgumentException e)
		{
			log.error("Invalid CMD line Arguments: " + e.getMessage());
			PlayerOptions.printOptions();
			return;
		}
		catch(Exception e)
		{
			log.error("Failed to load player configuration.", e);
			return;
		}
	}

}
