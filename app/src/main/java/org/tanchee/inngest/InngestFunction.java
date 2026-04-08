package org.tanchee.inngest;

import java.lang.reflect.Method;

import org.tanchee.inngest.config.InngestFunctionConfigBuilder;

public abstract class InngestFunction {
    public static final String FUNCTION_FAILED = "inngest/function.failed";

    public InngestFunctionConfigBuilder config(InngestFunctionConfigBuilder builder) {
        return builder;
    }

    /**
     * The function handler that will be run whenever the function is executed.
     *
     * @param ctx  The function context including event(s) that triggered the function
     * @param step A class with methods to define steps within the function
     */
    public abstract Object execute(
            FunctionContext ctx,
            Step step
    );

    private InngestFunctionConfigBuilder buildConfig() {
        InngestFunctionConfigBuilder builder = new InngestFunctionConfigBuilder();
        return config(builder);
    }

    public String id() {
        return buildConfig().id();
    }

    InternalInngestFunction toInngestFunction() {
        InngestFunctionConfigBuilder builder = new InngestFunctionConfigBuilder();
        InngestFunctionConfigBuilder configBuilder = config(builder);

        return new InternalInngestFunction(
                configBuilder,
                this::execute
        );
    }

    InternalInngestFunction toFailureHandler(String appId) {
        Method onFailureMethod;

        try {
            onFailureMethod = this.getClass().getMethod(
                    "onFailure",
                    FunctionContext.class,
                    Step.class
            );
        } catch (NoSuchMethodException e) {
            return null;
        }

        // Only generate the failure handler if the onFailure method was overridden
        if (onFailureMethod.getDeclaringClass() != InngestFunction.class) {
            InngestFunctionConfigBuilder fnConfig = buildConfig();

            String fullyQualifiedId = appId + "-" + fnConfig.id();
            String fnName = fnConfig.name() != null
                    ? fnConfig.name()
                    : fnConfig.id();

            InngestFunctionConfigBuilder configBuilder = new InngestFunctionConfigBuilder()
                    .id(fnConfig.id() + "-failure")
                    .name(fnName + " (failure)")
                    .triggerEventIf(
                            FUNCTION_FAILED,
                            "event.data.function_id == '" + fullyQualifiedId + "'"
                    );

            return new InternalInngestFunction(
                    configBuilder,
                    this::onFailure
            );
        }

        return null;
    }

    /**
     * Provide a function to be called if your function fails, meaning
     * that it ran out of retries and was unable to complete successfully.
     *
     * This is useful for sending warning notifications or cleaning up
     * after a failure and supports all the same functionality as a
     * regular handler.
     *
     * @param ctx  The function context including event(s) that triggered the function
     * @param step A class with methods to define steps within the function
     */
    public Object onFailure(
            FunctionContext ctx,
            Step step
    ) {
        return null;
    }
}
