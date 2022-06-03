package us.techhigh.maps

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import us.techhigh.maps.json.DirectionResponse
import us.techhigh.maps.nodes.MetadataNode
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Image
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

fun main() {
    initializeMap()

    val frame = JFrame()
    frame.size = Dimension(1000, 1000)
    frame.contentPane.layout = FlowLayout()
    frame.contentPane.add(JLabel(ImageIcon(imageWith()?.getScaledInstance(1000, 1000, Image.SCALE_SMOOTH))))
    frame.isVisible = true

    embeddedServer(CIO, host = "localhost", port = 80) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                encodeDefaults = true
            })
        }

        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title { +"WTHS Map" }
                        styleLink("/style.css")
                    }

                    body {
                        input {
                            placeholder = "From Location"
                            type = InputType.text
                            id = "from"
                        }
                        input {
                            placeholder = "To Location"
                            type = InputType.text
                            id = "to"
                        }
                        input {
                            type = InputType.button
                            id = "go"
                            value = "Go"
                        }

                        p {
                            id = "result"
                        }

                        script(src = "CoolProjecT.js") {

                        }
                    }
                }
            }

            get("/directions") {
                val from = School.findNode(call.parameters["from"]!!) as MetadataNode
                val to = School.findNode(call.parameters["to"]!!) as MetadataNode
                call.respond(DirectionResponse(from, to, from.paths(to)))
            }

            static("/") {
                resources("")
            }
        }
    }.start(wait = true)
}