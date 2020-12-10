package com.iroselle.shipdemo.bukkit

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftFallingBlock
import org.bukkit.entity.*
import org.bukkit.entity.FallingBlock
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class Ship {

    val initialLocation: Location
    val player: Player

    var direction: Vector

    val blockList = mutableListOf<FallingBlock>()
    val world: World
    lateinit var task: BukkitTask
//    val mount: FallingBlock
//    val house: Horse
    var removing = false

    constructor(initialLocation: Location, player: Player, fallingBlocks: MutableList<Pair<Location, Material>> = mutableListOf()) {
        ships[player] = this
        this.initialLocation = initialLocation
        this.player = player
        this.world = initialLocation.world!!
        this.direction = player.location.direction
        /*mount = world.spawnFallingBlock(Location(world, initialLocation.x, initialLocation.y + 1, initialLocation.z), Material.AIR.createBlockData())
        mount.setGravity(false)
        mount.dropItem = false
        mount.addPassenger(player)*/
/*        house = world.spawnEntity(initialLocation.block.location, EntityType.HORSE) as Horse
        house.isInvisible = true
        house.isInvulnerable = true
        house.setGravity(false)*/
//        player.teleport(Location(world, initialLocation.x, initialLocation.y + 1, initialLocation.z))
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
        ships[player] = this
        this.initialLocation = player.location
        this.player = player
        this.world = initialLocation.world!!
        this.direction = player.location.direction
//        player.teleport(Location(world, initialLocation.x, initialLocation.y + 1, initialLocation.z))

        blockList.addAll(blocksToFallingBlocks(blocks))
        start()

    }

    fun blocksToFallingBlocks(blocks: MutableList<Block>): MutableList<FallingBlock> {
        val map = mutableMapOf<Location, Material>()
        blocks.forEach { block ->
            val location = block.location.clone()
            val material = block.type
            map[location] = material
            block.type = Material.AIR
        }
        val fallingBlocks = mutableListOf<FallingBlock>()
        map.forEach { entry ->
            fallingBlocks.add(world.spawnFallingBlock(entry.key, entry.value.createBlockData()).also {
/*                val craftFallingBlock = it as CraftFallingBlock
                craftFallingBlock.ticksLived = -1*/
                it.setGravity(false)
                it.ticksLived = player.ticksLived
                it.dropItem = false
            })
        }
        return fallingBlocks
    }

    fun fallingBlockToBlock(fallingBlock: FallingBlock): Block {
        val location = fallingBlock.location.clone()
        location.block.type = fallingBlock.blockData.material
        location.block.blockData = fallingBlock.blockData
        fallingBlock.remove()
        return location.block
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
            move(0.2)
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

    fun move(increase: Double = 1.0) {

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
            Bukkit.getScheduler().runTask(shipDemo, Runnable {
                it.velocity = direction.multiply(increase)
//                player.velocity = direction.multiply(increase * (1 + player.width))
            })
        }

    }

}