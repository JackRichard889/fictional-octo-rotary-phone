package us.techhigh.maps.json

import us.techhigh.maps.enums.Direction
import us.techhigh.maps.nodes.DirectionNode

@kotlinx.serialization.Serializable
data class DirectionStep(val from: DirectionNode, val to: DirectionNode, val direction: Direction, val distance: Int)