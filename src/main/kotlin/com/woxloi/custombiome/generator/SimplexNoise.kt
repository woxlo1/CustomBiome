package com.woxloi.custombiome.generator

/**
 * Simplex Noise 実装（Stefan Gustavson アルゴリズムに基づく）。
 * 地形生成のノイズとして使用する。
 * シードを渡すことで再現性のある地形を生成できる。
 */
class SimplexNoise(seed: Long = 0L) {

    private val perm = IntArray(512)

    init {
        val rng = java.util.Random(seed)
        val base = IntArray(256) { it }.also { arr ->
            for (i in 255 downTo 1) {
                val j = rng.nextInt(i + 1)
                val tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp
            }
        }
        for (i in 0..511) perm[i] = base[i and 255]
    }

    // ----------------------------------------------------------------
    // 2D Simplex Noise
    // ----------------------------------------------------------------

    fun noise2D(xin: Double, yin: Double): Double {
        val F2 = 0.5 * (Math.sqrt(3.0) - 1.0)
        val G2 = (3.0 - Math.sqrt(3.0)) / 6.0

        val s = (xin + yin) * F2
        val i = fastFloor(xin + s)
        val j = fastFloor(yin + s)
        val t = (i + j) * G2
        val x0 = xin - (i - t)
        val y0 = yin - (j - t)

        val i1: Int; val j1: Int
        if (x0 > y0) { i1 = 1; j1 = 0 } else { i1 = 0; j1 = 1 }

        val x1 = x0 - i1 + G2
        val y1 = y0 - j1 + G2
        val x2 = x0 - 1.0 + 2.0 * G2
        val y2 = y0 - 1.0 + 2.0 * G2

        val ii = i and 255
        val jj = j and 255
        val gi0 = perm[ii + perm[jj]] % 12
        val gi1 = perm[ii + i1 + perm[jj + j1]] % 12
        val gi2 = perm[ii + 1 + perm[jj + 1]] % 12

        var n0 = 0.0; var n1 = 0.0; var n2 = 0.0
        var t0 = 0.5 - x0 * x0 - y0 * y0
        if (t0 >= 0) { t0 *= t0; n0 = t0 * t0 * dot(GRAD3[gi0], x0, y0) }
        var t1 = 0.5 - x1 * x1 - y1 * y1
        if (t1 >= 0) { t1 *= t1; n1 = t1 * t1 * dot(GRAD3[gi1], x1, y1) }
        var t2 = 0.5 - x2 * x2 - y2 * y2
        if (t2 >= 0) { t2 *= t2; n2 = t2 * t2 * dot(GRAD3[gi2], x2, y2) }
        return 70.0 * (n0 + n1 + n2)
    }

    // ----------------------------------------------------------------
    // オクターブ合成（fBm: fractional Brownian motion）
    // ----------------------------------------------------------------

    /**
     * 複数オクターブを合成してより自然な地形ノイズを生成する。
     * @param x, y       サンプル座標
     * @param octaves    オクターブ数（多いほど詳細）
     * @param persistence 各オクターブの振幅倍率（0.0～1.0）
     * @param lacunarity  各オクターブの周波数倍率（通常2.0）
     * @param scale      全体スケール
     * @return -1.0 ～ 1.0 の値
     */
    fun octaveNoise2D(
        x: Double,
        y: Double,
        octaves: Int = 6,
        persistence: Double = 0.5,
        lacunarity: Double = 2.0,
        scale: Double = 1.0
    ): Double {
        var total = 0.0
        var frequency = scale
        var amplitude = 1.0
        var maxValue = 0.0

        for (i in 0 until octaves) {
            total += noise2D(x * frequency, y * frequency) * amplitude
            maxValue += amplitude
            amplitude *= persistence
            frequency *= lacunarity
        }
        return total / maxValue
    }

    // ----------------------------------------------------------------
    // ヘルパー
    // ----------------------------------------------------------------

    private fun fastFloor(x: Double): Int = if (x > 0) x.toInt() else x.toInt() - 1

    private fun dot(g: IntArray, x: Double, y: Double): Double = g[0] * x + g[1] * y

    companion object {
        private val GRAD3 = arrayOf(
            intArrayOf(1,1,0), intArrayOf(-1,1,0), intArrayOf(1,-1,0), intArrayOf(-1,-1,0),
            intArrayOf(1,0,1), intArrayOf(-1,0,1), intArrayOf(1,0,-1), intArrayOf(-1,0,-1),
            intArrayOf(0,1,1), intArrayOf(0,-1,1), intArrayOf(0,1,-1), intArrayOf(0,-1,-1)
        )
    }
}
