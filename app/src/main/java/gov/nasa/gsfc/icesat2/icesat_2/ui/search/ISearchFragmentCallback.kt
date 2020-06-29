package gov.nasa.gsfc.icesat2.icesat_2.ui.search

interface ISearchFragmentCallback {
    fun searchButtonPressed(lat: Double, long: Double, radius: Double, calledFromSelectOnMap: Boolean)

    fun useCurrentLocationButtonPressed(simpleSearch: Boolean)

    fun selectOnMapButtonPressed()

    fun trackButtonPressed()
}