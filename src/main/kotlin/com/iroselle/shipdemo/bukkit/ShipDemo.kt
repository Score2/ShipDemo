package com.iroselle.shipdemo.bukkit

import me.scoretwo.utils.bukkit.command.patchs.bukkitCommandMap
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.Action.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*


class ShipDemo: JavaPlugin(), Listener {

    init {
        shipDemo = this
    }

    override fun onEnable() {
        bukkitCommandMap.register(description.name, object : Command(description.name, "", "", listOf("sd")) {

            override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
                if (!sender.hasPermission("shipdemo.admin")) {
                    sender.sendMessage("你没有权限.")
                    return true
                }
                if (args.isEmpty()) {
                    sender.sendMessage("")
                    sender.sendMessage("/sd spawn - 召唤或传送飞船到你的位置")
                    sender.sendMessage("/sd stop - 收回飞船")
                    sender.sendMessage("")
                    return true
                }
                when {
                    args[0] == "spawn" && sender is Player -> {
                        if (ships.containsKey(sender)) {
                            sender.sendMessage("你已经打开召唤一个飞船了, 输入/sd stop 收回飞船.")
                            return true
                        }

                        val underfootLocation = Location(sender.world, sender.location.blockX.toDouble(), (sender.location.blockY - 1).toDouble(), sender.location.blockZ.toDouble())
                        if (underfootLocation.block.type == Material.AIR) {
                            sender.sendMessage("脚下必须为一个有效方块")
                            return true
                        }

                        Thread {
                            sender.sendMessage("正在异步递归计算方块...")
                            val blocks = mutableListOf<Block>()

                            checkBlock(mutableMapOf(), underfootLocation.block).also { it[underfootLocation] = underfootLocation.block }.forEach {
                                blocks.add(it.value)
                            }

                            Bukkit.getScheduler().runTask(shipDemo, Runnable {
                                if (!sender.inventory.contains(rodItemStack)) sender.inventory.addItem(rodItemStack)
                                Ship(sender, blocks)
                            })

                            sender.sendMessage("成功召唤脚下的一个飞船")
                        }.start()

                    }
                    args[0] == "stop" && sender is Player -> {
                        if (!ships.containsKey(sender)) {
                            sender.sendMessage("你没有正在飞行的飞船")
                            return true
                        }
                        ships[sender]!!.stop()
                        ships.remove(sender)
                        sender.sendMessage("成功回收了飞船")
                    }
                    else -> {
                        sender.sendMessage("")
                        sender.sendMessage("/sd spawn - 召唤或传送飞船到你的位置")
                        sender.sendMessage("/sd stop - 收回飞船")
                        sender.sendMessage("")
                        return true
                    }
                }
                return true
            }

        })

        Bukkit.getPluginManager().registerEvents(this, this)
    }
/*
    fun checkBlock(block: Block): MutableMap<Location, Block> {
        val blocks = mutableMapOf<Location, Block>()

        for (blockFace in BlockFace.values()) {
            if (blockFace == BlockFace.SELF) continue
            if (blockFace == )
            checkBlock(block.getRelative(blockFace)).forEach {
                if (it.value.type != Material.AIR) {
                    blocks[it.key] = it.value
                }
            }
        }
        return blocks
    }*/

    fun checkBlock(map: MutableMap<Location, Block>, original: Block, prev: BlockFace? = null): MutableMap<Location, Block> {
        for (blockFace in BlockFace.values()) {
            if (blockFace == BlockFace.SELF) continue
            if (prev != null && blockFace == prev.oppositeFace) continue
            val block: Block = original.location.block.getRelative(blockFace)
            if (map.containsKey(block.location)) continue

            if (block.type != Material.AIR) {
                map[block.location] = block
                map.putAll(checkBlock(map, block, blockFace))
            }
        }
        return map
    }

    override fun onDisable() {
//        ship = null
    }


    @EventHandler
    fun onExecute(e: PlayerInteractEvent) {
        if (e.player.inventory.itemInMainHand != rodItemStack) return
        if (!ships.containsKey(e.player)) return

        e.isCancelled = true

        val ship = ships[e.player]!!

        when (e.action) {
            LEFT_CLICK_BLOCK, LEFT_CLICK_AIR -> {
                if (e.player.location.pitch > 0) {
                    ship.direction = Vector(0.0, -1.0, 0.0)
                    e.player.sendMessage("你使飞船下降了")
                } else {
                    ship.direction = Vector(0.0, 1.0, 0.0)
                    e.player.sendMessage("你使飞船上升了")
                }
                return
            }
            RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR -> {
                val direction = e.player.location.direction

                ship.direction = Vector(direction.x, 0.0, direction.z)
                e.player.sendMessage("你将飞船精确到你的朝向")
                return
            }
            else -> {

            }
        }
    }

    companion object {
//        var ship: Ship? = null
    }

}
val ships = mutableMapOf<Player, Ship>()

lateinit var shipDemo: ShipDemo

val rodItemStack = ItemStack(Material.FISHING_ROD).also { itemStack ->
    itemStack.itemMeta = itemStack.itemMeta!!.also { itemMeta ->
        itemMeta.setDisplayName("§e操控杆")
        itemMeta.lore = mutableListOf(
                "§6右键 §7-> §a船朝你的朝向",
                "§6左键 + 朝天 §7-> §a船上升",
                "§6左键 + 朝地 §7-> §a船下降"
        )
        itemMeta.isUnbreakable = true
    }
}

fun Block.equalsBlock(block: Block?): Boolean =
        this.world.name == block!!.world.name && this.x == block.x && this.y == block.y && this.z == block.z