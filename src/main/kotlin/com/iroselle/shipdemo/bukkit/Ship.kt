package com.iroselle.shipdemo.bukkit

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.*
import org.bukkit.entity.*
import org.bukkit.entity.FallingBlock
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class Ship {

    val initialLocation: Location
    val player: Player

    val blockList = mutableListOf<FallingBlock>()
    val world: World
    lateinit var task: BukkitTask
//    val mount: FallingBlock
//    val house: Horse
    var removing = false

    constructor(initialLocation: Location, player: Player, fallingBlocks: MutableList<Pair<Location, Material>> = mutableListOf()) {
        this.initialLocation = initialLocation
        this.player = player
        this.world = initialLocation.world!!
        /*mount = world.spawnFallingBlock(Location(world, initialLocation.x, initialLocation.y + 1, initialLocation.z), Material.AIR.createBlockData())
        mount.setGravity(false)
        mount.dropItem = false
        mount.addPassenger(player)*/
/*        house = world.spawnEntity(initialLocation.block.location, EntityType.HORSE) as Horse
        house.isInvisible = true
        house.isInvulnerable = true
        house.setGravity(false)*/
        player.teleport(Location(world, initialLocation.x, initialLocation.y + 1, initialLocation.z))
//        player.setGravity(false)

        fallingBlocks.forEach {
            generateFallingBlock(it.first, it.second)
        }

/*        addBlock(initialLocation.block.location, Material.DIAMOND_BLOCK, )
        addBlock(initialLocation.block.getRelative(NORTH).location, Material.GOLD_BLOCK)
        addBlock(initialLocation.block.getRelative(SOUTH).location, Material.GOLD_BLOCK)
        addBlock(initialLocation.block.getRelative(EAST).location, Material.GOLD_BLOCK)
        addBlock(initialLocation.block.getRelative(WEST).location, Material.GOLD_BLOCK)*/
        start()
    }

    constructor(player: Player, blocks: MutableList<Block>) {
        this.initialLocation = player.location
        this.player = player
        this.world = initialLocation.world!!
        player.teleport(Location(world, initialLocation.x, initialLocation.y + 1, initialLocation.z))

        blocks.forEach {
            blockList.add(blockToFallingBlock(it))
        }
        start()

    }

    fun blockToFallingBlock(block: Block): FallingBlock {
        block.type = Material.AIR
        return world.spawnFallingBlock(block.location, block.blockData)
    }

    fun fallingBlockToBlock(fallingBlock: FallingBlock): Block {
        fallingBlock.location.block.type = fallingBlock.blockData.material
        return fallingBlock.location.block
    }

    fun generateFallingBlock(location: Location, material: Material = Material.STONE) {
        val blockData = material.createBlockData()
        val fallingBlock = world.spawnFallingBlock(location, blockData)
        fallingBlock.setGravity(false)
        fallingBlock.isInvulnerable = true
        fallingBlock.dropItem = false

        blockList.add(fallingBlock)
    }

    fun start() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(shipDemo, Runnable {
            move(globalBlockFace, 0.2)
        }, 10, 5)
    }

    fun stop() {
        task.cancel()
        Bukkit.getScheduler().runTask(shipDemo, Runnable {
            blockList.forEach {
                fallingBlockToBlock(it)
                it.remove()
            }
            player.setGravity(true)
        })
        player.sendMessage("飞船被破坏")
    }

    fun move(blockFace: BlockFace, increase: Double = 1.0) {

        /*if (mount.passengers.isEmpty()) {
            if (removing) {
                return
            }
            removing = true
            Thread {
                stop()
            }.start()
            return
        }*/

        blockList.forEach {

            val vector = when (blockFace) {
                NORTH -> {
                    Vector(0.0, 0.0, -increase)
                }
                EAST -> {
                    Vector(increase, 0.0, 0.0)
                }
                SOUTH -> {
                    Vector(0.0, 0.0, increase)
                }
                WEST -> {
                    Vector(-increase, 0.0, 0.0)
                }
                UP -> {
                    Vector(0.0, increase, 0.0)
                }
                DOWN -> {
                    Vector(0.0, -increase, 0.0)
                }
                else -> {
                    Vector(0.0, 0.0, 0.0)
                }
            }
            Bukkit.getScheduler().runTask(shipDemo, Runnable {
                it.velocity = player.location.direction.multiply(increase)
                player.velocity = player.location.direction.multiply(increase * (1 + player.width))
            })
        }

    }

}