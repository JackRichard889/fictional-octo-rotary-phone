package us.techhigh.maps.nodes

import us.techhigh.maps.enums.Direction
import us.techhigh.maps.json.DirectionStep

expect open class DirectionNode {
    fun distanceTo(other: DirectionNode) : Double
    fun directionTo(other: DirectionNode) : Direction
    fun paths(to: DirectionNode) : List<DirectionStep>
    override fun toString(): String
}