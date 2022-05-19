import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

const val coordXScale = 75
const val coordYScale = 89
const val maxSteps = 32

enum class RoomType { CLASSROOM, OFFICE, SHOP, CAFETERIA, BATHROOM, OTHER }
data class Metadata(val building: Char, val floor: Int, val room: Int, val type: RoomType = RoomType.OTHER) {
    fun getIdentifier() = "$building$floor${(if (room < 10) "0" else "") + room.toString()}"
}

enum class Direction { NORTH, EAST, WEST, SOUTH }

open class DirectionNode(
    var neighbors: MutableList<DirectionNode> = mutableListOf(),
    val id: String,
    val position: Pair<Double, Double>
) {
    fun distanceTo(other: DirectionNode) : Double = sqrt((other.position.first - this.position.first).times(coordXScale).pow(2) + (other.position.second - this.position.second).times(coordYScale).pow(2))
    fun directionTo(other: DirectionNode) : Direction = when {
            abs(other.position.first - this.position.first) > abs(other.position.second - this.position.second) -> if (this.position.first < other.position.first) Direction.EAST else Direction.WEST
            abs(other.position.first - this.position.first) < abs(other.position.second - this.position.second) -> if (this.position.second > other.position.second) Direction.SOUTH else Direction.NORTH
            else -> throw Exception("Nodes are at the same position!")
        }

    fun addNeighbor(other: DirectionNode) {
        this.neighbors.add(other)
        other.neighbors.add(this)
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

fun formatDirections(path: List<DirectionNode>) =
    if (path.size == 2) {
        listOf("Start at ${path.first()}.") + ("Go " + path.first().directionTo(path.last()) + " for " + ceil(path.first().distanceTo(path.last())).toInt() + " feet towards " + path.last() + ".") + "Arrive at ${path.last()}!"
    } else if (path.size > 2) {
        listOf("Start at ${path.first()}.") + path.take(path.size - 1).mapIndexed { i, n ->
            "Go " + n.directionTo(path[i + 1]) + " for " + ceil(n.distanceTo(path[i + 1])).toInt() + " feet towards " + path[i + 1] + "."
        } + "Arrive at ${path.last()}!"
    } else {
        throw Exception("No path between points!")
    }

fun allNodes(start: DirectionNode, l: List<DirectionNode> = listOf(start)) : List<DirectionNode> {
    return (l + start.neighbors.filter { !l.contains(it) }.map { allNodes(it, l + it) }.flatten()).distinct()
}

@kotlinx.serialization.Serializable
data class NodeLoad(val elements: List<DirectionNodeLoad>)

@kotlinx.serialization.Serializable
data class DirectionNodeLoad(val x: Double, val y: Double, val neighbors: List<String>, val id: String, val room: String = "") {
    fun toNode() : DirectionNode = if (room.isEmpty()) DirectionNode(id = id, position = x to y) else MetadataNode(position = x to y, metadata = Metadata(room.first(), room.take(2).last().toString().toInt(), room.takeLast(2).toInt()), id = id)
}

private val json = Json { ignoreUnknownKeys = true }

fun buildNodeTree(k: String) : List<DirectionNode> {
    val elements = json.decodeFromString<NodeLoad>(k).elements
    val nodes = elements.map { it.toNode() }

    elements.forEachIndexed { i, n -> nodes[i].neighbors = n.neighbors.map { nod -> nodes.first { no -> no.id == nod } }.toMutableList() }

    return nodes
}

fun main() {
    val nod = buildNodeTree(object {}.javaClass.getResource("/floors/b1.json")?.readText() ?: "")

    repeat(500) {
        val x = nod.filterIsInstance<MetadataNode>().random()
        val y = nod.filterIsInstance<MetadataNode>().random()

        if (x != y) { println(formatDirections(x.paths(y)).joinToString("\n")) }
        println()
    }
}