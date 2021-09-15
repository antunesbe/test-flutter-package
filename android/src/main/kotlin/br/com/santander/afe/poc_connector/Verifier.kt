package br.com.santander.afe.poc_connector

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class Verifier(): HostnameVerifier {

    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return true;
    }
}