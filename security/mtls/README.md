# mTLS example

this folder contains a spring application, which uses mutual tls authentication.

it also contains integration tests which show how to use it for creating a java client for the application.

see:

* [./src/main/resources/certs](./src/main/resources/certs)
* [SecurityConfig.java](./src/main/java/info/ankin/how/spring/mtls/SecurityConfig.java)
* [MTlsDemoApplicationTest.java](./src/test/java/info/ankin/how/spring/mtls/MTlsDemoApplicationTest.java)
* [MTlsDemoApplication.java](./src/main/java/info/ankin/how/spring/mtls/MTlsDemoApplication.java)

the readme in `certs/` contains a reference to a guide to generating the certs.

the various parts (`SecurityConfig.java`, and `MTlsDemoApplicationTest`) (should) read only the parts of the certs they need to do their job.

The tests demonstrate that you need the right client cert to be able to request data.

the controller in `MTlsDemoApplication` even shows how you can read the identity of the cert in the request.
