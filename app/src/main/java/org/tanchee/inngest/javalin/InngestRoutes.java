package org.tanchee.inngest.javalin;

import org.tanchee.inngest.SupportedFrameworkName;
import org.tanchee.inngest.ServeConfig;
import org.tanchee.inngest.InngestFunction;
import org.tanchee.inngest.Inngest;

import io.javalin.Javalin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InngestRoutes {
    private static final Set<Integer> HTTP_PORTS = Set.of(80, 443);

    public static void serve(
        Javalin app,
        String path,
        Inngest client,
        List<InngestFunction> fnList,
        String id,
        String signingKey,
        String serveOrigin,
        String servePath,
        String logLevel,
        String baseUrl
    ) {
        ServeConfig config = new ServeConfig(
            client,
            id,
            baseUrl,
            signingKey,
            serveOrigin,
            servePath,
            logLevel
        );

        Map<String, InngestFunction> fnMap = fnList.stream()
            .collect(Collectors.toMap(InngestFunction::id, fn -> fn));

        CommHandler comm = new CommHandler(
            fnMap,
            client,
            config,
            SupportedFrameworkName.JAVALIN
        );

        app.get(path, ctx -> {
            String signature = ctx.header(InngestHeaderKey.Signature.value());
            String serverKind = ctx.header(InngestHeaderKey.ServerKind.value());
            String body = ctx.body();

            String response = comm.introspect(signature, body, serverKind);

            ctx.contentType("application/json");
            ctx.status(200);
            ctx.result(response);
        });

        app.post(path, ctx -> {
            String fnId = ctx.queryParam("fnId");
            if (fnId == null) {
                ctx.status(400).result("Missing fnId parameter");
                return;
            }

            try {
                var response = comm.callFunction(fnId, ctx.body());

                response.getHeaders().forEach(ctx::header);
                ctx.status(response.getStatusCode().code());
                ctx.result(response.getBody());

            } catch (Exception e) {
                ctx.status(500).result(e.toString());
            }
        });

        app.put(path, ctx -> {
            String syncId = ctx.queryParam(InngestQueryParamKey.SyncId.value());
            String origin = getOrigin(ctx);

            String response = comm.register(origin, syncId);
            ctx.status(200).result(response);
        });
    }

    private static String getOrigin(io.javalin.http.Context ctx) {
        StringBuilder origin = new StringBuilder()
                .append(ctx.scheme())
                .append("://")
                .append(ctx.host());

        int port = ctx.port();
        if (!HTTP_PORTS.contains(port)) {
            origin.append(":").append(port);
        }
        return origin.toString();
    }
}
