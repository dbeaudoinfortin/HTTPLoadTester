# HTTPLoadTester

I didn't like any of the load testers out there, so I built my own on my free time.

Contains two main app:
1) A Recorder for creating test plans,
2) A Player for generating load.

The recorder can be used as either an HTTP Proxy (built on Undertow) or a Servlet Filter. It supports both HTTP and HTTPS.

Both the player and the recorder can be managed via 1) the command line, 2) JMX or 3) REST API.

Requires Java 8 or higher.

## Recorder Proxy

The Recorder Proxy is used to 'record' HTTP calls to create a test plans out of them. It sits between the client and the server. It intercepts HTTP requests, records them to a test plan file and then forwards them to the server. 

Despite using the term 'proxy' this is not a true proxy. It is more similar to a man-in-the-middle attack. It can actually modify the content of the requests on the fly. The client points directly to the recorder and thinks it is talking to the server. The advantage of this approach is that the client doesn't need to be configured to use a proxy and doesn't even need to be proxy-aware. This means anything that makes HTTP calls can be recorded. It also means that only calls to a specific domain will be recorded. Of course, the downside is that it only supports HTTPS, so you have to manually accept the recorder's self-signed cert.

You can launch it by invoking the Main method of the com.dbf.loadtester.recorder.proxy.RecorderProxy class. It takes the following command line options:

```
 -dir <arg>            The test plan directory. Recorded test plans will be saved in this directory.
 -disableJMX           Disable the JMX management interface.
 -disableREST          Disable the REST API management interface.
 -fHost <arg>          Proxy forwarding host.
 -fHttpPort <arg>      Proxy forwarding HTTP port.
 -fHttpsPort <arg>     Proxy forwarding HTTPS port.
 -fixedSubs <arg>      Fixed substitutions in Base64 encoded Json format.
 -httpPort <arg>       Listener HTTP port.
 -httpsPort <arg>      Listener HTTPS port.
 -overrideHostHeader   Overrides the 'Host' header on every request to match the forwarding host.
 -restPort <arg>       Port to use for REST API management interface.
 -rewriteUrls          Inspects the response of every HTTP request and attempts to rewrite URLs to point back to the proxy.
 -start                Start recording immediately.
 ```

If you don't want to run the recorder in stand-alone man-in-the-middle mode, there is a servlet filter that you can plug into pretty much any Java web app. com.dbf.loadtester.recorder.filter.RecorderServletFilter can be inserted into any modern Web Server that supports Servlet Spec 3.1. 

Just like the stand-alone proxy, the filter supports management via both JMX and REST. But, if both of those options are unavailable to you, you can still start and stop the recorder by sending it a 'Magic Start/Stop Parameter' which is literally 'MAGIC_START_PARAM' or 'MAGIC_STOP_PARAM' added as an HTTP parameter to the URL of any request. This is really a 'last resort' option. 

## Load Test Player

The Load Test Player is used to play back previously recorder load test plans. It has been designed with a primary goal to minimize overhead in order to achieve high levels of concurrency. Under the hood, it uses version 4.5.x of Apache HTTP Client. It has SSL support and cookie support for stateful load test plans.   

You can launch it by invoking the Main method of the com.dbf.loadtester.player.LoadTestPlayer class. It takes the following command line options:

```
-actionDelay <arg>        Time between each action. Set to zero for no delay, set to -1 to use the test plan timings. Value in milliseconds.
 -applyFixedSubs          Apply fixed substitutions, such as <THREAD_ID>, in the test plan.
 -concurrentActions       Run the test plan actions concurrently. Used for stateless test plans.
 -cookieWhiteList <arg>   List of comma-separated Cookie names that will used from the test plan. All other cookies will be discarded.
 -disableJMX              Disable the JMX management interface.
 -disableREST             Disable the REST API management interface.
 -host <arg>              The target host.
 -httpPort <arg>          Port to use for HTTP requests.
 -httpsPort <arg>         Port to use for HTTPs requests.
 -keepAlive               Keep Load Test Player alive after all threads have halted.
 -minRunTime <arg>        Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run. Value in seconds.
 -overrideHttps           Override all HTTPs actions with HTTP
 -pause                   Pause and wait for JMX/REST invocation to start.
 -restPort <arg>          Port to use for REST API management interface.
 -shareConnections        Share connections (sockets) across threads for improved efficiency.
 -staggerTime <arg>       The average time offset between the start of each subsequent thread (staggered start). Value in milliseconds.
 -testPlanFile <arg>      The absolute path to the Json test plan file.
 -threadCount <arg>       The total number of threads to run. Each thread will repeat the test plan at least once and until MinRunTime is reached.
 -variableSubs <arg>      Variable substitutions in Base64 encoded Json format.
 ```
 
Like everything else, it supports management via both JMX and REST. Using either interface, you can get statistics on the execution time of individual actions and overall test plans. These statistics are more detailed than what is printed to the command line and are used by the HTTPLoadTesterGUI for its graphs.

## GUI

Some of the command line arguments can get complicated, so I built GUI in Visual Studio. Check it out: https://github.com/dbeaudoinfortin/HTTPLoadTesterGUI It contains a test plan recorder, test plan editor, a test plan player and a basic stats screent o keep on eye on things while your test plan is executing.
