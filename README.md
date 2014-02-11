# GCM4J [![Build Status](https://travis-ci.org/phonedeck/gcm4j.png?branch=master)](https://travis-ci.org/phonedeck/gcm4j)

## Overview

GCM4J is a simple, extensible, documented Google Cloud Messaging (GCM) client written in Java. Written and extensively used in production by [Phonedeck](http://phonedeck.com).

## Installation

The recommended way is to use the library as a Maven dependency.

```xml
<dependency>
  <groupId>com.phonedeck</groupId>
  <artifactId>gcm4j</artifactId>
  <version>1.0</version>
</dependency>
```


## Getting Started

### Creating Gcm and Sending the First Push Message

```java
// create a GCM4J client
Gcm gcm = new DefaultGcm(new GcmConfig()
  .withKey("your-gcm-key-from-google-apis"));
  
// assemble a request
GcmRequest request = new GcmRequest()
  .withRegistrationId("registration-id-reported-by-your-device")
  .withCollapseKey("sync")
  .withDelayWhileIdle(true)
  .withDataItem("command", "sync");

// send the request asynchronously
ListenableFuture<GcmResponse> responseFuture = gcm.send(request);

// wait for and process the response
GcmResponse response = responseFuture.get();
System.out.println("Response: " + response);
```

### Processing the Response Asynchronously

```java
Futures.addCallback(responseFuture, new FutureCallback<GcmResponse>() {  
  public void onSuccess(GcmResponse response) {
    System.out.println("Response: " + response);           
  }
  public void onFailure(Throwable t) {
    System.err.println("Error occured: " + t);
  }
});
```

### Error Handling

The user can face two types of errors:

1. network error: when the request GCM server could not be reached, or the GCM server responded with an error
2. GCM error result: when the GCM server processed the request, but could not complete it for some reason

In the first case the request itself throws an exception:

```java
// Synchronous
try {
  GcmResponse response = responseFuture.get();
} catch (ExecutionException ex) {
  if (ex.getCause() instanceof GcmNetworkException) {
    // GCM server gave a non-200 HTTP response
    GcmNetworkException gne = (GcmNetworkException) ex.getCause();
    System.err.println("GCM Server responded with and error: " + gne.getCode() + " " + gne.getResponse());
  } else if (ex.getCause() instanceof IOException) {
    // host not found or net is down
    System.err.println("Network error occured: " + ex);
  } else {
    // should not happen during normal operation
    System.err.println("Unknown error occured: " + ex);
  }  
} catch (InterruptedException ex) {
  // the thread waiting for the response got interrupted
  System.err.println("Thread interrupted");
  Thread.currentThread().interrupt();
}
```

```java
// Asynchronous
Futures.addCallback(responseFuture, new FutureCallback<GcmResponse>() {
  public void onSuccess(GcmResponse response) { 
    // everything's shiny
  }

  public void onFailure(Throwable t) {
    if (t instanceof GcmNetworkException) {
      // GCM server gave a non-200 HTTP response
      GcmNetworkException gne = (GcmNetworkException) t;
      System.err.println("GCM Server responded with and error: " + gne.getCode() + " " + gne.getResponse());
    } else if (t instanceof IOException) {
      // host not found or net is down
      System.err.println("Network error occured: " + t);
    } else {
      // should not happen during normal operation
      System.err.println("Unknown error occured: " + t);
    }  
  }
});
```

In the latter case the request gets a proper response, and each registration ID has its own status:

```java
for (Result result : response.getResults()) {
  if (result.getError() == null) {
    System.out.println("Aaaaalright");
  } else {
    System.err.println("Error " + result.getError() + " occured for registration id " + result.getRequestedRegistrationId());
  }
}
```

### Filters

Every request is run through a chain of filters. Each filter may modify the request, pass down the request to the next filter, get back and possibly modify the response, and pass the response up to the previous one.

The order the filters were added matters. The first added filter gets the request first and the response last. Therefore the request of the last filter is passed directly to the GCM Servers and the user gets the response of the last filter.

Simple logging filter example:
```java
public final class LoggingFilter implements GcmFilter {
  public ListenableFuture<GcmResponse> filter(GcmRequest request, FilterChain chain) {
    System.out.println("Got request: " + request);

    ListenableFuture<GcmResponse> response = chain.next(request);
    Futures.addCallback(response, new FutureCallback<GcmResponse>() {
      public void onSuccess(GcmResponse result) {
        System.out.println("Got response: " + result);
      }                        
      public void onFailure(Throwable t) {
        System.err.println("Exception occured: " + t);
      }
    });
    
    return response;
  }
}

GcmConfig config = new GcmConfig()
  .withFilter(new LoggingFilter());
```

## Version History

### 1.1 - Feb 11, 2014

- Possibility to handle Retry-After values in filters
- Richer Request and Response interfaces

### 1.0 - Jul 29, 2013

- Initial release


## Contributors

The maintainers would like to express their gratitude towards the contributors, that spared no time to make this library more powerful.

- @turf00 for providing a standard way to respect the server's Retry-After response on errors and a lot of test cases
- @jalogar for implementing missing auxiliary functions
