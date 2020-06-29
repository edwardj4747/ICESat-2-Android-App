package gov.nasa.gsfc.icesat2.icesat_2

interface IDownloadDataErrorCallback {
    fun searchTimedOut()
    fun noResultsFound()

    fun showSearchFeedback(reason: String)
}