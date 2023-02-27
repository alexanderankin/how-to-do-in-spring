# mTLS helper files

> https://www.baeldung.com/x-509-authentication-in-spring-security

## root ca

```shell
openssl req -nodes -x509 -sha256 -days 3650 -newkey rsa:4096 \
    -keyout rootCA.key -out rootCA.crt \
    -subj '/C=US/ST=State State/L=Any-town/OU=host/CN=localhost'
```

## server cert

request it:

```shell
openssl req -nodes -new -newkey rsa:4096 -keyout localhost.key -out localhost.csr \
    -subj '/C=US/ST=State State/L=Any-town/OU=client/CN=localhost'
```

> signing config: configuration needed to sign things with root cert

```shell
cat > localhost.ext <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
subjectAltName = @alt_names
[alt_names]
DNS.1 = localhost
EOF
```

grant the request:

```shell
openssl x509 -req -CA rootCA.crt -CAkey rootCA.key \
    -in localhost.csr -out localhost.crt \
    -days 3650 \
    -CAcreateserial -extfile localhost.ext
```

# packaging it up in a `jks`:

```shell
# https://stackoverflow.com/a/62863490
#openssl pkcs12 \
#    -keypbe NONE -certpbe NONE -nomaciter -passout pass: \
#    -export -out localhost.p12 -name "localhost" \
#    -inkey localhost.key -in localhost.crt

openssl pkcs12 -export -nodes -out localhost.p12 -inkey localhost.key \
    -in localhost.crt -certfile rootCA.crt \
    -passout pass:changeit

keytool -importkeystore -noprompt \
    -storepass 'changeit' \
    -srcstorepass 'changeit' \
    -deststorepass 'changeit' \
    -srckeystore localhost.p12 -srcstoretype PKCS12 \
    -destkeystore keystore.jks -deststoretype JKS
```
