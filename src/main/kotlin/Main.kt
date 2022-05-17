import kotlin.math.pow
import kotlin.math.sqrt

const val coordXScale = 1.0
const val coordYScale = 1.0
const val maxSteps = 16

data class Metadata(val building: Char, val floor: Int, val room: Int) {
    fun getIdentifier() : String = "$building$floor${(if (room < 10) "0" else "") + room.toString()}"
}

enum class Direction { NORTH, EAST, WEST, SOUTH }

class DirectionNode(
    private val neighbors: MutableList<DirectionNode> = mutableListOf(),
    val id: String,
    val position: Pair<Double, Double>
) {
    fun distanceTo(other: DirectionNode) : Double = sqrt((other.position.first - this.position.first).times(coordXScale).pow(2) + (other.position.second - this.position.second).times(coordYScale).pow(2))
    fun directionTo(other: DirectionNode) : Direction = when {
            this.position.first > other.position.first -> Direction.WEST
            this.position.first < other.position.first -> Direction.EAST
            this.position.second > other.position.second -> Direction.SOUTH
            this.position.second < other.position.second -> Direction.NORTH
            else -> throw Exception("Nodes are at the same position!")
        }

    fun getNeighbors() = neighbors
    fun addNeighbor(other: DirectionNode) {
        this.neighbors.add(other)
        other.neighbors.add(this)
    }

    fun paths(to: DirectionNode) : List<DirectionNode> {
        fun getAllPathsR(
            from: DirectionNode,
            to: DirectionNode,
            step: Int = 0,
            path: List<DirectionNode> = listOf()
        ): List<DirectionNode>? {
            if (from == to || step > maxSteps) {
                return path
            }
            return from.neighbors.filter { !path.contains(it) }
                .firstNotNullOfOrNull { getAllPathsR(it, to, step + 1, path + it) }
        }

        return this.neighbors.mapNotNull { getAllPathsR(it, to) }.minByOrNull { it.size } ?: emptyList()
    }

    override fun toString(): String = id
}

fun mapDirections(path: List<DirectionNode>) : List<String> =
    path.take(path.size - 1).mapIndexed { i, n ->
        "Go " + n.directionTo(path[i + 1]) + " for " + n.distanceTo(path[i + 1]) + " feet towards " + path[i + 1] + "."
    } + "Arrive at ${path.last()}!"

fun main() {
    val n1 = DirectionNode(position = -1.0 to 0.0, id = "1")
    val n2 = DirectionNode(position = 0.0 to 0.0, id = "2")
    val n3 = DirectionNode(position = 0.0 to 1.0, id = "3")
    val n4 = DirectionNode(position = 1.0 to 1.0, id = "4")
    val n5 = DirectionNode(position = 0.5 to 1.0, id = "5")
    val n6 = DirectionNode(position = 1.0 to 0.5, id = "6")

    n1.addNeighbor(n2)
    n2.addNeighbor(n3)
    n3.addNeighbor(n4)
    n2.addNeighbor(n5)
    n5.addNeighbor(n6)
    n6.addNeighbor(n4)

    println(mapDirections(n1.paths(n4)).joinToString("\n"))
}