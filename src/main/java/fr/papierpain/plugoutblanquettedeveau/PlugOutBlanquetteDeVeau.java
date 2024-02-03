package fr.papierpain.plugoutblanquettedeveau;

import fr.papierpain.plugoutblanquettedeveau.items.CocktailMolotov;
import org.bukkit.plugin.java.JavaPlugin;

public class PlugOutBlanquetteDeVeau extends JavaPlugin {
    @Override
    public void onEnable() {
        // Events
        getServer().getPluginManager().registerEvents(new CocktailMolotov(this), this);

        getLogger().info("La blanquette de veau est dans la place MAMEN !");
    }

    @Override
    public void onDisable() {
        getLogger().info("Tchau les nazes !");
    }
}
