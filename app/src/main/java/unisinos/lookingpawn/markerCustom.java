package unisinos.lookingpawn;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

public class markerCustom {

    private String id;
    private Marker marker;
    private Circle circle;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public markerCustom(String id, Marker marker, Circle circle) {

        this.id = id;
        this.marker = marker;
        this.circle = circle;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

}
