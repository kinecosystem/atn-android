# atn-android
A Wrapper for the Kin Core SDK that is used for Stellar Scale test on real users using the ATN Token

### Usage
`atn-android` expose very simple API, that should be intergrated at message receive and message sent flows inside Kik.


```java
//init the ATN module
ATN atn = new ATN();
//call onMessageSent with Android Context when message is sent
atn.onMessageSent(context);
//call onMessageReceived with Android Context when message is sent
atn.onMessageReceived(context);
```

## Build

Add this to your module's `build.gradle` file.
```gradle
repositories {
    ...
    maven {
        url 'https://jitpack.io'
        credentials { username YOUR-JITPACK-AUTHTOKEN }
    }
}
...
dependencies {
    ...
    compile("com.github.kinfoundation:atn-android:LATEST-COMMIT-ON-MASTER-BRANCH"){
        //prevents conflicts with kik app
        exclude group: 'com.android.support'
        exclude group: 'com.squareup.okhttp3'
    }
}
```
In the above `build.gradle`:
For getting a token, go to https://jitpack.io and sign in with your github account. 
Authorize jitpack, then navigate to https://jitpack.io/w/user to get your AccessToken. Ensure that jitpack is authorized 
for private repositories for `kinfoundation` organization. 
