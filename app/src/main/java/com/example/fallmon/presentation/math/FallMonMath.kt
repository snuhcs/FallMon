package com.example.fallmon.presentation.math

import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt



object FallMonMath {
    fun standardDeviation(array: Array<Float>, average: Float): Float {
        var variance = 0.0f
        for(f in array) variance += (f - average).pow(2)
        return sqrt(variance / array.size)
    }

    fun rootMeanSquare(array: Array<Float>): Float {
        var sumSquare = 0.0f
        for(f in array) sumSquare += f*f
        return sqrt(sumSquare / array.size)
    }

    fun maxAmplitude(array: Array<Float>): Float {
        var maxAmp = 0.0f
        for(f in array) maxAmp = max(maxAmp, abs(f))
        return maxAmp
    }

    fun minAmplitude(array: Array<Float>): Float {
        var minAmp: Float = Float.MAX_VALUE
        for(f in array) minAmp = min(minAmp, abs(f))
        return minAmp
    }

    fun median(array: Array<Float>): Float {
        val sortedArray = array.sorted()
        return sortedArray[array.size / 2]
    }

    // Simplified assume that WINDOW_SIZE = 75
    fun percentile1(array: Array<Float>): Float {
        val sortedArray = array.sorted()
        return (sortedArray[18] + sortedArray[19]) / 2
    }

    // Simplified assume that WINDOW_SIZE = 75
    fun percentile3(array: Array<Float>): Float {
        val sortedArray = array.sorted()
        return (sortedArray[55] + sortedArray[56]) / 2
    }

    fun nzc(array: Array<Float>): Float {
        val signArray = Array(array.size) { 0.0f }
        for(i: Int in array.indices)
            if(array[i] > 0) signArray[i] = 1.0f
            else if(array[i] < 0) signArray[i] = -1.0f
        // else signArray[i] = 0.0f // don't have to modify

        var sum = 0.0f
        for(i: Int in 1 until array.size)
            sum += abs(signArray[i] - signArray[i-1])
        return sum
    }

    fun skewness(array: Array<Float>, average: Float, standardDeviation: Float): Float {
        val sumCubedDiff = array.sumOf { (it - average).toDouble().pow(3.0) }
        val skewness = (sumCubedDiff / array.size) / standardDeviation.toDouble().pow(3.0)
        return skewness.toFloat()
    }

    fun kurtosis(array: Array<Float>, average: Float, standardDeviation: Float): Float {
        val sumFourthPowerDiff = array.sumOf { (it - average).toDouble().pow(4.0) }
        val skewness = (sumFourthPowerDiff / array.size) / standardDeviation.toDouble().pow(4.0) - 3.0
        return skewness.toFloat()
    }

    fun fft(input: DoubleArray): DoubleArray {
        val fft = DoubleFFT_1D(input.size.toLong() * 2)

        // make double -> ( real double, imag double(=0) )
        val result = DoubleArray(input.size * 2) { 0.0 }
        for (i in input.indices) result[i * 2] = input[i]

        fft.realForward(result)
        return result
    }

    fun frequencySpectrum(array: Array<Float>): Array<Float> {
        // FFT needs DoubleArray Type
        val doubleArray = DoubleArray(array.size) { array[it].toDouble() }

        // perform FFT
        val fftResultDouble = fft(doubleArray)
        val fftResult = Array(array.size * 2) { fftResultDouble[it].toFloat() }

        // Calculate frequency spectrum
        val freqSpec = fftResult
            .drop(2)    //  exclude index 0

        val result = Array(array.size - 1) { 0.0f }
        for(i in 0 until freqSpec.size/2) {
            result[i] = sqrt(freqSpec[i*2].pow(2) + freqSpec[i*2+1].pow(2))
        }

        // resize the result and return
        return result
    }

    // below two functions have error in calculation
    /*fun entropy(array: Array<Float>): Float {
        val counts = array.groupBy { it }.mapValues { it.value.size }
        val probabilities = counts.values.map { it / array.size }
        return -probabilities.sumOf { it * log2(it.toDouble()) }.toFloat()
    }*/

    fun entropy(array: Array<Float>): Float {

        val sum = array.sum()
        if(sum == 0.0f) return 0.0f

        return -array.sumOf { (it / sum) * ln((it / sum).toDouble()) }.toFloat()
    }

    fun energy(array: Array<Float>): Float {
        var squareSum = 0.0f
        for(f in array) squareSum += f*f
        return squareSum / array.size
    }
}