package com.example.kbtkedunglo.utilsclass

fun meterToKilometer(nilaiMeter:Float):String{
    val nilaiKilometer = nilaiMeter / 1000
    val nilaiBulat = Math.round(nilaiKilometer * 10.0) / 10.0
    val nilaiM = Math.round(nilaiMeter * 10.0) / 10.0
    if(nilaiBulat > 1){
        return "${nilaiBulat} Km"
    }else{
        return "${nilaiM} Meter"
    }
}
fun formatDurationMedal(detik: Int): String {
    val jam = detik / 3600
    val sisaJam = detik % 3600
    val menit = sisaJam / 60
    val sisaDetik = sisaJam % 60

    return when {
        jam > 0 -> String.format("%dj%02dm%02ds", jam, menit, sisaDetik)
        menit > 0 -> String.format("%dm%02ds", menit, sisaDetik)
        else -> String.format("%02ds", sisaDetik)
    }
}