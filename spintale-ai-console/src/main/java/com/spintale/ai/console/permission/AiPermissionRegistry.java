package com.spintale.ai.console.permission;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiPermissionRegistry
{
    private final Set<String> registeredPermissions = ConcurrentHashMap.newKeySet();

    public void registerPermission(String permission)
    {
        registeredPermissions.add(permission);
    }

    public void registerPermissions(String... permissions)
    {
        for (String permission : permissions)
        {
            registeredPermissions.add(permission);
        }
    }

    public boolean isRegistered(String permission)
    {
        return registeredPermissions.contains(permission);
    }

    public Set<String> getAllPermissions()
    {
        return Set.copyOf(registeredPermissions);
    }

    public void initDefaultPermissions()
    {
        registerPermissions(
                "ai:model:list",
                "ai:model:query",
                "ai:model:add",
                "ai:model:edit",
                "ai:model:remove",
                "ai:knowledge:list",
                "ai:knowledge:query",
                "ai:knowledge:add",
                "ai:knowledge:edit",
                "ai:knowledge:remove",
                "ai:document:list",
                "ai:document:query",
                "ai:document:add",
                "ai:document:edit",
                "ai:document:remove",
                "ai:document:index",
                "ai:agent:list",
                "ai:agent:query",
                "ai:agent:add",
                "ai:agent:edit",
                "ai:agent:remove",
                "ai:tool:list",
                "ai:tool:query",
                "ai:run:list",
                "ai:run:query",
                "ai:run:trace",
                "ai:run:cost",
                "ai:eval:list",
                "ai:eval:run",
                "ai:approval:list",
                "ai:approval:approve",
                "ai:approval:reject"
        );
    }
}
