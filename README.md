SwiftShare
A Simple, Efficient, Peer-to-Peer File Transfer Tool

SwiftShare is a simple, peer-to-peer file transfer tool written in Java. It utilizes JavaFX for the GUI and the standard java.io and java.net APIs. SwiftShare is designed to transfer files directly to a peer's computer, eliminating the need for third-party cloud services or USB drives.

Main Features
Simple, intuitive user interface

Supports multiple concurrent file transfers to different connections

File transfer manager: pause or cancel transfers (right-click a drop-down tab)

No server or internet required—works over local network

Installation
DISCLAIMER: Use this tool only with people you personally know and trust. The authors are not liable for any misuse or illegal activity.

Download the latest release JAR from the target folder or build it yourself using Maven.

Double-click SwiftShare-1.0.jar to run, or use the command line as shown below.

Requirements
Java 17 Runtime or higher

JavaFX 17 SDK (download from GluonHQ)

~14 MB of disk space

How to Run
Download and extract JavaFX 17 SDK.

Open a terminal in the folder containing your JAR.

Run:

bash
java --module-path "C:\path\to\javafx-sdk-17.0.17\lib" --add-modules javafx.controls,javafx.fxml -jar "C:\path\to\SwiftShare-1.0.jar"
Replace the paths above with your actual JavaFX SDK and JAR locations.

How to Use
Decide between you and your peer who will be the Host. The host must port forward their network on a port of their choice (default is 54000) unless both parties are on the same local network.

If you are sending the file:

Click Select and locate the file to be sent.

If you are hosting:
2. Click the Host checkbox next to the Port input field.
3. Input your desired port in the Port input field.
4. Click Send and wait for your peer to connect.

Otherwise:
2. Type your peer's IP address (excluding the port) into the IP Address input field.
3. Type your peer's chosen port in the Port input field.
4. Click Send once your peer has completed their setup.

If you are receiving the file:

Click Save and select the destination where the incoming file will be saved.

If you are hosting:
2. Click the Host checkbox next to the Port input field.
3. Input your desired port in the Port input field.
4. Click Receive and wait for your peer to connect.

Otherwise:
2. Type your peer's IP address (excluding the port) into the IP Address input field.
3. Type your peer's chosen port in the Port input field.
4. Click Receive once your peer has completed their setup.

Known Issues
If one end cancels a transfer while the other user is paused, resuming the transfer may cause the system to assume the transfer was successful, as the connection was terminated. This is due to reading the end of the input stream being the same as a disconnect. Please delete any partial files if this occurs.

Enjoy using SwiftShare for fast, secure, and private file transfers!