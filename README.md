# Whatsapp

## Project Description
This app is like WhatsApp. This is an **android app** that allows its users to send text messages, share images using camera or gallery and share their location with their friends. Users register with their **phone number** and their friends are made using their contact list (the same way as in WhatsApp).

## Tools and APIs Used
* Android Studio
* Firebase Authentication
* Firebase Realtime Database
* Firebase Storage
* Firebase Invite


## Interfaces

### Chats/Conacts and Settings
<p align="middle">
  <img src="../master/Screenshots/s1.png"/>
</p>
 
### Messages
<p align="middle">
  <img src="../master/Screenshots/s2.png"/>
</p>


## Functionalities

This app uses **Firebase Phone Number Authentication** which verifies a user’s phone number by sending a code to it. The user is then authenticated to use the app. Then the user is asked to provide a name and profile picture. Profile picture is stored in **Firebase Storage**.

The app uses **Firebase Realtime Database** for text messaging and location sharing. In this way, user gets message instantaneously.

The app uses **Firebase Realtime Database** and **Firebase Storage** for image sharing.

The app also uses **Firebase Invites** to give user the facility to invite others to use this app using email or text message.

User can also change his/her name or status. Status work the same way as in WhatsApp.


## How to Run

An apk file [WhatsApp.apk](../master/WhatsApp.apk) is provided which can be installed on an Android Phone.

In order to have a look at the code files and understand the working, simply download or clone this repository and open it in Android Studio.


## Contact
You can get in touch with me on my LinkedIn Profile: [Farhan Shoukat](https://www.linkedin.com/in/farhan-shoukat/)


## License
[MIT](../master/LICENSE)
Copyright (c) 2018 Farhan Shoukat
