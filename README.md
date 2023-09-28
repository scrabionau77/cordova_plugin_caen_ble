# installazione plugin 

- [] utilizza il comando 'cordova plugin add .../maeCaenBlePlugin' recandoti da terminale nella repository del progetto

# nel progetto in cui installerai il plugin dovrai modificare i seguenti file:

- [] Creare una cartella "libs" nella radice del progetto e inserire dentro il file "CAENRFIDLibrary-release.aar" che trovi nella cartella "libs" di questo progetto
- [] Modificare il build.gradle (Module:app) del progetto che si trova all'interno del percorso 'platforms/android' e aggiungere il seguente codice
```
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    implementation(name: 'CAENRFIDLibrary-release', ext: 'aar')
}
```
- [] Aggiungere sempre nel build.gradle (Module:app) la seguente riga di codice `minSdkVersion 28` al posto della seguente riga di codice `minSdkVersion cordovaConfig.MIN_SDK_VERSION`
- [] il file AndroidManifest.xml deve essere così scritto:
```
<?xml version='1.0' encoding='utf-8'?>
<manifest android:hardwareAccelerated="true" android:versionCode="10000" android:versionName="1.0.0" xmlns:android="http://schemas.android.com/apk/res/android">
    <supports-screens android:anyDensity="true" android:largeScreens="true" android:normalScreens="true" android:resizeable="true" android:smallScreens="true" android:xlargeScreens="true" />
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <application android:hardwareAccelerated="true" android:icon="@mipmap/ic_launcher" android:label="@string/app_name" android:supportsRtl="true">
        <activity android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|smallestScreenSize|screenLayout|uiMode" android:exported="true" android:label="@string/activity_name" android:launchMode="singleTop" android:name="MainActivity" android:theme="@style/Theme.App.SplashScreen" android:windowSoftInputMode="adjustResize">
            <intent-filter android:label="@string/launcher_name">
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```