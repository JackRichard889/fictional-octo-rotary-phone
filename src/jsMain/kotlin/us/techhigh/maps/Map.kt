package us.techhigh.maps

import kotlinext.js.asJsObject
import kotlinext.js.getOwnPropertyNames
import org.w3c.dom.*
import org.w3c.dom.events.MouseEvent
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import us.techhigh.maps.data.Building
import kotlin.math.*

fun getImage(path: String): HTMLImageElement {
    val image = window.document.createElement("img") as HTMLImageElement
    image.src = path
    return image
}

val canvas = initializeCanvas()

fun initializeCanvas(): HTMLCanvasElement {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    context.canvas.width  = window.innerWidth
    context.canvas.height = window.innerHeight
    document.body!!.appendChild(canvas)
    return canvas
}

val context: CanvasRenderingContext2D
    get() {
        return canvas.getContext("2d") as CanvasRenderingContext2D
    }

class CanvasState(private val canvas: HTMLCanvasElement) {
    private var width = canvas.width
    private var height = canvas.height
    private val size: Vector = v(width.toDouble(), height.toDouble())
    private val context = us.techhigh.maps.context
    private val interval = 1000 / 30  // This is 30 FPS.

    init {
        canvas.onmousedown = { e: MouseEvent -> }

        canvas.onmousemove = { e: MouseEvent -> }

        canvas.onmouseup = { e: MouseEvent -> }

        canvas.ondblclick = { e: MouseEvent -> }

        window.setInterval({ draw() }, interval)
    }

    private fun mousePos(e: MouseEvent): Vector {
        var offset = Vector()
        var element: HTMLElement? = canvas
        while (element != null) {
            val el: HTMLElement = element
            offset += Vector(el.offsetLeft.toDouble(), el.offsetTop.toDouble())
            element = el.offsetParent as HTMLElement?
        }
        return Vector(e.pageX, e.pageY) - offset
    }

    private fun clear() {
        context.fillStyle = "#D0D0D0"
        context.fillRect(0.0, 0.0, width.toDouble(), height.toDouble())
        context.strokeStyle = "#000000"
        context.lineWidth = 4.0
        context.strokeRect(0.0, 0.0, width.toDouble(), height.toDouble())
    }

    private fun draw() {
        clear()

        points.forEach {
            context.fillStyle = "#ff0000"
            context.fillRect(((it.x / 2) + 0.5) * width, ((it.y * -1 / 2) + 0.5) * height, 2.0, 2.0)
            it.neighbors.forEach { n ->
                val point = points.first { p -> p.id == n }
                context.moveTo(((it.x / 2) + 0.5) * width, ((it.y * -1 / 2) + 0.5) * height)
                context.lineTo(((point.x / 2) + 0.5) * width, ((point.y * -1 / 2) + 0.5) * height)
                context.stroke()
                context.moveTo(0.0, 0.0)
            }
        }
    }
}


fun v(x: Double, y: Double) = Vector(x, y)

class Vector(val x: Double = 0.0, val y: Double = 0.0) {
    operator fun plus(v: Vector) = v(x + v.x, y + v.y)
    operator fun unaryMinus() = v(-x, -y)
    operator fun minus(v: Vector) = v(x - v.x, y - v.y)
    operator fun times(koef: Double) = v(x * koef, y * koef)
    fun rotatedBy(theta: Double): Vector {
        val sin = sin(theta)
        val cos = cos(theta)
        return v(x * cos - y * sin, x * sin + y * cos)
    }

    fun contained(topLeft: Vector, size: Vector) = (x >= topLeft.x) && (x <= topLeft.x + size.x) &&
            (y >= topLeft.y) && (y <= topLeft.y + size.y)
}

var points = listOf<Building.NodeSerializer.DirectionNodeLoad>()

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun main() {
    window.fetch("/floors/d3.json", RequestInit(method = "GET")).then {
        if (it.ok) {
            it.text().then { str ->
                points = json.decodeFromString<Building.NodeSerializer.NodeLoad>(str).elements
            }
        }
    }

    CanvasState(canvas).apply {

    }
}