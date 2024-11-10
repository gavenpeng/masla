/*
 * Copyright 2018 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.msw.masla.protocol.http.netty.ssl;


import com.msw.masla.protocol.http.netty.exception.MaslaException;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslServerContext;
import io.netty.handler.ssl.OpenSslSessionStats;
import io.netty.handler.ssl.OpenSslSessionTicketKey;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.internal.tcnative.SessionTicketKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Created by Gavin.peng on 2023/9/25.
 */
public class BaseSslContextFactory implements SslContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(BaseSslContextFactory.class);

    private static final Boolean ALLOW_USE_OPENSSL = Boolean.TRUE;
    protected static long SSL_STAT_MERTIC_INTERVAL = 5000;
    protected static final String SSL_SESSION_TICKET_FILE = "ticket.key";

    protected final ServerSslConfig serverSslConfig;

    public BaseSslContextFactory(ServerSslConfig serverSslConfig) {
//        this.spectatorRegistry = Objects.requireNonNull(spectatorRegistry);
        this.serverSslConfig = Objects.requireNonNull(serverSslConfig);
    }


    @Override
    public SslContextBuilder createBuilderForServer() {
        try {
            ArrayList<X509Certificate> trustedCerts = getTrustedX509Certificates();
            SslProvider sslProvider = chooseSslProvider();

            LOG.debug("Using SslProvider of type {}", sslProvider.name());

            SslContextBuilder builder = newBuilderForServer()
                    .ciphers(getCiphers(), getCiphersFilter())
                    .sessionTimeout(serverSslConfig.getSessionTimeout())
                    .sslProvider(sslProvider);

            if (serverSslConfig.getClientAuth() != null && trustedCerts != null && !trustedCerts.isEmpty()) {
                builder = builder
                        .trustManager(trustedCerts.toArray(new X509Certificate[0]))
                        .clientAuth(serverSslConfig.getClientAuth());
            }
            //开启oscp
            builder.enableOcsp(true);
            return builder;
        }
        catch (Exception e) {
            throw new RuntimeException("Error configuring SslContext!", e);
        }
    }

    /**
     * This function is meant to call the correct overload of {@code SslContextBuilder.forServer()}.  It should not
     * apply any other customization.
     */
    protected SslContextBuilder newBuilderForServer() throws IOException {
        LOG.debug("Using certChainFile {}", serverSslConfig.getCertChainFile());
        try (InputStream keyInput = getKeyInputStream();
                InputStream certChainInput = new FileInputStream(serverSslConfig.getCertChainFile())) {
            return SslContextBuilder.forServer(certChainInput, keyInput);
        }
    }

    @Override
    public void enableSessionTickets(SslContext sslContext) {
        // TODO
        OpenSslServerContext openSslContext = (OpenSslServerContext) sslContext;
        try {
            File ticketFile = new File(BaseSslContextFactory.class.getClassLoader().getSystemResource(SSL_SESSION_TICKET_FILE).getFile());
            InputStream inputStream = new FileInputStream(ticketFile);
            ByteArrayOutputStream outBuffer  = new ByteArrayOutputStream();
            byte[] buffer = new byte[48];
            int size = 0;
            while((size = inputStream.read(buffer))>0){
                outBuffer.write(buffer,0,size);
            }
            byte[] ticketData = outBuffer.toByteArray();

            OpenSslSessionTicketKey[] tickets = new OpenSslSessionTicketKey[1];
            for (int i = 0, a = 0; i < tickets.length; i++) {
                byte[] name = Arrays.copyOfRange(ticketData, a, SessionTicketKey.NAME_SIZE);
                a += SessionTicketKey.NAME_SIZE;
                byte[] hmacKey = Arrays.copyOfRange(ticketData, a, SessionTicketKey.NAME_SIZE+SessionTicketKey.HMAC_KEY_SIZE);
                a += SessionTicketKey.HMAC_KEY_SIZE;
                byte[] aesKey = Arrays.copyOfRange(ticketData, a, SessionTicketKey.TICKET_KEY_SIZE);
                a += SessionTicketKey.AES_KEY_SIZE;
                tickets[i] = new OpenSslSessionTicketKey(name,hmacKey,aesKey);
            }

            openSslContext.sessionContext().setTicketKeys(tickets);
        }catch (IOException e){
            throw new MaslaException("Masla init ssl session ticket failed",e);
        }

    }


    public void configureOpenSslStatsMetrics(SslContext sslContext, String sslContextId) {
        // Setup metrics tracking the OpenSSL stats.
        if (sslContext instanceof ReferenceCountedOpenSslContext) {
            Thread sslMerticTask = new Thread(new OpenSslStatsMetrics(sslContext));
            sslMerticTask.start();
        }
    }

    public void showOpenSslStatsMetrics(SslContext sslContext, String sslContextId) {
        // Setup metrics tracking the OpenSSL stats.


        if (sslContext instanceof ReferenceCountedOpenSslContext) {

            OpenSslSessionStats stats = ((ReferenceCountedOpenSslContext) sslContext).sessionContext().stats();

            openSslStatGauge(stats, sslContextId, "accept", OpenSslSessionStats::accept);
            openSslStatGauge(stats, sslContextId, "accept_good", OpenSslSessionStats::acceptGood);
            openSslStatGauge(stats, sslContextId, "accept_renegotiate", OpenSslSessionStats::acceptRenegotiate);
            openSslStatGauge(stats, sslContextId, "number", OpenSslSessionStats::number);
            openSslStatGauge(stats, sslContextId, "connect", OpenSslSessionStats::connect);
            openSslStatGauge(stats, sslContextId, "connect_good", OpenSslSessionStats::connectGood);
            openSslStatGauge(stats, sslContextId, "connect_renegotiate", OpenSslSessionStats::connectRenegotiate);
            openSslStatGauge(stats, sslContextId, "hits", OpenSslSessionStats::hits);
            openSslStatGauge(stats, sslContextId, "cb_hits", OpenSslSessionStats::cbHits);
            openSslStatGauge(stats, sslContextId, "misses", OpenSslSessionStats::misses);
            openSslStatGauge(stats, sslContextId, "timeouts", OpenSslSessionStats::timeouts);
            openSslStatGauge(stats, sslContextId, "cache_full", OpenSslSessionStats::cacheFull);
            openSslStatGauge(stats, sslContextId, "ticket_key_fail", OpenSslSessionStats::ticketKeyFail);
            openSslStatGauge(stats, sslContextId, "ticket_key_new", OpenSslSessionStats::ticketKeyNew);
            openSslStatGauge(stats, sslContextId, "ticket_key_renew", OpenSslSessionStats::ticketKeyRenew);
            openSslStatGauge(stats, sslContextId, "ticket_key_resume", OpenSslSessionStats::ticketKeyResume);
        }
    }

    private void openSslStatGauge(
            OpenSslSessionStats stats, String sslContextId, String statName,
            ToDoubleFunction<OpenSslSessionStats> value) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Masla ssl {} stat {} value {}", sslContextId, statName, value.applyAsDouble(stats));
        }
    }


    public static SslProvider chooseSslProvider() {
        // Use openssl only if available and has ALPN support (ie. version > 1.0.2).
        SslProvider sslProvider;
        if (ALLOW_USE_OPENSSL.booleanValue() && OpenSsl.isAvailable() && OpenSsl.isAlpnSupported()) {
            sslProvider = SslProvider.OPENSSL;
        }
        else {
            sslProvider = SslProvider.JDK;
        }
        return sslProvider;
    }

    public ServerSslConfig getServerSslConfig() {
        return serverSslConfig;
    }

    @Override
    public String[] getProtocols() {
        return serverSslConfig.getProtocols();
    }

    public List<String> getCiphers() throws NoSuchAlgorithmException {
        return serverSslConfig.getCiphers();
    }

    protected CipherSuiteFilter getCiphersFilter() {
        return SupportedCipherSuiteFilter.INSTANCE;
    }

    protected ArrayList<X509Certificate> getTrustedX509Certificates() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        ArrayList<X509Certificate> trustedCerts = new ArrayList<>();

        // Add the certificates from the JKS truststore - ie. the CA's of the client cert that peer Zuul's will use.
        if (serverSslConfig.getClientAuth() == ClientAuth.REQUIRE || serverSslConfig.getClientAuth() == ClientAuth.OPTIONAL) {
            // Get the encrypted bytes of the truststore password.
            byte[] trustStorePwdBytes;
            if (serverSslConfig.getClientAuthTrustStorePassword() != null) {
                trustStorePwdBytes = Base64.getDecoder().decode(serverSslConfig.getClientAuthTrustStorePassword());
            }
            else if (serverSslConfig.getClientAuthTrustStorePasswordFile() != null) {
                trustStorePwdBytes = Files.readAllBytes(serverSslConfig.getClientAuthTrustStorePasswordFile().toPath());
            }
            else {
                throw new IllegalArgumentException("Must specify either ClientAuthTrustStorePassword or ClientAuthTrustStorePasswordFile!");
            }

            // Decrypt the truststore password.
            String trustStorePassword = getTruststorePassword(trustStorePwdBytes);

            boolean dumpDecryptedTrustStorePassword = false;
            if (dumpDecryptedTrustStorePassword) {
                LOG.debug("X509Cert Trust Store Password " + trustStorePassword);
            }

            final KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(serverSslConfig.getClientAuthTrustStoreFile()),
                    trustStorePassword.toCharArray());

            Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                X509Certificate cert = (X509Certificate) trustStore.getCertificate(aliases.nextElement());
                trustedCerts.add(cert);
            }
        }

        return trustedCerts;
    }

    /**
     * Can be overridden to implement your own decryption scheme.
     *
     */
    protected String getTruststorePassword(byte[] trustStorePwdBytes) {
        return new String(trustStorePwdBytes).trim();
    }

    /**
     * Can be overridden to implement your own decryption scheme.
     */
    protected InputStream getKeyInputStream() throws IOException {
        return new FileInputStream(serverSslConfig.getKeyFile());
    }

    /**
     * Async timeout thread
     */
    protected class OpenSslStatsMetrics implements Runnable {

        private SslContext sslContext;
        private volatile boolean asyncTimeoutRunning = true;

        public OpenSslStatsMetrics(SslContext sslContext){
            this.sslContext = sslContext;
        }


        /**
         * The background thread that checks async requests and fires the
         * timeout if there has been no activity.
         */
        @Override
        public void run() {

            // Loop until we receive a shutdown command
            while (asyncTimeoutRunning) {
                try {
                    Thread.sleep(SSL_STAT_MERTIC_INTERVAL);
                    showOpenSslStatsMetrics(this.sslContext,"443");
                } catch (InterruptedException e) {
                    LOG.warn("Masla async timeout thread is interrupted!!!");
                    asyncTimeoutRunning = false;
                    // Ignore
                }
            }
            LOG.warn("Masla ssl stat monitor thread exit!!!");
        }

        protected void stop() {
            asyncTimeoutRunning = false;
        }
    }

}
