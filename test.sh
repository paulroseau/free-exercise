#!/bin/sh

http POST http://localhost:8080/user/ < user1.json

sleep 1

http http://localhost:8080/user/1
