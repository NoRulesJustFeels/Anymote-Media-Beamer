Anymote-Media-Beamer
====================

<p>The Anymote-Media-Beamer application allows you to beam video files from computers to Google TV devices.</p>

<p>You need a <a href="http://www.oracle.com/technetwork/java/javase/downloads/index.html">Java 1.6 runtime environment (JRE)</a> for your operating system. 
Download the beamer.jar file from the Github downloads page for this project.
Double-click the beamer.jar file to start the application.</p>

<p>When you connect to a Google TV device for the first time, you will be prompted to enter a PIN code that is displayed on the Google TV device.
Pairing will only happen once per Google TV device. Then you can drag-and-drop video files onto the application to beam it to the Google TV device.</p>

<p>To play the videos on your Google TV device you need a media player. Google TV does not come with a default media player. 
You can install media players such as <a href="https://play.google.com/store/apps/details?id=net.gtvbox.videoplayer">GTVBox Video Player</a> 
(when you use GTVBox you might be prompted to select the kind of media before playback starts; select "Seekable file or HLS")</p>

<p>Playback is limited to the <a href="https://developers.google.com/tv/android/docs/gtv_media_formats">video formats supported by Google TV</a> and the manufacturers of the Google TV hardware.
Anymote-Media-Beamer does not do any transcoding of the video.</p>

<p>The computer running the Anymote-Media-Beamer application needs to be on the same network as the Google TV device. 
To play the video, a web server is created on port 8080 on your computer and you might have to configure your firewall to allow incoming connections to access the video.</p>

<p>Note for developers: The Anymote-Media-Beamer project includes the jar file for the <a href="https://github.com/entertailion/Anymote-for-Java">Anymote-for-Java</a> library in its lib directory. 
Developers should check out that project and export a jar file for the latest version of the code. 
When you export the Anymote-for-Java project as a jar file, do not include its lib directory since this project already includes those jar files.</p>


<iframe width="560" height="315" src="http://www.youtube.com/embed/FWxjOts8kZk" frameborder="0" allowfullscreen></iframe>

<p>References:
<ul>
<li><a href="https://developers.google.com/tv/remote/docs/pairing">Google TV Pairing Protocol</a></li>
<li><a href="https://code.google.com/p/anymote-protocol/">Anymote Protocol</a></li>
<li><a href="https://developers.google.com/tv/remote/docs/developing">Building Second-screen Applications for Google TV</a></li>
<li>The ultimate Google TV remote: <a href="https://play.google.com/store/apps/details?id=com.entertailion.android.remote">Able Remote</a></li>
</ul>
</p>