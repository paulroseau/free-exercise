#!/bin/sh

ERRORED=0

if ! which http >/dev/null 2>&1; then
  echo "http command not found." 1>&2
  echo "Install it with" 1>&2
  echo "  brew install httpie" 1>&2
  ERRORED=1
fi

if ! which jq >/dev/null 2>&1; then
  echo "jq command not found." 1>&2
  echo "Install it with" 1>&2
  echo "  brew install jq" 1>&2
  ERRORED=1
fi

if [ $ERRORED -eq 1 ]; then
  exit 255
fi

echo "Test #1 : We should not get inexsitant user"
RESP=$(http http://localhost:8080/user/1)
echo $RESP
STATUS=$(echo $RESP | jq -r .status)
if [ $STATUS == "error" ]
then
  echo "Test #1 -> OK"
else
  echo "Test #1 -> KO"
fi
sleep 1

echo "Test #2 : We should post a valid user"
RESP=$(http POST http://localhost:8080/user/ < valid-user.json)
echo $RESP
STATUS=$(echo $RESP | jq -r .status)
if [ $STATUS == "success" ]
then
  echo "Test #2 -> OK"
else
  echo "Test #2 -> KO"
fi
sleep 1

echo "Test #3 : We should not post invalid user"
RESP=$(http POST http://localhost:8080/user/ < invalid-user.json)
echo $RESP
STATUS=$(echo $RESP | jq -r .status)
if [ $STATUS == "error" ]
then
  echo "Test #3 -> OK"
else
  echo "Test #3 -> KO"
fi
sleep 1

echo "Test #4 : We should get existing user"
RESP=$(http http://localhost:8080/user/1)
echo $RESP
AGE=$(echo $RESP | jq -r .age)
if [ $AGE -eq 25 ]
then
  echo "Test #4 -> OK"
else
  echo "Test #4 -> KO"
fi
sleep 1

echo "Test #5 : We should update existing user"
RESP=$(http PUT http://localhost:8080/user/1 < new-valid-user.json)
echo $RESP
STATUS=$(echo $RESP | jq -r .status)
if [ $STATUS == "success" ]
then
  RESP=$(http http://localhost:8080/user/1)
  echo $RESP
  NEW_AGE=$(echo $RESP | jq -r .age)
  if [ $NEW_AGE -eq 26 ]
  then
    echo "Test #5 -> OK"
  else
    echo "Test #5 -> KO (update didn't actually occur)"
  fi
else
  echo "Test #5 -> KO"
fi
sleep 1

echo "Test #6 : We should not update inexisting user"
RESP=$(http PUT http://localhost:8080/user/2 < new-valid-user.json)
echo $RESP
STATUS=$(echo $RESP | jq -r .status)
if [ $STATUS == "error" ]
then
  echo "Test #6 -> OK"
else
  echo "Test #6 -> KO"
fi
sleep 1

echo "Test #7 : We should delete existing user"
RESP=$(http DELETE http://localhost:8080/user/1)
echo $RESP
STATUS=$(echo $RESP | jq -r .status)
if [ $STATUS == "success" ]
then
  # User should not be gotten anymore
  STATUS=$(http http://localhost:8080/user/1 | jq -r .status)
  if [ $STATUS == "error" ]
  then
    echo "Test #7 -> OK"
  else
    echo "Test #7 -> KO (delete didn't actually occur)"
  fi
else
  echo "Test #7 -> KO"
fi

echo "Test #8 : We should not delete inexisting user"
RESP=$(http DELETE http://localhost:8080/user/1)
echo $RESP
STATUS=$(echo $RESP | jq -r .status)
if [ $STATUS == "error" ]
then
  echo "Test #8 -> OK"
else
  echo "Test #8 -> KO"
fi
