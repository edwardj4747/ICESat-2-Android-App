package gov.nasa.gsfc.icesat2.icesat_2

enum class SearchError {
    TIMED_OUT, NO_RESULTS
}

interface IDownloadDataErrorCallback {
    fun addErrorToSet(searchError: SearchError)
}