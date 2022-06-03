package us.techhigh.maps

import us.techhigh.maps.data.Building
import us.techhigh.maps.nodes.DirectionNode
import java.awt.Color
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

val floor0: BufferedImage = ImageIO.read(object { }::class.java.classLoader.getResource("maps/0.png"))
val floor1: BufferedImage = ImageIO.read(object { }::class.java.classLoader.getResource("maps/1.png"))
val floor2: BufferedImage = ImageIO.read(object { }::class.java.classLoader.getResource("maps/2.png"))
val floor3: BufferedImage = ImageIO.read(object { }::class.java.classLoader.getResource("maps/3.png"))
val floor4: BufferedImage = ImageIO.read(object { }::class.java.classLoader.getResource("maps/4.png"))

val translatedSchool: MutableList<Building> = mutableListOf()

fun initializeMap() {
    translatedSchool.addAll(School.buildings.toMutableList().onEach { building ->
        building.floors.forEach { floor ->
            floor.forEach { node ->
                node.position = (((node.position.first + 1) / 2) * 4032 / 3) to (((node.position.second - 1) / -2) * 3024 / 3)
            }
        }
    })
}

fun imageWith(vararg steps: DirectionNode) : BufferedImage? {
    if (translatedSchool.isEmpty()) return null

    val floor = floor1
    val g = floor.createGraphics()
    translatedSchool.map(Building::floors)[1].flatten().forEach {
        g.color = Color.CYAN
        g.fill(Ellipse2D.Double((it.position.first - 8), (it.position.second - 8), 16.0, 16.0))
    }
    g.dispose()

    return floor
}