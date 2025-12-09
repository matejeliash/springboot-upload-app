#1/bin/sh

# login

# Make request and save JWT to variable
JWT=$(curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
-d '{  "email":"herower6@gmail.com", "password" : "heslo", "username" : "matej" }' | jq -r '.token')

echo "$JWT"


#curl  -X GET "localhost:8080/users/info"      -H "Authorization: Bearer ${JWT}"

#curl  -X GET "localhost:8080/users"      -H "Authorization: Bearer ${JWT}"



curl -v -X GET "localhost:8080/users/me"      -H "Authorization: Bearer ${JWT}"



curl -v -X POST "http://localhost:8080/files/upload" \
  -H "Authorization: Bearer ${JWT}" \
  -F "file=@test_file.txt"


curl -v -X POST "http://localhost:8080/files/upload" \
  -H "Authorization: Bearer ${JWT}" \
  -F "file=@/home/melias/.zshrc"

curl -v -X GET "http://localhost:8080/files" \
  -H "Authorization: Bearer ${JWT}" \

