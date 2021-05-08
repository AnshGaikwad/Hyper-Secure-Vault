<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/AnshGaikwad/Hyper-Secure-Vault">
    <img src="https://github.com/AnshGaikwad/Hyper-Secure-Vault/blob/main/images/logo.jpg" alt="Logo" width="150" height="150">
  </a>

  <h1 align="center">Hyper Secure Vault</h1>

  <p align="center">
    An Organizational based Vault which expertises in data hiding using Steganography to avoid data leak even after Cyber Attacks
    <br />
    <br />
    <a href="">View Demo</a>
    •
    <a href="https://github.com/AnshGaikwad/Hyper-Secure-Vault-Backend">Backend Code</a>
    •
    <a href="https://github.com/AnshGaikwad/Hyper-Secure-Vault/issues">Report Bug</a>
    •
    <a href="https://github.com/AnshGaikwad/Hyper-Secure-Vault/issues">Request Feature</a>
  </p>
</p>

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#introduction">Introduction</a></li>
    <li><a href="#why-hyper-secure-vault">Why Hyper Secure Vault</a></li>
    <li><a href="#tools-and-technologies-used">Tools and Technologies used</a></li>
    <li><a href="#setup">Setup</a></li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#implemented-features">Implemented Features</a></li>
    <li><a href="#further-work">Further Work</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact-me">Contact Me</a></li>
  </ol>
</details>

<!-- INTRODUCTION -->
## Introduction

An Organizational based Vault that features in data hiding using Steganography to avoid data leak even after Cyber Attacks. Additionally, some other cryptography tools have been added like text and file encryption using RSA and AES.

<!-- WHY HYPER SECURE VAULT -->
## Why Hyper Secure Vault
Take any organization in general, its sensitive data is stored in a third-party cloud service such as Google Cloud Platform, Amazon Web Service or Microsoft Azure. Whenever any data is stored online, the threat of cyber attacks is always available. Even if we don't know, there can be some security flaw. My solution for this is to hide the sensitive data within the not so important data without letting the attackers know, so, that even the cloud data is leaked, the attackers will not get any sensitive data as its hidden in the leaked data but they don't know that and they will eventually not waste there time cracking that which can take a lot of time as its encrypted with AES. No one would like to waste their time cracking something which they don't know is there.

<!-- TOOLS AND TECHNOLOGIES USED -->
## Tools and Technologies used

* [Java 15.0.2](https://www.oracle.com/java/technologies/javase/jdk15-archive-downloads.html) => Programming Language used.
* [JavaFx SDK 16](https://openjfx.io/) => To build the GUI.
* [Java Crypto](https://docs.oracle.com/javase/7/docs/api/javax/crypto/package-summary.html) => To implement AES encryption on embedded data
* [Zlib Compression](https://stackoverflow.com/questions/6173920/zlib-compression-using-deflate-and-inflate-classes-in-java) => To compress data to be embedded
* [Spring Boot](https://spring.io/projects/spring-boot) => To build the authentication API
* [PostgreSQL](https://www.postgresql.org/) => Databased used with Spring Boot JDBC
* [Google Cloud Storage](https://cloud.google.com/storage) => To store encrypted images

<!-- SETUP -->
## Setup
Run the command in your terminal
```
git clone https://github.com/AnshGaikwad/Hyper-Secure-Vault.git
```
Or you can just clone it through [Android Studio](https://developer.android.com/studio) which will be much easier.

After cloning, you can follow the [YouTube tutorial by Bro Code](https://youtu.be/Ope4icw6bVk) to set up Java Fx.

To use this application, you will need to execute the backend code on your local system the Repo link can be found [here](https://github.com/AnshGaikwad/Hyper-Secure-Vault-Backend). The Backend hasn't been yet hosted on a cloud.

<!-- USAGE -->
## Usage

Hyper Secure Platform provides a Cloud-Based Data storage platform for organizations to hide the sensitive data inside some garbage data to avoid data leak even after cyber attacks. The employee can enter the organization ID and view all the uploaded data, though he will again need a password that is encrypted using AES and compressed using Zlib to retrieve the file. 

<!-- IMPLEMENTED FEATURES -->
## Implemented Features

Currently, the cloud integration is about to be done. Though I wasn't gonna leak my Cloud Credentials, I thought about making this Repository public now itself.

<p align="center">
  <img src="https://github.com/AnshGaikwad/Hyper-Secure-Vault/blob/main/images/login.PNG" alt="Logo" width="325" height="250">
  <img src="https://github.com/AnshGaikwad/Hyper-Secure-Vault/blob/main/images/register.PNG" alt="Logo" width="325" height="250">
</p>

Above you can see the login and registration UI which I have implemented using JavaFX, it is pretty basic, I agree, more advanced UI can be done.

<p align="center">
  <img src="https://github.com/AnshGaikwad/Hyper-Secure-Vault/blob/main/images/steganography.PNG" alt="Logo" width="750" height="500">
</p>

This is the main feature of the project i.e. Steganography implemented, using the LSB algorithm. I have implemented three types of Steganography: embedding Text Message, Documents, and Images, which is again encrypted by AES and compressed by Zlib if the option is enabled. Also, the option to choose pixel size is included to focus on quality or size as per the user wishes.

<p align="center">
  <img src="https://github.com/AnshGaikwad/Hyper-Secure-Vault/blob/main/images/RSA.PNG" alt="Logo" width="325" height="250">
  <img src="https://github.com/AnshGaikwad/Hyper-Secure-Vault/blob/main/images/aes.PNG" alt="Logo" width="325" height="250">
</p>

The above two are optional features that I added just I wanted to learn how they work and implement them. The right-hand side is RSA Cipher which encrypts text using RSA and the left-hand side one is AES 128 bit file encryption in which we can use and save the key and decrypt the file whenever we needed.

<!-- FUTURE SCOPE -->
## Further Work
  <ol>
    <li>Implementing Backend for Organization's Database.</li>
    <li>Implementing Cloud Storage.</li>
    <li>Implementing Other Steganographic Algorithms such as for Audio and Video.</li>
    <li>Redisigning the UI in a more better way.</li>
  </ol>

<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/VeryCoolFeature`)
3. Commit your Changes (`git commit -m 'Add a VeryCoolFeature'`)
4. Push to the Branch (`git push origin feature/VeryCoolFeature`)
5. Open a Pull Request

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.

<!-- CONTACT ME -->
## Contact Me

Ansh Gaikwad - [anshyg2002@gmail.com](mailto:anshyg2002@gmail.com)
