package com.account.moneytranser.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;

public enum MyFeatures implements Feature {
    @EnabledByDefault
    @Label("Async Executor")
    ASYNC_EXECUTOR,

    @Label("Rabbit MQ")
    RABBIT_MQ;
}
