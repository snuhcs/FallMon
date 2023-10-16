package com.example.fallmon.presentation.math

import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


object FallMonMath {
    fun standardDeviation(array: Array<Float>, average: Float): Float {
        var variance: Float = 0.0f
        for(f in array) variance += (f - average).pow(2)
        return sqrt(variance / array.size)
    }

    fun rootMeanSquare(array: Array<Float>): Float {
        var sumSquare: Float = 0.0f
        for(f in array) sumSquare += f*f
        return sqrt(sumSquare / array.size)
    }

    fun maxAmplitude(array: Array<Float>): Float {
        var maxAmp: Float = 0.0f
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
    fun percentile_1(array: Array<Float>): Float {
        val sortedArray = array.sorted()
        return (sortedArray[18] + sortedArray[19]) / 2
    }

    // Simplified assume that WINDOW_SIZE = 75
    fun percentile_3(array: Array<Float>): Float {
        val sortedArray = array.sorted()
        return (sortedArray[55] + sortedArray[56]) / 2
    }

    fun nzc(array: Array<Float>): Float {
        var signArray = Array<Float>(array.size) { 0.0f }
        for(i: Int in 0 until array.size)
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

    fun frequencySpectrum(array: Array<Float>): Array<Float> {
        // FFT needs DoubleArray Type
        val doubleArray = DoubleArray(array.size) { array[it].toDouble() }

        // padding 0 for make array size to power of 2
        var size = 1
        while (size < array.size) size *= 2
        val paddedDoubleArray = DoubleArray(size)

        System.arraycopy(doubleArray, 0, paddedDoubleArray, 0, array.size)

        // Create FFT instance
        val transformer = FastFourierTransformer(DftNormalization.STANDARD)

        // Perform FFT
        val fft = transformer.transform(paddedDoubleArray, TransformType.FORWARD)

        // Calculate frequency spectrum
        val freqSpec = fft
            .drop(1)    //  exclude index 0
            .map { it.abs() }

        // resize the result and return
        return freqSpec.map { it.toFloat() }.toTypedArray().sliceArray(0..array.size - 2)
    }
}