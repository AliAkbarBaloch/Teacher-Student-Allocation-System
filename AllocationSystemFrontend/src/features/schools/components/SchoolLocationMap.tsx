import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { UNIVERSITY_OF_PASSAU_COORDS } from "@/lib/utils/geoUtils";
import { MAP_DEFAULT_ZOOM, MAP_DEFAULT_ZOOM_WITHOUT_COORDS, MAP_HEIGHT } from "@/lib/constants/app";

// Create custom icon for school location with beautiful modern styling
const createSchoolIcon = () => {
  return L.divIcon({
    className: "custom-school-marker",
    html: `
      <div class="marker-container">
        <div class="marker-pulse"></div>
        <div class="marker-pin marker-pin-school">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="white"/>
          </svg>
        </div>
      </div>
      <style>
        .marker-container {
          position: relative;
          width: 56px;
          height: 56px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .marker-pulse {
          position: absolute;
          width: 56px;
          height: 56px;
          border-radius: 50%;
          background: linear-gradient(135deg, rgba(248, 151, 28, 0.4) 0%, rgba(255, 184, 77, 0.4) 100%);
          animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
          z-index: 0;
        }
        .marker-pin {
          position: relative;
          width: 48px;
          height: 48px;
          border-radius: 50% 50% 50% 0;
          transform: rotate(-45deg);
          display: flex;
          align-items: center;
          justify-content: center;
          border: 4px solid white;
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3), 0 0 0 3px rgba(248, 151, 28, 0.15), inset 0 2px 4px rgba(255, 255, 255, 0.3);
          z-index: 1;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          cursor: pointer;
        }
        .marker-pin-school {
          background: linear-gradient(135deg, #F8971C 0%, #FFB84D 50%, #FFA726 100%);
        }
        .marker-pin svg {
          transform: rotate(45deg);
          filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
        }
        .marker-pin:hover {
          transform: rotate(-45deg) scale(1.15);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4), 0 0 0 4px rgba(248, 151, 28, 0.25), inset 0 2px 4px rgba(255, 255, 255, 0.4);
        }
        @keyframes pulse {
          0%, 100% {
            transform: scale(1);
            opacity: 0.8;
          }
          50% {
            transform: scale(1.3);
            opacity: 0;
          }
        }
      </style>
    `,
    iconSize: [56, 56],
    iconAnchor: [28, 56],
    popupAnchor: [0, -56],
  });
};

// Create custom icon for university location with beautiful modern styling
const createUniversityIcon = () => {
  return L.divIcon({
    className: "custom-university-marker",
    html: `
      <div class="marker-container">
        <div class="marker-pulse-university"></div>
        <div class="marker-pin marker-pin-university">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="white"/>
            <path d="M2 17L12 22L22 17V12L12 17L2 12V17Z" fill="white"/>
          </svg>
        </div>
      </div>
      <style>
        .marker-container {
          position: relative;
          width: 56px;
          height: 56px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .marker-pulse-university {
          position: absolute;
          width: 56px;
          height: 56px;
          border-radius: 50%;
          background: linear-gradient(135deg, rgba(79, 70, 229, 0.4) 0%, rgba(124, 58, 237, 0.4) 100%);
          animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
          z-index: 0;
        }
        .marker-pin {
          position: relative;
          width: 48px;
          height: 48px;
          border-radius: 50% 50% 50% 0;
          transform: rotate(-45deg);
          display: flex;
          align-items: center;
          justify-content: center;
          border: 4px solid white;
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3), 0 0 0 3px rgba(79, 70, 229, 0.15), inset 0 2px 4px rgba(255, 255, 255, 0.3);
          z-index: 1;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          cursor: pointer;
        }
        .marker-pin-university {
          background: linear-gradient(135deg, #4F46E5 0%, #6366F1 50%, #7C3AED 100%);
        }
        .marker-pin svg {
          transform: rotate(45deg);
          filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
        }
        .marker-pin:hover {
          transform: rotate(-45deg) scale(1.15);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4), 0 0 0 4px rgba(79, 70, 229, 0.25), inset 0 2px 4px rgba(255, 255, 255, 0.4);
        }
        @keyframes pulse {
          0%, 100% {
            transform: scale(1);
            opacity: 0.8;
          }
          50% {
            transform: scale(1.3);
            opacity: 0;
          }
        }
      </style>
    `,
    iconSize: [56, 56],
    iconAnchor: [28, 56],
    popupAnchor: [0, -56],
  });
};

// Memoize icons outside component to prevent recreation on every render
const SCHOOL_ICON = createSchoolIcon();
const UNIVERSITY_ICON = createUniversityIcon();

interface SchoolLocationMapProps {
  latitude: number | string | null | undefined;
  longitude: number | string | null | undefined;
  schoolName?: string;
  className?: string;
}

/**
 * Component that updates the map view when coordinates change
 */
function MapUpdater({ latitude, longitude }: { latitude: number; longitude: number }) {
  const map = useMap();

  useEffect(() => {
    if (latitude && longitude && !isNaN(latitude) && !isNaN(longitude)) {
      map.setView([latitude, longitude], 13);
    }
  }, [latitude, longitude, map]);

  return null;
}

/**
 * Beautiful map component that displays a school location
 * Shows both the school location and University of Passau as reference points
 */
export function SchoolLocationMap({
  latitude,
  longitude,
  schoolName = "School Location",
  className = "",
}: SchoolLocationMapProps) {
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  const lat = typeof latitude === "string" ? parseFloat(latitude) : latitude;
  const lon = typeof longitude === "string" ? parseFloat(longitude) : longitude;

  const hasValidCoordinates = lat !== null && lat !== undefined && lon !== null && lon !== undefined && !isNaN(lat) && !isNaN(lon);

  // Default center to University of Passau if no valid coordinates
  const centerLat = hasValidCoordinates ? (lat as number) : UNIVERSITY_OF_PASSAU_COORDS.latitude;
  const centerLon = hasValidCoordinates ? (lon as number) : UNIVERSITY_OF_PASSAU_COORDS.longitude;

  if (!isMounted) {
    return (
      <div
        className={`flex items-center justify-center rounded-lg border border-dashed border-muted-foreground/25 bg-muted/10 p-8 ${className}`}
      >
        <p className="text-sm text-muted-foreground">Loading map...</p>
      </div>
    );
  }

  if (!hasValidCoordinates) {
    return (
      <div
        className={`flex items-center justify-center rounded-lg border border-dashed border-muted-foreground/25 bg-muted/10 p-8 ${className}`}
      >
        <p className="text-sm text-muted-foreground">
          Enter latitude and longitude to see the location on the map
        </p>
      </div>
    );
  }

  // At this point, we know lat and lon are valid numbers
  const validLat = lat as number;
  const validLon = lon as number;

  return (
    <>
      <style>{`
        .custom-popup .leaflet-popup-content-wrapper {
          padding: 0;
          border-radius: 12px;
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15), 0 2px 8px rgba(0, 0, 0, 0.1);
          overflow: hidden;
        }
        .custom-popup .leaflet-popup-content {
          margin: 0;
          min-width: 200px;
        }
        .custom-popup .leaflet-popup-tip {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        .popup-content {
          display: flex;
          flex-direction: column;
        }
        .popup-header {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 16px 16px 12px;
          color: white;
        }
        .popup-header-school {
          background: linear-gradient(135deg, #F8971C 0%, #FFB84D 100%);
        }
        .popup-header-university {
          background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
        }
        .popup-header svg {
          flex-shrink: 0;
          filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
        }
        .popup-title {
          font-size: 15px;
          font-weight: 600;
          margin: 0;
          line-height: 1.4;
          text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        }
        .popup-body {
          padding: 12px 16px 16px;
          background: white;
        }
        .popup-info-item {
          display: flex;
          flex-direction: column;
          gap: 4px;
        }
        .popup-label {
          font-size: 11px;
          font-weight: 600;
          color: #6b7280;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }
        .popup-value {
          font-size: 12px;
          font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
          color: #1f2937;
          background: #f9fafb;
          padding: 6px 10px;
          border-radius: 6px;
          border: 1px solid #e5e7eb;
          word-break: break-all;
        }
      `}</style>
      <div className={`rounded-lg border overflow-hidden ${className}`}>
        <MapContainer
          center={[centerLat, centerLon]}
          zoom={hasValidCoordinates ? MAP_DEFAULT_ZOOM : MAP_DEFAULT_ZOOM_WITHOUT_COORDS}
          style={{ height: MAP_HEIGHT, width: "100%" }}
          className="z-0"
        >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapUpdater latitude={validLat} longitude={validLon} />
        {/* School location marker */}
        <Marker position={[validLat, validLon]} icon={SCHOOL_ICON}>
          <Popup
            className="custom-popup"
            closeButton={true}
            autoPan={true}
            autoPanPadding={[50, 50]}
          >
            <div className="popup-content">
              <div className="popup-header popup-header-school">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="currentColor"/>
                </svg>
                <h3 className="popup-title">{schoolName || "School Location"}</h3>
              </div>
              <div className="popup-body">
                <div className="popup-info-item">
                  <span className="popup-label">Coordinates</span>
                  <span className="popup-value">{validLat.toFixed(6)}, {validLon.toFixed(6)}</span>
                </div>
              </div>
            </div>
          </Popup>
        </Marker>
        {/* University of Passau marker */}
        <Marker
          position={[UNIVERSITY_OF_PASSAU_COORDS.latitude, UNIVERSITY_OF_PASSAU_COORDS.longitude]}
          icon={UNIVERSITY_ICON}
        >
          <Popup
            className="custom-popup"
            closeButton={true}
            autoPan={true}
            autoPanPadding={[50, 50]}
          >
            <div className="popup-content">
              <div className="popup-header popup-header-university">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor"/>
                  <path d="M2 17L12 22L22 17V12L12 17L2 12V17Z" fill="currentColor"/>
                </svg>
                <h3 className="popup-title">University of Passau</h3>
              </div>
              <div className="popup-body">
                <div className="popup-info-item">
                  <span className="popup-label">Coordinates</span>
                  <span className="popup-value">
                    {UNIVERSITY_OF_PASSAU_COORDS.latitude.toFixed(6)},{" "}
                    {UNIVERSITY_OF_PASSAU_COORDS.longitude.toFixed(6)}
                  </span>
                </div>
              </div>
            </div>
          </Popup>
        </Marker>
      </MapContainer>
    </div>
    </>
  );
}
