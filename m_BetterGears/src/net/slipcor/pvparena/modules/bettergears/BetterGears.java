package net.slipcor.pvparena.modules.bettergears;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;

public class BetterGears extends ArenaModule {
	static HashMap<String, String> colorMap;
	Debug db = new Debug(600);

	HashMap<ArenaTeam, Short[]> colors = new HashMap<ArenaTeam, Short[]>();
	HashMap<ArenaClass, Short> levels = new HashMap<ArenaClass, Short>();

	public BetterGears() {
		super("BetterGears");
		db = new Debug(401);
	}

	@Override
	public String version() {
		return "v0.10.1.7";
	}

	@Override
	public boolean checkCommand(String s) {
		return s.equals("!bg") || s.startsWith("bettergear");
	}

	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		// !bg [teamname] | show
		// !bg [teamname] color <R> <G> <B> | set color
		// !bg [classname] | show
		// !bg [classname] level <level> | protection level
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(
					sender,
					Language.parse(MSG.ERROR_NOPERM,
							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}

		if (!PAA__Command.argCountValid(sender, arena, args, new Integer[] { 2,
				4, 6 })) {
			return;
		}

		ArenaClass c = arena.getClass(args[1]);

		if (c == null) {
			ArenaTeam team = arena.getTeam(args[1]);
			if (team != null) {
				// !bg [teamname] | show
				// !bg [teamname] color <R> <G> <B> | set color

				if (args.length == 2) {
					arena.msg(sender, Language.parse(
							MSG.MODULE_BETTERGEARS_SHOWTEAM,
							team.getColoredName(),
							String.valueOf(levels.get(team))));
					return;
				}

				if ((args.length != 6) || !args[2].equalsIgnoreCase("color")) {
					printHelp(sender);
					return;
				}

				try {
					Short[] rgb = new Short[3];
					rgb[0] = Short.parseShort(args[3]);
					rgb[1] = Short.parseShort(args[4]);
					rgb[2] = Short.parseShort(args[5]);
					arena.getArenaConfig().setManually(
							"modules.bettergears.colors." + team.getName(),
							StringParser.joinArray(rgb, ","));
					arena.getArenaConfig().save();
					colors.put(team, rgb);
					arena.msg(sender, Language.parse(
							MSG.MODULE_BETTERGEARS_TEAMDONE,
							team.getColoredName(), args[3]));
				} catch (Exception e) {
					arena.msg(sender,
							Language.parse(MSG.ERROR_NOT_NUMERIC, args[3]));
				}

				return;
			}
			// no team AND no class!

			arena.msg(sender,
					Language.parse(MSG.ERROR_CLASS_NOT_FOUND, args[1]));
			arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, args[1]));
			printHelp(sender);
			return;
		}
		// !bg [classname] | show
		// !bg [classname] level <level> | protection level

		if (args.length == 2) {
			arena.msg(
					sender,
					Language.parse(MSG.MODULE_BETTERGEARS_SHOWCLASS,
							c.getName(), String.valueOf(levels.get(c))));
			return;
		}

		if ((args.length != 4) || !args[2].equalsIgnoreCase("level")) {
			printHelp(sender);
			return;
		}

		try {
			short l = Short.parseShort(args[3]);
			arena.getArenaConfig().setManually(
					"modules.bettergears.levels." + c.getName(), l);
			arena.getArenaConfig().save();
			levels.put(c, l);
			arena.msg(
					sender,
					Language.parse(MSG.MODULE_BETTERGEARS_CLASSDONE,
							c.getName(), args[3]));
		} catch (Exception e) {
			arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[3]));
		}
	}

	@Override
	public void configParse(YamlConfiguration cfg) {
		for (ArenaClass c : arena.getClasses()) {
			if (cfg.get("modules.bettergears.levels." + c.getName()) == null)
				cfg.set("modules.bettergears.levels." + c.getName(),
						parseClassNameToDefaultProtection(c.getName()));
		}
		for (ArenaTeam t : arena.getTeams()) {
			if (cfg.get("modules.bettergears.colors." + t.getName()) == null)
				cfg.set("modules.bettergears.colors." + t.getName(),
						parseTeamColorStringToRGB(t.getColor().name()));
		}
		if (colors.isEmpty()) {
			setup();
		}
	}

	void equip(ArenaPlayer ap) {

		short r = 0;
		short g = 0;
		short b = 0;
		
		if (getArena().isFreeForAll()) {
			r = (short) ((new Random()).nextInt(256));
			g = (short) ((new Random()).nextInt(256));
			b = (short) ((new Random()).nextInt(256));
		} else {
			r = colors.get(ap.getArenaTeam())[0];
			g = colors.get(ap.getArenaTeam())[1];
			b = colors.get(ap.getArenaTeam())[2];
		}
		

		ItemStack[] isArmor = new ItemStack[4];
		isArmor[0] = new ItemStack(Material.LEATHER_HELMET, 1);
		isArmor[1] = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		isArmor[2] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		isArmor[3] = new ItemStack(Material.LEATHER_BOOTS, 1);

		Color c = Color.fromBGR(b, g, r);
		for (int i = 0; i < 4; i++) {
			LeatherArmorMeta lam = (LeatherArmorMeta) isArmor[i].getItemMeta();
			
			lam.setColor(c);
			isArmor[i].setItemMeta(lam);
		}

		Short s = levels.get(ap.getArenaClass());
		
		if (s == null) {
			String autoClass = getArena().getArenaConfig().getString(CFG.READY_AUTOCLASS);
			ArenaClass ac = getArena().getClass(autoClass);
			s = levels.get(ac);
		}
		

		isArmor[0].addUnsafeEnchantment(
				Enchantment.PROTECTION_ENVIRONMENTAL, s);
		isArmor[0]
				.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, s);
		isArmor[0].addUnsafeEnchantment(Enchantment.PROTECTION_FALL, s);
		isArmor[0].addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, s);
		isArmor[0]
				.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, s);

		ap.get().getInventory().setHelmet(isArmor[0]);
		ap.get().getInventory().setChestplate(isArmor[1]);
		ap.get().getInventory().setLeggings(isArmor[2]);
		ap.get().getInventory().setBoots(isArmor[3]);
	}

	@Override
	public void initiate(Player player) {
		if (colors.isEmpty()) {
			setup();
		}
	}

	@Override
	public void reset(boolean force) {
		colors.remove(arena);
		levels.remove(arena);
	}

	@Override
	public void parseStart() {

		if (colors.isEmpty()) {
			setup();
		}
		// debug();

		for (ArenaPlayer ap : arena.getFighters()) {
			equip(ap);
		}
	}

	private void setup() {
		db.i("Setting up BetterGears");
		colors = new HashMap<ArenaTeam, Short[]>();
		levels = new HashMap<ArenaClass, Short>();

		for (ArenaClass c : arena.getClasses()) {
			Short s = 0;
			try {
				s = Short
						.valueOf(String.valueOf(arena.getArenaConfig()
								.getUnsafe(
										"modules.bettergears.levels."
												+ c.getName())));
				db.i(c.getName() + " : " + s);
			} catch (Exception e) {
			}
			levels.put(c, s);
		}

		for (ArenaTeam t : arena.getTeams()) {
			Short[] s = parseRGBToShortArray(arena.getArenaConfig().getUnsafe(
					"modules.bettergears.colors." + t.getName()));
			colors.put(t, s);
			db.i(t.getName() + " : " + StringParser.joinArray(s, ","));
		}
	}

	private Short parseClassNameToDefaultProtection(String name) {
		if (name.equals("Tank")) {
			return 10;
		} else if (name.equals("Swordsman")) {
			return 4;
		} else if (name.equals("Ranger")) {
			return 1;
		} else if (name.equals("Pyro")) {
			return 1;
		}
		return null;
	}

	public void parseRespawn(Player player, ArenaTeam team, DamageCause cause,
			Entity damager) {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
			new EquipRunnable(ap, this);
		}
	}

	private Short[] parseRGBToShortArray(Object o) {
		Short[] result = new Short[3];
		result[0] = 255;
		result[1] = 255;
		result[2] = 255;

		db.i("parsing RGB:");
		db.i(String.valueOf(o));

		if (!(o instanceof String)) {
			return result;
		}

		String s = (String) o;

		if (s == null || s.equals("") || !s.contains(",")
				|| s.split(",").length < 3) {
			return result;
		}

		try {
			String[] split = s.split(",");
			result[0] = Short.parseShort(split[0]);
			result[1] = Short.parseShort(split[1]);
			result[2] = Short.parseShort(split[2]);
		} catch (Exception e) {
		}
		return result;
	}

	private String parseTeamColorStringToRGB(String name) {
		if (colorMap == null) {
			colorMap = new HashMap<String, String>();

			colorMap.put("BLACK", "0,0,0");
			colorMap.put("DARK_BLUE", "0,0,153");
			colorMap.put("DARK_GREEN", "0,68,0");
			colorMap.put("DARK_AQUA", "0,153,153");
			colorMap.put("DARK_RED", "153,0,0");

			colorMap.put("DARK_PURPLE", "153,0,153");
			colorMap.put("GOLD", "0,0,0");
			colorMap.put("GRAY", "153,153,153");
			colorMap.put("DARK_GRAY", "68,68,68");
			colorMap.put("BLUE", "0,0,255");
			colorMap.put("GREEN", "0,255,0");

			colorMap.put("AQUA", "0,255,255");
			colorMap.put("RED", "255,0,0");
			colorMap.put("LIGHT_PURPLE", "255,0,255");
			colorMap.put("PINK", "255,0,255");
			colorMap.put("YELLOW", "255,255,0");
			colorMap.put("WHITE", "255,255,255");
		}

		String s = colorMap.get(name);
		db.i("team " + name + " : " + s);
		return s == null ? "255,255,255" : s;
	}

	private void printHelp(CommandSender sender) {
		arena.msg(sender, "/pa [arenaname] !bg [teamname]  | show team color");
		arena.msg(sender,
				"/pa [arenaname] !bg [teamname] color �c<R> �a<G> �9<B>�r | set color");
		arena.msg(sender,
				"/pa [arenaname] !bg [classname] | show protection level");
		arena.msg(sender,
				"/pa [arenaname] !bg [classname] level <level> | set protection level");
	}
}
