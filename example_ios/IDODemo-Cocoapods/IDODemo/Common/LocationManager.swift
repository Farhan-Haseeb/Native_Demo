//
//  LocationManager.swift
//  Runner
//
//  Created by hc on 2024/12/19.
//

import Foundation
import CoreLocation
import UIKit

class LocationManager: NSObject, CLLocationManagerDelegate {
    
    private let locationManager = CLLocationManager()
    static let shared = LocationManager()
    
    
    private(set) var previousLocation: CLLocation?
    private var routeCoordinates: [CLLocationCoordinate2D] = []
    private(set) var totalDistance: CLLocationDistance = 0.0
    
    var locationUpdateBlock: ((_ locations: [CLLocation]) -> Void)?
    var locationErrorBlock: ((_ error: Error) -> Void)?
    var didChangeAuthorization: ((_ status: CLAuthorizationStatus) -> Void)?
    
    override init() {
        super.init()
        setupLocationManager()
    }
    
    private func setupLocationManager() {
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = kCLDistanceFilterNone
    }
    
    // MARK: - Permission methods
    
    /// Get the current permission status
    private func getAuthorizationStatus() -> CLAuthorizationStatus {
        if #available(iOS 14.0, *) {
            return locationManager.authorizationStatus
        } else {
            return CLLocationManager.authorizationStatus()
        }
    }
    
    /// Request location permission
    func requestLocationPermission() {
        guard CLLocationManager.locationServicesEnabled() else {
            showLocationServiceDisabledAlert()
            return
        }
        
        locationManager.requestWhenInUseAuthorization()
        // Or, as needed, use
        // locationManager.requestAlwaysAuthorization()
    }
    
    /// Check the location permission status
    func checkLocationAuthorization() -> Bool {
        let status = getAuthorizationStatus()
        
        switch status {
        case .notDetermined:
            requestLocationPermission()
            return false
            
        case .restricted, .denied:
            showGoToSettingsAlert()
            return false
            
        case .authorizedWhenInUse, .authorizedAlways:
            return true
            
        @unknown default:
            return false
        }
    }
    
    // MARK: - CLLocationManagerDelegate
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        switch status {
        case .authorizedWhenInUse, .authorizedAlways:
            startUpdatingLocation()
            
        case .denied, .restricted:
            stopUpdatingLocation()
            
        case .notDetermined:
            break
            
        @unknown default:
            break
        }
    }
    
    // iOS 14 and later use this delegate method
    @available(iOS 14.0, *)
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        switch status {
        case .authorizedWhenInUse, .authorizedAlways:
            startUpdatingLocation()
            
        case .denied, .restricted:
            stopUpdatingLocation()
            
        case .notDetermined:
            break
            
        @unknown default:
            break
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let newLocation = locations.last else { return }
        handleLocationUpdate(newLocation)
        guard let newLocation = locations.last else { return }
        
        // Calculate the distance
        if let previousLocation = previousLocation {
            let distance = newLocation.distance(from: previousLocation)
            totalDistance += distance
        }
        previousLocation = newLocation
        
        updateRoute(with: newLocation.coordinate)
        
        locationUpdateBlock?(locations)
    }
    
    private func updateRoute(with coordinate: CLLocationCoordinate2D) {
        routeCoordinates.append(coordinate)
        
        // Draw the polyline
//        let polyline = MKPolyline(coordinates: routeCoordinates, count: routeCoordinates.count)
//        mapView.addOverlay(polyline)
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        handleLocationError(error)
        locationErrorBlock?(error)
    }
    
    // MARK: - Helper methods
    
    func startUpdatingLocation() {
        locationManager.startUpdatingLocation()
    }
    
    func stopUpdatingLocation() {
        locationManager.stopUpdatingLocation()
        routeCoordinates.removeAll()
        totalDistance = 0
        previousLocation = nil
    }
    
    private func handleLocationUpdate(_ location: CLLocation) {
        print("Location update: \(location.coordinate.latitude), \(location.coordinate.longitude) signal:\(location.horizontalAccuracy)")
    }
    
    private func handleLocationError(_ error: Error) {
        //print("Location error: \(error.localizedDescription)")
    }
    
    // MARK: - Alert prompts
    
    private func showLocationServiceDisabledAlert() {
        let alert = UIAlertController(
            title: "Location services are off",
            message: "Please turn on location services in Settings",
            preferredStyle: .alert
        )
        
        let settingsAction = UIAlertAction(title: "Go to Settings", style: .default) { _ in
            // Jump to system Settings
            if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
                UIApplication.shared.open(settingsURL)
            }
        }
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel)
        
        alert.addAction(settingsAction)
        alert.addAction(cancelAction)
        
        // Get the topmost ViewController to present the alert
        if let topVC = getTopViewController() {
            topVC.present(alert, animated: true)
        }
    }
    
    private func showGoToSettingsAlert() {
        let alert = UIAlertController(
            title: "Location permission required",
            message: "Please allow access to your location in Settings",
            preferredStyle: .alert
        )
        
        let settingsAction = UIAlertAction(title: "Go to Settings", style: .default) { _ in
            if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
                UIApplication.shared.open(settingsURL)
            }
        }
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel)
        
        alert.addAction(settingsAction)
        alert.addAction(cancelAction)
        
        if let topVC = getTopViewController() {
            topVC.present(alert, animated: true)
        }
    }
    
    // Get the top ViewController
    private func getTopViewController() -> UIViewController? {
        let keyWindow = UIApplication.shared.windows.filter {$0.isKeyWindow}.first
        var topViewController = keyWindow?.rootViewController
        
        while let presentedViewController = topViewController?.presentedViewController {
            topViewController = presentedViewController
        }
        
        return topViewController
    }
}


extension CLLocation {
    
    func gpsSignal() -> Int {
        if (horizontalAccuracy > 0 && horizontalAccuracy <= 25) {
            return 0x01
        }
        return 0x00
    }
}
