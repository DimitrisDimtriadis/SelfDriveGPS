package gr.teicm.informatics.selfdrivegps.Utilities;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import gr.teicm.informatics.selfdrivegps.Controller.Controller;
import gr.teicm.informatics.selfdrivegps.FieldMath.NavigationPolylineAlgorithm;

import static java.lang.Float.MAX_VALUE;
import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class FieldFunctionsUtilities {
    private static Controller controller = new Controller();

    //Function to know if user is in polygon or not
    public static boolean PointIsInRegion(LatLng mLatLng, ArrayList<LatLng> thePath) {
        int crossings = 0;
        int count = thePath.size();
        LatLng a,b;

        for (int i=0; i < count; i++) { // for each edge
            a = thePath.get(i);
            int j = i + 1;
            if (j >= count) {
                j = 0;
            }
            b = thePath.get(j);
            if (RayCrossesSegment(mLatLng, a, b)) {
                crossings++;
            }
        }
        return (crossings % 2 == 1); // odd number of crossings?
    }

    //Ray algorithm to calculate area of polygon
    private static boolean RayCrossesSegment(LatLng point, LatLng a, LatLng b) {
        double px = point.longitude;
        double py = point.latitude;
        double ax = a.longitude;
        double ay = a.latitude;
        double bx = b.longitude;
        double by = b.latitude;
        if (ay > by)
        {
            ax = b.longitude;
            ay = b.latitude;
            bx = a.longitude;
            by = a.latitude;
        }
        // alter longitude to cater for 180 degree crossings
        if (px < 0) { px += 360; }
        if (ax < 0) { ax += 360; }
        if (bx < 0) { bx += 360; }

        if (py == ay || py == by) py += 0.00000001;
        if ((py > by || py < ay) || (px > Math.max(ax, bx))) return false;
        if (px < Math.min(ax, bx)) return true;

        double red = (ax != bx) ? ((by - ay) / (bx - ax)) : MAX_VALUE;
        double blue = (ax != px) ? ((py - ay) / (px - ax)) : MAX_VALUE;
        return (blue >= red);
    }

    //Algorithm which find the point (x meter away with accordingly bearing)
    public static LatLng calculateLocationFewMetersAhead(LatLng sourceLatLng, double mBearing, double mMeter){
        double distRadians = mMeter / (6372797.6); // earth radius in meters

        double lat1 = sourceLatLng.latitude * PI / 180;
        double lon1 = sourceLatLng.longitude * PI / 180;

        double lat2 = asin(sin(lat1) * cos(distRadians) + cos(lat1) * sin(distRadians) * cos(Math.toRadians(mBearing)));
        double lon2 = lon1 + atan2(sin(Math.toRadians(mBearing)) * sin(distRadians) * cos(lat1), cos(distRadians) - sin(lat1) * sin(lat2));

        double nLat = lat2 * 180 / PI;
        double nLon = lon2 * 180 / PI;
        return new LatLng(nLat, nLon);
    }

    //Take 2 points and find their bearing
    public static double calculateBearing(LatLng startLatLng, LatLng endLatLng){
        double startLat = startLatLng.latitude;
        double startLng = startLatLng.longitude;
        double endLat = endLatLng.latitude;
        double endLng = endLatLng.longitude;

        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff= Math.toRadians(endLng - startLng);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    //Check every arrayList if has place to add more points to fill the space
    public static void checkIfEveryPolylineMatchToTheEndOfBorder(ArrayList<LatLng> baseArrayListToAddExtraLatLng, double bearingOfPolyline){

        LatLng pointOfmArrayToCheck;
        pointOfmArrayToCheck = FieldFunctionsUtilities.calculateLocationFewMetersAhead(baseArrayListToAddExtraLatLng.get(baseArrayListToAddExtraLatLng.size()-1), bearingOfPolyline, 1); // 1m ahead of main line to fill spaces;
        while(FieldFunctionsUtilities.PointIsInRegion(pointOfmArrayToCheck, controller.getArrayListForField())){
            baseArrayListToAddExtraLatLng.add(pointOfmArrayToCheck); // Add to arrayList the new LatLng
            pointOfmArrayToCheck = FieldFunctionsUtilities.calculateLocationFewMetersAhead(pointOfmArrayToCheck, bearingOfPolyline, 1); // 1m ahead of main line to fill spaces
        }

        pointOfmArrayToCheck = FieldFunctionsUtilities.calculateLocationFewMetersAhead(baseArrayListToAddExtraLatLng.get(0), bearingOfPolyline+180, 1); // 1m ahead of main line to fill spaces
        while(FieldFunctionsUtilities.PointIsInRegion(pointOfmArrayToCheck, controller.getArrayListForField())){ //Function that do the reverse of above to fill spots below of line
            baseArrayListToAddExtraLatLng.add(0, pointOfmArrayToCheck); // Add to arrayList the new LatLng
            pointOfmArrayToCheck = FieldFunctionsUtilities.calculateLocationFewMetersAhead(pointOfmArrayToCheck, bearingOfPolyline+180, 1); // 1m ahead of main line to fill spaces
        }
    }
    //Take 1 ArrayList<LatLng> and finds if the point(size/2) belongs to field (#2) (It used on MultiPolyline Algorithm)
    public static boolean checkIfNextPolylineIsInsideOfField(ArrayList<LatLng> givenArrayListToCheck, double mBearing, double mMeter){
        boolean resultForCheckingIfPointIsInsideOfField = false;

        //Check every spot (x meter away with specific bearing) and if found at least one inside (stops) and return true
        for(int i=0; i<givenArrayListToCheck.size(); i++){
            LatLng tempSpot = FieldFunctionsUtilities.calculateLocationFewMetersAhead(givenArrayListToCheck.get(i), mBearing, mMeter);
            //TODO: Need more test before place the next multiPolyline
            if(FieldFunctionsUtilities.PointIsInRegion(tempSpot, controller.getArrayListForField())){
                resultForCheckingIfPointIsInsideOfField = true;
                i = givenArrayListToCheck.size();
            }
        }
        return resultForCheckingIfPointIsInsideOfField;
    }

    //Function to generate invisible parallel
    // Under right orientation the parallelLine.get(0) is the right Line and the parallelLine.get(1) is the left Line. So the 1 is for left and 0 for right
    public static void generateTempLineAndNavigationAlgorithm(GoogleMap googleMap, LatLng mCurrentLocation, double bearingOfUser){
        Boolean focusOnSpecificSecondLine = false; //initialize variable in which i will save (if user is inside of main Line)

        if(checkingInWhichPolylineUserEntered(mCurrentLocation)){ //Check if user is in anyone of MultiPolyLines

            //Get the main ArrayList to generate the 2 polyLines
            ArrayList<ArrayList<LatLng>> parPolyline = NavigationPolylineAlgorithm.algorithmForCreatingTwoInvisibleParallelPolylineForNavigation(controller.getArrayListForLineToFocus());

            for(ArrayList<LatLng> temp : parPolyline){ //It separate them

                //Add the border to check if cross one of two
                focusOnSpecificSecondLine = ApproachPolylineUtilities.bdccGeoDistanceCheckWithRadius(temp, mCurrentLocation, Controller.MAIN_RADIUS_TO_RECOGNISE_SECONDARY_POLYLINE);

                if(focusOnSpecificSecondLine) { //Check if user cross the border of one of 2 lines show it!
                    controller.setSecondLineThatActivated(temp); //If found one save which one is it
                    break; //Stop "for" loop  so it doesn't change the value with one that isn't correct
                }
            }

            if(focusOnSpecificSecondLine){ //After pass "for" loop, check if user cross on of parallel line
                MapsUtilities.placePolylineParallel(controller.getSecondLineThatActivated(), googleMap); // If he pass it, draw only this specific polyLines on map

                double bearingOfMainLine = controller.getBearingForNavigationPurpose();

                registerStatusForNavigationBar(isOrientationReversed(bearingOfUser, bearingOfMainLine), parPolyline.indexOf(controller.getSecondLineThatActivated()));
            }else{ // If he doesn't cron none of two, re-draw the map
                MapsUtilities.recreateFieldWithMultiPolyline(googleMap);
                registerStatusForNavigationBar(false, 2);
            }
        }else{
            MapsUtilities.recreateFieldWithMultiPolyline(googleMap); // Secure that after move out of the specific ArrayList, the map while come to his normal
            registerStatusForNavigationBar(false, -1);
        }
    }
    //Recognize in which polyline you are (It is inside of above function)
    private static Boolean checkingInWhichPolylineUserEntered(LatLng currentLocation){
        Boolean focusOnSpecificMainLine = false;

        for(ArrayList<LatLng> focusedPolyline : controller.getArrayListOfMultipliedPolyLines()){ // Set polyLines to test it about which one is the user

            focusOnSpecificMainLine = ApproachPolylineUtilities.bdccGeoDistanceCheckWithRadius(focusedPolyline, currentLocation, Controller.MAIN_RADIUS_TO_RECOGNISE_MAIN_POLYLINE);

            if(focusOnSpecificMainLine){

                //Set it on controller to get then number of index to show it on MapsActivity
                controller.setArrayListForLineToFocus(focusedPolyline);

                // Get the bearing to use it next for the navigation Algorithm
                controller.setBearingForNavigationPurpose(calculateBearing(focusedPolyline.get(0), focusedPolyline.get(focusedPolyline.size()-1)));
                break;
            }
        }
        return focusOnSpecificMainLine;
    }
    // Function to simplify the generateTempLineAndNavigationAlgorithm because it has start become spaghetti
    private static Boolean isOrientationReversed(double bearingOfUser, double bearingOfLine) {
        //if returns false, we don't change anything. If Function return false we must reverse left and right
        double startLimit = bearingOfLine - 90;
        double endLimit = bearingOfLine + 90;
        if(startLimit<0){
            return !((0 <= bearingOfUser && bearingOfUser <= endLimit) || (startLimit+360 <= bearingOfUser && bearingOfUser < 360));
        }else if(endLimit>=360){
            return !((0 <= bearingOfUser && bearingOfUser <= endLimit-360) || (startLimit <= bearingOfUser && bearingOfUser < 360));
        }else{
            return !(startLimit <= bearingOfUser && bearingOfUser <= endLimit);
        }

    }
    private static void registerStatusForNavigationBar(Boolean isOrientationReversed, int numberWhichCorrespondsOnIndexOfLine){
        if(isOrientationReversed && numberWhichCorrespondsOnIndexOfLine == 0){
            controller.setLocationOfUserForNavigationBar(Controller.LEFT);
        }else if(!isOrientationReversed && numberWhichCorrespondsOnIndexOfLine == 0){
            controller.setLocationOfUserForNavigationBar(Controller.RIGHT);

        }else if(isOrientationReversed && numberWhichCorrespondsOnIndexOfLine == 1){
            controller.setLocationOfUserForNavigationBar(Controller.RIGHT);
        }else if(!isOrientationReversed && numberWhichCorrespondsOnIndexOfLine == 1) {
            controller.setLocationOfUserForNavigationBar(Controller.LEFT);

        }else if(!isOrientationReversed && numberWhichCorrespondsOnIndexOfLine == 2){
            controller.setLocationOfUserForNavigationBar(Controller.MID);
        }else if(!isOrientationReversed && numberWhichCorrespondsOnIndexOfLine == -1){
            controller.setLocationOfUserForNavigationBar(Controller.NONE);
        }

    }

    //Algorithm to make touch line feature available
    public static Boolean algorithmForTouchMainLine(ArrayList<LatLng> mArray, Context context){

        if(mArray.size() == 2){ // Check if user set 2 markers

            Location firstTempLocation = new Location(LocationManager.GPS_PROVIDER);
            Location secondTempLocation = new Location(LocationManager.GPS_PROVIDER);
            ArrayList<LatLng> mArrayListForMainLineBeforeCheck = new ArrayList<>(); //ArrayList to save spots of created main line
            ArrayList<LatLng> mArrayListForMainLineAfterCheck = new ArrayList<>(); //ArrayList to save spots of created main line
            LatLng tempSpotForFillMainLine = mArray.get(0); //Temp spot in every loop
            int distanceThatAlgorithmCovered = 0, distanceOfTwoSpots ;

            double mBearing = calculateBearing(mArray.get(0), mArray.get(1)); // To Calculate bearing of 2 spots

            firstTempLocation.setLatitude(mArray.get(0).latitude);     //Initialize with values the 2 locations
            firstTempLocation.setLongitude(mArray.get(0).longitude);   //So I could take the distance
            secondTempLocation.setLatitude(mArray.get(1).latitude);
            secondTempLocation.setLongitude(mArray.get(1).longitude);

            distanceOfTwoSpots = (int) firstTempLocation.distanceTo(secondTempLocation); //Calculate the distance of 2 spots

            do{ // Loop to calculate the main line based on
                tempSpotForFillMainLine = calculateLocationFewMetersAhead(tempSpotForFillMainLine, mBearing, distanceThatAlgorithmCovered);
                mArrayListForMainLineBeforeCheck.add(tempSpotForFillMainLine);

                distanceThatAlgorithmCovered = distanceThatAlgorithmCovered + 3;
            }while (distanceThatAlgorithmCovered < distanceOfTwoSpots);

            //Take the arrayList which create from touchLine an check every spot individual
            for(int i=0; i<mArrayListForMainLineBeforeCheck.size(); i++){
                //Check which one point is inside of field. If return true, it place the spot on ArrayList to create the main Line
                if(FieldFunctionsUtilities.PointIsInRegion(mArrayListForMainLineBeforeCheck.get(i), controller.getArrayListForField())){
                    mArrayListForMainLineAfterCheck.add(mArrayListForMainLineBeforeCheck.get(i));
                }
            }

            //Check if arrayList for main line is empty
            if(mArrayListForMainLineAfterCheck.size() > 1 ){
                //Set the controller for main line so i could continue the  flow of app action
                controller.setArrayListForLine(mArrayListForMainLineAfterCheck);
                //Reset the front-end map without markers
                MapsUtilities.recreateFieldWithMultiPolyline(controller.getGoogleMap());
                return true;
            }else{
                Toast.makeText(context, "The line is out of field border !", Toast.LENGTH_SHORT).show();
                controller.setProgramStatus(Controller.MODE_0_TOUCH_LISTENER); //Re-set mode to 'Create Line' to show the right buttons on toolbar to
                return false;
            }
        }else{ // If user hasn't set 2 marker show the message below
            Toast.makeText(context, "You must add 2 point to create the main line !", Toast.LENGTH_SHORT).show();
            controller.setProgramStatus(Controller.MODE_0_TOUCH_LISTENER); //Re-set mode to 'Create Line' to show the right buttons on toolbar to
            return false;
        }
    }


    public static void calculationOfWidthForCoverRoute(LatLng mLocation){
        float requiredWidth = controller.getMeterOfRange(); // in meters
        Projection projection = controller.getGoogleMap().getProjection();
        Point pointCenter = projection.toScreenLocation(mLocation); // point in pixels
        LatLng neighbor = projection.fromScreenLocation(new Point(pointCenter.x + 1000, pointCenter.y));
        float[] distance = new float[1];
        Location.distanceBetween(mLocation.latitude, mLocation.longitude, neighbor.latitude, neighbor.longitude, distance); // return distance in meters
        float pixelsWidth = requiredWidth / (distance[0] / 1000f); // 10 meters converted to pixels
        controller.setValueForCoverPolyline(pixelsWidth); //Set it on Controller
    }

    //Every scenario of changed antenna
    public static LatLng algorithmForDifferentCenterPoint(LatLng mCurrentLocation, Float mBearingOfUser){
        LatLng mNewCenterPointForNavigationPurpose;
        Float changeAntennaBearing; //Every case has different bearing
        Integer mMeter; //Meters away of choose distance away of center

        if(controller.getAntennaFront() != 0){ //Front
            changeAntennaBearing = mBearingOfUser + 0;
            mMeter = controller.getAntennaFront();
        }else if(controller.getAntennaBack() != 0){ //Back
            changeAntennaBearing = mBearingOfUser + 180;
            mMeter = controller.getAntennaBack();
        }else if(controller.getAntennaLeft() !=0){ //Left
            changeAntennaBearing = mBearingOfUser + 270;
            mMeter = controller.getAntennaLeft();
        }else if(controller.getAntennaRight() !=0){ //Right
            changeAntennaBearing = mBearingOfUser + 90;
            mMeter = controller.getAntennaRight();
        }else{
            changeAntennaBearing = mBearingOfUser;
            mMeter = 0;
        }
        //Base on values from above calculate new position of antenna center
        mNewCenterPointForNavigationPurpose = FieldFunctionsUtilities.calculateLocationFewMetersAhead(mCurrentLocation, changeAntennaBearing, mMeter);
        return mNewCenterPointForNavigationPurpose;
    }
}
