package org.tanchee.inngest;

public enum OpCode {
    StepRun,
    StepError,
    Sleep,
    StepStateFailed,
    Step,
    WaitForEvent,
    InvokeFunction;
}
