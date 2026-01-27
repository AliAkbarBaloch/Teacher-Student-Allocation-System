/**
 * Geographic utility functions for distance calculations and coordinate handling
 */

/**
 * University of Passau coordinates (center point for distance calculations)
 */
export const UNIVERSITY_OF_PASSAU_COORDS = {
  latitude: 48.56755,
  longitude: 13.45211,
} as const;

/**
 * Calculate the distance between two coordinates using the Haversine formula.
 * This calculates the great-circle distance (straight-line distance over the Earth's surface),
 * also known as "as the crow flies" distance.
 * 
 * Note: This is NOT road/driving distance. Road distance is typically longer and requires
 * a mapping API (e.g., Google Maps, OpenRouteService) to calculate.
 * 
 * @param lat1 Latitude of first point
 * @param lon1 Longitude of first point
 * @param lat2 Latitude of second point
 * @param lon2 Longitude of second point
 * @returns Distance in kilometers (straight-line distance)
 */
export function calculateDistance(
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number {
  const earthRadiusKm = 6371; // Earth's radius in kilometers
  const dLat = toRadians(lat2 - lat1);
  const dLon = toRadians(lon2 - lon1);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) *
      Math.cos(toRadians(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = earthRadiusKm * c;

  return Math.round(distance * 100) / 100; // Round to 2 decimal places
}

/**
 * Calculate distance from a point to University of Passau
 * @param latitude Latitude of the point
 * @param longitude Longitude of the point
 * @returns Distance in kilometers, or null if coordinates are invalid
 */
export function calculateDistanceFromUniversity(
  latitude: number | string | null | undefined,
  longitude: number | string | null | undefined
): number | null {
  if (latitude === null || latitude === undefined || latitude === "") {
    return null;
  }
  if (longitude === null || longitude === undefined || longitude === "") {
    return null;
  }

  const lat = typeof latitude === "string" ? parseFloat(latitude) : latitude;
  const lon = typeof longitude === "string" ? parseFloat(longitude) : longitude;

  if (isNaN(lat) || isNaN(lon)) {
    return null;
  }
  if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
    return null;
  }

  return calculateDistance(
    lat,
    lon,
    UNIVERSITY_OF_PASSAU_COORDS.latitude,
    UNIVERSITY_OF_PASSAU_COORDS.longitude
  );
}

/**
 * Convert degrees to radians
 */
function toRadians(degrees: number): number {
  return degrees * (Math.PI / 180);
}

/**
 * Validate if coordinates are valid
 */
export function isValidCoordinates(
  latitude: number | string | null | undefined,
  longitude: number | string | null | undefined
): boolean {
  if (latitude === null || latitude === undefined || latitude === "") {
    return false;
  }
  if (longitude === null || longitude === undefined || longitude === "") {
    return false;
  }

  const lat = typeof latitude === "string" ? parseFloat(latitude) : latitude;
  const lon = typeof longitude === "string" ? parseFloat(longitude) : longitude;

  if (isNaN(lat) || isNaN(lon)) {
    return false;
  }
  if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
    return false;
  }

  return true;
}

