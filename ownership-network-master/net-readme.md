
*** command run ***
chmod +x net.sh

./net.sh up        # full start: CA → certs → network → channel → chaincode → explorer
./net.sh status    # see running containers
./net.sh logs explorer
./net.sh down
./net.sh reset     # full clean reset

