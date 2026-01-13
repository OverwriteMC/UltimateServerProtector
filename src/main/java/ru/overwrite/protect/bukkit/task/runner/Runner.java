package ru.overwrite.protect.bukkit.task.runner;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.task.task.Task;

public interface Runner {

    Task runPlayer(@NotNull Runnable task, @NotNull Player player);

    Task run(@NotNull Runnable task);

    Task runAsync(@NotNull Runnable task);

    Task runDelayed(@NotNull Runnable task, long delayTicks);

    Task runDelayedAsync(@NotNull Runnable task, long delayTicks);

    Task runPeriodical(@NotNull Runnable task, long delayTicks, long periodTicks);

    Task runPeriodicalAsync(@NotNull Runnable task, long delayTicks, long periodTicks);

    void cancelTasks();
}