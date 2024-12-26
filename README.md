Demo server to test org.owasp.dependency-check-maven (jeremylong/DependencyCheck) configuration, e.g.:
```xml
    <suppressionFiles>
        <suppressionFile>http://localhost:8080/basic/suppressions.xml</suppressionFile>
    </suppressionFiles>
    <suppressionFileUser>my-username</suppressionFileUser>
    <suppressionFilePassword>my-secret-password</suppressionFilePassword> 
```

Several endpoinds are provided:
* /bearer/suppressions.xml expects a Bearer auth, if not provided, replies with HTTP-401 and WWW-Authenticate: [Bearer realm="hosted suppressions"]
* /basic/suppressions.xml expects a Basic auth, if not provided, replies with HTTP-401 and WWW-Authenticate: [Basic realm="hosted suppressions"]
* /basic302/suppressions.xml expects a Basic auth, if not provided, replies with HTTP-302 and WWW-Authenticate: [Basic realm="hosted suppressions"]
* /unauthenticated/suppressions.xml doesn't expect any auth

Notes:
- [x] None of the authenticated actually check the provided crendentials.
- [x] On success, the api gives back a HTTP-200 and the content of https://github.com/ftiercelin/SuppressionServer/blob/main/src/main/resources/publishedSuppressions.xml
