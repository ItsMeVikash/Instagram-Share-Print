# Instagram-Share-Print
---------------------------
Android app for downloading browsing sharing and printing images/videos

Tool Used
----------

Android Studio IDE

<a class="github-button" href="https://play.google.com/store/apps/details?id=vikashkumar.instagramshare" data-size="large" aria-label="Download ntkme/github-buttons on GitHub">View On Google Play</a>

Screenshots
------------

1.Main Screen

![alt text](https://github.com/ItsMeVikash/Android/blob/master/src/Screenshot_20190112-005221.jpg)

2.Image Loaded after pasting url

![alt text](https://github.com/ItsMeVikash/Android/blob/master/src/Screenshot_20190112-005241.jpg)

3. Printing Image Screen

![alt text](https://github.com/ItsMeVikash/Android/blob/master/src/Screenshot_20190112-005251.jpg)

4.Video Screen

![alt text](https://github.com/ItsMeVikash/Android/blob/master/src/Screenshot_20190112-005636.jpg)

5. Sharing Content

![alt text](https://github.com/ItsMeVikash/Android/blob/master/src/Screenshot_20190112-005649.jpg)




How to Code:-
--------------
<a class="github-button" href="https://play.google.com/store/apps/details?id=vikashkumar.instagramshare" data-size="large" aria-label="Download ntkme/github-buttons on GitHub">Download Apk from PlayStore</a>

1. Get Url from Instagram
2. Use InstagramApi to get the final url of image and video
3. or You can get into source code using JSoup Library  <a class="github-button" href="https://jsoup.org/download" data-size="large" aria-label="Download ntkme/github-buttons on GitHub">Jsoup Maven and Gradle Dependencies</a>
4. Once You get the Final Image/Video Url Use Picasso or Glide Library to Load images from url
      <a class="github-button" href="https://github.com/bumptech/glide" data-size="large" aria-label="Download ntkme/github-buttons on GitHub">Glide Github</a>
5. For Loading video from url get Url connection

 `try {
     	URL myUrl = new URL(url);
      	URLConnection connection = myUrl.openConnection();      
      	is = connection.getInputStream();
      	bis = new BufferedInputStream(is);
      	fos = new FileOutputStream(destFile); 
	int current = 0;
   	while ((current = bis.read()) != -1) {
        	fos.write(current);
         }
        fos.close();
    }catch(Exception e) {
      //Log.e(TAG, "Error while downloading and saving file !", e);
    }`
    
6. For Printing Image Use below piece of code

`private void doPhotoPrint() {
    PrintHelper photoPrinter = new PrintHelper(getActivity());
    photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.droids);
    photoPrinter.printBitmap("droids.jpg - test print", bitmap);
}`



