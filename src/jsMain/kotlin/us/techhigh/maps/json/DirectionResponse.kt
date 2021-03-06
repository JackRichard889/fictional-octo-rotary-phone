package us.techhigh.maps.json

import us.techhigh.maps.nodes.MetadataNode

@kotlinx.serialization.Serializable
actual class DirectionResponse(val from: MetadataNode, val to: MetadataNode, val steps: List<DirectionStep>)