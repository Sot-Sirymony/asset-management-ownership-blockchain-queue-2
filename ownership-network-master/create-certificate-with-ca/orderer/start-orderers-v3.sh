#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

CRYPTO_DIR="${SCRIPT_DIR}/../../channel/crypto-config/ordererOrganizations/ownify.com"
TLS_CERT="${SCRIPT_DIR}/../fabric-ca/ordererOrg/tls-cert.pem"

# ✅ FIX: connect to CA via host.docker.internal (Mac host),
# because CA TLS cert is valid for localhost but NOT for ca_orderer.
#CA_HOST="host.docker.internal"
#CA_PORT="9054"
#CA_NAME="ca-orderer"
CA_HOST="ca_orderer"
CA_PORT="9054"
CA_NAME="ca-orderer"

enroll_admin() {
  echo
  echo "Enroll the CA admin"
  echo "==================="
  echo

  mkdir -p "${CRYPTO_DIR}"
  export FABRIC_CA_CLIENT_HOME="${CRYPTO_DIR}"

  fabric-ca-client enroll -u "https://admin:adminpw@${CA_HOST}:${CA_PORT}" \
    --caname "${CA_NAME}" \
    --tls.certfiles "${TLS_CERT}"

  echo "PWD        = [${PWD}]"
  echo "SCRIPT_DIR  = [${SCRIPT_DIR}]"
  echo "CRYPTO_DIR  = [${CRYPTO_DIR}]"
  echo "TLS_CERT    = [${TLS_CERT}]"
  echo "CA_URL      = [https://admin:***@${CA_HOST}:${CA_PORT}]"
}

enable_node_ous() {
  echo
  echo "Enable NodeOUs"
  echo "=============="
  echo

  mkdir -p "${CRYPTO_DIR}/msp"

  cat > "${CRYPTO_DIR}/msp/config.yaml" <<'EOF'
NodeOUs:
  Enable: true
  ClientOUIdentifier:
    Certificate: cacerts/ca_orderer-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: client
  PeerOUIdentifier:
    Certificate: cacerts/ca_orderer-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: peer
  AdminOUIdentifier:
    Certificate: cacerts/ca_orderer-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: admin
  OrdererOUIdentifier:
    Certificate: cacerts/ca_orderer-9054-ca-orderer.pem
    OrganizationalUnitIdentifier: orderer
EOF
}

register_orderers() {
  export FABRIC_CA_CLIENT_HOME="${CRYPTO_DIR}"

  for orderer in orderer orderer2 orderer3; do
    echo
    echo "Register ${orderer}"
    echo "================"

    fabric-ca-client register --caname "${CA_NAME}" \
      --id.name "${orderer}" --id.secret "ordererpw" \
      --id.type "orderer" \
      --tls.certfiles "${TLS_CERT}"
  done

  echo
  echo "Register ordererAdmin"
  echo "====================="

  fabric-ca-client register --caname "${CA_NAME}" \
    --id.name "ordererAdmin" --id.secret "ordererAdminpw" \
    --id.type "admin" \
    --tls.certfiles "${TLS_CERT}"
}

generate_orderer_msp() {
  for orderer in orderer orderer2 orderer3; do
    echo
    echo "Generate the ${orderer} MSP"
    echo "=========================="

    mkdir -p "${CRYPTO_DIR}/orderers/${orderer}.ownify.com"

    fabric-ca-client enroll -u "https://${orderer}:ordererpw@${CA_HOST}:${CA_PORT}" \
      --caname "${CA_NAME}" \
      -M "${CRYPTO_DIR}/orderers/${orderer}.ownify.com/msp" \
      --csr.hosts "${orderer}.ownify.com" \
      --csr.hosts "localhost" \
      --csr.hosts "host.docker.internal" \
      --tls.certfiles "${TLS_CERT}"

    cp "${CRYPTO_DIR}/msp/config.yaml" \
      "${CRYPTO_DIR}/orderers/${orderer}.ownify.com/msp/config.yaml"
  done
}

generate_orderer_tls_certificates() {
  for orderer in orderer orderer2 orderer3; do
    echo
    echo "Generate the ${orderer}-TLS certificates"
    echo "======================================="

    fabric-ca-client enroll -u "https://${orderer}:ordererpw@${CA_HOST}:${CA_PORT}" \
      --caname "${CA_NAME}" \
      -M "${CRYPTO_DIR}/orderers/${orderer}.ownify.com/tls" \
      --enrollment.profile "tls" \
      --csr.hosts "${orderer}.ownify.com" \
      --csr.hosts "localhost" \
      --csr.hosts "host.docker.internal" \
      --tls.certfiles "${TLS_CERT}"
  done
}

copy_and_rename_tls_files() {
  echo
  echo "Copy and rename TLS files"
  echo "========================"
  echo

  for orderer in orderer orderer2 orderer3; do
    tls_dir="${CRYPTO_DIR}/orderers/${orderer}.ownify.com/tls"

    mkdir -p "${tls_dir}"

    cp "${tls_dir}/tlscacerts/"* "${tls_dir}/ca.crt"
    cp "${tls_dir}/signcerts/"* "${tls_dir}/server.crt"
    cp "${tls_dir}/keystore/"* "${tls_dir}/server.key"
  done
}

create_tls_ca_certificates_for_orderers() {
  echo
  echo "Create TLS CA certs for each orderer MSP"
  echo "========================================"
  echo

  for orderer in orderer orderer2 orderer3; do
    mkdir -p "${CRYPTO_DIR}/orderers/${orderer}.ownify.com/msp/tlscacerts"
    cp "${CRYPTO_DIR}/orderers/${orderer}.ownify.com/tls/tlscacerts/"* \
      "${CRYPTO_DIR}/orderers/${orderer}.ownify.com/msp/tlscacerts/tlsca.ownify.com-cert.pem"
  done
}

create_tls_ca_certificate_for_org_msp() {
  echo
  echo "Create TLS CA cert for org MSP"
  echo "=============================="
  echo

  mkdir -p "${CRYPTO_DIR}/msp/tlscacerts"
  cp "${CRYPTO_DIR}/orderers/orderer.ownify.com/tls/tlscacerts/"* \
    "${CRYPTO_DIR}/msp/tlscacerts/tlsca.ownify.com-cert.pem"
}

create_tls_ca_certificate_for_org_tlsca() {
  echo
  echo "Copy TLS CA cert to organization tlsca"
  echo "======================================"
  echo

  mkdir -p "${CRYPTO_DIR}/tlsca"
  cp "${CRYPTO_DIR}/orderers/orderer.ownify.com/tls/tlscacerts/"* \
    "${CRYPTO_DIR}/tlsca/tlsca.ownify.com-cert.pem"
}

#create_ca_certificate_for_org_ca() {
#  echo
#  echo "Copy CA cert to organization ca"
#  echo "==============================="
#  echo
#
#  mkdir -p "${CRYPTO_DIR}/ca"
#  cp "${CRYPTO_DIR}/orderers/orderer.ownify.com/msp/cacerts/"* \
#    "${CRYPTO_DIR}/ca/ca.ownify.com-cert.pem"
#
#  cp "${CRYPTO_DIR}/msp/keystore/"* \
#    "${CRYPTO_DIR}/ca/priv_sk"
#}

create_ca_certificate_for_org_ca() {
  echo
  echo "Copy CA cert to organization ca"
  echo "==============================="
  echo

  mkdir -p "${CRYPTO_DIR}/ca"

  # Copy CA cert (ok to pick the single file in cacerts)
  cp "${CRYPTO_DIR}/orderers/orderer.ownify.com/msp/cacerts/"* \
    "${CRYPTO_DIR}/ca/ca.ownify.com-cert.pem"

  # Copy ONE private key into priv_sk (pick the first file in keystore)
  keyfile="$(ls -1 "${CRYPTO_DIR}/msp/keystore"/* | head -n 1)"
  cp "${keyfile}" "${CRYPTO_DIR}/ca/priv_sk"
}


sleep 2s
enroll_admin
sleep 1s
enable_node_ous
sleep 1s
register_orderers
sleep 1s
generate_orderer_msp
sleep 1s
generate_orderer_tls_certificates
sleep 1s
copy_and_rename_tls_files
sleep 1s
create_tls_ca_certificates_for_orderers
sleep 1s
create_tls_ca_certificate_for_org_msp
sleep 1s
create_tls_ca_certificate_for_org_tlsca
sleep 1s
create_ca_certificate_for_org_ca

echo
echo "✅ Done. Generated crypto material under:"
echo "${CRYPTO_DIR}"
echo
