#1/bin/sh

 curl -X POST -v localhost:8080/auth/signup \
	 -H "Content-Type: application/json" \
        -d '{ "username":"matej", "email":"herower6@gmail.com", "password" : "heslo" }'

