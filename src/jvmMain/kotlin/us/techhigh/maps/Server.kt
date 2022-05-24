package us.techhigh.maps

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

const val coordXScale = 1
const val coordYScale = 1
const val maxSteps = 128

abstract class Building(val identifier: Char) {
    abstract val floors: List<List<DirectionNode>>
    abstract val floorRange: List<Int>

    fun paths(_from: String, _to: String) : List<DirectionStep> {
        val from = floors.flatten().first { it.id == _from || if (it is MetadataNode) it.metadata.getIdentifier() == _from else false }
        val to = floors.flatten().first { it.id == _to || if (it is MetadataNode) it.metadata.getIdentifier() == _to else false }
        return from.paths(to)
    }

    fun loadElevators() {
        val elevators = floors.flatten().filterIsInstance<FlooredNode>().filter { it.type == "elevator" }
        floors.forEachIndexed { i, it ->
            val elevator = it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }
            it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }.floor = floorRange[i]
            it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }.neighbors.addAll(elevators.filter { it.id != elevator.id })
        }
    }

    fun loadStairs() {
        floors.first()
            .filterIsInstance<FlooredNode>()
            .filter { it.type.contains("stair") }
            .map { it.type }.forEach { stair ->
                val stairsAll = floors.flatten().filterIsInstance<FlooredNode>().filter { it.type == stair }
                floors.forEachIndexed { index, directionNodes ->
                    val stairs = directionNodes.filterIsInstance<FlooredNode>().filter { it.type == stair }
                    if (stairs.isNotEmpty()) {
                        directionNodes.filterIsInstance<FlooredNode>().first { it.type == stair }.floor = floorRange[index]
                        directionNodes.filterIsInstance<FlooredNode>().first { it.type == stair }.neighbors.addAll(stairsAll.filter { it.id != stairs.first().id })
                    }
                }
            }
    }
}

object School {
    private val buildings = listOf(
        BuildingB,
        BuildingC,
        BuildingD
    )


    init {
        // TODO: connect all buildings and floors
    }

    fun findNode(node: String) = buildings.map { it.floors }.flatten().flatten().first { it.id == node || if (it is MetadataNode) it.metadata.getIdentifier() == node else false }
}

object BuildingB : Building('B') {
    override val floorRange: List<Int> = (0..4).toList()
    override val floors = listOf(
        buildNodeTree(this::class.java.classLoader.getResource("floors/b0.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/b1.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/b2.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/b3.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/b4.json")?.readText() ?: "")
    )

    init {
        loadElevators()
        loadStairs()
    }
}

object BuildingC : Building('C') {
    override val floorRange: List<Int> = (1 ..4).filter { it != 2 }.toList()
    override val floors = listOf(
        buildNodeTree(this::class.java.classLoader.getResource("floors/c1.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/c3.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/c4.json")?.readText() ?: "")
    )

    init {
        loadStairs()
    }
}

object BuildingD : Building('D') {
    override val floorRange: List<Int> = (1..4).filter { it != 2 }.toList()
    override val floors = listOf(
        buildNodeTree(this::class.java.classLoader.getResource("floors/d1.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/d3.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/d4.json")?.readText() ?: "")
    )

    init {
        loadElevators()
        loadStairs()
    }
}

enum class RoomType { CLASSROOM, OFFICE, SHOP, CAFETERIA, BATHROOM, OTHER }

@kotlinx.serialization.Serializable
data class Metadata(val building: Char, val floor: Int, val room: Int, val type: RoomType = RoomType.OTHER) {
    fun getIdentifier() = "$building$floor${(if (room < 10) "0" else "") + room.toString()}"
}

enum class Direction { NORTH, EAST, WEST, SOUTH, ELEVATOR, STAIRS, UNSPECIFIED }

@kotlinx.serialization.Serializable
open class DirectionNode(
    @Transient var neighbors: MutableList<DirectionNode> = mutableListOf(),
    val id: String,
    val position: Pair<Double, Double>
) {
    private fun distanceTo(other: DirectionNode) : Double {
        if (this is FlooredNode && other is FlooredNode) { return 0.0 }
        return sqrt((other.position.first - this.position.first).times(coordXScale).pow(2) + (other.position.second - this.position.second).times(coordYScale).pow(2))
    }
    private fun directionTo(other: DirectionNode) : Direction = when {
            this is FlooredNode && other is FlooredNode && this.type == "elevator" -> Direction.ELEVATOR
            this is FlooredNode && other is FlooredNode && this.type.contains("stair") -> Direction.STAIRS
            abs(other.position.first - this.position.first) > abs(other.position.second - this.position.second) -> if (this.position.first < other.position.first) Direction.EAST else Direction.WEST
            abs(other.position.first - this.position.first) < abs(other.position.second - this.position.second) -> if (this.position.second > other.position.second) Direction.SOUTH else Direction.NORTH
            else -> { println(this); println(other); throw Exception("Nodes are at the same position!") }
        }

    fun paths(to: DirectionNode) : List<DirectionStep> {
        fun getAllPathsR(
            from: DirectionNode,
            to: DirectionNode,
            step: Int = 0,
            path: List<DirectionNode> = listOf(from)
        ): List<DirectionNode>? {
            if (from == to) { return path }
            if (step > maxSteps) { return null }
            return from.neighbors.filter { !path.contains(it) }
                .mapNotNull { getAllPathsR(it, to, step + 1, path + it) }
                .minByOrNull { it.size }
        }

        val path = getAllPathsR(this, to) ?: emptyList()
        val steps = mutableListOf<DirectionStep>()

        var distanceSum = 0
        var lastChange: Direction = Direction.UNSPECIFIED
        path.take(path.size - 1).forEachIndexed { index, directionNode ->
            if (lastChange == directionNode.directionTo(path[index + 1])) {
                distanceSum += ceil(directionNode.distanceTo(path[index + 1])).toInt()
            } else {
                if (lastChange == Direction.UNSPECIFIED) { lastChange = directionNode.directionTo(path[index + 1]) }
                else { steps.add(DirectionStep(from = directionNode, to = path[index + 1], distance = distanceSum, direction = lastChange)) }
                lastChange = directionNode.directionTo(path[index + 1])
                distanceSum = ceil(directionNode.distanceTo(path[index + 1])).toInt()
            }
        }

        return steps
    }

    override fun toString(): String = id
}

@kotlinx.serialization.Serializable
class MetadataNode(@Transient private val _neighbors: MutableList<DirectionNode> = mutableListOf(), @Transient private val _position: Pair<Double, Double> = 0.0 to 0.0, @Transient private val _id: String = "", val metadata: Metadata) : DirectionNode(_neighbors, _id, _position) {
    override fun toString() = metadata.getIdentifier()
}

@kotlinx.serialization.Serializable
class FlooredNode(@Transient private val _neighbors: MutableList<DirectionNode> = mutableListOf(), @Transient private val _position: Pair<Double, Double> = 0.0 to 0.0, @Transient private val _id: String = "", val type: String, var floor: Int = 0) : DirectionNode(_neighbors, _id, _position)

@kotlinx.serialization.Serializable
data class NodeLoad(val elements: List<DirectionNodeLoad>)

@kotlinx.serialization.Serializable
data class DirectionNodeLoad(val x: Double, val y: Double, val neighbors: List<String>, val id: String, val room: String = "", val type: String = "") {
    fun toNode() : DirectionNode {
        return if (room.isEmpty() && type.isEmpty()) {
            DirectionNode(id = id, position = x to y)
        } else if (type == "elevator" || type.contains("stair")) {
            FlooredNode(_position = x to y, _id = id, type = type)
        } else {
            MetadataNode(_position = x to y, metadata = Metadata(room.first(), room.take(2).last().toString().toInt(), room.takeLast(2).toInt()), _id = id)
        }
    }
}

private val json = Json { ignoreUnknownKeys = true }

fun buildNodeTree(k: String) : List<DirectionNode> {
    val elements = json.decodeFromString<NodeLoad>(k).elements
    val nodes = elements.map { it.toNode() }

    elements.forEachIndexed { i, n -> nodes[i].neighbors = n.neighbors.map { nod -> nodes.first { no -> no.id == nod } }.toMutableList() }

    return nodes
}

@kotlinx.serialization.Serializable
data class DirectionStep(val from: DirectionNode, val to: DirectionNode, val direction: Direction, val distance: Int)

@kotlinx.serialization.Serializable
data class DirectionResponse(val from: MetadataNode, val to: MetadataNode, val steps: List<DirectionStep>)

fun main() {
    embeddedServer(CIO, port = 80) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                encodeDefaults = true
            })
        }

        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title { +"WTHS Map" }
                    }

                    body {
                        h1 { +"Hello world." }
                        script(src = "CoolProjecT.js") {

                        }
                    }
                }
            }

            get("/directions") {
                val from = School.findNode(call.parameters["from"]!!) as MetadataNode
                val to = School.findNode(call.parameters["to"]!!) as MetadataNode
                call.respond(DirectionResponse(from, to, from.paths(to)))
            }

            static("/") {
                resources("")
            }
        }
    }.start(wait = true)
}