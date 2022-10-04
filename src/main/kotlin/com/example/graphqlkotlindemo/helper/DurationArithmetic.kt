package com.example.graphqlkotlindemo.helper

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

fun Collection<Duration>.min() = this.minOf { it.inWholeNanoseconds }.nanoseconds

fun Collection<Duration>.max() = this.maxOf { it.inWholeNanoseconds }.nanoseconds

fun Collection<Duration>.mean() = (this.sumOf { it.toDouble(DurationUnit.NANOSECONDS) } / this.size).nanoseconds

fun Collection<Duration>.std(): Duration {
    val mean = this.mean()
    val standardDeviationNanos = this.sumOf {
        (it - mean).toDouble(DurationUnit.NANOSECONDS).pow(2.0)
    }

    return sqrt(standardDeviationNanos / this.size).nanoseconds
}
