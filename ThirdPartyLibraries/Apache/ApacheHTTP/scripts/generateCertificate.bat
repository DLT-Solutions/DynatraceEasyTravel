set path=%cd%
set OPENSSL_CONF=%path%\openssl-easytravel.cnf 
set RANDFILE=%path%\.rnd

"%path%\..\..\openssl\openssl.exe" genrsa -out easytravel-san.key 2048

"%path%\..\..\openssl\openssl.exe" req -config openssl-easytravel.cnf -new -out easytravel-san.req -key easytravel-san.key -subj "/C=AT/ST=Austria/L=Linz/O=Dynatrace/OU=IT/CN=Dynatrace"

"%path%\..\..\openssl\openssl.exe" x509 -req -in easytravel-san.req -out easytravel-san.crt -CAkey CA.key -CA CA.cer -days 365 -CAcreateserial -CAserial easytravel-CAserial -extensions v3_req -extfile openssl-easytravel.cnf