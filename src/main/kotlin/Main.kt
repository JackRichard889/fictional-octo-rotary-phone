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

    fun paths(_from: String, _to: String) : List<DirectionNode> {
        val from = floors.flatten().first { it.id == _from || if (it is MetadataNode) it.metadata.getIdentifier() == _from else false }
        val to = floors.flatten().first { it.id == _to || if (it is MetadataNode) it.metadata.getIdentifier() == _to else false }
        return from.paths(to)
    }
}

object BuildingB : Building('B') {
    override val floors = listOf(
        buildNodeTree(object {}.javaClass.getResource("/floors/b0.json")?.readText() ?: ""),
        buildNodeTree(object {}.javaClass.getResource("/floors/b1.json")?.readText() ?: ""),
        buildNodeTree(object {}.javaClass.getResource("/floors/b2.json")?.readText() ?: ""),
        buildNodeTree(object {}.javaClass.getResource("/floors/b3.json")?.readText() ?: ""),
        buildNodeTree(object {}.javaClass.getResource("/floors/b4.json")?.readText() ?: "")
    )

    init {
        val elevators = floors.flatten().filterIsInstance<FlooredNode>().filter { it.type == "elevator" }
        floors.forEachIndexed { i, it ->
            val elevator = it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }
            it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }.floor = i
            it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }.neighbors.addAll(elevators.filter { it.id != elevator.id })
        }
    }
}

enum class RoomType { CLASSROOM, OFFICE, SHOP, CAFETERIA, BATHROOM, OTHER }
data class Metadata(val building: Char, val floor: Int, val room: Int, val type: RoomType = RoomType.OTHER) {
    fun getIdentifier() = "$building$floor${(if (room < 10) "0" else "") + room.toString()}"
}

enum class Direction { NORTH, EAST, WEST, SOUTH, ELEVATOR, UNSPECIFIED }

open class DirectionNode(
    var neighbors: MutableList<DirectionNode> = mutableListOf(),
    val id: String,
    val position: Pair<Double, Double>
) {
    fun distanceTo(other: DirectionNode) : Double {
        if (this is FlooredNode && other is FlooredNode) { return 0.0 }
        return sqrt((other.position.first - this.position.first).times(coordXScale).pow(2) + (other.position.second - this.position.second).times(coordYScale).pow(2))
    }
    fun directionTo(other: DirectionNode) : Direction = when {
            this is FlooredNode && other is FlooredNode -> Direction.ELEVATOR
            abs(other.position.first - this.position.first) > abs(other.position.second - this.position.second) -> if (this.position.first < other.position.first) Direction.EAST else Direction.WEST
            abs(other.position.first - this.position.first) < abs(other.position.second - this.position.second) -> if (this.position.second > other.position.second) Direction.SOUTH else Direction.NORTH
            else -> throw Exception("Nodes are at the same position!")
        }

    fun paths(to: DirectionNode) : List<DirectionNode> {
        fun getAllPathsR(
            from: DirectionNode,
            to: DirectionNode,
            step: Int = 0,
            path: List<DirectionNode> = listOf(from)
        ): List<DirectionNode>? {
            if (from == to || step > maxSteps) {
                return path
            }
            return from.neighbors.filter { !path.contains(it) }
                .firstNotNullOfOrNull { getAllPathsR(it, to, step + 1, path + it) }
        }

        return getAllPathsR(this, to) ?: emptyList()
    }

    override fun toString(): String = id
}

class MetadataNode(neighbors: MutableList<DirectionNode> = mutableListOf(), position: Pair<Double, Double>, id: String, val metadata: Metadata) : DirectionNode(neighbors, id, position) {
    override fun toString() = metadata.getIdentifier()
}

class FlooredNode(neighbors: MutableList<DirectionNode> = mutableListOf(), position: Pair<Double, Double>, id: String, val type: String, var floor: Int = 0) : DirectionNode(neighbors, id, position)

fun List<DirectionNode>.format() : List<String> {
    val builder = StringBuilder()

    builder.append("Start at ${this.first()}.\n")
    var distanceSum = 0
    var lastChange: Direction = Direction.UNSPECIFIED
    take(size - 1).forEachIndexed { index, directionNode ->
        if (lastChange == directionNode.directionTo(this[index + 1])) {
            distanceSum += ceil(directionNode.distanceTo(this[index + 1])).toInt()
        } else {
            if (lastChange == Direction.UNSPECIFIED) { lastChange = directionNode.directionTo(this[index + 1]) }
            else { builder.append("Continue ${lastChange.toString().lowercase()} towards $directionNode for $distanceSum feet.\n") }
            lastChange = directionNode.directionTo(this[index + 1])
            distanceSum = ceil(directionNode.distanceTo(this[index + 1])).toInt()
        }
    }
    builder.append("Arrive at ${this.last()}!")

    return builder.toString().split("\n")
}

@kotlinx.serialization.Serializable
data class NodeLoad(val elements: List<DirectionNodeLoad>)

@kotlinx.serialization.Serializable
data class DirectionNodeLoad(val x: Double, val y: Double, val neighbors: List<String>, val id: String, val room: String = "", val type: String = "") {
    fun toNode() : DirectionNode {
        return if (room.isEmpty() && type.isEmpty()) {
            DirectionNode(id = id, position = x to y)
        } else if (type == "elevator" || type.contains("stair")) {
            FlooredNode(position = x to y, id = id, type = type)
        } else {
            MetadataNode(position = x to y, metadata = Metadata(room.first(), room.take(2).last().toString().toInt(), room.takeLast(2).toInt()), id = id)
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

fun main() {
    println(BuildingB.paths("B216", "B303").format().joinToString("\n"))
}