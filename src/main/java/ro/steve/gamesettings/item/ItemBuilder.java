package ro.steve.gamesettings.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ro.steve.gamesettings.util.Color;

import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    private boolean glowing;

    public ItemBuilder() {
        item = new ItemStack(Material.FEATHER);
        meta = item.getItemMeta();
        glowing = false;
    }

    public ItemBuilder type(Material material) {
        item.setType(material);
        return this;
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(Color.process(name));
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder glowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder withPersistent(String namespacedKey, PersistentDataType type, Object value) {
        meta.getPersistentDataContainer().set(NamespacedKey.minecraft(namespacedKey), type, value);
        return this;
    }

    public ItemStack build() {
        if (glowing) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }
}
