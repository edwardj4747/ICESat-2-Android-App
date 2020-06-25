package gov.nasa.gsfc.icesat2.icesat_2

interface ILaunchSingleMarkerMap {
    fun navigateToSingleMarkerMap(lat: Double, long: Double, title: String, dateObjectTime: Long)
}