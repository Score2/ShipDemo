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
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
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
                    sender.sendMessage("/sd look - 修改飞船朝向(s, n, w, e | u, d)")
                    sender.sendMessage("")
                    return true
                }
                when {
                    args[0] == "spawn" && sender is Player -> {
                        val underfootLocation = Location(sender.world, sender.location.blockX.toDouble(), (sender.location.blockY - 1).toDouble(), sender.location.blockZ.toDouble())
                        if (underfootLocation.block.type == Material.AIR) {
                            sender.sendMessage("脚下必须为一个有效方块")
                            return true
                        }

                        Thread {
                            sender.sendMessage("正在异步递归计算方块...")
                            val blocks = mutableListOf<Block>()

                            checkBlock(underfootLocation.block).forEach {
                                blocks.add(it.value)
                            }

                            Ship(sender, blocks)

                            sender.sendMessage("成功召唤脚下的一个飞船")
                        }.start()

                    }
                    args[0] == "look" && args.size < 2 -> {
                        when (args[1]) {
                            "s" -> {
                                globalBlockFace = BlockFace.SOUTH
                            }
                            "n" -> {
                                globalBlockFace = BlockFace.NORTH
                            }
                            "w" -> {
                                globalBlockFace = BlockFace.WEST
                            }
                            "e" -> {
                                globalBlockFace = BlockFace.EAST
                            }
                            "u" -> {
                                globalBlockFace = BlockFace.UP
                            }
                            "d" -> {
                                globalBlockFace = BlockFace.DOWN
                            }
                            else -> {
                                sender.sendMessage("参数仅 s, n, w, e | u, d 可用")
                            }

                        }

                        sender.sendMessage("成功切换飞船位置")
                    }
                    else -> {
                        sender.sendMessage("")
                        sender.sendMessage("/sd spawn - 召唤或传送飞船到你的位置")
                        sender.sendMessage("/sd look - 修改飞船朝向(s, n, w, e | u, d)")
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

    fun checkBlock(original: Block, prev: BlockFace? = null): Map<Location, Block> {
        val map = mutableMapOf<Location, Block>()
        for (blockFace in BlockFace.values()) {
            if (blockFace == BlockFace.SELF) continue
            if (prev != null && blockFace == prev.oppositeFace) continue
            val block: Block = original.location.block.getRelative(blockFace)
            if (block.type != Material.AIR) {
                map[block.location] = block
                map.putAll(checkBlock(block, blockFace))
            }
        }
        return map
    }

    override fun onDisable() {
//        ship = null
    }

    companion object {
//        var ship: Ship? = null
    }

}

lateinit var shipDemo: ShipDemo

var globalBlockFace = BlockFace.SOUTH

fun Block.equalsBlock(block: Block?): Boolean =
        this.world.name == block!!.world.name && this.x == block.x && this.y == block.y && this.z == block.z