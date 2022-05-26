<img src="https://static.mparticle.com/sdk/mp_logo_black.svg" width="280"><br>

# mParticle Android Media SDK

Hello! This is the public repo of the mParticle Android Media SDK. We've built the mParticle platform to take a new approach to web and mobile app data and the platform has grown to support 50+ services and SDKs, including developer tools, analytics, attribution, messaging, and advertising services. mParticle is designed to serve as the connector between all of these services - check out [our site](http://mparticle.com), or hit us at developers@mparticle.com to learn more.

## Documentation

Fully detailed documentation and other information about mParticle Android SDK can be found at our doc site

-   [Core mParticle SDK](https://docs.mparticle.com/developers/sdk/android/getting-started)

-   [Media SDK](https://docs.mparticle.com/developers/sdk/android/media)

# Getting Started

Please be aware that this SDK is built as an extension of and requires the use of the [mParticle Android SDK](https://github.com/mParticle/mparticle-android-sdk/).

## Include and Initialize the SDK

Below summarizes the major steps to get the Android Media SDK up and running. In addition to the below, we have built a sample app that provides a more in depth look at how to send `MediaEvent`s to Adobe's Heartbeat Kit. See that [sample app here](https://github.com/mParticle/mparticle-media-samples)

You can grab the Core SDK via Maven Central. Please see the badge above and follow the [releases page](https://github.com/mParticle/mparticle-android-sdk/releases) to stay up to date with the latest version.

```groovy
dependencies {
    implementation 'com.mparticle:android-media:1.3.2'
}
```




### Code Samples

Starting a MediaSession and logging events. The `MediaSession` fields `title`, `mediaContentId`, `duration`, `streamType`, and `contentType` are all required fields. We reccomend you use the constants defined in `StreamType` and `ContentType` for the `streamType` and `contentType` fields respectively, but free formed Strings are accepted if these constants don't accurately describe your Media

```kotlin
    //initialize the core SDK
    val options = MParticleOptions.builder(context)
        .credentials("key", "secret")
        .build()
    MParticle.start(options)
    
    //initialize a MediaSession
    val mediaSession = MediaSession.builder {
        title = "Media Title"
        mediaContentId = "123"
        duration = 1000
        streamType = StreamType.LIVE_STEAM
        contentType = ContentType.VIDEO
    }
    
    //start the MediaSession
    mediaSession.logMediaSessionStart()
    
    //log events
    mediaSession.logPlay()
    mediaSession.logPause()
```

Updating playhead position while logging an event

```kotlin
    mediaSession.logPlay(
        Options(currentPlayheadPosition = 120000)
    )
```

Including customAttributes in an event. Common customAttribute keys are available as constants in `OptionsAttributeKeys`, but they may also be free formed Strings

```kotlin
    val options = Options(
            customAttributes = mapOf(
                "isFullScreen" to "true",
                CONTENT_EPISODE to "episode1",
                PLAYER_NAME to "JWPlayer"
            )
        )
    mediaSession.logPlay(options)
```

#### Logging `MediaEvent`s as Custom Events (`MPEvent`) to the MParticle Server

1) Automatically Log all `MediaEvent`s created by the `MediaSession`, as `MPEvent`s

> note: "UpdatePlayheadPosition" `MediaEvent`s will never be automatically logged to the MParticle Server

```kotlin
val mediaSession = MediaSession.builder {
    title = "Media Title"
    mediaContentId = "123"
    duration = 1000
    streamType = StreamType.LIVE_STEAM
    contentType = ContentType.VIDEO
    
    logMPEvents = true
}
```

2) Create a Custom Event(`MPEvent`) within the context of a MediaSession.

```kotlin
val mpEvent = mediaSession.buildMPEvent("Milestone", mapOf(
    "type" to "95%"
  ))
MParticle.getInstance()?.logEvent(mpEvent)
```

3) Log select `MediaEvent`s as Custom Events(`MPEvents`). The example flattens and logs `MediaEvent` created by `MediaSession.logPlay()` calls
```kotlin
mediaSession.mediaEventListener = { mediaEvent ->
    if (mediaEvent.eventName == MediaEventName.PLAY) {
        val mpEvent = mediaEvent.toMPEvent()
        MParticle.getInstance()?.logEvent(mpEvent)
    }
}
```

#### Logging Media Errors

The `MediaSession#logError(String, Map)` method is similar to the generic `MParticle#logError(String, Map)` method, but should be used for logging events specific to an individual `MediaSession`.

Like all other `Media` events, "Error" events will not be sent to the MParticle Server by default, but will be consumed by local kits. To log this event to the MParticle Server, follow [the previously outlined instructions](#logging-mediaevents-as-custom-events-mpevent-to-the-mparticle-server)

example: 
```kotlin
//log a MediaSession-specific error as a MediaEvent
mediaSession.logError("Player Crashed", mapOf("foo", "bar"))

//log a general error as an core event
MParticle.getInstance()?.logError("Request timed out", mapOf("foo", "bar"))
```

# Contibution Guidelines

At mParticle, we are proud of our code and like to keep things open source. If you'd like to contribute, simply fork this repo, push any code changes to your fork, and submit a Pull Request against the `development` branch of mParticle-android-media-sdk.

## Support

<support@mparticle.com>

* [SDK Documentation](https://docs.mparticle.com/developers/sdk/android/media/)
* [Javadocs](http://docs.mparticle.com/developers/sdk/android/javadocs/index.html)


## License

The mParticle Android SDK is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0). See the LICENSE file for more info.

