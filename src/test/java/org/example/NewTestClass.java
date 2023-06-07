package org.example;

import gg.flyte.neptune.annotation.Exclude;
import gg.flyte.neptune.annotation.Inject;

@Exclude
public class NewTestClass {

    @Inject
    private TestClass testClass;

}
