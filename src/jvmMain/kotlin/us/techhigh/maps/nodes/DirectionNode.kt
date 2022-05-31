package us.techhigh.maps.nodes

import us.techhigh.maps.constants.maxSteps
import us.techhigh.maps.data.Building
import us.techhigh.maps.enums.Direction
import us.techhigh.maps.json.DirectionStep
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

@kotlinx.serialization.Serializable
actual open class DirectionNode(
    @kotlinx.serialization.Transient var neighbors: MutableList<DirectionNode> = mutableListOf(),
    val id: String,
    val position: Pair<Double, Double>
) {
    @kotlinx.serialization.Transient var building: Building? = null

    actual fun distanceTo(other: DirectionNode) : Double {
        if (this is FlooredNode && other is FlooredNode) { return 0.0 }
        return sqrt((other.position.first - this.position.first).times(building!!.scale.first).pow(2) + (other.position.second - this.position.second).times(building!!.scale.second).pow(2))
    }
    actual fun directionTo(other: DirectionNode) : Direction = when {
        this is FlooredNode && other is FlooredNode && this.type == "elevator" -> Direction.ELEVATOR
        this is FlooredNode && other is FlooredNode && this.type.contains("stair") -> Direction.STAIRS
        abs(other.position.first - this.position.first) > abs(other.position.second - this.position.second) -> if (this.position.first < other.position.first) Direction.EAST else Direction.WEST
        abs(other.position.first - this.position.first) < abs(other.position.second - this.position.second) -> if (this.position.second > other.position.second) Direction.SOUTH else Direction.NORTH
        else -> { println(this); println(other); throw Exception("Nodes are at the same position!") }
    }

    actual fun paths(to: DirectionNode) : List<DirectionStep> {
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

    actual override fun toString(): String = id
}