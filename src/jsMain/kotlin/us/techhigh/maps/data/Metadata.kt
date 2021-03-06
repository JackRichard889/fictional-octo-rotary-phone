package us.techhigh.maps.data

import us.techhigh.maps.enums.RoomType

@kotlinx.serialization.Serializable
actual class Metadata(val building: Char, val floor: Int, val room: Int, val type: RoomType = RoomType.OTHER) {
    actual fun getIdentifier() = "$building$floor${(if (room < 10) "0" else "") + room.toString()}"
}