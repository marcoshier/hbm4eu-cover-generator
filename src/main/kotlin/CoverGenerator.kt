import Constants.Companion.chemicals
import Constants.Companion.contentScale
import Constants.Companion.couples
import Constants.Companion.loadColors
import Constants.Companion.palette
import com.badlogic.gdx.physics.box2d.*
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.blur.GaussianBlur
import org.openrndr.extra.fx.edges.Contour
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.shapes.grid
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

val bleed = 5 // 3 + 3 mm

fun main() = application {

    configure {
        width = 1080
        height = 1920
        hideWindowDecorations = true
        windowAlwaysOnTop = false
    }

    program {
        var currentCover = 0

        // colors
        loadColors()
        println(this.application.windowPosition)


        // data
        val scraper = PeerScraper("data/PeerReviewsNew.csv")
        val titleFont = loadFont("data/fonts/Inter-Medium.ttf", 96.0 / contentScale, contentScale = 2.0)
        val authorsFont = loadFont("data/fonts/Inter-Medium.ttf", 38.0 / contentScale, contentScale = 2.0)


        // simulation
        var minRadius = 60.0 / contentScale
        val G = 0.0

        var world = World(Vector2(0.0, G).toFloat(), false)
        val bodies = arrayListOf<bodyCircle>()


        // bounds
        createChain(drawer.bounds.contour.segments.map { it.start }, world)


        //particles
        fun initParticles() {

            val keywords = scraper.keywords[currentCover]

            world.clearForces()
            bodies.map { world.destroyBody(it.body) }
            bodies.clear()

            chemicals.forEach { chemical ->

                var r = minRadius

                for(keyword in keywords) {

                    if (chemical.lowercase() in keyword) {
                        r += (200.0 / contentScale) + Math.random() * (36.0 / contentScale)
                    }

                }

                val pos = Vector2(Math.random() * width, Math.random() * height)
                val body = pos.createCircularBody(dens = 1.0f, restit = 0.01f, radius = r, world = world)
                bodies.add(body)

            }
        }
        initParticles()

        val screenshots = extend(Screenshots()) {
        }

        keyboard.keyUp.listen {
            when(it.key) {

                KEY_ARROW_LEFT -> {
                    currentCover = (currentCover - 1).coerceIn(0..scraper.rowsNumber)
                    initParticles()
                }
                KEY_ARROW_RIGHT -> {
                    currentCover = (currentCover + 1).coerceIn(0..scraper.rowsNumber)
                    initParticles()
                }

            }
        }





        // effects
        val aurasRt = renderTarget(width, height, contentScale = 2.0) {
            depthBuffer()
            colorBuffer()
        }
        val particlesRt = renderTarget(width, height, contentScale = 2.0) {
            depthBuffer()
            colorBuffer()
        }
        val cb = colorBuffer(width, height, contentScale = 2.0)
        val blur = GaussianBlur()
        val cc = Contour()

        val fullRt = renderTarget(width, height, contentScale = 2.0) {
            depthBuffer()
            colorBuffer()
        }




        extend {

            drawer.clear(ColorRGBa.GRAY)
            drawer.fill = ColorRGBa.BLACK


            drawer.stroke = null


            drawer.isolatedWithTarget(fullRt) {

                drawer.clear(ColorRGBa.GRAY)

                // auras
                drawer.isolatedWithTarget(aurasRt) {
                    drawer.clear(ColorRGBa.TRANSPARENT)
                    drawer.fill = ColorRGBa.WHITE.shade(0.67)
                    drawer.circles(
                        bodies.map { it.body.worldCenter.toDouble() },
                        bodies.map { it.radius + (it.radius * 0.3) })
                }

                drawer.isolatedWithTarget(particlesRt) {
                    drawer.clear(ColorRGBa.TRANSPARENT)


                    blur.spread = 5.0
                    blur.sigma = 5.0
                    blur.apply(aurasRt.colorBuffer(0), cb)
                    cc.contourColor = ColorRGBa.WHITE
                    cc.contourOpacity = 0.67
                    cc.contourWidth = 0.8
                    cc.levels = 5.0
                    cc.apply(cb, cb)
                    drawer.image(cb)


                    // particles
                    drawer.circles {
                        bodies.forEachIndexed { i, circle ->
                            this.fill = palette.colors[i]
                            this.circle(circle.body.worldCenter.toDouble(), circle.radius)
                        }
                    }


                    // title
                    val margin = 95.0

                    drawer.fontMap = titleFont
                    writer {
                        box = Rectangle(margin, margin - 34.0, (width.toDouble() / 4.0) * 3.0 + (margin), height / 2.0 - margin)
                        newLine()
                        text(scraper.titles[currentCover])
                    }


                    // authors
                    val margin2 = -95.0 + 38.0

                    drawer.fill = ColorRGBa.BLACK
                    drawer.fontMap = authorsFont
                    writer {
                        box = Rectangle(margin + 3.0, -margin2, (width.toDouble() / 4.0) * 3.0, height.toDouble())
                        newLine()
                        text(scraper.authors[currentCover], visible = false)
                        newLine()
                        val textHeight = cursor.y
                        cursor.y = height - textHeight + 18.0
                        text(scraper.authors[currentCover])
                    }


                    doPhysicsStep(seconds.toFloat(), world)
                }

                drawer.image(aurasRt.colorBuffer(0))
                drawer.image(particlesRt.colorBuffer(0))

            }


            drawer.image(fullRt.colorBuffer(0), 0.0, 0.0, width * 1.0, height* 1.0)


        }

    }
}


fun Vector2.createCircularBody(dens: Float = 1.0f, frict: Float = 0.01f, restit: Float = 0.00f, radius: Double = 5.0, world: World ): bodyCircle {

    val bodyDef = BodyDef().apply {
        position.set(x.toFloat(), y.toFloat())
        type = BodyDef.BodyType.DynamicBody
    }

    val fixtureDef = FixtureDef().apply {
        shape = CircleShape()
        density = dens
        friction = frict
        restitution = restit
        shape.radius = (radius).toFloat()
    }

    val body = world.createBody(bodyDef)
    body.createFixture(fixtureDef)
    return bodyCircle(body, radius)

}

fun createChain(vertices: List<Vector2>, world: World): List<Vector2> {
    val bodyDef = BodyDef().apply {
        type = BodyDef.BodyType.StaticBody
        position.set(Vector2.ZERO.toFloat())
    }
    val body = world.createBody(bodyDef)
    val chain = ChainShape()
    val vertices = vertices.map { it.toFloat() }

    chain.createLoop(vertices.toTypedArray())
    body.createFixture(chain, 1.0f)

    return vertices.map { it.toDouble() }
}