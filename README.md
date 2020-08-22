# ICESat-2-Android-App

This app was developed in Summer 2020 for the Crysopheric Sciences Laboratory at NASA Goddard Space Flight Center. It allows a user to determine when the [ICESat-2](https://icesat-2.gsfc.nasa.gov/) satellite will be near a particular area using the satellite's Refernce Ground Tracks. Practical applications extend to ground validation campaigns and to civilian scientists working with the Globe Earth app.

This app was developed using *Kotlin* and the *Android Framework*. It implements the *Google Maps API* and downloads data from an *AWS Elastic Beanstalk Environment*. A *python* script was used to convert KML files into csv data which was then upload to an *AWS S3 Bucket*. A *Node.js* script runs on the AWS environment to process the search queries and returns appropriate satellite data for the query. Basic *SQL* is used to store user's data inside the app. 

To run this app on an android device, download and install it from the [Google Play Store](https://play.google.com/store/apps/details?id=gov.nasa.gsfc.icesat2.icesat_2).
      
 <p align="center">
   <img height = "300" src="AppStorePhotos/image1.jpg">
   <img height = "300" src="AppStorePhotos/image2.jpg">
   <img height = "300" src="AppStorePhotos/image3.jpg">
</p>

 <p align="center">
   <img height = "300" src="AppStorePhotos/image4.jpg">
   <img height = "300" src="AppStorePhotos/image5.jpg">
   <img height = "300" src="AppStorePhotos/image6.jpg">
</p>

<p align="center">
   <img height = "300" src="AppStorePhotos/image7.jpg">
   <img height = "300" src="AppStorePhotos/image8.jpg">
</p>
   
   
  

