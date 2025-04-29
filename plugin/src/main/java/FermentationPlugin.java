package com.example.fermentationplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class FermentationPlugin extends JavaPlugin implements Listener {

    // Le titre de l'inventaire custom
    public static final String TITLE = "§6Tonneau de fermentation";

    @Override
    public void onEnable() {
        addInvisibleItemFrameRecipe();
        addFermentationBarrelRecipe();
        // Enregistrer les événements
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // Méthode pour créer l'inventaire de fermentation (tonneau custom)
    public static Inventory createFermentationGUI() {
        Inventory inv = Bukkit.createInventory(null, 9, TITLE);

        // Emplacements utiles
        inv.setItem(0, createPlaceholder("§bFiole d'eau ici"));
        inv.setItem(1, createPlaceholder("§fSucre ici"));
        inv.setItem(2, createPlaceholder("§dIngrédient #1"));
        inv.setItem(3, createPlaceholder("§dIngrédient #2"));

        // Bouton "Fermenter"
        ItemStack fermentButton = new ItemStack(Material.BREWING_STAND);
        ItemMeta meta = fermentButton.getItemMeta();
        meta.setDisplayName("§aLancer la fermentation");
        fermentButton.setItemMeta(meta);
        inv.setItem(8, fermentButton); // Slot d'action

        return inv;
    }

    // Créer un "place holder" pour l'inventaire
    private static ItemStack createPlaceholder(String name) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    // Gérer le clic sur un bloc "baril" custom pour ouvrir l'inventaire
    @EventHandler
    public void onBarrelClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BARREL) {
            // Vérifie que le bloc est un baril avec un nom custom
            if (event.getClickedBlock().getCustomName() != null && event.getClickedBlock().getCustomName().equals("Fermenteur")) {
                Player player = event.getPlayer();
                // Ouvre l'inventaire de fermentation
                player.openInventory(createFermentationGUI());
                event.setCancelled(true);
            }
        }
    }

    // Gérer l'interaction dans le GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Vérifie si l'utilisateur a cliqué sur le bouton "Fermenter"
        if (slot == 8) { // Slot 8 = "Fermenter" button
            Inventory inv = event.getInventory();
            ItemStack water = inv.getItem(0); // Fiole d'eau
            ItemStack sugar = inv.getItem(1); // Sucre
            ItemStack ingr1 = inv.getItem(2); // Ingrédient 1
            ItemStack ingr2 = inv.getItem(3); // Ingrédient 2

            // Si les ingrédients sont valides, créer l'alcool
            if (isValid(water, sugar, ingr1, ingr2)) {
                ItemStack result = createCustomAlcohol(ingr1, ingr2);
                player.getInventory().addItem(result); // Donne l'alcool à l'utilisateur
                player.sendMessage("§aVous avez créé un alcool !");
                inv.clear(); // Vide l'inventaire
                player.closeInventory(); // Ferme l'inventaire
            } else {
                player.sendMessage("§cIngrédients invalides.");
            }

            event.setCancelled(true);
        }
    }

    // Vérifie que les ingrédients nécessaires sont présents
    private boolean isValid(ItemStack water, ItemStack sugar, ItemStack ingr1, ItemStack ingr2) {
        return water != null && water.getType() == Material.POTION && 
               sugar != null && sugar.getType() == Material.SUGAR && 
               ingr1 != null && ingr2 != null &&
               ingr1.getType() != Material.AIR && ingr2.getType() != Material.AIR;
    }

    // Créer un alcool custom basé sur les ingrédients
    private ItemStack createCustomAlcohol(ItemStack ingr1, ItemStack ingr2) {
        ItemStack bottle = new ItemStack(Material.POTION);
        ItemMeta meta = (ItemMeta) bottle.getItemMeta();

        meta.setDisplayName("§dAlcool artisanal");
        meta.setLore(Arrays.asList("§7Ingrédients :", "§8- " + ingr1.getType(), "§8- " + ingr2.getType()));
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0), true);

        bottle.setItemMeta(meta);
        return bottle;
    }

    private void addInvisibleItemFrameRecipe() {
        ItemStack item = new ItemStack(Material.ITEM_FRAME);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Cadre Invisible");
            item.setItemMeta(meta);
        }

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "invisible_frame"), item);
        recipe.shape(" G ", "GIG", " G ");
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('I', Material.ITEM_FRAME);

        Bukkit.addRecipe(recipe);
    }
    private void addFermentationBarrelRecipe () {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Fermenteur");
            item.setItemMeta(meta);
        }

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "Fermenteur"), item);
        recipe.shape(" L ", "LBL", " L ");
        recipe.setIngredient('L', Material.LOG);
        recipe.setIngredient('B', Material.BARREL);

        Bukkit.addRecipe(recipe);
    }
}