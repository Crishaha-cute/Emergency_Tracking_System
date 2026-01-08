package com.ets.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import com.ets.model.Emergency;
import java.util.List;

public class MapView {

    /**
     * Creates a single marker map view
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param markerTitle Title for the marker popup
     * @return VBox containing the map
     */
    public static VBox createSingleMarkerMap(double latitude, double longitude, String markerTitle) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // Map controls
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(5));

        Label coordLabel = new Label(String.format("📍 Location: %.6f, %.6f", latitude, longitude));
        coordLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

        controls.getChildren().add(coordLabel);

        // WebView for map
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        String mapHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html, body { 
                        margin: 0; 
                        padding: 0; 
                        width: 100%%;
                        height: 100%%;
                        font-family: Arial, sans-serif;
                        overflow: hidden;
                    }
                    #map { 
                        position: absolute;
                        top: 0; left: 0; right: 0; bottom: 0;
                        width: 100%%; 
                        height: 100%%; 
                    }
                    .custom-popup {
                        font-size: 14px;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    // Initialize map
                    var map = L.map('map', { zoomControl: true }).setView([%f, %f], 15);
                    
                    // Add OpenStreetMap tiles
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors',
                        maxZoom: 19,
                        minZoom: 3,
                        detectRetina: true
                    }).addTo(map);
                    
                    // Custom red icon for emergency
                    var redIcon = L.icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowSize: [41, 41]
                    });
                    
                    // Add marker
                    var marker = L.marker([%f, %f], {icon: redIcon}).addTo(map);
                    marker.bindPopup("<div class='custom-popup'>🚨 %s</div>").openPopup();
                    
                    // Add circle to show area
                    var circle = L.circle([%f, %f], {
                        color: 'red',
                        fillColor: '#f03',
                        fillOpacity: 0.2,
                        radius: 200
                    }).addTo(map);

                    // Keep map sharp on resize
                    window.addEventListener('resize', function() {
                        map.invalidateSize();
                    });
                </script>
            </body>
            </html>
            """.formatted(latitude, longitude, latitude, longitude, markerTitle, latitude, longitude);

        webEngine.loadContent(mapHtml);
        webView.setContextMenuEnabled(false);
        webView.setPrefHeight(600);
        webView.setPrefWidth(Double.MAX_VALUE);
        VBox.setVgrow(webView, Priority.ALWAYS);

        container.getChildren().addAll(controls, webView);
        return container;
    }

    /**
     * Creates a multi-marker map view for multiple emergencies
     * @param emergencies List of emergencies to display
     * @return VBox containing the map with all emergency markers
     */
    public static VBox createMultiMarkerMap(List<Emergency> emergencies) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // Map info
        HBox info = new HBox(10);
        info.setPadding(new Insets(5));

        Label countLabel = new Label("📍 Showing " + emergencies.size() + " emergency location(s)");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        info.getChildren().add(countLabel);

        // Build markers JavaScript
        StringBuilder markersScript = new StringBuilder();
        double centerLat = 9.0820; // Default center (Tandag, Caraga)
        double centerLng = 126.3061;

        if (!emergencies.isEmpty()) {
            // Calculate center from emergencies
            centerLat = emergencies.stream().mapToDouble(Emergency::getLatitude).average().orElse(9.0820);
            centerLng = emergencies.stream().mapToDouble(Emergency::getLongitude).average().orElse(126.3061);

            // Build markers
            for (int i = 0; i < emergencies.size(); i++) {
                Emergency em = emergencies.get(i);
                String color = getMarkerColor(em.getEmergencyType().toString());
                String icon = getEmergencyIcon(em.getEmergencyType().toString());

                markersScript.append(String.format("""
                    var marker%d = L.marker([%f, %f], {icon: %sIcon}).addTo(map);
                    marker%d.bindPopup(`
                        <div style='min-width: 200px;'>
                            <h3 style='margin: 5px 0; color: #e74c3c;'>%s %s Emergency</h3>
                            <p style='margin: 5px 0;'><strong>User:</strong> %s</p>
                            <p style='margin: 5px 0;'><strong>Status:</strong> %s</p>
                            <p style='margin: 5px 0;'><strong>Location:</strong> %s</p>
                            <p style='margin: 5px 0; font-size: 11px; color: #7f8c8d;'>%s</p>
                        </div>
                    `);
                    
                    """,
                        i, em.getLatitude(), em.getLongitude(), color,
                        i, icon, em.getEmergencyType(),
                        em.getUserName() != null ? em.getUserName() : "Unknown",
                        em.getStatus(),
                        em.getLocationAddress() != null ? em.getLocationAddress() : "Unknown",
                        em.getCreatedAt()
                ));
            }
        }

        // WebView for map
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        String mapHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html, body { 
                        margin: 0; 
                        padding: 0; 
                        width: 100%%;
                        height: 100%%;
                        font-family: Arial, sans-serif;
                        overflow: hidden;
                    }
                    #map { 
                        position: absolute;
                        top: 0; left: 0; right: 0; bottom: 0;
                        width: 100%%; 
                        height: 100%%; 
                    }
                    .leaflet-popup-content {
                        margin: 10px;
                    }
                    .leaflet-popup-content h3 {
                        margin-top: 0;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    // Initialize map
                    var map = L.map('map', { zoomControl: true }).setView([%f, %f], 13);
                    
                    // Add OpenStreetMap tiles
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors',
                        maxZoom: 19,
                        minZoom: 3,
                        detectRetina: true
                    }).addTo(map);
                    
                    // Define colored icons for different emergency types
                    var redIcon = L.icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowSize: [41, 41]
                    });
                    
                    var orangeIcon = L.icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-orange.png',
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowSize: [41, 41]
                    });
                    
                    var blueIcon = L.icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowSize: [41, 41]
                    });
                    
                    var yellowIcon = L.icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-yellow.png',
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowSize: [41, 41]
                    });
                    
                    // Add markers
                    %s
                    
                    // Add legend
                    var legend = L.control({position: 'bottomright'});
                    legend.onAdd = function (map) {
                        var div = L.DomUtil.create('div', 'info legend');
                        div.style.backgroundColor = 'white';
                        div.style.padding = '10px';
                        div.style.border = '2px solid #ccc';
                        div.style.borderRadius = '5px';
                        div.style.fontSize = '12px';
                        div.innerHTML = `
                            <strong>Emergency Types:</strong><br>
                            <span style='color: red;'>●</span> Medical<br>
                            <span style='color: orange;'>●</span> Fire<br>
                            <span style='color: blue;'>●</span> Crime<br>
                            <span style='color: #FFD700;'>●</span> Accident
                        `;
                        return div;
                    };
                    legend.addTo(map);

                    // Keep map sharp on resize
                    window.addEventListener('resize', function() {
                        map.invalidateSize();
                    });
                </script>
            </body>
            </html>
            """.formatted(centerLat, centerLng, markersScript.toString());

        webEngine.loadContent(mapHtml);
        webView.setContextMenuEnabled(false);
        webView.setPrefHeight(600);
        webView.setPrefWidth(Double.MAX_VALUE);
        VBox.setVgrow(webView, Priority.ALWAYS);

        container.getChildren().addAll(info, webView);
        return container;
    }

    /**
     * Get marker color based on emergency type
     */
    private static String getMarkerColor(String emergencyType) {
        return switch (emergencyType) {
            case "MEDICAL" -> "red";
            case "FIRE" -> "orange";
            case "CRIME" -> "blue";
            case "ACCIDENT" -> "yellow";
            default -> "red";
        };
    }

    /**
     * Get emoji icon for emergency type
     */
    private static String getEmergencyIcon(String emergencyType) {
        return switch (emergencyType) {
            case "MEDICAL" -> "🚑";
            case "FIRE" -> "🔥";
            case "CRIME" -> "🚔";
            case "ACCIDENT" -> "⚠️";
            default -> "🚨";
        };
    }

    /**
     * Creates a map centered on default location (Tandag, Caraga)
     * @return VBox containing the default map
     */
    public static VBox createDefaultMap() {
        return createSingleMarkerMap(9.0820, 126.3061, "Your Location - Tandag, Caraga");
    }
}