Root Packet Capture
===



>**Requires root**

Root Packet Capture is very basic packet capture app for rooted android devices.
The captured packets can later be analysed using tools like [wireshark](https://www.wireshark.org/).
### How it works:
For capturing packets it uses a precompiled [tcpdump binary](http://www.androidtcpdump.com/). The tcpdump binary is bundled in the apk by including it in the assets folder.

The app uses [libsuperuser](https://github.com/Chainfire/libsuperuser) for starting an interactive root shell from where the tcpdump binary runs.

Once packet capture is started the app need not be running. The tcpdump binary continues to run even after activity is closed. While reopening the app checks for any already running instance of tcpdump and doesnot create another process. The tcpdump process can be closed by using the "**Stop Capture**" or "**Stop and Exit**" buttons.

In the current implementation the output is stored in "*/mnt/sdcard/xxxx.pcap*" using the command:
```sh
/data/data/devilrx.smartpacker/files/tcpdump.bin -w /mnt/sdcard/0001.pcap
```

However if "-w" flag is not desired change the command and, capture the live output by modifying the:
```java
public void onLine(String line) {
```
in **TcpdumpPacketCapture.java** as required.
> **Note**: The current implementation stores the .pcap file in sd card. The .pcap file might grow very large if the network use is very high. Implement without -w flag or stop/restart capture.

**Feel free to fork and improve :)**
