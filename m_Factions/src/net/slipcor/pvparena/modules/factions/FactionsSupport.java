package net.slipcor.pvparena.modules.factions;

import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.loadables.ArenaModule;

public class FactionsSupport extends ArenaModule {
	
	public FactionsSupport() {
		super("Factions");
	}
	
	@Override
	public String version() {
		return "v0.10.0.0";
	}
	
	@Override
	public void parseEnable() {
		Bukkit.getPluginManager().registerEvents(new FactionsListener(this), PVPArena.instance);
	}
}
