package us.dragonma.backgrounds.update

internal class Ratio(x: Int, y: Int) {
    private val x: Int
    private val y: Int

    init {
        val gcd = gcd(x, y)
        this.x = x / gcd
        this.y = y / gcd
    }

    constructor(r: Pair<Int, Int>) : this(r.first, r.second)
    constructor(r: String) : this(r.split("[/:x]".toRegex(), 2).map { it.toInt() }.toPair())

    override fun toString(): String {
        return "$x:$y"
    }

    fun prettyPrint(): String {
        return "${this.toDouble().toString().take(10)} ($this)"
    }

    private fun toDouble(): Double {
        return x.toDouble() / y.toDouble()
    }

    private fun gcd(vararg ts: Int): Int {
        var gcd = ts.minOrNull() ?: 1
        while (gcd > 1) {
            if (ts.all { it % gcd == 0 }) {
                break
            }
            gcd--
        }

        return gcd
    }
}

private fun <T> List<T>.toPair(): Pair<T, T> {
    if (this.size != 2) {
        throw IllegalArgumentException("List is not of length 2!")
    }
    return Pair(this[0], this[1])
}
