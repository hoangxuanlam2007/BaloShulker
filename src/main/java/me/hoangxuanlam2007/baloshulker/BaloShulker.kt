package me.hoangxuanlam2007.baloshulker

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
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
import java.io.*
import java.nio.charset.StandardCharsets
import org.bukkit.configuration.file.FileConfiguration

class BaloShulker : JavaPlugin(), Listener {

    private lateinit var customMessages: FileConfiguration

    // Default Plugin prefix
    private fun format(message: String): String {
        return "§7[ §5Balo§dShulker §7] §f»§r $message"
    }

    override fun onEnable() {
        // Register the event listener
        server.pluginManager.registerEvents(this, this)

        // Get the plugin's description
        val pluginVersion = description.version
        val pluginName = description.name

        // Log a message to the console when the plugin is enabled
        Bukkit.getConsoleSender().sendMessage(format(ChatColor.GREEN.toString() + "Plugin enabled!"))
        Bukkit.getConsoleSender().sendMessage(format(ChatColor.GREEN.toString() + "You are using $pluginName v$pluginVersion!"))

        // Load or create messages.yml
        val dataFolder = dataFolder // Assuming dataFolder is the plugin's data folder
        if (!dataFolder.exists()) {
            dataFolder.mkdirs() // Create the necessary directories
        }

        val customMessagesFile = File(dataFolder, "messages.yml")
        if (!customMessagesFile.exists()) {
            try {
                customMessagesFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            // Add comment for easier configuration
            try {
                val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(customMessagesFile), StandardCharsets.UTF_8))
                writer.write("# ================================================================================================================================|");
                writer.newLine();
                writer.write("# > This Plugin is privately coded by ChimmFX, also known as Hoàng Xuân Lâm. <");
                writer.newLine();
                writer.write("# - This plugin outperforms ShulkerPack with an array of advanced configurations, along with the ability to set up custom messages.");
                writer.newLine();
                writer.write("# - Feel confident in utilizing this plugin!");
                writer.newLine();
                writer.write("#");
                writer.newLine();
                writer.write("# - There are two permissions that integrate seamlessly with any permissions plugin, such as LuckPerm:");
                writer.newLine();
                writer.write("#     + baloshulker.use");
                writer.newLine();
                writer.write("#     + baloshulker.reload");
                writer.newLine();
                writer.write("# - These permissions function as their names suggest.");
                writer.newLine();
                writer.write("#");
                writer.newLine();
                writer.write("# For further information, bug reports, or to get in touch, please email: chim31102007@gmail.com");
                writer.newLine();
                writer.write("# Author's GitHub: https://github.com/hoangxuanlam2007");
                writer.newLine();
                writer.write("# ================================================================================================================================|");
                writer.newLine()
                writer.newLine()
                writer.newLine()
                writer.newLine()
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            customMessages = YamlConfiguration.loadConfiguration(customMessagesFile)

            // Set default messages
            setDefaultMessage("command_usage", "§cUse §7<§f/bs help§7> §cfor help.")
            setDefaultMessage("command_unknown", "§cUnknown command. Use §7<§f/bs help§7> §cfor help.")
            setDefaultMessage("command_only_player", "§cOnly players can use this command!")
            setDefaultMessage("command_player_don't_have_permission", "§cYou don't have permission to use this command!")
            setDefaultMessage("player_don't_have_reloadperm", "§cYou don't have permission to reload the plugin!")
            setDefaultMessage("player_not_holding_shulker", "§cYou are not holding a shulker box!")
            setDefaultMessage("help_header", "§d§m ‡         §8(§5Balo§dShulker §eHelp§8)§d§m         ‡")
            setDefaultMessage("help_line_1", "§7   <§f/bs open§7> §7: Open Balo Shulker.")
            setDefaultMessage("help_line_2", "§7   <§f/bs help§7> §7: Show command help.")
            setDefaultMessage("help_footer", "§d§m ‡                                         ‡")
            try {
                customMessages.save(customMessagesFile)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            customMessages = YamlConfiguration.loadConfiguration(customMessagesFile)
        }
    }

    private fun setDefaultMessage(key: String, defaultValue: String) {
        if (!customMessages.contains(key)) {
            customMessages.set(key, ChatColor.translateAlternateColorCodes('&', defaultValue))
            try {
                customMessages.save(File(dataFolder, "messages.yml"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getMessage(key: String): String {
        val message = customMessages.getString(key, "") ?: ""
        return ChatColor.translateAlternateColorCodes('&', message)
    }

    override fun onDisable() {
        // Log a message to the console when the plugin is disabled
        Bukkit.getConsoleSender().sendMessage(format(ChatColor.RED.toString() + "Plugin disabled!"))
    }

    private val openedShulkers = HashMap<Player, ItemStack>()

    // Map to store custom titles for each colored shulker box
    private val shulkerBoxTitles = mapOf(
        Material.WHITE_SHULKER_BOX to "§fBalo Shulker trắng",
        Material.ORANGE_SHULKER_BOX to "§6Balo Shulker cam",
        Material.MAGENTA_SHULKER_BOX to "§dBalo Shulker hồng sậm",
        Material.LIGHT_BLUE_SHULKER_BOX to "§bBalo Shulker xanh lam nhạt",
        Material.YELLOW_SHULKER_BOX to "§eBalo Shulker vàng",
        Material.LIME_SHULKER_BOX to "§aBalo Shulker xanh lá mạ",
        Material.PINK_SHULKER_BOX to "§dBalo Shulker hồng",
        Material.GRAY_SHULKER_BOX to "§8Balo Shulker xám",
        Material.LIGHT_GRAY_SHULKER_BOX to "§7Balo Shulker xám nhạt",
        Material.CYAN_SHULKER_BOX to "§3Balo Shulker xanh lơ",
        Material.PURPLE_SHULKER_BOX to "§5Balo Shulker tím",
        Material.BLUE_SHULKER_BOX to "§9Balo Shulker xanh nước biển",
        Material.BROWN_SHULKER_BOX to "§6Balo Shulker nâu",
        Material.GREEN_SHULKER_BOX to "§2Balo Shulker xanh lá",
        Material.RED_SHULKER_BOX to "§cBalo Shulker đỏ",
        Material.BLACK_SHULKER_BOX to "§0Balo Shulker đen",
        Material.SHULKER_BOX to "§dBalo Shulker" // Default title for the regular shulker box
    )

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
            val title = shulkerBoxTitles[shulkerBoxMaterial] ?: "§fBalo Shulker"

            shulkerBox.customName = title
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
        if (command.name.equals("bs", ignoreCase = true)) {
            if (args.isNotEmpty()) {
                when (args[0].toLowerCase()) {
                    "open" -> {
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
                                    sender.sendMessage(format(getMessage("player_not_holding_shulker")))
                                }
                            } else {
                                sender.sendMessage(format(getMessage("command_player_don't_have_permission")))
                            }
                        } else {
                            sender.sendMessage(format(getMessage("command_only_player")))
                        }
                    }
                    "reload" -> {
                        if (sender.hasPermission("baloshulker.reload")) {
                            reloadPlugin()

                            // Get the plugin's description
                            val pluginVersion = description.version
                            val pluginName = description.name

                            sender.sendMessage(format("§aBaloShulker plugin reloaded successfully!"))
                            sender.sendMessage(format("§aYou are using $pluginName v$pluginVersion!"))
                        } else {
                            sender.sendMessage(format(getMessage("player_don't_have_reloadperm")))
                        }
                    }
                    "help" -> {
                        // Display help message for /bs help command
                        showHelp(sender)
                    }
                    else -> {
                        // Unknown command, show help or error message here
                        sender.sendMessage(format(getMessage("command_unknown")))
                    }
                }
            } else {
                // No arguments provided, show help or error message here
                sender.sendMessage(format(getMessage("command_usage")))
            }
            return true
        }
        return false
    }

    private fun showHelp(sender: CommandSender) {
        // Modify this function to show your help message
        sender.sendMessage(getMessage("help_header"))
        sender.sendMessage("")
        sender.sendMessage(getMessage("help_line_1"))
        sender.sendMessage(getMessage("help_line_2"))
        sender.sendMessage("")
        sender.sendMessage(getMessage("help_footer"))
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): List<String>? {
        if (cmd.name.equals("bs", ignoreCase = true)) {
            if (args.size == 1) {
                val suggestions = mutableListOf<String>()

                if (sender.hasPermission("baloshulker.use")) {
                    suggestions.add("help") // Add 'help' as a suggestion
                    suggestions.add("open") // Add 'open' as a suggestion
                }

                if (sender.hasPermission("baloshulker.reload")) {
                    suggestions.add("reload") // Add 'reload' as a suggestion for users with OP or permission
                }

                return suggestions
            }
        }
        return null
    }

    private fun reloadPlugin() {
        // Disable the plugin
        Bukkit.getPluginManager().disablePlugin(this)

        // Enable the plugin
        Bukkit.getPluginManager().enablePlugin(this)
    }
}