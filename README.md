# ML-in-Android-Studio
These are 3 Projects that deploy image classification models into Android Studio.

# How to Run
Downlold Zip file for the repository, and Extract it.

Open Android Studio, click File -> new -> import Project and select any of the 3 projects.
Make sure that Tenserflow lite dependencie is sycned. To check, extend Gradle Scripts and double click on build.gradle(Module :app), 
check if the deoendenciey is there if not copy this (implementation 'org.tensorflow:tensorflow-lite:2.9.0') to your dependencies, a "sync" option will show up at the top that you need to click.

# Add Images to your Emulator 
If you are using an emulator, Unzip the file for the images.
Start your emulater and click on Device Managar on the right. click on more options for the selected emulater and open Device Explorer. 
go Storage -> emulated -> 0 -> Download. Right-Click download and select upload and select an image.
When you are done. go to you files on your emulator and click on the images to see it.

# Export Profiler Recordings
After finiahing a recoding on Profiler, click Past Recodings, click the recording that you want to export and click "Export recording" at the bottom. select its path and click ok.


