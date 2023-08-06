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

class BaloShulker : JavaPlugin(), Listener {

    private val openedShulkers = HashMap<Player, ItemStack>()

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
        return item.type == Material.SHULKER_BOX || item.type.name.endsWith("_SHULKER_BOX")
    }

    private fun openShulkerBox(player: Player, shulkerBoxItem: ItemStack) {
        val shulkerBoxMeta = shulkerBoxItem.itemMeta as? BlockStateMeta

        if (shulkerBoxMeta != null && shulkerBoxMeta.blockState is ShulkerBox) {
            val shulkerBox = shulkerBoxMeta.blockState as ShulkerBox
            player.openInventory(shulkerBox.inventory)
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
                saveShulkerContents(shulkerBoxItem, event.inventory.contents.filterNotNull().toTypedArray())
            }

            openedShulkers.remove(player)
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

    private fun saveShulkerContents(shulkerItem: ItemStack, contents: Array<ItemStack>) {
        val shulkerBoxMeta = shulkerItem.itemMeta as? BlockStateMeta

        if (shulkerBoxMeta != null && shulkerBoxMeta.blockState is ShulkerBox) {
            val shulkerBox = shulkerBoxMeta.blockState as ShulkerBox
            shulkerBox.inventory.contents = contents
            shulkerBox.update()
            shulkerBoxMeta.blockState = shulkerBox
            shulkerItem.itemMeta = shulkerBoxMeta
        }
    }
}