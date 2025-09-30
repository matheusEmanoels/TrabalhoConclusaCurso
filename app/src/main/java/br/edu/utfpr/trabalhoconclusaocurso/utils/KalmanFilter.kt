package br.edu.utfpr.trabalhoconclusaocurso.utils

class KalmanFilter(
    private val processNoise: Double = 1.0,   // quanto menor, mais suave (mas pode atrasar)
    private val measurementNoise: Double = 4.0 // incerteza do GPS (média entre 3m a 10m)
) {
    private var lat: Double? = null
    private var lon: Double? = null
    private var variance: Double = -1.0

    fun process(measuredLat: Double, measuredLon: Double): Pair<Double, Double> {
        if (lat == null || lon == null) {
            lat = measuredLat
            lon = measuredLon
            variance = 1.0
        } else {
            // Atualiza estimativa
            variance += processNoise

            // Kalman gain
            val k = variance / (variance + measurementNoise)

            // Ajusta latitude/longitude
            lat = lat!! + k * (measuredLat - lat!!)
            lon = lon!! + k * (measuredLon - lon!!)

            // Atualiza variância
            variance *= (1 - k)
        }
        return Pair(lat!!, lon!!)
    }
}