/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.console.agent;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import org.apache.ignite.console.agent.handlers.DatabaseListener;
import org.apache.ignite.console.agent.handlers.RestListener;
import org.apache.ignite.console.agent.rest.RestExecutor;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.X;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_CONNECT_ERROR;
import static io.socket.client.Socket.EVENT_DISCONNECT;
import static io.socket.client.Socket.EVENT_ERROR;
import static org.apache.ignite.IgniteSystemProperties.IGNITE_PROG_NAME;
import static org.apache.ignite.console.agent.AgentUtils.fromJSON;
import static org.apache.ignite.console.agent.AgentUtils.toJSON;

/**
 * Ignite Web Agent launcher.
 */
public class AgentLauncher {
    /** */
    private static final Logger log = LoggerFactory.getLogger(AgentLauncher.class);

    /** */
    private static final String EVENT_CLUSTER_BROADCAST_START = "cluster:broadcast:start";

    /** */
    private static final String EVENT_CLUSTER_BROADCAST_STOP = "cluster:broadcast:stop";

    /** */
    private static final String EVENT_CLUSTER_DISCONNECTED = "cluster:disconnected";

    /** */
    private static final String EVENT_DEMO_BROADCAST_START = "demo:broadcast:start";

    /** */
    private static final String EVENT_DEMO_BROADCAST_STOP = "demo:broadcast:stop";

    /** */
    private static final String EVENT_SCHEMA_IMPORT_DRIVERS = "schemaImport:drivers";

    /** */
    private static final String EVENT_SCHEMA_IMPORT_SCHEMAS = "schemaImport:schemas";

    /** */
    private static final String EVENT_SCHEMA_IMPORT_METADATA = "schemaImport:metadata";

    /** */
    private static final String EVENT_NODE_VISOR_TASK = "node:visorTask";

    /** */
    private static final String EVENT_NODE_REST = "node:rest";

    /** */
    private static final String EVENT_AUTHENTICATE = "authenticate";

    /** */
    private static final String EVENT_AUTHENTICATED = "authenticated";

    /** */
    private static final String EVENT_UNAUTHORIZED = "unauthorized";

    /** */
    private static final String EVENT_EXPIRED = "expired";

    /** */
    private static final String EVENT_RESET_TOKEN = "resetToken";

    static {
        // Optionally remove existing handlers attached to j.u.l root logger.
        SLF4JBridgeHandler.removeHandlersForRootLogger();

        // Add SLF4JBridgeHandler to j.u.l's root logger.
        SLF4JBridgeHandler.install();
    }

    /**
     * Create a trust manager that trusts all certificates It is not using a particular keyStore
     */
    private static X509TrustManager getTrustManager() {
        return new X509TrustManager() {
            /** {@inheritDoc} */
            @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            /** {@inheritDoc} */
            @Override public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }

            /** {@inheritDoc} */
            @Override public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }
        };
    }

    /**
     * On error listener.
     */
    private static final Emitter.Listener onError = new Emitter.Listener() {
        @Override public void call(Object... args) {
            Throwable e = (Throwable)args[0];

            ConnectException ce = X.cause(e, ConnectException.class);

            if (ce != null)
                log.error("Failed to establish connection to server (connection refused).");
            else {
                Exception ignore = X.cause(e, SSLHandshakeException.class);

                if (ignore != null) {
                    log.error("Failed to establish SSL connection to server, due to errors with SSL handshake.");
                    log.error("Add to environment variable JVM_OPTS parameter \"-Dtrust.all=true\" to skip certificate validation in case of using self-signed certificate.");

                    System.exit(1);
                }

                ignore = X.cause(e, UnknownHostException.class);

                if (ignore != null) {
                    log.error("Failed to establish connection to server, due to errors with DNS or missing proxy settings.");
                    log.error("Documentation for proxy configuration can be found here: http://apacheignite.readme.io/docs/web-agent#section-proxy-configuration");

                    System.exit(1);
                }

                ignore = X.cause(e, IOException.class);

                if (ignore != null && "404".equals(ignore.getMessage())) {
                    log.error("Failed to receive response from server (connection refused).");

                    return;
                }

                if (ignore != null && "407".equals(ignore.getMessage())) {
                    log.error("Failed to establish connection to server, due to proxy requires authentication.");

                    String userName = System.getProperty("https.proxyUsername", System.getProperty("http.proxyUsername"));

                    if (userName == null || userName.trim().isEmpty())
                        userName = readLine("Enter proxy user name: ");
                    else
                        System.out.println("Read username from system properties: " + userName);

                    char[] pwd = readPassword("Enter proxy password: ");

                    final PasswordAuthentication pwdAuth = new PasswordAuthentication(userName, pwd);

                    Authenticator.setDefault(new Authenticator() {
                        @Override protected PasswordAuthentication getPasswordAuthentication() {
                            return pwdAuth;
                        }
                    });

                    return;
                }

                log.error("Connection error.", e);
            }
        }
    };

    /**
     * On disconnect listener.
     */
    protected static final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override public void call(Object... args) {
            log.error("Connection closed: {}", args);
        }
    };

    /**
     * On demo start request.
     */
    protected static final Emitter.Listener onDemoStart = new Emitter.Listener() {
        @Override public void call(Object... args) {
            log.warn(String.valueOf(args[0]));
        }
    };

    /**
     * @param fmt Format string.
     * @param args Arguments.
     */
    private static String readLine(String fmt, Object... args) {
        if (System.console() != null)
            return System.console().readLine(fmt, args);

        System.out.print(String.format(fmt, args));

        return new Scanner(System.in).nextLine();
    }

    /**
     * @param fmt Format string.
     * @param args Arguments.
     */
    private static char[] readPassword(String fmt, Object... args) {
        if (System.console() != null)
            return System.console().readPassword(fmt, args);

        System.out.print(String.format(fmt, args));

        return new Scanner(System.in).nextLine().toCharArray();
    }

    /**
     * @param args Args.
     */
    protected AgentConfiguration loadConfiguration(String[] args) {
        final AgentConfiguration cfg = new AgentConfiguration();

        JCommander jCommander = new JCommander(cfg);

        String runner = System.getProperty(IGNITE_PROG_NAME, "ignite-web-agent.{sh|bat}");

        jCommander.setProgramName(runner);

        try {
            jCommander.parse(args);
        }
        catch (ParameterException pe) {
            log.error("Failed to parse command line parameters: " + Arrays.toString(args), pe);

            jCommander.usage();

            System.exit(1);
        }

        String prop = cfg.configPath();

        AgentConfiguration propCfg = new AgentConfiguration();

        try {
            File f = AgentUtils.resolvePath(prop);

            if (f == null)
                log.warn("Failed to find agent property file: {}", prop);
            else
                propCfg.load(f.toURI().toURL());
        }
        catch (IOException e) {
            if (!AgentConfiguration.DFLT_CFG_PATH.equals(prop))
                log.warn("Failed to load agent property file: " + prop, e);
        }

        cfg.merge(propCfg);

        if (cfg.help()) {
            jCommander.usage();

            System.exit(0);
        }

        return cfg;
    }

    /**
     * @param cfg Config.
     */
    protected void requestTokens(AgentConfiguration cfg) {
        System.out.println("Security token is required to establish connection to the Web Console.");
        System.out.println(String.format("It is available on the Profile page: %s/profile", cfg.serverUri()));

        String tokens = String.valueOf(readPassword("Enter security tokens separated by comma: "));

        cfg.tokens(Arrays.asList(tokens.trim().split(",")));
    }

    /**
     * @param srvUri Server uri.
     */
    protected void initProxy(URI srvUri) {
        // Create proxy authenticator using passed properties.
        switch (srvUri.getScheme()) {
            case "http":
            case "https":
                final String username = System.getProperty(srvUri.getScheme() + ".proxyUsername");
                final char[] pwd = System.getProperty(srvUri.getScheme() + ".proxyPassword", "").toCharArray();

                Authenticator.setDefault(new Authenticator() {
                    @Override protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, pwd);
                    }
                });

                break;

            default:
                // No-op.
        }
    }

    protected Map<String, Object> loadAuthParams(AgentConfiguration cfg) {
        HashMap<String, Object> msg = U.newHashMap(4);

        msg.put("disableDemo", cfg.disableDemo());

        String clsName = AgentLauncher.class.getSimpleName() + ".class";

        String clsPath = AgentLauncher.class.getResource(clsName).toString();

        if (clsPath.startsWith("jar")) {
            String manifestPath = clsPath.substring(0, clsPath.lastIndexOf('!') + 1) +
                "/META-INF/MANIFEST.MF";

            try {
                Manifest manifest = new Manifest(new URL(manifestPath).openStream());

                Attributes attr = manifest.getMainAttributes();

                msg.put("ver", attr.getValue("Implementation-Version"));
                msg.put("bt", attr.getValue("Build-Time"));
            }
            catch (IOException e) {
                log.error("Failed to receive build time, version from /META-INF/MANIFEST.MF", e);
            }
        }

        return msg;
    }

    protected IO.Options loadOptions() {
        IO.Options opts = new IO.Options();

        opts.path = "/agents";

        // Workaround for use self-signed certificate
        if (Boolean.getBoolean("trust.all")) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");

                X509TrustManager trustMgr = getTrustManager();

                // Create an SSLContext that uses our TrustManager
                ctx.init(null, new TrustManager[] { trustMgr }, null);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(ctx.getSocketFactory(), trustMgr)
                    .build();

                // default settings for all sockets
                opts.callFactory = okHttpClient;
                opts.webSocketFactory = okHttpClient;
            }
            catch (Exception ignore) {
                // No-op.
            }
        }

        return opts;
    }

    protected CountDownLatch attachListeners(final Socket client, final AgentConfiguration cfg) {
        final CountDownLatch latch = new CountDownLatch(1);

        final RestExecutor restExecutor = new RestExecutor(cfg.nodeUri());

        DatabaseListener dbHnd = new DatabaseListener(cfg);
        RestListener restHnd = new RestListener(restExecutor);

        final Map<String, Object> authMsg = loadAuthParams(cfg);

        client
            .on(EVENT_CONNECT, new Emitter.Listener() {
                @Override public void call(Object... args) {
                    log.info("Connection established.");

                    authMsg.put("tokens", toJSON(cfg.tokens()));

                    client.emit(EVENT_AUTHENTICATE, new JSONObject(authMsg));
                }})
            .on(EVENT_CONNECT_ERROR, onError)
            .on(EVENT_ERROR, onError)
            .on(EVENT_DISCONNECT, onDisconnect)
            .on(EVENT_AUTHENTICATED, new Emitter.Listener() {
                @Override public void call(Object... args) {
                    try {
                        List<String> activeTokens = fromJSON(args[0], List.class);

                        if (!F.isEmpty(activeTokens)) {
                            Collection<String> missedTokens = cfg.tokens();

                            cfg.tokens(activeTokens);

                            missedTokens.removeAll(activeTokens);

                            if (!F.isEmpty(missedTokens)) {
                                String tokens = F.concat(missedTokens, ", ");

                                log.warn("Failed to authenticate with token(s): {}. " +
                                    "Please check agent configuration.", tokens);
                            }

                            log.info("Authentication success.");
                        }
                    }
                    catch (Exception e) {
                        log.error("Failed to authenticate agent. Please check agent\'s tokens", e);

                        System.exit(1);
                    }
                }
            })
            .on(EVENT_EXPIRED, new Emitter.Listener() {
                @Override public void call(Object... args) {
                    log.error("You are using an older version of the agent. Please reload agent.");

                    System.exit(1);
                }
            })
            .on(EVENT_UNAUTHORIZED, new Emitter.Listener() {
                @Override public void call(Object... args) {
                    log.error("Failed to authenticate with token(s): {}. Please reload agent archive or check agent configuration.",
                        F.concat(cfg.tokens(), ", "));

                    System.exit(1);
                }
            })
            .on(EVENT_RESET_TOKEN, new Emitter.Listener() {
                @Override public void call(Object... args) {
                    String tok = String.valueOf(args[0]);

                    log.warn("Security token has been reset: {}", tok);

                    cfg.tokens().remove(tok);

                    if (cfg.tokens().isEmpty()) {
                        client.off();

                        latch.countDown();
                    }
                }
            })

//            .on(EVENT_CLUSTER_BROADCAST_START, clusterLsnr.start())
//            .on(EVENT_CLUSTER_BROADCAST_STOP, clusterLsnr.stop())
//
//            .on(EVENT_DEMO_BROADCAST_START, demoHnd.start())
//            .on(EVENT_DEMO_BROADCAST_STOP, demoHnd.stop())

            .on(EVENT_SCHEMA_IMPORT_DRIVERS, dbHnd.availableDriversListener())
            .on(EVENT_SCHEMA_IMPORT_SCHEMAS, dbHnd.schemasListener())
            .on(EVENT_SCHEMA_IMPORT_METADATA, dbHnd.metadataListener())
            
            .on(EVENT_NODE_REST, restHnd)
            .on(EVENT_NODE_VISOR_TASK, restHnd);

        return latch;
    }

    /**
     * @param args Args.
     */
    protected void start(String[] args) {
        AgentConfiguration cfg = loadConfiguration(args);

        System.out.println();
        System.out.println("Agent configuration:");
        System.out.println(cfg);
        System.out.println();

        URI srvUri;

        try {
            srvUri = URI.create(cfg.serverUri());
        }
        catch (Exception e) {
            log.error("Failed to parse server URI.", e);

            return;
        }

        if (cfg.tokens() == null)
            requestTokens(cfg);

        initProxy(srvUri);

        log.info("Connecting to: {}", cfg.serverUri());

        Socket client = IO.socket(srvUri, loadOptions());
        
        try {
            CountDownLatch latch = attachListeners(client, cfg);

            client.connect();

            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            log.warn("Agent has been interrupted.", e);
        } finally {
            client.close();
        }
    }

    /**
     * @param args Args.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Ignite Web Console Agent...");

        new AgentLauncher().start(args);
    }
}
