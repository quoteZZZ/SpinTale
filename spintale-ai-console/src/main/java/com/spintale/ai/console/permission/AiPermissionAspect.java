package com.spintale.ai.console.permission;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.spintale.common.utils.SecurityUtils;

@Aspect
@Component
public class AiPermissionAspect
{
    @Autowired
    private AiPermissionRegistry permissionRegistry;

    @Around("@annotation(aiPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, AiPermission aiPermission) 
            throws Throwable
    {
        String permission = aiPermission.value();

        if (!permissionRegistry.isRegistered(permission))
        {
            throw new SecurityException("未注册的权限: " + permission);
        }

        try
        {
            boolean hasPermission = SecurityUtils.hasPermi(permission);
            if (!hasPermission)
            {
                throw new SecurityException("没有权限: " + permission);
            }
        }
        catch (Exception e)
        {
            throw new SecurityException("权限校验失败: " + e.getMessage());
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(aiAuditLog)")
    public Object recordAuditLog(ProceedingJoinPoint joinPoint, AiAuditLog aiAuditLog)
            throws Throwable
    {
        String operation = aiAuditLog.operation();
        String module = aiAuditLog.module();
        Object[] args = aiAuditLog.recordParams() ? joinPoint.getArgs() : null;

        long startTime = System.currentTimeMillis();
        Throwable error = null;
        Object result = null;

        try
        {
            result = joinPoint.proceed();
            return result;
        }
        catch (Throwable e)
        {
            error = e;
            throw e;
        }
        finally
        {
            long duration = System.currentTimeMillis() - startTime;
            recordLog(module, operation, args, result, error, duration);
        }
    }

    private void recordLog(String module, String operation, Object[] args, 
            Object result, Throwable error, long duration)
    {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("[").append(module).append("] ");
        logBuilder.append(operation);
        logBuilder.append(" - 耗时: ").append(duration).append("ms");

        if (error != null)
        {
            logBuilder.append(" - 失败: ").append(error.getMessage());
        }
        else
        {
            logBuilder.append(" - 成功");
        }

        System.out.println(logBuilder.toString());
    }
}
