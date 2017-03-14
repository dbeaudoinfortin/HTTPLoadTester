# HTTPLoadTester

I didn't like any of the load testers out there so I built my own on my free time.

Contains two main app:
1) a Recorder for creating test plans,
2) a Player for generating load.

The recorder can be used as either an HTTP Proxy (built on Undertow) or a Servlet Filter. Supports both HTTP and HTTPS.

Both the player and the recorder can be managed via 1) the command line, 2) JMX or 3) REST API.
