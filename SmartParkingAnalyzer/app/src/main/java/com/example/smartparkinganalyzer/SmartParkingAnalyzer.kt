package com.example.smartparkinganalyzer

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParkingAnalyzer

data class Vehicle(
    val plate: String,
    val type: String,
    val hours: Int,
    val electric: Boolean?,
    val vip: Boolean
)

sealed class ParkingResult {
    data class Paid(val fee: Int) : ParkingResult()
    data class Free(val reason: String) : ParkingResult()
    data class Violation(val reason: String) : ParkingResult()
}

@ParkingAnalyzer
fun analyze(vehicle: Vehicle): ParkingResult {
    return when {
        vehicle.hours <= 0 ->
            ParkingResult.Violation("invalid duration")

        vehicle.hours > 8 ->
            ParkingResult.Violation("overtime parking")

        vehicle.vip ->
            ParkingResult.Free("VIP privilege")

        vehicle.electric == true && vehicle.hours <= 2 ->
            ParkingResult.Free("electric promotion")

        else -> {
            val rate = when (vehicle.type.uppercase()) {
                "CAR" -> 3
                "TRUCK" -> 5
                "BUS" -> 6
                "BIKE" -> 2
                "VAN" -> 4
                else -> 4
            }
            ParkingResult.Paid(rate * vehicle.hours)
        }
    }
}

fun runAnalyzer() {
    val vehicles = listOf(
        Vehicle("ABC123", "CAR", 3, false, false),
        Vehicle("XYZ999", "TRUCK", 5, false, false),
        Vehicle("ELEC42", "CAR", 2, true, true),
        Vehicle("BUS777", "BUS", 10, false, false),
        Vehicle("BIKE11", "BIKE", 1, null, false),
        Vehicle("GREEN8", "CAR", 2, true, false),
        Vehicle("VIP001", "VAN", 4, null, true)
    )

    println("=== Smart Parking Report ===")

    val results = vehicles.map { vehicle ->
        vehicle to analyze(vehicle)
    }

    results.forEach { (vehicle, result) ->
        val message = when (result) {
            is ParkingResult.Paid ->
                "${vehicle.plate} (${vehicle.type}) paid $${result.fee}"

            is ParkingResult.Free ->
                "${vehicle.plate} (${vehicle.type}) free parking: ${result.reason}"

            is ParkingResult.Violation ->
                "${vehicle.plate} (${vehicle.type}) violation: ${result.reason}"
        }
        println(message)
    }

    println("\n=== Statistics ===")

    val totalRevenue = results.sumOf { (_, result) ->
        when (result) {
            is ParkingResult.Paid -> result.fee
            else -> 0
        }
    }

    val violations = results.count { (_, result) ->
        result is ParkingResult.Violation
    }

    val vehiclesByType = vehicles
        .groupBy { it.type.uppercase() }
        .mapValues { it.value.size }

    val longStay = vehicles
        .filter { it.hours > 4 }
        .map { it.plate }

    val outcomeStats = results
        .groupBy { (_, result) ->
            when (result) {
                is ParkingResult.Paid -> "Paid"
                is ParkingResult.Free -> "Free"
                is ParkingResult.Violation -> "Violation"
            }
        }
        .mapValues { it.value.size }

    println("Total revenue: $$totalRevenue")
    println("Violations: $violations")
    println("Vehicles per type: $vehiclesByType")
    println("Long stays (>4 hrs): $longStay")
    println("Outcome distribution: $outcomeStats")
}