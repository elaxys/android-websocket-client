#!/bin/sh

FILE_KEY=privkey.pem
FILE_CERT=cacert.pem
DAYS=365

# Generates private key without password protection
# openssl can derive the public key from the private key
openssl genrsa -out $FILE_KEY 2048

# Creates self-signed certificate 
openssl req -new -x509 -days $DAYS -key $FILE_KEY -out $FILE_CERT


