import geoLocationManager from '@ohos.geoLocationManager';
import common from '@ohos.app.ability.common';
import abilityAccessCtrl, { Permissions } from '@ohos.abilityAccessCtrl';

// Define a callback type for receiving location info
type LocationCallback = (locationData: geoLocationManager.Location) => void;
// Define a callback type for receiving satellite info
type SatelliteStatusCallback = (status: geoLocationManager.SatelliteStatusInfo) => void;

export class LocationManager {
  private static instance: LocationManager;
  private locationRequest: geoLocationManager.LocationRequest;
  private locationCallback: LocationCallback | null = null;
  private abilityContext: common.UIAbilityContext;
  private totalDistance: number = 0.0; // Total distance, unit: meters
  private previousLocation?: geoLocationManager.Location; // Previous location point

  // Private constructor, ensures instances are only created through getInstance
  private constructor(context: common.UIAbilityContext) {
    this.abilityContext = context;
    // Configure the location request parameters
    this.locationRequest = {
      priority: geoLocationManager.LocationRequestPriority.ACCURACY, // Prefer high accuracy
      timeInterval: 5000, // 5-second update interval
      distanceInterval: 3, // Update once every 3 meters of movement
    };
  }

  // Public accessor for the singleton
  public static getInstance(context: common.UIAbilityContext): LocationManager {
    if (!LocationManager.instance) {
      LocationManager.instance = new LocationManager(context);
    }
    // Reset the distance and previous point each time the instance is fetched, to start new tracking
    LocationManager.instance.resetDistance();
    return LocationManager.instance;
  }

  /**
   * Check and request the location permission
   */
  private async checkAndRequestPermissions(): Promise<boolean> {
    const permissions: Permissions[] = ['ohos.permission.LOCATION', 'ohos.permission.APPROXIMATELY_LOCATION'];
    const atManager = abilityAccessCtrl.createAtManager();

    try {
      // Check the precise location permission
      let grantStatus =
        await atManager.checkAccessToken(this.abilityContext.applicationInfo.accessTokenId, permissions[0]);
      if (grantStatus === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
        console.log('Location permission granted');
        return true;
      }

      // If not granted, request the permission from the user
      const result = await atManager.requestPermissionsFromUser(this.abilityContext, permissions);
      if (result.authResults[0] === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
        console.log('Location permission request succeeded');
        return true;
      } else {
        console.error('Location permission request failed');
        return false;
      }
    } catch (err) {
      console.error('Permission check or request error:', JSON.stringify(err));
      return false;
    }
  }

  /**
   * Start real-time location
   * @param callback Callback for receiving location info
   */
  public async startLocationUpdates(callback: LocationCallback): Promise<void> {
    if (!callback) {
      console.error('No callback provided');
      return;
    }

    const hasPermission = await this.checkAndRequestPermissions();
    if (!hasPermission) {
      console.error('Cannot start location, required permission missing');
      return;
    }

    const isLocationEnabled = await geoLocationManager.isLocationEnabled();
    if (!isLocationEnabled) {
      console.warn('Location service is off, please enable it in system settings');
      // The user could be guided to settings here
      return;
    }

    this.locationCallback = callback;
    try {
      // Register the location update listener
      geoLocationManager.on('locationChange', this.locationRequest, this.onLocationChange);
      console.log('Real-time location started');
      this.startSatelliteStatusUpdates(this.onSatelliteStatusChange)
    } catch (err) {
      console.error('Failed to start location:', JSON.stringify(err));
    }
  }

  /**
   * Stop real-time location
   */
  public stopLocationUpdates(): void {
    if (this.onLocationChange) { // Check that the callback itself exists
      try {
        // Unregister the location update listener
        geoLocationManager.off('locationChange', this.onLocationChange);
        this.locationCallback = null; // Clear the externally provided callback
        this.stopSatelliteStatusUpdates(this.onSatelliteStatusChange)
        console.log('Real-time location stopped');
      } catch (err) {
        console.error('Failed to stop location:', JSON.stringify(err));
      }
    }
  }

  /**
   * Start listening to GNSS satellite status
   * @param callback Callback for receiving satellite status info
   */
  private startSatelliteStatusUpdates(callback: SatelliteStatusCallback): void {
    if (!callback) {
      console.error('No satellite status callback provided');
      return;
    }
    try {
      // Subscribe to the 'satelliteStatusChange' event
      geoLocationManager.on('satelliteStatusChange', callback);
      console.log('GNSS satellite status listening started');
    } catch (err) {
      console.error('Failed to start GNSS satellite status listening:', JSON.stringify(err));
    }
  }

  /**
   * Stop listening to GNSS satellite status
   * @param callback The callback previously used to subscribe
   */
  private stopSatelliteStatusUpdates(callback: SatelliteStatusCallback): void {
    if (!callback) {
      console.error('The original callback must be provided when stopping listening');
      return;
    }
    try {
      // Unsubscribe from the 'satelliteStatusChange' event
      geoLocationManager.off('satelliteStatusChange', callback);
      console.log('GNSS satellite status listening stopped');
    } catch (err) {
      console.error('Failed to stop GNSS satellite status listening:', JSON.stringify(err));
    }
  }

  /**
   * Get the accumulated total distance
   * @returns number Total distance (meters)
   */
  public getTotalDistance(): number {
    return this.totalDistance;
  }

  /**
   * Reset the distance calculator
   */
  private resetDistance(): void {
    this.totalDistance = 0.0;
    this.previousLocation = undefined;
    console.log('Distance calculator reset');
  }

  public gpsSignal(): number {
    return this.previousLocation?.accuracy ?? 0;
  }

  /**
   * Callback handler for location updates
   * (This is where the core distance logic lives)
   */
  private onLocationChange = (newLocation: geoLocationManager.Location) => {
    if (!newLocation) {
      return;
    }

    // --- Distance calculation logic ---
    // Check whether a previous location point exists
    if (this.previousLocation) {
      try {
        // [Core fix] Call our own Haversine calculation method
        const distance = this.calculateDistance(this.previousLocation, newLocation);

        // Add to the total distance
        this.totalDistance += distance;
        console.log(`Distance moved this time: ${distance.toFixed(2)} m, total distance: ${this.totalDistance.toFixed(2)} m`);
      } catch (err) {
        console.error('Error while calculating distance:', JSON.stringify(err));
      }
    }

    // Set the previous point to the current one, ready for the next calculation
    this.previousLocation = newLocation;
    // --- End of distance calculation logic ---

    // Call the externally provided callback to pass on the new location
    if (this.locationCallback) {
      this.locationCallback(newLocation);
      console.log(`New location: longitude=${newLocation.longitude}, latitude=${newLocation.latitude}, accuracy=${newLocation.accuracy} m`);
    }
  };
  // This is the callback that handles satellite signals
  private onSatelliteStatusChange = (status: geoLocationManager.SatelliteStatusInfo) => {
    // 'status' object is the signal info we need
    const totalSatellites = status.satellitesNumber;
    let infoText = `Visible satellites: ${totalSatellites}\n`;
    infoText += '--- Details ---\n';

    for (let i = 0; i < totalSatellites; i++) {
      const id = status.satelliteIds[i];
      // carrierToNoiseDensitys is the carrier-to-noise ratio, the key indicator of signal strength
      const cno = status.carrierToNoiseDensitys[i];
      const altitude = status.altitudes[i]; // Elevation angle
      const azimuth = status.azimuths[i]; // Azimuth

      infoText += `Satellite ID: ${id}, signal strength (C/N₀): ${cno.toFixed(2)} dB-Hz\n`;
    }

    console.info(infoText);
  }

  /**
   * Calculate the distance between two geographic coordinates using the Haversine formula (unit: meters)
   * @param loc1 The first location point
   * @param loc2 The second location point
   * @returns The distance between the two points (meters)
   */
  private calculateDistance(loc1: geoLocationManager.Location, loc2: geoLocationManager.Location): number {
    const R = 6371e3; // Mean Earth radius, unit: meters
    const lat1 = loc1.latitude * Math.PI / 180; // Convert degrees to radians
    const lat2 = loc2.latitude * Math.PI / 180;
    const deltaLat = (loc2.latitude - loc1.latitude) * Math.PI / 180;
    const deltaLon = (loc2.longitude - loc1.longitude) * Math.PI / 180;

    const a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
      Math.cos(lat1) * Math.cos(lat2) *
      Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    const distance = R * c; // Final distance
    return distance;
  }
}