package com.example.paul.all_sensor_logger.bt.devices;

import android.bluetooth.BluetoothDevice;

public class Location {
	
	private final BluetoothDevice mDevice;
	
	//GPS fields
	private long mTime;  //UTC time in milliseconds 
	private double mLatitude; //in degree
	private double mLongitude; //in degree
	private double mAltitude = 0.0; //in m
	private boolean mHasAltitude = false;
	private float mSpeed = 0.0f; //in m/s
	private boolean mHasSpeed = false;
	private float mBearing = -1; //the angle to north in degree, from 0 ~ 359
	private boolean mHasBearing = false;
	
	// Cache the inputs and outputs of computeDistanceAndBearing
    // so calls to distanceTo() and bearingTo() can share work
    private double mLat1 = 0.0;
    private double mLon1 = 0.0;
    private double mLat2 = 0.0;
    private double mLon2 = 0.0;
    private float mDistance = 0.0f;
    private float mInitialBearing = 0.0f;
    // Scratchpad
    private final float[] mResults = new float[2];
	
	public Location(Long time, double lat, double lng, BluetoothDevice device) {
		this.mTime = time;
		this.mLatitude = lat;
		this.mLongitude = lng;
		this.mDevice = device;
	}
	
	public BluetoothDevice getBTDevice() {
		return this.mDevice;
	}
	
	
	public long getTime() {
        return mTime;
    }
	
	public void setTime(long time) {
        mTime = time;
    }
	
	public double getLatitude() {
        return mLatitude;
    }
	
	public void setLatitude(double latitude) {
        mLatitude = latitude;
    }
	
	public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public boolean hasAltitude() {
        return mHasAltitude;
    }

    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double altitude) {
        mAltitude = altitude;
        mHasAltitude = true;
    }

    public void removeAltitude() {
        mAltitude = 0.0f;
        mHasAltitude = false;
    }

    
    public boolean hasSpeed() {
        return mHasSpeed;
    }
    
    public float getSpeed() {
        return mSpeed;
    }
    
    public void setSpeed(float speed) {
        mSpeed = speed;
        mHasSpeed = true;
    }

    public void removeSpeed() {
        mSpeed = 0.0f;
        mHasSpeed = false;
    }

 
    public boolean hasBearing() {
        return mHasBearing;
    }

    public float getBearing() {
        return mBearing;
    }

    public void setBearing(float bearing) {
        while (bearing < 0.0f) {
            bearing += 360.0f;
        }
        while (bearing >= 360.0f) {
            bearing -= 360.0f;
        }
        mBearing = bearing;
        mHasBearing = true;
    }

    public void removeBearing() {
        mBearing = 0.0f;
        mHasBearing = false;
    }
    
	
	
	public static void distanceBetween(double startLatitude, double startLongitude,
	        double endLatitude, double endLongitude, float[] results) {
	        if (results == null || results.length < 1) {
	            throw new IllegalArgumentException("results is null or has length < 1");
	        }
	        computeDistanceAndBearing(startLatitude, startLongitude,
	            endLatitude, endLongitude, results);
	}
	
	
	public float distanceTo(Location dest) {
        // See if we already have the result
        synchronized (mResults) {
            if (mLatitude != mLat1 || mLongitude != mLon1 ||
                dest.mLatitude != mLat2 || dest.mLongitude != mLon2) {
                computeDistanceAndBearing(mLatitude, mLongitude,
                    dest.mLatitude, dest.mLongitude, mResults);
                mLat1 = mLatitude;
                mLon1 = mLongitude;
                mLat2 = dest.mLatitude;
                mLon2 = dest.mLongitude;
                mDistance = mResults[0];
                mInitialBearing = mResults[1];
            }
            return mDistance;
        }
    }
	
	
	public float bearingTo(Location dest) {
        synchronized (mResults) {
            // See if we already have the result
            if (mLatitude != mLat1 || mLongitude != mLon1 ||
                            dest.mLatitude != mLat2 || dest.mLongitude != mLon2) {
                computeDistanceAndBearing(mLatitude, mLongitude,
                    dest.mLatitude, dest.mLongitude, mResults);
                mLat1 = mLatitude;
                mLon1 = mLongitude;
                mLat2 = dest.mLatitude;
                mLon2 = dest.mLongitude;
                mDistance = mResults[0];
                mInitialBearing = mResults[1];
            }
            return mInitialBearing;
        }
    }
	
	
	private static void computeDistanceAndBearing(double lat1, double lon1,
	        double lat2, double lon2, float[] results) {
	        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
	        // using the "Inverse Formula" (section 4)

	        int MAXITERS = 20;
	        // Convert lat/long to radians
	        lat1 *= Math.PI / 180.0;
	        lat2 *= Math.PI / 180.0;
	        lon1 *= Math.PI / 180.0;
	        lon2 *= Math.PI / 180.0;

	        double a = 6378137.0; // WGS84 major axis
	        double b = 6356752.3142; // WGS84 semi-major axis
	        double f = (a - b) / a;
	        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

	        double L = lon2 - lon1;
	        double A = 0.0;
	        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
	        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

	        double cosU1 = Math.cos(U1);
	        double cosU2 = Math.cos(U2);
	        double sinU1 = Math.sin(U1);
	        double sinU2 = Math.sin(U2);
	        double cosU1cosU2 = cosU1 * cosU2;
	        double sinU1sinU2 = sinU1 * sinU2;

	        double sigma = 0.0;
	        double deltaSigma = 0.0;
	        double cosSqAlpha = 0.0;
	        double cos2SM = 0.0;
	        double cosSigma = 0.0;
	        double sinSigma = 0.0;
	        double cosLambda = 0.0;
	        double sinLambda = 0.0;

	        double lambda = L; // initial guess
	        for (int iter = 0; iter < MAXITERS; iter++) {
	            double lambdaOrig = lambda;
	            cosLambda = Math.cos(lambda);
	            sinLambda = Math.sin(lambda);
	            double t1 = cosU2 * sinLambda;
	            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
	            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
	            sinSigma = Math.sqrt(sinSqSigma);
	            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
	            sigma = Math.atan2(sinSigma, cosSigma); // (16)
	            double sinAlpha = (sinSigma == 0) ? 0.0 :
	                cosU1cosU2 * sinLambda / sinSigma; // (17)
	            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
	            cos2SM = (cosSqAlpha == 0) ? 0.0 :
	                cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

	            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
	            A = 1 + (uSquared / 16384.0) * // (3)
	                (4096.0 + uSquared *
	                 (-768 + uSquared * (320.0 - 175.0 * uSquared)));
	            double B = (uSquared / 1024.0) * // (4)
	                (256.0 + uSquared *
	                 (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
	            double C = (f / 16.0) *
	                cosSqAlpha *
	                (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
	            double cos2SMSq = cos2SM * cos2SM;
	            deltaSigma = B * sinSigma * // (6)
	                (cos2SM + (B / 4.0) *
	                 (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
	                  (B / 6.0) * cos2SM *
	                  (-3.0 + 4.0 * sinSigma * sinSigma) *
	                  (-3.0 + 4.0 * cos2SMSq)));

	            lambda = L +
	                (1.0 - C) * f * sinAlpha *
	                (sigma + C * sinSigma *
	                 (cos2SM + C * cosSigma *
	                  (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

	            double delta = (lambda - lambdaOrig) / lambda;
	            if (Math.abs(delta) < 1.0e-12) {
	                break;
	            }
	        }

	        float distance = (float) (b * A * (sigma - deltaSigma));
	        results[0] = distance;
	        if (results.length > 1) {
	            float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
	                cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
	            initialBearing *= 180.0 / Math.PI;
	            results[1] = initialBearing;
	            if (results.length > 2) {
	                float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
	                    -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
	                finalBearing *= 180.0 / Math.PI;
	                results[2] = finalBearing;
	            }
	        }
	    }
}
