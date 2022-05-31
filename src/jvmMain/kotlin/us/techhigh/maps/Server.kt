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

fun main() {
    embeddedServer(CIO, port = 80) {
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