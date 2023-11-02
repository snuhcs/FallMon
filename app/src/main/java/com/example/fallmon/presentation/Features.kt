package com.example.fallmon.presentation

import com.example.fallmon.presentation.math.FallMonMath

typealias FeatureExtractor = (Array<Float>) -> Float
object Feature {
    // features used in classification model
    // redundant calculations(average, standardDeviation) exist
    val average:FeatureExtractor = {v -> v.average().toFloat()}
    val standardDeviation:FeatureExtractor = {v -> FallMonMath.standardDeviation(v, average(v))}
    val rootMinSquare:FeatureExtractor = {v -> FallMonMath.rootMeanSquare(v)}
    val maxAmplitude:FeatureExtractor = {v -> FallMonMath.maxAmplitude(v)}
    val minAmplitude:FeatureExtractor = {v -> FallMonMath.minAmplitude(v)}
    val median:FeatureExtractor = {v -> FallMonMath.median(v)}
    val nzc:FeatureExtractor = {v -> FallMonMath.nzc(v)}
    val skewness:FeatureExtractor = {v -> FallMonMath.skewness(v, average(v), standardDeviation(v))}
    val kurtosis:FeatureExtractor = {v -> FallMonMath.kurtosis(v, average(v), standardDeviation(v))}
    val percentile1:FeatureExtractor = {v -> FallMonMath.percentile1(v)}
    val percentile3:FeatureExtractor = {v -> FallMonMath.percentile3(v)}
    val freqAverage:FeatureExtractor = {v -> FallMonMath.frequencySpectrum(v).average().toFloat()}
    val freqMedian:FeatureExtractor = {v -> FallMonMath.median(FallMonMath.frequencySpectrum(v))}
    val freqEntropy:FeatureExtractor = {v -> FallMonMath.entropy(FallMonMath.frequencySpectrum(v))}
    val freqEnergy:FeatureExtractor = {v -> FallMonMath.energy(FallMonMath.frequencySpectrum(v))}
}