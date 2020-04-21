//   Copyright 2012 Matthew Lowden
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

/*
This class was borrowed from https://github.com/MatthewLowden/RPi-XPT2046-Touchscreen-Java
Had to change everything having to do with the old GPIO library he used and replace it with HardKernel's odroid wiringPi code.
I left the old code in case it helps someone trying to modify it to work with some other library
 */

package org.swampsoft.odroidTouchscreen;

public class SPIManager {

    public native int wiringPiSetup();
    public native void pinMode(int port, int value);
    public native void digitalWrite(int port, int onOff);
    public native int digitalRead(int port);

	/*
	private GpioGateway gpio;
	private static final Boardpin SPI_MOSI = Boardpin.PIN19_GPIO10;
	private static final Boardpin SPI_MISO = Boardpin.PIN21_GPIO9;
	private static final Boardpin SPI_SCLK = Boardpin.PIN23_GPIO11;
	private static final Boardpin SPI_CE0 = Boardpin.PIN24_GPIO8;
	*/

	private static final int SPI_MOSI = 12; // SPI input   actually pin 19 on board
	private static final int SPI_MISO = 13; // SPI output  actually pin 21 on board
    private static final int SPI_SCLK = 14; // SPI clock   actually pin 23 on board
    private static final int SPI_CE0 = 11; //              actually pin 26 on board

    boolean GPIOSetup = false;

    // this block is to optimize variables found in this class
	boolean currentMOSIstate;
	byte byteToSend;
	boolean desiredState;
	int numBytes;
	byte[] buffer;
	int currentBit;
	byte receiveByte;
	int bit;

	SPIManager() {
		System.out.println("***** Starting wiringPi *****");
        wiringPiSetup();
		System.out.println("***** wiringPi started *****");

		//gpio = null;
		// Register Shutdown hook.
		//Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
	}

	public void SPISelect() {
		/*
		if (null == gpio)
			setUpGPIO();

		// Bring the select line low.
		gpio.setValue(SPI_CE0, false);
		*/
        if (!GPIOSetup) setUpGPIO();
        digitalWrite(SPI_CE0, 0);
	}

	public void SPIUnSelect() {
		/*
		if (null == gpio)
			setUpGPIO();

		// Bring the select line high.
		gpio.setValue(SPI_CE0, true);
		 */
        if (!GPIOSetup) setUpGPIO();
        digitalWrite(SPI_CE0, 1);
	}
	
	public void SPIPulseClock() {
		//gpio.setValue(SPI_SCLK, true);
		//gpio.setValue(SPI_SCLK, false);
        digitalWrite(SPI_SCLK, 1);
        digitalWrite(SPI_SCLK, 0);
	}

	public void SPISend(byte[] data) {
		//if (null == gpio) setUpGPIO();
        if (!GPIOSetup) setUpGPIO();

		// Send MSB->LSB order.
		
		currentMOSIstate = false;

		for (int i = 0; i < data.length; i++) {
			byteToSend = data[i];
			for (int j = 0; j < 8; j++) {

				desiredState = false;
				if ((byteToSend & 0x80) > 0)
					desiredState = true;
				
				if (desiredState == true && currentMOSIstate == false) {
					//gpio.setValue(SPI_MOSI, true);
                    digitalWrite(SPI_MOSI, 1);
					currentMOSIstate = true;
				} else if (desiredState == false && currentMOSIstate == true) {
					//gpio.setValue(SPI_MOSI, false);
                    digitalWrite(SPI_MOSI, 0);
					currentMOSIstate = false;
				}
				
				// Pulse the clock.
				SPIPulseClock();

				// Shift to the next bit.
				byteToSend <<= 1;
			}
		}
		if (currentMOSIstate == true)
		{
			//gpio.setValue(SPI_MOSI, false);
            digitalWrite(SPI_MOSI, 0);
		}
	}

	public byte[] SPIReceive(int numBits) {
		//if (null == gpio) setUpGPIO();
        if (!GPIOSetup) setUpGPIO();
		
		numBytes = (numBits + 7) / 8;

		buffer = new byte[numBytes];

		// Array is filled in received byte order.
		// Any padding bits are the least significant bits, of the last byte.

		currentBit = 0;
		for (int i = 0; i < numBytes; i++) {
			receiveByte = 0x00;
			for (int j = 0; j < 8; j++) {
				// Shift to the next bit.
				receiveByte <<= 1;

				// Skip padding bits
				currentBit++;				
				if (currentBit > numBits)
					continue;

				/*
				// Set the clock high.
				gpio.setValue(SPI_SCLK, true);

				// Read the value.
				boolean bit = gpio.getValue(SPI_MISO);

				// Set the clock low.
				gpio.setValue(SPI_SCLK, false);

				// Set the received bit.
				if (bit) {
					receiveByte |= 1;
				}
				*/
				digitalWrite(SPI_SCLK, 1);
				bit = digitalRead(SPI_MISO);
				digitalWrite(SPI_SCLK, 0);
				if (bit > 0){
				    receiveByte |= 1;
                }
			}
			buffer[i] = receiveByte;
		}

		return buffer;
	}

	private void setUpGPIO() {

		// Set up GPIO
		/*
		gpio = new GpioGatewayImpl();

		gpio.export(SPI_MOSI);
		gpio.setDirection(SPI_MOSI, Direction.OUT);

		gpio.export(SPI_MISO);
		gpio.setDirection(SPI_MISO, Direction.IN);

		gpio.export(SPI_SCLK);
		gpio.setDirection(SPI_SCLK, Direction.OUT);
		gpio.setValue(SPI_SCLK, false);

		gpio.export(SPI_CE0);
		gpio.setDirection(SPI_CE0, Direction.OUT);
		gpio.setValue(SPI_CE0, true);
		*/
		System.out.println("***** Initializing GPIO *****");

		pinMode(SPI_MOSI, 1); // 1 is output, 0 is input
        pinMode(SPI_MISO, 0);
        pinMode(SPI_SCLK, 1);
        digitalWrite(SPI_SCLK, 0);
        pinMode(SPI_CE0, 1);
        digitalWrite(SPI_CE0, 1);

        GPIOSetup = true;

		System.out.println("***** GPIO Initialized *****");
	}

	public class Shutdown implements Runnable {

		// This Runnable is called on shutdown to ensure the GPIO pin is
		// released.

		@Override
		public void run() {
			shutDownGPIO();

			// Output a CR so that the command prompt is in the regular place on
			// exit.
			System.out.println();
		}
	}

	private void shutDownGPIO() {
		/*
		if (null != gpio) {

			gpio.unexport(SPI_CE0);
			gpio.unexport(SPI_MISO);
			gpio.unexport(SPI_MOSI);
			gpio.unexport(SPI_SCLK);

		}
		*/
	}

}
