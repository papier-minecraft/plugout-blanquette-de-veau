package fr.papierpain.plugoutblanquettedeveau.items;

import org.bukkit.*;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Random;

/**
 * Molotov cocktail item
 * This item is a throwable potion that creates a fire explosion on impact.
 */
public class CocktailMolotov implements Listener {
    /**
     * Item constants
     */
    public static final String ITEM_NAME = ChatColor.GRAY + "Molotov Cocktail";
    public static final Material ITEM_MATERIAL = Material.SPLASH_POTION;
    public static final String ITEM_KEY = "grenade_molotov_cocktail";

    /**
     * Item properties
     */
    private final ItemStack item;
    private final NamespacedKey key;
    private final Plugin plugin;

    /**
     * Constructor
     *
     * @param plugin Plugin
     */
    public CocktailMolotov(Plugin plugin) {
        this.item = new ItemStack(ITEM_MATERIAL, 1);

        // Potion color
        PotionMeta potionMeta = (PotionMeta) this.item.getItemMeta();
        assert potionMeta != null;
        potionMeta.setColor(Color.ORANGE);
        potionMeta.setDisplayName(ITEM_NAME);
        this.item.setItemMeta(potionMeta);

        this.plugin = plugin;
        this.key = new NamespacedKey(this.plugin, ITEM_KEY);

        this.generateRecipe();
    }

    /**
     * Generate the recipe for the molotov cocktail
     */
    private void generateRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(this.key, this.item);
        recipe.shape("P", "G", "B");

        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('G', Material.GUNPOWDER);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);

        Bukkit.addRecipe(recipe);
    }

    /**
     * Event handler for the molotov cocktail
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler
    public void onMolotov(PlayerInteractEvent event) {
        // Get player
        Player eventPlayer = event.getPlayer();

        // Get the ItemStack in hand
        ItemStack itemInMainHand = eventPlayer.getInventory().getItemInMainHand();
        ItemStack itemInMainHandToCompare = itemInMainHand.clone();
        itemInMainHandToCompare.setAmount(1);

        if (!itemInMainHandToCompare.equals(this.item)) {
            return;
        }

        // Get World of player
        World playerWorld = eventPlayer.getWorld();

        // Event when player throws the molotov (drops the item in front of him)
        final Item droppedItem = playerWorld.dropItem(eventPlayer.getEyeLocation(), this.item);
        droppedItem.setMetadata(ITEM_KEY, new FixedMetadataValue(this.plugin, 0));
        droppedItem.setVelocity(eventPlayer.getEyeLocation().getDirection());
        itemInMainHand.setAmount(itemInMainHand.getAmount() - 1); // Remove 1 item from the stack

        // Explosion of molotov
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            droppedItem.getLocation().getBlock().setType(Material.FIRE);

            for (int i = 0; i < (new Random()).nextInt(25 - 8) + 8; i++) {
                FallingBlock fireBlock = droppedItem.getWorld().spawnFallingBlock(droppedItem.getLocation(), Bukkit.createBlockData(Material.FIRE));

                // Expanding the fire (between -0.2 and 0.2 in each direction)
                float x = (new Random()).nextFloat() * 0.4f - 0.2f;
                float y = (new Random()).nextFloat() * 0.6f;
                float z = (new Random()).nextFloat() * 0.4f - 0.2f;

                fireBlock.setVelocity(new Vector(x, y, z));
                fireBlock.setDropItem(false);
            }

            Objects.requireNonNull(droppedItem.getLocation().getWorld()).playEffect(droppedItem.getLocation(), Effect.POTION_BREAK, 5);
            droppedItem.getLocation().getWorld().playEffect(droppedItem.getLocation(), Effect.MOBSPAWNER_FLAMES, 6);
            droppedItem.remove();
        }, 35);
    }

    /**
     * Prevents picking up the item. A thrown molotov shouldn't be able to be
     * picked up again.
     *
     * @param event EntityPickupItemEvent
     */
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getItem().hasMetadata(ITEM_KEY)) {
            event.setCancelled(true);
        }
    }
}
