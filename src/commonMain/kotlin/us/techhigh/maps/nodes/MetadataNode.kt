package us.techhigh.maps.nodes

import kotlinx.serialization.Transient
import us.techhigh.maps.data.Metadata

@kotlinx.serialization.Serializable
class MetadataNode(@Transient private val _neighbors: MutableList<DirectionNode> = mutableListOf(), @Transient private val _position: Pair<Double, Double> = 0.0 to 0.0, @Transient private val _id: String = "", val metadata: Metadata) : DirectionNode(_neighbors, _id, _position) {
    override fun toString() = metadata.getIdentifier()
}