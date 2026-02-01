Quick start — Bring the network up (very short flow)

This file contains the minimal, clear sequence you asked for. Run these steps from a Bash-compatible shell (WSL, Git Bash, or Linux/macOS). Do not run these .sh scripts directly in PowerShell unless you use WSL (for example: `wsl bash`).

1) Go to the certificate/CA folder and start the CA-related containers

```bash
cd create-certificate-with-ca
# start CA services using Docker Compose
docker compose up -d
```

2) Start the orderer services

```bash
cd orderer
# run the orderer startup helper
./start-orderers.sh
```

3) Start the organization (Org1) services

```bash
cd ../org1
# run the org startup helper (this repository uses start-orgs.sh)
./start-orgs.sh
```

4) Go back to the ownership-network root and run the main setup script

```bash
cd ../..   # back to ownership-network root
# make sure setup.sh is executable once (only needed first time)
chmod +x setup.sh
# then run it
./setup.sh
```

Notes and quick checks
- Always run these commands in a Bash-compatible shell. If you're on Windows, the easiest route is WSL or Git Bash.
- If any script reports "permission denied", run `chmod +x <script>` and re-run it.
- If Docker containers fail to start, check logs with:

```bash
docker compose ps
docker compose logs -f
```

- If a script fails, capture its output and the failing container logs and re-run the failed step after fixing the issue.

That's it — the four-step minimal flow you requested. If you want, I can also:
- Add a single wrapper script that runs these four steps in order and checks for errors after each step.
- Adapt this flow with PowerShell-friendly commands that launch WSL automatically.