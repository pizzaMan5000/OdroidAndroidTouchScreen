# OdroidAndroidTouchScreen
<p>
  <a href="http://www.swampsoft.org/android/odroidtouchscreen/odroidtouchscreen.php">Download APK file here</a>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;This is an Android app I designed to make my Waveshare 5inch Raspberry Pi touchscreen work with android and Odroid C1+, but it should work for any screen that uses the XPT2046 chip (and possibly ADS7843/ADS7846 chips) and Odroid C1, C2, N2, and XU4. These screens were designed for the Raspberry Pi running linux and nothing else. The screen uses the XPT2046 chip for touch recognition, like many cheap Raspberry Pi screens online, and of course, not much documentation. The only software I could find for it was for Linux. After doing some research, I realized I would have to make my own driver. Unfortunately, it would be too much work for me to make a proper driver for Android and for others to install it as well. The alternative is to make an app that talks to the touchscreen and uses Android's native commands to inject gestures (tap, swipe). I found a Java app on github that someone made to read touches and modified it to work with a special version of the wiringPi library made to work with Odroids that have a GPIO. I also set this app up as an accessibility service so it can be enabled by simply starting the service in Android Accessibility options and it starts when Android starts automatically. I tried to make it work with other screen sizes other than 5inch so any screen with an XPT2046 chip can be used with this software. Supposedly the Xpt2046 chip is compatible with the ADS7843/ADS7846 chip, so it might work with ADS7843/ADS7846 chips too. This app is not in the Google Play Store because it supports Android 4.4 Kitkat, which is too old for the Play Store.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;The methods used in this app mean that touch gestures aren't going to be just like if you used this screen with a Raspberry Pi. It's not a real Android driver. This app could be modified to use Android's accessibility gesture injection, which would be much better, but the Odroids use old Android versions which don't have that feature. Also this could easily be modified to work with a Raspberry Pi by just switching the Odroid wiringPi library with the Raspberry Pi wiringPi library.
</p>
<p>
&nbsp;&nbsp;&nbsp;&nbsp;Setup and calibration is done by setting the screen resolution and the touch resolution. The touch resolution should by 4096x4096 by default. You shouldn't have to change it, but you can if you need. You can also shift the X,Y coordinates if your taps are not on target.
</p>
<p>
Hardware installation for touch screen with XPT2046:
<br>
&nbsp;&nbsp;&nbsp;&nbsp;The screen is designed to attach directly to a Raspberry Pi's GPIO, which connects power and touch input to the Odroid. Video is connected via HDMI. Unfortunately, simply plugging the screen in to the Odroid's GPIO does not work. Not sure why, but it just didn't seem to get the proper power. The usb power on the screen does work, so instead of connecting the screen directly to the Odroid, just connect what's needed for the input controller to communicate with Odroid (SPI in/out, SPI IRQ, SPI Chip Select, SPI clock). Do not connect any power pins except the ground (I used pin 25). Just connect screen pins: 19, 21, 22, 23, 25, 26 to the same pins on the Odroid C1+. Except for the XU4, the other Odroids seem to have the similar GPIOs as the C1+, so they should work with the same setup. The XU4 uses different pins and will have to be wired different. The Android code is also set up for Odroid C1+ pins, so you will need to get the source code from Github and modify, then compile the code. So that'll be a little project for someone.
</p>
<br>

USED PINS:<br>
PIN &nbsp; SYMBOL &nbsp;&nbsp;&nbsp;&nbsp;	DESCRIPTION<br>
25&nbsp;&nbsp;	GND	 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;   Ground <br>
19&nbsp;&nbsp;	TP_SI	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  SPI data input of Touch Panel<br>
21&nbsp;&nbsp;	TP_SO	 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; SPI data output of Touch Panel<br>
22&nbsp;&nbsp;	TP_IRQ&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	Touch Panel interrupt, low level while the Touch Panel detects touching<br>
23&nbsp;&nbsp;	TP_SCK&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	SPI clock of Touch Panel<br>
26&nbsp;&nbsp;	TP_CS&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	  Touch Panel chip selection, low active<br>
