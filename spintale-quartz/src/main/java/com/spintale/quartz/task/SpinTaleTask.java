package com.spintale.quartz.task;

import org.springframework.stereotype.Component;

import com.spintale.common.utils.StringUtils;

/**
 * Scheduled task examples.
 *
 * @author spintale
 */
@Component("spinTaleTask")
public class SpinTaleTask
{
    public void multipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        System.out.println(StringUtils.format(
                "Execute task with params: string={}, boolean={}, long={}, double={}, integer={}",
                s, b, l, d, i));
    }

    public void params(String params)
    {
        System.out.println("Execute task with param: " + params);
    }

    public void noParams()
    {
        System.out.println("Execute task without params.");
    }
}
