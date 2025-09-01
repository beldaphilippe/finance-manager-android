# Privacy Friendly Finance Manager

This application can be used to monitor and manage personal financials.


## Modifications brought

This repository is a fork of [Privacy Friendly Finance Manager](https://github.com/SecUSo/privacy-friendly-finance-manager).

I modified it to add an option to export the database in CSV format directly to a Google Drive *encrypted* with a custom passphrase.
This option is available on main screen, under the tree dot menu.
The decryption of backups can be performed on a POSIX environnement with the python script `decrypt.py`.

Note:
I disabled the currency localisation, the app will display the amounts in Euro.


## Installation

A Java environment is needed for the compilation (and installation) of the application.

### Application compilation

`./gradlew assembleDebug`

### Application installation

* Connect device to computer and enable the "Transferring Files" option on mobile device.
* Run `./gradlew installDebug`
