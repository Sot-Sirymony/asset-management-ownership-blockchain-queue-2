
*** command run ***
chmod +x net.sh

./net.sh up        # full start: CA → certs → network → channel → chaincode → explorer
./net.sh status    # see running containers
./net.sh logs explorer
./net.sh down
./net.sh reset     # full clean reset


chmod +x net.sh
./net.sh up

docker network rm test


./net.sh gen-crypto
./net.sh up-fabric
./net.sh channel
./net.sh deploy-cc
./net.sh up-explorer
