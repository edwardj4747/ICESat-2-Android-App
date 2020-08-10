package gov.nasa.gsfc.icesat2.icesat_2.ui.search

interface ISearchFragmentCallback {
    fun searchButtonPressed(lat: Double, long: Double, radius: Double, calledFromSelectOnMap: Boolean, time: Long = -1L, pastResults: Boolean = false, futureResults: Boolean = true)

    fun useCurrentLocationButtonPressed(simpleSearch: Boolean)

    fun selectOnMapButtonPressed()

    fun trackButtonPressed()
}