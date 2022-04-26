import Constants.Companion.accumulator
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.palette.PaletteStudio
import org.openrndr.math.Vector2


class Constants {
    companion object {
        const val contentScale = 1

        // Physics
        var accumulator = 0f
        var TIME_STEP = 1 / 240f
        var VELOCITY_ITERATIONS = 1
        var POSITION_ITERATIONS = 1

        // chemicals
        val palette = PaletteStudio()
        var chemicals = listOf(
            "Arsenic",
            "Benzophenones",
            "Mycotoxins",
            "Cadmium",
            "Flame retardants",
            "Chemical mixtures",
            "Phthalates",
            "Per-/poly-fluorinated compounds",
            "Lead",
            "Aprotic solvents",
            "Mercury",
            "Diisocyanates and Anilines family",
            "Pesticides",
            "Bisphenols",
            "Chromium VI",
            "Acrylamide",
            "Polycyclic Aromatic Hydrocarbons",
            "Chemicals of emerging concern"
        )
        var couples = listOf<Pair<String, ColorRGBa>>()


        fun loadColors() {
            palette.loadExternal("data/hbm-palette.json")
            couples = chemicals zip palette.colors
        }
    }

}


class bodyCircle(var body:Body, var radius : Double)


fun doPhysicsStep(deltaTime: Float, world: World) {

    val frameTime = Math.min(deltaTime, 0.25f)
    accumulator += frameTime
    while (accumulator >= Constants.TIME_STEP) {
        world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS)
        accumulator -= Constants.TIME_STEP
    }

}

fun resetAccumulator() {
    accumulator = 0f
}

fun com.badlogic.gdx.math.Vector2.toDouble(): Vector2 {
    return Vector2(x.toDouble(), y.toDouble())
}

fun Vector2.toFloat(): com.badlogic.gdx.math.Vector2 {
    return com.badlogic.gdx.math.Vector2(x.toFloat(), y.toFloat())
}