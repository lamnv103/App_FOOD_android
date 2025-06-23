import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class LocationModel(
    var id: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var name: String = ""
) : Serializable {

    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90 degrees" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180 degrees" }
    }

    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}


