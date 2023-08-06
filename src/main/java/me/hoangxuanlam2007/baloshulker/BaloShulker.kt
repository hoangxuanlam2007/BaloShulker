package me.hoangxuanlam2007.baloshulker

import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.ChatColor

class BaloShulker : JavaPlugin(), Listener {

    private val openedShulkers = HashMap<Player, ItemStack>()

    // Map to store custom titles for each colored shulker box
    private val shulkerBoxTitles = mapOf(
        Material.WHITE_SHULKER_BOX to "&fBalo Shulker trắng",
        Material.ORANGE_SHULKER_BOX to "&6Balo Shulker cam",
        Material.MAGENTA_SHULKER_BOX to "&dBalo Shulker hồng sậm",
        Material.LIGHT_BLUE_SHULKER_BOX to "&bBalo Shulker xanh nước biển nhạt",
        Material.YELLOW_SHULKER_BOX to "&eBalo Shulker vàng",
        Material.LIME_SHULKER_BOX to "&aBalo Shulker xanh lá mạ",
        Material.PINK_SHULKER_BOX to "&dBalo Shulker hồng",
        Material.GRAY_SHULKER_BOX to "&8Balo Shulker xám",
        Material.LIGHT_GRAY_SHULKER_BOX to "&7Balo Shulker xám nhạt",
        Material.CYAN_SHULKER_BOX to "&3Balo Shulker lam",
        Material.PURPLE_SHULKER_BOX to "&5Balo Shulker tím",
        Material.BLUE_SHULKER_BOX to "&9Balo Shulker xanh nước biển",
        Material.BROWN_SHULKER_BOX to "&6Balo Shulker nâu",
        Material.GREEN_SHULKER_BOX to "&2Balo Shulker xanh lá",
        Material.RED_SHULKER_BOX to "&cBalo Shulker đỏ",
        Material.BLACK_SHULKER_BOX to "&0Balo Shulker đen",
        Material.SHULKER_BOX to "&dBalo Shulker" // Default title for the regular shulker box
    )

    override fun onEnable() {
        // Register the event listener
        server.pluginManager.registerEvents(this, this)

        // Log a message to the console when the plugin is enabled
        logger.info("BaloShulker plugin enabled!")
    }

    override fun onDisable() {
        // Log a message to the console when the plugin is disabled
        logger.info("BaloShulker plugin disabled!")
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action = event.action
        val hand = event.hand
        val item = if (hand == EquipmentSlot.HAND) player.inventory.itemInMainHand else player.inventory.itemInOffHand

        // Check if the player is holding a shulker box in the hand and right-clicked in the air
        if (isShulkerBox(item) && action == Action.RIGHT_CLICK_AIR) {
            event.isCancelled = true

            // Open the shulker box
            openShulkerBox(player, item)
        }
    }

    private fun isShulkerBox(item: ItemStack): Boolean {
        return item.type in shulkerBoxTitles.keys
    }

    private fun openShulkerBox(player: Player, shulkerBoxItem: ItemStack) {
        val shulkerBoxMeta = shulkerBoxItem.itemMeta as? BlockStateMeta

        if (shulkerBoxMeta != null && shulkerBoxMeta.blockState is ShulkerBox) {
            val shulkerBox = shulkerBoxMeta.blockState as ShulkerBox
            val shulkerBoxMaterial = shulkerBoxItem.type

            // Set the custom title for each colored shulker box
            val title = shulkerBoxTitles[shulkerBoxMaterial] ?: "Balo Shulker"

            shulkerBox.customName = ChatColor.translateAlternateColorCodes('&', title)
            player.openInventory(shulkerBox.inventory)

            // Store the shulker box item when it is opened
            openedShulkers[player] = shulkerBoxItem
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player

        // Check if the player has a shulker that was previously opened
        if (player != null && openedShulkers.containsKey(player)) {
            val shulkerBoxItem = openedShulkers[player]
            if (shulkerBoxItem != null) {
                // Store the current state of the shulker box inventory
                saveShulkerContents(shulkerBoxItem, event.inventory.contents)
            }

            openedShulkers.remove(player)
        }
    }

    private fun saveShulkerContents(shulkerItem: ItemStack, contents: Array<ItemStack?>) {
        val shulkerBoxMeta = shulkerItem.itemMeta as? BlockStateMeta

        if (shulkerBoxMeta != null && shulkerBoxMeta.blockState is ShulkerBox) {
            val shulkerBox = shulkerBoxMeta.blockState as ShulkerBox
            shulkerBox.inventory.contents = contents
            shulkerBox.update()
            shulkerBoxMeta.blockState = shulkerBox
            shulkerItem.itemMeta = shulkerBoxMeta
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name.equals("bs", ignoreCase = true) && args.isNotEmpty() && args[0].equals("open", ignoreCase = true)) {
            if (sender is Player) {
                // Check if the player has the permission to open shulker boxes
                if (sender.hasPermission("baloshulker.use")) {
                    val itemInMainHand = sender.inventory.itemInMainHand
                    val itemInOffHand = sender.inventory.itemInOffHand

                    // Check if the player is holding a shulker box in either hand
                    if (isShulkerBox(itemInMainHand)) {
                        openShulkerBox(sender, itemInMainHand)
                    } else if (isShulkerBox(itemInOffHand)) {
                        openShulkerBox(sender, itemInOffHand)
                    } else {
                        sender.sendMessage("You are not holding a shulker box!")
                    }
                } else {
                    sender.sendMessage("You don't have permission to use this command!")
                }
            } else {
                sender.sendMessage("Only players can use this command!")
            }
            return true
        }
        return false
    }
}