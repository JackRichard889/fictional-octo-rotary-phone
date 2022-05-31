package us.techhigh.maps.nodes

import us.techhigh.maps.data.Metadata

@kotlinx.serialization.Serializable
actual class MetadataNode(@kotlinx.serialization.Transient private val _neighbors: MutableList<DirectionNode> = mutableListOf(), @kotlinx.serialization.Transient private val _position: Pair<Double, Double> = 0.0 to 0.0, @kotlinx.serialization.Transient private val _id: String = "", val metadata: Metadata) : DirectionNode(_neighbors, _id, _position) {
    actual override fun toString() = metadata.getIdentifier()
}